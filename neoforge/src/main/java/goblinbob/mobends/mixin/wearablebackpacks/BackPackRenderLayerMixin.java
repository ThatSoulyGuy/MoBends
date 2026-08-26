package goblinbob.mobends.mixin.wearablebackpacks;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.compat.WearableBackpacksCompat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.nyfaria.wearablebackpacks.client.layer.BackPackRenderLayer", remap = false)
public class BackPackRenderLayerMixin
{
    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD"),
            require = 0)
    private void mobends$beginFollowAnimatedBody(PoseStack poseStack, MultiBufferSource bufferSource,
                                                 int packedLight, LivingEntity entity,
                                                 float limbSwing, float limbSwingAmount, float partialTicks,
                                                 float ageInTicks, float netHeadYaw, float headPitch,
                                                 CallbackInfo ci)
    {
        WearableBackpacksCompat.beginFollow(poseStack, entity);
    }

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("RETURN"),
            require = 0)
    private void mobends$endFollowAnimatedBody(PoseStack poseStack, MultiBufferSource bufferSource,
                                               int packedLight, LivingEntity entity,
                                               float limbSwing, float limbSwingAmount, float partialTicks,
                                               float ageInTicks, float netHeadYaw, float headPitch,
                                               CallbackInfo ci)
    {
        WearableBackpacksCompat.endFollow(poseStack);
    }

    @Redirect(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isShiftKeyDown()Z"),
            require = 0)
    private boolean mobends$skipVanillaSneakOffset(LivingEntity entity)
    {
        if (WearableBackpacksCompat.isFollowing())
        {
            return false;
        }
        return entity.isShiftKeyDown();
    }
}
