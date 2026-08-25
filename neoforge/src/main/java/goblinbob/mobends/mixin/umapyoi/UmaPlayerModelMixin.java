package goblinbob.mobends.mixin.umapyoi;

import goblinbob.mobends.compat.UmapyoiCompat;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.tracen.umapyoi.client.model.UmaPlayerModel", remap = false)
public class UmaPlayerModelMixin
{
    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("HEAD"),
            require = 0)
    private void mobends$captureTailRestPose(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                             float ageInTicks, float netHeadYaw, float headPitch,
                                             CallbackInfo ci)
    {
        UmapyoiCompat.captureRestPose(this);
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("RETURN"),
            require = 0)
    private void mobends$applyBentPose(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                       float ageInTicks, float netHeadYaw, float headPitch,
                                       CallbackInfo ci)
    {
        UmapyoiCompat.applyPose(this, entity);
    }
}
