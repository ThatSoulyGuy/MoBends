package goblinbob.mobends.core.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.compat.PlayerAnimationLibCompat;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.SpiderMutator;
import goblinbob.mobends.standard.mutators.WolfMutator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class EntityRenderHandler
{
    // Track which entities we pushed pose for (to avoid imbalanced stack)
    private static final java.util.Set<Integer> entitiesWithPushedPose = new java.util.HashSet<>();

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void beforeLivingRender(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<?>> event)
    {
        final LivingEntity living = event.getEntity();
        final EntityBender<LivingEntity> entityBender = EntityBenderRegistry.instance.getForEntity(living);

        if (entityBender == null)
        {
            return;
        }

        // Check if PlayerAnimationLib has an active animation for this entity
        // If so, let PlayerAnimationLib handle the animation and skip Mo'Bends
        if (PlayerAnimationLibCompat.hasActiveAnimation(living))
        {
            // De-apply any existing mutation so vanilla/PlayerAnimationLib can render normally
            final LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer =
                (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) event.getRenderer();
            entityBender.deapplyMutation(renderer, living);
            return;
        }

        final LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer =
            (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) event.getRenderer();
        final float pt = event.getPartialTick();
        final PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        entitiesWithPushedPose.add(living.getId());

        if (entityBender.isAnimated())
        {
            if (entityBender.applyMutation(renderer, living, pt))
            {
                final Object rawMutator = entityBender.getMutator(renderer);
                final Mutator<?, LivingEntity, ?> mutator =
                    (Mutator<?, LivingEntity, ?>) rawMutator;
                final LivingEntityData<LivingEntity> data =
                    (LivingEntityData<LivingEntity>) mutator.getData(living);

                // Set the mutator in the render context so the mixin can intercept rendering
                if (rawMutator instanceof BipedMutator<?, ?, ?> bipedMutator)
                {
                    MoBendsRenderContext.setCurrentBipedMutator(bipedMutator);

                    // Sync animated poses to vanilla model so layers (armor, held items) can use them
                    EntityModel<?> model = renderer.getModel();
                    if (model instanceof HumanoidModel<?> humanoidModel)
                    {
                        bipedMutator.syncPosesToVanillaModel(humanoidModel);
                    }
                }
                else if (rawMutator instanceof SpiderMutator spiderMutator)
                {
                    MoBendsRenderContext.setCurrentSpiderMutator(spiderMutator);
                }
                else if (rawMutator instanceof WolfMutator wolfMutator)
                {
                    MoBendsRenderContext.setCurrentWolfMutator(wolfMutator);
                }

                entityBender.beforeRender(data, living, pt, poseStack);
            }
        }
        else
        {
            entityBender.deapplyMutation(renderer, living);
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void afterLivingRender(RenderLivingEvent.Post<? extends LivingEntity, ? extends EntityModel<?>> event)
    {
        // Always clear the render context after rendering
        MoBendsRenderContext.clear();

        final LivingEntity living = event.getEntity();
        final EntityBender<LivingEntity> entityBender = EntityBenderRegistry.instance.getForEntity(living);

        if (entityBender == null)
            return;

        // Only pop if we pushed for this entity
        if (entitiesWithPushedPose.remove(living.getId()))
        {
            entityBender.afterRender(living, event.getPartialTick(), event.getPoseStack());
            event.getPoseStack().popPose();
        }
    }
}
