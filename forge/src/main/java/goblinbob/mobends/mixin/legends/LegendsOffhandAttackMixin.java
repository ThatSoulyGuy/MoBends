package goblinbob.mobends.mixin.legends;

import goblinbob.mobends.compat.LegendsModCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.tihyo.legends.client.animation.AnimationUtils", remap = false)
public class LegendsOffhandAttackMixin
{
    @Inject(
            method = "setupOffhandAttackAnimation(Lnet/minecraft/client/model/HumanoidModel;)V",
            at = @At("HEAD"),
            require = 0)
    private static void mobends$beginOffhandAttack(CallbackInfo ci)
    {
        LegendsModCompat.beginSuppressedWrites(LegendsModCompat.BODY);
    }

    @Inject(
            method = "setupOffhandAttackAnimation(Lnet/minecraft/client/model/HumanoidModel;)V",
            at = @At("RETURN"),
            require = 0)
    private static void mobends$endOffhandAttack(CallbackInfo ci)
    {
        LegendsModCompat.endSuppressedWrites();
    }
}
