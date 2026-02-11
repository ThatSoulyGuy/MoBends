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
import goblinbob.mobends.lib.util.GUtil;
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

/**
 * Base class for entity model mutators.
 * Updated for Minecraft 1.20.1.
 *
 * @param <D> The entity data type
 * @param <E> The entity type
 * @param <M> The model type
 */
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

    private final IEntityDataFactory<E> dataFactory;
    protected List<RenderLayer<E, M>> layerRenderers;

    public Mutator(IEntityDataFactory<E> dataFactory)
    {
        this.dataFactory = dataFactory;
    }

    /**
     * Used to fetch private data from the original renderer.
     * Uses reflection to access the layers field.
     * Tries known field names first, then falls back to type-based search
     * to handle different obfuscation mappings (Mojang vs SRG).
     */
    @SuppressWarnings("unchecked")
    public void fetchFields(LivingEntityRenderer<E, M> renderer)
    {
        // Getting the layer renderers using reflection
        try
        {
            if (layersField == null)
            {
                // Try known field names first (Mojang: "layers", SRG: "f_115313_")
                String[] fieldNames = {"layers", "f_115313_"};
                for (String name : fieldNames)
                {
                    try
                    {
                        layersField = LivingEntityRenderer.class.getDeclaredField(name);
                        layersField.setAccessible(true);
                        LOGGER.debug("Found layers field via name '{}'", name);
                        break;
                    }
                    catch (NoSuchFieldException ignored)
                    {
                    }
                }

                // Fallback: search by type (List containing RenderLayer instances)
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
                                LOGGER.debug("Found layers field via type search: '{}'", field.getName());
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

    /**
     * Sets the model parameter back to its vanilla state.
     * Used to demutate the model.
     */
    public abstract void applyVanillaModel(M model);

    /**
     * Swaps out the vanilla layers for their custom counterparts,
     * and if it's a vanilla model, it stores the vanilla layers
     * for future mutation reversal.
     */
    public abstract void swapLayer(LivingEntityRenderer<E, M> renderer, int index, boolean isModelVanilla);

    /**
     * Swaps the custom layers back with the vanilla layers.
     * Used to demutate the model.
     */
    public abstract void deswapLayer(LivingEntityRenderer<E, M> renderer, int index);

    /**
     * Creates all the custom parts you need! It swaps all the
     * original parts with newly created custom parts.
     */
    public abstract boolean createParts(M original, float scaleFactor);

    /**
     * Mutate the renderer's model.
     */
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
            // If this model wasn't mutated before, save it as the vanilla model.
            this.storeVanillaModel(model);
        }

        this.createParts(model, scaleFactor);

        // Swapping layers
        if (this.layerRenderers != null)
        {
            for (int i = 0; i < layerRenderers.size(); ++i)
            {
                swapLayer(renderer, i, isModelVanilla);
            }
        }

        return true;
    }

    /**
     * Performs the steps needed to demutate the model.
     */
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

    /**
     * Update the model parameters from the entity state.
     */
    public void updateModel(E entity, LivingEntityRenderer<E, M> renderer, float partialTicks)
    {
        boolean shouldSit = entity.isPassenger()
                && EntityHelper.shouldRiderSit(entity.getVehicle());

        float f = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float f1 = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float yaw = f1 - f;

        if (shouldSit && entity.getVehicle() instanceof LivingEntity)
        {
            LivingEntity vehicle = (LivingEntity) entity.getVehicle();
            f = Mth.rotLerp(partialTicks, vehicle.yBodyRotO, vehicle.yBodyRot);
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
        }

        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        float f5 = 0.0F;
        float f6 = 0.0F;

        if (!entity.isPassenger())
        {
            // 1.20.1 uses walkAnimation for limb swing
            f5 = entity.walkAnimation.speed(partialTicks);
            f6 = entity.walkAnimation.position(partialTicks);

            if (entity.isBaby())
                f6 *= 3.0F;
            if (f5 > 1.0F)
                f5 = 1.0F;
            yaw = f1 - f;
        }

        this.headYaw = yaw;
        this.headPitch = pitch;
        this.limbSwing = f6;
        this.limbSwingAmount = f5;
        this.swingProgress = entity.getAttackAnim(partialTicks);
    }

    /**
     * Perform animations on the entity data.
     */
    public void performAnimations(D data, String animatedEntityKey, LivingEntityRenderer<E, M> renderer, float partialTicks)
    {
        data.headYaw.set(Mth.wrapDegrees(this.headYaw));
        data.headPitch.set(Mth.wrapDegrees(this.headPitch));
        data.limbSwing.set(this.limbSwing);
        data.limbSwingAmount.set(this.limbSwingAmount);
        data.swingProgress.set(this.swingProgress);

        KumoVariableRegistry.instance.provideTemporaryData(data);

        // noinspection unchecked
        final IAnimationController<D> controller = (IAnimationController<D>) data.getController();
        final Collection<String> actions = controller.perform(data);

        SmoothVector3f lastGlobalOffset = new SmoothVector3f(data.globalOffset);
        SmoothVector3f lastLocalOffset = new SmoothVector3f(data.localOffset);
        if (NetworkConfiguration.instance.areBendsPacksAllowed())
        {
            BendsPackPerformer.INSTANCE.performCurrentPack(data, animatedEntityKey, actions);

            if (NetworkConfiguration.instance.isMovementLimited())
            {
                // Limit movement
                data.globalOffset.limitDistanceTo(lastGlobalOffset, 10F);
                data.localOffset.limitDistanceTo(lastLocalOffset, 10F);
            }
        }
    }

    /**
     * Sync the mutator state with the entity data.
     */
    public abstract void syncUpWithData(D data);

    /**
     * Get existing data for the entity.
     */
    public D getData(E entity)
    {
        return EntityDatabase.instance.get(entity);
    }

    /**
     * Get or create data for the entity.
     */
    public D getOrMakeData(E entity)
    {
        return EntityDatabase.instance.getOrMake(dataFactory, entity);
    }

    /**
     * True, if this renderer wasn't mutated before.
     */
    public abstract boolean isModelVanilla(M model);

    /**
     * Returns true, if this model should skip the mutation process.
     */
    public abstract boolean shouldModelBeSkipped(EntityModel<?> model);

    /**
     * Called right after this mutator has been refreshed.
     */
    public void postRefresh()
    {
        // No default behaviour
    }

    /**
     * Render the mutated model.
     * This is the new 1.21.1 rendering method using PoseStack and VertexConsumer.
     */
    public abstract void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                                       int packedLight, int packedOverlay, int color);

    /**
     * Whether the mutator should render custom models.
     */
    public abstract boolean shouldRenderCustom();

    /**
     * Called before rendering to set up the render context.
     */
    public void beforeRender(D data, E entity, float partialTicks, PoseStack poseStack)
    {
        // Default implementation - subclasses can override
    }

    /**
     * Called after rendering to clean up.
     */
    public void afterRender(D data, E entity, float partialTicks, PoseStack poseStack)
    {
        // Default implementation - subclasses can override
    }
}
