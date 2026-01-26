package goblinbob.mobends.mixin.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.neoforge.mixin.MixinBridge;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to ensure elytra renders correctly with Mo'Bends body transforms.
 */
@Mixin(ElytraLayer.class)
public abstract class ElytraLayerMixin<T extends LivingEntity>
{
    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD")
    )
    private void mobends$onRenderStart(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci)
    {
        // Check if Mo'Bends has data for this entity
        if (!MixinBridge.hasAnimationData(entity))
        {
            return;
        }

        // The body transform will be applied to the elytra through the parent
        // model's transform. Additional adjustments can be applied here if needed.
    }
}
