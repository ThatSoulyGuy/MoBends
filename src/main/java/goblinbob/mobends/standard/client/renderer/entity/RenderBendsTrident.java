package goblinbob.mobends.standard.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.world.entity.projectile.ThrownTrident;

public class RenderBendsTrident extends ThrownTridentRenderer
{
    public RenderBendsTrident(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void render(ThrownTrident entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight)
    {
        if (ModConfig.tridentTrail)
        {
            ArrowTrailManager.renderTrail(entity, poseStack, partialTicks);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
