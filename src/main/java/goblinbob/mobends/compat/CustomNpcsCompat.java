package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import goblinbob.mobends.api.addon.AddonAnimationRegistry;
import goblinbob.mobends.api.animation.MoBendsAnimationControl;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.client.renderer.entity.mutated.BipedRenderer;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.CustomNpcData;
import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.CustomNpcMutator;
import goblinbob.mobends.standard.mutators.SpiderMutator;
import goblinbob.mobends.standard.mutators.SquidMutator;
import goblinbob.mobends.standard.mutators.WolfMutator;
import goblinbob.mobends.standard.previewer.BipedPreviewer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class CustomNpcsCompat
{
    public enum RenderRoute
    {
        OWN_MODEL,
        DISPLAY_ANIMATED,
        DISPLAY_VANILLA
    }

    private static final String MOD_ID = "customnpcs";
    private static final String ENTITY_KEY = "customnpcs:customnpc";
    private static final String ENTITY_NAME = "entity.customnpcs.customnpc";

    private static final String NPC_CLASS = "noppes.npcs.entity.EntityCustomNpc";
    private static final String NPC_INTERFACE_CLASS = "noppes.npcs.entity.EntityNPCInterface";
    private static final String RENDERER_CLASS = "noppes.npcs.client.renderer.RenderCustomNpc";
    private static final String MODEL_DATA_CLASS = "noppes.npcs.ModelData";
    private static final String MODEL_DATA_SHARED_CLASS = "noppes.npcs.ModelDataShared";
    private static final String PART_CONFIG_CLASS = "noppes.npcs.ModelPartConfig";

    private static final String[] UNUSED_VARIANT_CLASSES = {
            "noppes.npcs.entity.EntityNpcAlex",
            "noppes.npcs.entity.EntityNpcClassicPlayer",
            "noppes.npcs.entity.EntityNPC64x32"};

    private static final int ANIMATION_NONE = 0;
    private static final int ANIMATION_SITTING = 1;
    private static final int ANIMATION_CROUCHING = 4;
    private static final int JOB_PUPPET = 9;

    private static final float DEFAULT_SIZE = 5.0F;
    private static final float SHOULDER_OVERHANG = 2.0F;
    private static final float ARM_INSET = 1.0F;

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Class<?> npcClass;
    private static Class<?> npcInterfaceClass;
    private static Class<?> rendererClass;

    private static Field modelDataField;
    private static Field currentAnimationField;
    private static Field jobField;
    private static Field displayField;
    private static Method jobTypeMethod;
    private static Method getSizeMethod;

    private static Method getDisplayEntityMethod;
    private static Method getLegsYMethod;
    private static Field simpleRenderField;
    private static Field headField;
    private static Field bodyField;
    private static Field leftArmField;
    private static Field rightArmField;
    private static Field leftLegField;
    private static Field rightLegField;

    private static Field scaleXField;
    private static Field scaleYField;
    private static Field scaleZField;

    private static Field ownModelField;
    private static Field borrowedModelField;
    private static Field ownLayersField;

    private static final Map<Class<?>, Method[]> PUPPET_ACTIVE_CACHE = new ConcurrentHashMap<>();

    private static final Map<LivingEntity, WeakReference<LivingEntity>> displayOwners = new WeakHashMap<>();

    private static final float[] bodyScale = new float[3];
    private static final float[] scratchScale = new float[3];

    private CustomNpcsCompat()
    {
    }

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        try
        {
            initReflection();
            isLoaded = true;
        }
        catch (Throwable t)
        {
            isLoaded = false;
        }
    }

    private static void initReflection() throws Exception
    {
        npcClass = Class.forName(NPC_CLASS);
        npcInterfaceClass = Class.forName(NPC_INTERFACE_CLASS);
        rendererClass = Class.forName(RENDERER_CLASS);

        final Class<?> modelDataClass = Class.forName(MODEL_DATA_CLASS);
        final Class<?> sharedClass = Class.forName(MODEL_DATA_SHARED_CLASS);
        final Class<?> configClass = Class.forName(PART_CONFIG_CLASS);

        modelDataField = npcClass.getField("modelData");
        currentAnimationField = npcInterfaceClass.getField("currentAnimation");
        jobField = npcInterfaceClass.getField("job");
        displayField = npcInterfaceClass.getField("display");
        jobTypeMethod = jobField.getType().getMethod("getType");
        getSizeMethod = displayField.getType().getMethod("getSize");

        getDisplayEntityMethod = modelDataClass.getMethod("getEntity", npcInterfaceClass);
        simpleRenderField = modelDataClass.getField("simpleRender");
        getLegsYMethod = sharedClass.getMethod("getLegsY");
        headField = sharedClass.getField("head");
        bodyField = sharedClass.getField("body");
        leftArmField = sharedClass.getField("arm1");
        rightArmField = sharedClass.getField("arm2");
        leftLegField = sharedClass.getField("leg1");
        rightLegField = sharedClass.getField("leg2");

        scaleXField = configClass.getField("scaleX");
        scaleYField = configClass.getField("scaleY");
        scaleZField = configClass.getField("scaleZ");

        ownModelField = rendererClass.getField("npcmodel");
        borrowedModelField = rendererClass.getField("otherModel");
        ownLayersField = rendererClass.getField("npclayers");
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    @SuppressWarnings("unchecked")
    public static void register(AddonAnimationRegistry registry, String[] animations, String[] alterableParts)
    {
        if (!isModLoaded())
        {
            return;
        }

        try
        {
            final Class<LivingEntity> typed = (Class<LivingEntity>) npcClass;
            registry.registerNewEntity(ENTITY_KEY, ENTITY_NAME, typed,
                    CustomNpcData::new, CustomNpcMutator::new,
                    new BipedRenderer<>(), new BipedPreviewer<>(), animations, alterableParts);
        }
        catch (Throwable ignored)
        {
        }

        for (final String className : UNUSED_VARIANT_CLASSES)
        {
            try
            {
                final Class<?> variant = Class.forName(className);
                if (LivingEntity.class.isAssignableFrom(variant))
                {
                    MoBendsAnimationControl.excludeEntityClass((Class<? extends LivingEntity>) variant);
                }
            }
            catch (Throwable ignored)
            {
            }
        }
    }

    public static boolean isNpc(LivingEntity entity)
    {
        return isLoaded && entity != null && npcClass.isInstance(entity);
    }

    public static int currentAnimationOf(LivingEntity entity)
    {
        if (!isNpc(entity))
        {
            return ANIMATION_NONE;
        }

        try
        {
            return currentAnimationField.getInt(entity);
        }
        catch (Throwable t)
        {
            return ANIMATION_NONE;
        }
    }

    public static boolean isSitting(LivingEntity entity)
    {
        return currentAnimationOf(entity) == ANIMATION_SITTING;
    }

    public static boolean isExternallyPosed(LivingEntity entity)
    {
        if (!isNpc(entity))
        {
            return false;
        }

        final int animation = currentAnimationOf(entity);
        if (animation != ANIMATION_NONE && animation != ANIMATION_SITTING && animation != ANIMATION_CROUCHING)
        {
            return true;
        }

        return isPuppetActive(entity);
    }

    private static boolean isPuppetActive(LivingEntity entity)
    {
        try
        {
            final Object job = jobField.get(entity);
            if (job == null || ((Number) jobTypeMethod.invoke(job)).intValue() != JOB_PUPPET)
            {
                return false;
            }

            final Method[] active = PUPPET_ACTIVE_CACHE.computeIfAbsent(job.getClass(), type -> {
                try
                {
                    return new Method[] {type.getMethod("isActive")};
                }
                catch (Throwable t)
                {
                    return new Method[0];
                }
            });

            return active.length == 1 && Boolean.TRUE.equals(active[0].invoke(job));
        }
        catch (Throwable t)
        {
            return false;
        }
    }

    public static void applyPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (mutator == null || vanillaModel == null || !isExternallyPosed(entity))
        {
            return;
        }

        mutator.adoptPoseFromVanillaModel(vanillaModel, null, null);
    }

    public static float renderScaleOf(LivingEntity entity)
    {
        if (!isLoaded || entity == null)
        {
            return 1.0F;
        }

        final LivingEntity npc = npcClass.isInstance(entity) ? entity : ownerOf(entity);
        if (npc == null)
        {
            return 1.0F;
        }

        try
        {
            final Object display = displayField.get(npc);
            return display == null ? 1.0F : ((Number) getSizeMethod.invoke(display)).floatValue() / DEFAULT_SIZE;
        }
        catch (Throwable t)
        {
            return 1.0F;
        }
    }

    @SuppressWarnings("unchecked")
    public static <E extends LivingEntity> HumanoidModel<E> ownModelOf(LivingEntityRenderer<E, ?> renderer)
    {
        if (!isLoaded || renderer == null || !rendererClass.isInstance(renderer))
        {
            return null;
        }

        try
        {
            return ownModelField.get(renderer) instanceof HumanoidModel<?> model ? (HumanoidModel<E>) model : null;
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <E extends LivingEntity, M extends EntityModel<E>> List<RenderLayer<E, M>> ownLayersOf(
            LivingEntityRenderer<E, M> renderer)
    {
        if (!isLoaded || renderer == null || !rendererClass.isInstance(renderer))
        {
            return null;
        }

        try
        {
            return ownLayersField.get(renderer) instanceof List<?> layers ? (List<RenderLayer<E, M>>) layers : null;
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    private static LivingEntity ownerOf(LivingEntity display)
    {
        final WeakReference<LivingEntity> owner = displayOwners.get(display);
        return owner == null ? null : owner.get();
    }

    private static Object modelDataOf(LivingEntity entity)
    {
        try
        {
            return modelDataField.get(entity);
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    private static LivingEntity displayEntityOf(LivingEntity npc, Object modelData)
    {
        try
        {
            return getDisplayEntityMethod.invoke(modelData, npc) instanceof LivingEntity display ? display : null;
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    private static float legsDropOf(Object modelData)
    {
        try
        {
            return ((Number) getLegsYMethod.invoke(modelData)).floatValue() * 16.0F;
        }
        catch (Throwable t)
        {
            return 0.0F;
        }
    }

    public static void applyModelScaling(LivingEntity entity, BipedEntityData<?> data)
    {
        if (data == null || !isNpc(entity))
        {
            return;
        }

        final Object modelData = modelDataOf(entity);
        if (modelData == null || !readScale(modelData, bodyField, bodyScale))
        {
            return;
        }

        final float drop = legsDropOf(modelData);

        applyRoot(data.body, bodyScale, drop);
        applyChild(data.head, modelData, headField, 0.0F);
        applyChild(data.leftArm, modelData, leftArmField, 1.0F);
        applyChild(data.rightArm, modelData, rightArmField, -1.0F);

        if (readScale(modelData, leftLegField, scratchScale))
        {
            applyRoot(data.leftLeg, scratchScale, drop);
        }
        if (readScale(modelData, rightLegField, scratchScale))
        {
            applyRoot(data.rightLeg, scratchScale, drop);
        }
    }

    private static boolean readScale(Object modelData, Field configField, float[] scale)
    {
        try
        {
            final Object config = configField.get(modelData);
            if (config == null)
            {
                return false;
            }

            scale[0] = scaleXField.getFloat(config);
            scale[1] = scaleYField.getFloat(config);
            scale[2] = scaleZField.getFloat(config);
            return true;
        }
        catch (Throwable t)
        {
            return false;
        }
    }

    private static void applyRoot(ModelPartTransform bone, float[] scale, float drop)
    {
        if (bone == null)
        {
            return;
        }

        bone.scale.set(scale[0], scale[1], scale[2]);
        bone.preRotationScale.set(1.0F, 1.0F, 1.0F);
        bone.globalOffset.set(0.0F, drop, 0.0F);
    }

    private static void applyChild(ModelPartTransform bone, Object modelData, Field configField, float side)
    {
        if (bone == null || !readScale(modelData, configField, scratchScale))
        {
            return;
        }

        bone.scale.set(scratchScale[0], scratchScale[1], scratchScale[2]);
        bone.preRotationScale.set(divide(1.0F, bodyScale[0]), divide(1.0F, bodyScale[1]), divide(1.0F, bodyScale[2]));

        if (side == 0.0F)
        {
            bone.globalOffset.set(0.0F, 0.0F, 0.0F);
            return;
        }

        bone.globalOffset.set(side * ARM_INSET * (divide(scratchScale[0], bodyScale[0]) - 1.0F),
                SHOULDER_OVERHANG * (divide(scratchScale[1], bodyScale[1]) - 1.0F),
                0.0F);
    }

    private static float divide(float value, float divisor)
    {
        return divisor == 0.0F ? value : value / divisor;
    }

    public static void compensateSyncedPivots(LivingEntity entity, HumanoidModel<?> model)
    {
        if (model == null || !isNpc(entity))
        {
            return;
        }

        final Object modelData = modelDataOf(entity);
        if (modelData == null)
        {
            return;
        }

        final float drop = legsDropOf(modelData);
        if (drop == 0.0F)
        {
            return;
        }

        model.head.y -= drop;
        if (model.hat != null)
        {
            model.hat.y -= drop;
        }
        model.body.y -= drop;
        model.leftArm.y -= drop;
        model.rightArm.y -= drop;

        if (model instanceof PlayerModel<?> playerModel)
        {
            playerModel.jacket.copyFrom(model.body);
            playerModel.leftSleeve.copyFrom(model.leftArm);
            playerModel.rightSleeve.copyFrom(model.rightArm);
        }
    }

    public static boolean isAttachedDisplayEntity(LivingEntity entity)
    {
        if (!isLoaded || entity == null)
        {
            return false;
        }

        final LivingEntity npc = ownerOf(entity);
        if (npc == null)
        {
            displayOwners.remove(entity);
            return false;
        }

        final Level level = Minecraft.getInstance().level;
        final Object modelData = modelDataOf(npc);

        if (npc.isRemoved() || level == null || level.getEntity(npc.getId()) != npc
                || modelData == null || displayEntityOf(npc, modelData) != entity)
        {
            displayOwners.remove(entity);
            return false;
        }

        return true;
    }

    private static void alignDisplayRotations(LivingEntity npc, LivingEntity display, float partialTicks)
    {
        final float bodyYaw = Mth.rotLerp(partialTicks, npc.yBodyRotO, npc.yBodyRot);
        display.yBodyRotO = bodyYaw;
        display.yBodyRot = bodyYaw;

        final float headYaw = Mth.rotLerp(partialTicks, npc.yHeadRotO, npc.yHeadRot);
        display.yHeadRotO = headYaw;
        display.yHeadRot = headYaw;

        final float yaw = Mth.rotLerp(partialTicks, npc.yRotO, npc.getYRot());
        display.yRotO = yaw;
        display.setYRot(yaw);

        final float pitch = Mth.lerp(partialTicks, npc.xRotO, npc.getXRot());
        display.xRotO = pitch;
        display.setXRot(pitch);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static RenderRoute routeRender(LivingEntity entity, LivingEntityRenderer<?, ?> renderer, float partialTicks,
                                          PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        if (!isNpc(entity) || renderer == null || !rendererClass.isInstance(renderer))
        {
            return RenderRoute.OWN_MODEL;
        }

        final Object modelData = modelDataOf(entity);
        if (modelData == null)
        {
            return RenderRoute.OWN_MODEL;
        }

        final LivingEntity display = displayEntityOf(entity, modelData);
        if (display == null)
        {
            return RenderRoute.OWN_MODEL;
        }

        try
        {
            if (simpleRenderField.getBoolean(modelData))
            {
                return RenderRoute.OWN_MODEL;
            }
        }
        catch (Throwable t)
        {
            return RenderRoute.OWN_MODEL;
        }

        final EntityRenderer<?> displayRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(display);
        if (!(displayRenderer instanceof LivingEntityRenderer<?, ?> vanillaRenderer))
        {
            return RenderRoute.OWN_MODEL;
        }

        if (vanillaRenderer == renderer || npcInterfaceClass.isInstance(display))
        {
            return RenderRoute.DISPLAY_VANILLA;
        }

        final Object borrowed;
        try
        {
            borrowed = borrowedModelField.get(renderer);
        }
        catch (Throwable t)
        {
            return RenderRoute.DISPLAY_VANILLA;
        }

        if (!(borrowed instanceof EntityModel<?> model) || vanillaRenderer.getModel() != model)
        {
            return RenderRoute.DISPLAY_VANILLA;
        }

        final EntityBender<LivingEntity> displayBender = EntityBenderRegistry.instance.getForEntity(display);
        if (displayBender == null || !displayBender.isAnimated())
        {
            return RenderRoute.DISPLAY_VANILLA;
        }

        displayOwners.put(display, new WeakReference<>(entity));

        alignDisplayRotations(entity, display, partialTicks);

        if (!displayBender.applyMutation((LivingEntityRenderer) vanillaRenderer, display, partialTicks))
        {
            return RenderRoute.DISPLAY_VANILLA;
        }

        final Object rawMutator = displayBender.getMutator(vanillaRenderer);
        if (rawMutator == null)
        {
            return RenderRoute.DISPLAY_VANILLA;
        }

        final Mutator<?, LivingEntity, ?> mutator = (Mutator<?, LivingEntity, ?>) rawMutator;
        final LivingEntityData<LivingEntity> data = (LivingEntityData<LivingEntity>) mutator.getData(display);
        if (data == null)
        {
            return RenderRoute.DISPLAY_VANILLA;
        }

        MoBendsRenderContext.setCurrentEntity(display);

        if (rawMutator instanceof BipedMutator<?, ?, ?> bipedMutator)
        {
            MoBendsRenderContext.setCurrentBipedMutator(bipedMutator);
            MoBendsRenderContext.beginMainModelRender();

            final HumanoidModel<?> humanoidModel = bipedMutator.humanoidViewOf(model);
            if (humanoidModel != null)
            {
                MoBendsRenderContext.setCurrentVanillaModel(humanoidModel);
                bipedMutator.syncPosesToVanillaModel(humanoidModel);
            }
        }
        else if (rawMutator instanceof SpiderMutator spiderMutator)
        {
            MoBendsRenderContext.setCurrentSpiderMutator(spiderMutator);
            MoBendsRenderContext.beginMainModelRender();
        }
        else if (rawMutator instanceof SquidMutator squidMutator)
        {
            MoBendsRenderContext.setCurrentSquidMutator(squidMutator);
            MoBendsRenderContext.beginMainModelRender();
        }
        else if (rawMutator instanceof WolfMutator wolfMutator)
        {
            MoBendsRenderContext.setCurrentWolfMutator(wolfMutator);
            MoBendsRenderContext.beginMainModelRender();
        }

        MoBendsRenderContext.setCurrentRenderBuffers(bufferSource, packedLight);
        displayBender.beforeRender(data, display, partialTicks, poseStack);

        return RenderRoute.DISPLAY_ANIMATED;
    }
}
