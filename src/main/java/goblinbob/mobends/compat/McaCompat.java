package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.platform.Platform;
import goblinbob.mobends.api.addon.AddonAnimationRegistry;
import goblinbob.mobends.standard.client.model.adaptive.AdaptiveHumanoidGeometry;
import goblinbob.mobends.standard.client.renderer.entity.mutated.BipedRenderer;
import goblinbob.mobends.standard.client.renderer.entity.mutated.ZombieRenderer;
import goblinbob.mobends.standard.data.McaVillagerData;
import goblinbob.mobends.standard.data.ZombieData;
import goblinbob.mobends.standard.mutators.McaVillagerMutator;
import goblinbob.mobends.standard.mutators.McaZombieVillagerMutator;
import goblinbob.mobends.standard.previewer.BipedPreviewer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class McaCompat
{
    private static final String MOD_ID = "mca";

    private static final String[] PACKAGE_PREFIXES = {"", "forge.", "neoforge.", "fabric.", "quilt."};

    private static final String VILLAGER_CLASS = "net.mca.entity.VillagerEntityMCA";
    private static final String ZOMBIE_VILLAGER_CLASS = "net.mca.entity.ZombieVillagerEntityMCA";

    private static final String[] WEAR_FIELDS = {
            "bodyWear", "leftArmwear", "rightArmwear", "leftLegwear", "rightLegwear"};

    private static final Set<String> UNANIMATED_AGE_STATES = Set.of("BABY", "TODDLER", "CHILD");

    private static final float BREAST_PITCH = (float) Math.PI * 0.3F;

    private static final float MODEL_HEIGHT = 2.0F;

    private static final Map<Class<?>, Boolean> MCA_MODELS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field[]> WEAR_FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field[]> WEARS_HIDDEN_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method[]> BREAST_METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method[]> AGE_STATE_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method[]> SCALE_FACTOR_CACHE = new ConcurrentHashMap<>();

    private McaCompat()
    {
    }

    public static void register(AddonAnimationRegistry registry, String[] villagerAnimations,
                                String[] zombieAnimations, String[] alterableParts)
    {
        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        goblinbob.mobends.api.animation.MoBendsAnimationControl.registerStaticPose(
                MOD_ID, McaCompat::isUnanimatedAge);

        final Class<?> villagerClass = resolve(VILLAGER_CLASS);
        if (villagerClass != null)
        {
            try
            {
                @SuppressWarnings("unchecked")
                final Class<LivingEntity> typed = (Class<LivingEntity>) villagerClass;
                registry.registerNewEntity("mca:male_villager", "entity.mca.villager", typed,
                        McaVillagerData::new, McaVillagerMutator::new,
                        new BipedRenderer<>(), new BipedPreviewer<>(), villagerAnimations, alterableParts);
            }
            catch (Throwable ignored)
            {
            }
        }

        final Class<?> zombieVillagerClass = resolve(ZOMBIE_VILLAGER_CLASS);
        if (zombieVillagerClass != null && Zombie.class.isAssignableFrom(zombieVillagerClass))
        {
            try
            {
                @SuppressWarnings("unchecked")
                final Class<Zombie> typed = (Class<Zombie>) zombieVillagerClass;
                registry.registerNewEntity("mca:male_zombie_villager", "entity.mca.zombie_villager", typed,
                        ZombieData::new, McaZombieVillagerMutator::new,
                        new ZombieRenderer<>(), new BipedPreviewer<>(), zombieAnimations, alterableParts);
            }
            catch (Throwable ignored)
            {
            }
        }
    }

    public static boolean renderMutatedInstead(Object model, PoseStack poseStack, VertexConsumer vertexConsumer,
                                               int packedLight, int packedOverlay, int color)
    {
        if (!(model instanceof HumanoidModel<?> humanoidModel))
        {
            return false;
        }

        final goblinbob.mobends.standard.mutators.BipedMutator<?, ?, ?> mutator =
                goblinbob.mobends.core.client.MoBendsRenderContext.getCurrentBipedMutator();
        if (mutator == null || !mutator.shouldRenderCustom())
        {
            return false;
        }

        if (goblinbob.mobends.core.client.MoBendsRenderContext.isInMainModelRender())
        {
            if (goblinbob.mobends.core.client.MoBendsRenderContext.isInArmorRender())
            {
                return false;
            }

            mutator.setBabyHeadScale(1.0F);
            mutator.renderMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);

            final HumanoidModel<?> vanillaModel =
                    goblinbob.mobends.core.client.MoBendsRenderContext.getCurrentVanillaModel();
            if (vanillaModel != null)
            {
                mutator.syncPosesToVanillaModel(vanillaModel);
            }

            goblinbob.mobends.core.client.MoBendsRenderContext.endMainModelRender();
            return true;
        }

        if (!mutator.isOverlayModel(humanoidModel, null))
        {
            return false;
        }

        mutator.renderOverlayModel(humanoidModel, null, poseStack, vertexConsumer,
                packedLight, packedOverlay, color);
        return true;
    }

    public static float nominalHeightOf(LivingEntity entity)
    {
        return renderScaleOf(entity) > 0.0F ? MODEL_HEIGHT : 0.0F;
    }

    public static float renderScaleOf(LivingEntity entity)
    {
        if (entity == null)
        {
            return 0.0F;
        }

        final Method[] scaleFactor = SCALE_FACTOR_CACHE.computeIfAbsent(entity.getClass(), type -> {
            try
            {
                final Method method = type.getMethod("getRawVerticalScaleFactor");
                method.setAccessible(true);
                return new Method[] {method};
            }
            catch (Throwable t)
            {
                return new Method[0];
            }
        });

        if (scaleFactor.length == 0)
        {
            return 0.0F;
        }

        try
        {
            final float scale = ((Number) scaleFactor[0].invoke(entity)).floatValue();
            return scale > 0.01F ? scale : 0.0F;
        }
        catch (Throwable t)
        {
            return 0.0F;
        }
    }

    public static void initializePreviewAppearance(Object entity)
    {
        if (entity == null)
        {
            return;
        }

        try
        {
            final Method initialize = entity.getClass().getMethod("initialize",
                    net.minecraft.world.entity.MobSpawnType.class);
            initialize.setAccessible(true);
            initialize.invoke(entity, net.minecraft.world.entity.MobSpawnType.COMMAND);
        }
        catch (Throwable ignored)
        {
        }
    }

    private static Class<?> resolve(String className)
    {
        for (final String prefix : PACKAGE_PREFIXES)
        {
            try
            {
                final Class<?> candidate = Class.forName(prefix + className);
                if (LivingEntity.class.isAssignableFrom(candidate))
                {
                    return candidate;
                }
            }
            catch (Throwable ignored)
            {
            }
        }

        return null;
    }

    public static boolean isUnanimatedAge(LivingEntity entity)
    {
        if (entity == null)
        {
            return false;
        }

        final Method[] ageState = AGE_STATE_CACHE.computeIfAbsent(entity.getClass(), type -> {
            try
            {
                final Method method = type.getMethod("getAgeState");
                method.setAccessible(true);
                return new Method[] {method};
            }
            catch (Throwable t)
            {
                return new Method[0];
            }
        });

        if (ageState.length == 0)
        {
            return false;
        }

        try
        {
            final Object state = ageState[0].invoke(entity);
            return state instanceof Enum<?> value && UNANIMATED_AGE_STATES.contains(value.name());
        }
        catch (Throwable t)
        {
            return false;
        }
    }

    public static boolean isMcaModel(HumanoidModel<?> model)
    {
        if (model == null)
        {
            return false;
        }

        return MCA_MODELS.computeIfAbsent(model.getClass(), type -> {
            for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass())
            {
                if (current.getName().endsWith("net.mca.client.model.VillagerEntityBaseModelMCA")
                        || current.getName().endsWith("net.mca.client.model.PlayerEntityExtendedModel"))
                {
                    return true;
                }
            }
            return false;
        });
    }

    public static AdaptiveHumanoidGeometry.WearParts wearPartsOf(HumanoidModel<?> model)
    {
        if (!isMcaModel(model) || wearsHidden(model))
        {
            return null;
        }

        final Field[] fields = wearFields(model.getClass());
        if (fields == null)
        {
            return null;
        }

        try
        {
            return new AdaptiveHumanoidGeometry.WearParts(
                    (ModelPart) fields[0].get(model),
                    (ModelPart) fields[1].get(model),
                    (ModelPart) fields[2].get(model),
                    (ModelPart) fields[3].get(model),
                    (ModelPart) fields[4].get(model));
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    public static void alignWearParts(HumanoidModel<?> model)
    {
        final Field[] fields = wearFields(model == null ? null : model.getClass());
        if (fields == null)
        {
            return;
        }

        try
        {
            ((ModelPart) fields[0].get(model)).copyFrom(model.body);
            ((ModelPart) fields[1].get(model)).copyFrom(model.leftArm);
            ((ModelPart) fields[2].get(model)).copyFrom(model.rightArm);
            ((ModelPart) fields[3].get(model)).copyFrom(model.leftLeg);
            ((ModelPart) fields[4].get(model)).copyFrom(model.rightLeg);
        }
        catch (Throwable ignored)
        {
        }
    }

    public static void renderBreasts(HumanoidModel<?> model, PoseStack poseStack, VertexConsumer vertexConsumer,
                                     int packedLight, int packedOverlay, int color)
    {
        if (!isMcaModel(model) || !model.body.visible)
        {
            return;
        }

        final Method[] accessors = breastAccessors(model.getClass());
        if (accessors == null)
        {
            return;
        }

        try
        {
            if (!((ModelPart) accessors[0].invoke(model)).visible)
            {
                return;
            }

            final float size = ((Number) accessors[1].invoke(model)).floatValue()
                    * ((Number) accessors[3].invoke(accessors[2].invoke(model))).floatValue();
            if (size <= 0.0F)
            {
                return;
            }

            @SuppressWarnings("unchecked")
            final Iterable<ModelPart> parts = (Iterable<ModelPart>) accessors[4].invoke(model);

            poseStack.pushPose();
            poseStack.scale(size * 0.2F + 1.05F, size * 0.75F + 0.75F, size * 0.75F + 0.75F);

            for (final ModelPart part : parts)
            {
                final float pitch = part.xRot;
                part.xRot = BREAST_PITCH;
                renderTinted(part, poseStack, vertexConsumer, packedLight, packedOverlay, color);
                part.xRot = pitch;
            }

            poseStack.popPose();
        }
        catch (Throwable ignored)
        {
        }
    }

    private static boolean wearsHidden(HumanoidModel<?> model)
    {
        final Field[] field = WEARS_HIDDEN_CACHE.computeIfAbsent(model.getClass(), type -> {
            for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass())
            {
                try
                {
                    final Field found = current.getDeclaredField("wearsHidden");
                    found.setAccessible(true);
                    return new Field[] {found};
                }
                catch (Throwable ignored)
                {
                }
            }
            return new Field[0];
        });

        if (field.length == 0)
        {
            return false;
        }

        try
        {
            return field[0].getBoolean(model);
        }
        catch (Throwable t)
        {
            return true;
        }
    }

    private static Field[] wearFields(Class<?> modelClass)
    {
        if (modelClass == null)
        {
            return null;
        }

        final Field[] cached = WEAR_FIELD_CACHE.computeIfAbsent(modelClass, type -> {
            final Field[] fields = new Field[WEAR_FIELDS.length];
            for (int i = 0; i < WEAR_FIELDS.length; ++i)
            {
                try
                {
                    fields[i] = type.getField(WEAR_FIELDS[i]);
                }
                catch (Throwable t)
                {
                    return new Field[0];
                }

                if (!ModelPart.class.isAssignableFrom(fields[i].getType()))
                {
                    return new Field[0];
                }
            }
            return fields;
        });

        return cached.length == WEAR_FIELDS.length ? cached : null;
    }

    private static Method[] breastAccessors(Class<?> modelClass)
    {
        final Method[] cached = BREAST_METHOD_CACHE.computeIfAbsent(modelClass, type -> {
            try
            {
                final Method breastPart = type.getMethod("getBreastPart");
                final Method breastSize = type.getMethod("getBreastSize");
                final Method dimensions = type.getMethod("getDimensions");
                final Method breastFactor = dimensions.getReturnType().getMethod("getBreasts");
                final Method breastParts = type.getMethod("getBreastParts");

                breastPart.setAccessible(true);
                breastSize.setAccessible(true);
                dimensions.setAccessible(true);
                breastFactor.setAccessible(true);
                breastParts.setAccessible(true);

                return new Method[] {breastPart, breastSize, dimensions, breastFactor, breastParts};
            }
            catch (Throwable t)
            {
                return new Method[0];
            }
        });

        return cached.length == 5 ? cached : null;
    }

    private static void renderTinted(ModelPart part, PoseStack poseStack, VertexConsumer vertexConsumer,
                                     int packedLight, int packedOverlay, int color)
    {
        //? if >=1.21 {
        /*part.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        *///?} else {
        part.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                ((color >>> 24) & 0xFF) / 255.0F);
        //?}
    }
}
