package goblinbob.mobends.standard.client.renderer.entity.mutated;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.world.entity.LivingEntity;

public class BipedRenderer<T extends LivingEntity> extends MutatedRenderer<T>
{

    @Override
    protected void renderLocalAccessories(T entity, EntityData<?> data, float partialTicks, PoseStack poseStack)
    {
        if (goblinbob.mobends.compat.BetterCombatCompat.suppressesTrail(entity))
        {
            return;
        }

        if (data instanceof BipedEntityData)
        {
            BipedEntityData<?> bipedData = (BipedEntityData<?>) data;
            if (ModConfig.showSwordTrail)
            {
                final float modelScale = (entity.isBaby() ? getChildScale() : 1.0F)
                        * goblinbob.mobends.core.util.EntityScaleHelper.getRenderScale(entity);

                poseStack.pushPose();
                poseStack.scale(scale, scale, scale);
                bipedData.swordTrail.render(poseStack, entity, modelScale);
                bipedData.offHandSwordTrail.render(poseStack, entity, modelScale);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
            }
        }
    }

    @Override
    protected void transformLocally(T entity, EntityData<?> data, float partialTicks, PoseStack poseStack)
    {
        if (entity.isCrouching())
        {
            poseStack.translate(0F, 5F * scale, 0F);
        }
    }

}
