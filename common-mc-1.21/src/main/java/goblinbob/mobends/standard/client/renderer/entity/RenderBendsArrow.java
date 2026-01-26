package goblinbob.mobends.standard.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.projectile.AbstractArrow;

public abstract class RenderBendsArrow<T extends AbstractArrow> extends ArrowRenderer<T>
{
    public RenderBendsArrow(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight)
    {
        if (ModConfig.showArrowTrails)
        {
            ArrowTrailManager.renderTrail(entity, entity.getX(), entity.getY(), entity.getZ(), partialTicks);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
