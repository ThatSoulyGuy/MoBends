package goblinbob.mobends.mixin.mca;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.compat.McaCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.mca.client.model.VillagerEntityBaseModelMCA", remap = false)
public class VillagerModelMcaMixin
{
    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true, require = 0)
    private void mobends$interceptRender(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         int packedLight, int packedOverlay, int color,
                                         CallbackInfo ci)
    {
        if (McaCompat.renderMutatedInstead(this, poseStack, vertexConsumer, packedLight, packedOverlay, color))
        {
            ci.cancel();
        }
    }
}
