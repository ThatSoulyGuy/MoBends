package goblinbob.mobends.core.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.kumo.variable.KumoVariableRegistry;
import goblinbob.mobends.lib.math.vector.SmoothVector3f;
import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.pack.BendsPackPerformer;
import goblinbob.mobends.core.util.EntityHelper;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

public abstract class Mutator<D extends LivingEntityData<E>, E extends LivingEntity, M extends EntityModel<E>>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Mutator.class);
    private static Field layersField;

    protected M vanillaModel;
    protected float headYaw;
    protected float headPitch;
    protected float limbSwing;
    protected float limbSwingAmount;
    protected float swingProgress;
    protected float ridingBodyYaw;

    private final IEntityDataFactory<E> dataFactory;
    protected List<RenderLayer<E, M>> layerRenderers;

    public Mutator(IEntityDataFactory<E> dataFactory)
    {
        this.dataFactory = dataFactory;
    }

    @SuppressWarnings("unchecked")
    public void fetchFields(LivingEntityRenderer<E, M> renderer)
    {
        try
        {
            if (layersField == null)
            {
                String[] fieldNames = {"layers", "f_115313_"};
                for (String name : fieldNames)
                {
                    try
                    {
                        layersField = LivingEntityRenderer.class.getDeclaredField(name);
                        layersField.setAccessible(true);
                        break;
                    }
                    catch (NoSuchFieldException ignored)
                    {
                    }
                }

                if (layersField == null)
                {
                    for (Field field : LivingEntityRenderer.class.getDeclaredFields())
                    {
                        if (field.getType() == List.class)
                        {
                            field.setAccessible(true);
                            Object value = field.get(renderer);
                            if (value instanceof List<?> list && !list.isEmpty()
                                && list.get(0) instanceof RenderLayer)
                            {
                                layersField = field;
                                break;
                            }
                        }
                    }
                }
            }

            if (layersField != null)
            {
                this.layerRenderers = (List<RenderLayer<E, M>>) layersField.get(renderer);
            }
            else
            {
                LOGGER.error("Could not find layers field in LivingEntityRenderer");
            }
        }
        catch (IllegalAccessException e)
        {
            LOGGER.error("Failed to access layers field in LivingEntityRenderer", e);
        }
    }

    public abstract void storeVanillaModel(M model);

    public abstract void applyVanillaModel(M model);

    public abstract void swapLayer(LivingEntityRenderer<E, M> renderer, int index, boolean isModelVanilla);

    public abstract void deswapLayer(LivingEntityRenderer<E, M> renderer, int index);

    public abstract boolean createParts(M original, float scaleFactor);

    public boolean mutate(LivingEntityRenderer<E, M> renderer)
    {
        M model = renderer.getModel();
        if (model == null || this.shouldModelBeSkipped(model))
            return false;

        this.fetchFields(renderer);

        float scaleFactor = 0F;

        boolean isModelVanilla = this.isModelVanilla(model);
        if (isModelVanilla)
        {
            this.storeVanillaModel(model);
        }

        this.createParts(model, scaleFactor);

        if (this.layerRenderers != null)
        {
            for (int i = 0; i < layerRenderers.size(); ++i)
            {
                swapLayer(renderer, i, isModelVanilla);
            }
        }

        return true;
    }

    public void demutate(LivingEntityRenderer<E, M> renderer)
    {
        M model = renderer.getModel();
        if (this.shouldModelBeSkipped(model))
            return;

        this.applyVanillaModel(model);

        if (this.layerRenderers != null)
        {
            for (int i = 0; i < layerRenderers.size(); ++i)
            {
                this.deswapLayer(renderer, i);
            }
        }
    }

    private static float guiHeldHeadYaw = 0.0F;
    private static float guiHeldHeadPitch = 0.0F;
    private static boolean guiHeldLookCaptured = false;

    public void updateModel(E entity, LivingEntityRenderer<E, M> renderer, float partialTicks)
    {
        boolean shouldSit = entity.isPassenger()
                && EntityHelper.shouldRiderSit(entity.getVehicle());

        float f = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float f1 = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float yaw = f1 - f;

        this.ridingBodyYaw = 0.0F;

        if (shouldSit && entity.getVehicle() instanceof LivingEntity)
        {
            LivingEntity vehicle = (LivingEntity) entity.getVehicle();
            float vehicleYaw = Mth.rotLerp(partialTicks, vehicle.yBodyRotO, vehicle.yBodyRot);
            f = vehicleYaw;
            yaw = f1 - f;
            float f3 = Mth.wrapDegrees(yaw);

            if (f3 < -85.0F)
                f3 = -85.0F;
            if (f3 >= 85.0F)
                f3 = 85.0F;

            f = f1 - f3;

            if (f3 * f3 > 2500.0F)
                f += f3 * 0.2F;

            yaw = f1 - f;
            this.ridingBodyYaw = Mth.wrapDegrees(f - vehicleYaw);
        }

        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        float f5 = 0.0F;
        float f6 = 0.0F;

        if (!entity.isPassenger())
        {
            f5 = entity.walkAnimation.speed(partialTicks);
            f6 = entity.walkAnimation.position(partialTicks);

            if (entity.isBaby())
                f6 *= 3.0F;
            if (f5 > 1.0F)
                f5 = 1.0F;
            yaw = f1 - f;
        }

        final Minecraft mc = Minecraft.getInstance();
        final boolean localPlayer = entity == mc.player;
        final boolean screenOpen = mc.screen != null;

        if (localPlayer && screenOpen && ModConfig.disableMovementInGui && guiHeldLookCaptured
                && !goblinbob.mobends.core.client.MoBendsRenderContext.isInGuiEntityRender())
        {
            this.headYaw = guiHeldHeadYaw;
            this.headPitch = guiHeldHeadPitch;
        }
        else
        {
            this.headYaw = yaw;
            this.headPitch = pitch;

            if (localPlayer && !screenOpen)
            {
                guiHeldHeadYaw = yaw;
                guiHeldHeadPitch = pitch;
                guiHeldLookCaptured = true;
            }
        }
        this.limbSwing = f6;
        this.limbSwingAmount = f5;
        this.swingProgress = entity.getAttackAnim(partialTicks);
    }

    public void performAnimations(D data, String animatedEntityKey, LivingEntityRenderer<E, M> renderer, float partialTicks)
    {
        data.headYaw.set(Mth.wrapDegrees(this.headYaw));
        data.headPitch.set(Mth.wrapDegrees(this.headPitch));
        data.limbSwing.set(this.limbSwing);
        data.limbSwingAmount.set(this.limbSwingAmount);
        data.swingProgress.set(this.swingProgress);
        data.setRidingBodyYaw(this.ridingBodyYaw);

        KumoVariableRegistry.instance.provideTemporaryData(data);

        final IAnimationController<D> controller = (IAnimationController<D>) data.getController();
        final Collection<String> actions = controller.perform(data);

        if (NetworkConfiguration.instance.areBendsPacksAllowed())
        {
            final boolean limitMovement = NetworkConfiguration.instance.isMovementLimited();
            final SmoothVector3f lastGlobalOffset = limitMovement ? new SmoothVector3f(data.globalOffset) : null;
            final SmoothVector3f lastLocalOffset = limitMovement ? new SmoothVector3f(data.localOffset) : null;

            BendsPackPerformer.INSTANCE.performCurrentPack(data, animatedEntityKey, actions);

            if (limitMovement)
            {
                data.globalOffset.limitDistanceTo(lastGlobalOffset, 10F);
                data.localOffset.limitDistanceTo(lastLocalOffset, 10F);
            }
        }
    }

    public abstract void syncUpWithData(D data);

    public D getData(E entity)
    {
        return EntityDatabase.instance.get(entity);
    }

    public D getOrMakeData(E entity)
    {
        return EntityDatabase.instance.getOrMake(dataFactory, entity);
    }

    public abstract boolean isModelVanilla(M model);

    public abstract boolean shouldModelBeSkipped(EntityModel<?> model);

    public void postRefresh()
    {
    }

    public abstract void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                                       int packedLight, int packedOverlay, int color);

    public abstract boolean shouldRenderCustom();

    public void beforeRender(D data, E entity, float partialTicks, PoseStack poseStack)
    {
    }

    public void afterRender(D data, E entity, float partialTicks, PoseStack poseStack)
    {
    }
}
