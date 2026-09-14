package goblinbob.mobends.mixin.legends;

import goblinbob.mobends.compat.LegendsModCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {
        "com.tihyo.legends.abilities.passives.SpeedAbility",
        "com.tihyo.legends.abilities.passives.AdjustableSpeedAbility"
}, remap = false)
public class LegendsSpeedAnimationMixin
{
    @Inject(
            method = "setupAnimation(Lcom/tihyo/legends/client/events/SetupAnimationEvent;)V",
            at = @At("HEAD"),
            require = 0)
    private void mobends$beginSpeedAnimation(CallbackInfo ci)
    {
        LegendsModCompat.beginSuppressedWrites(LegendsModCompat.ALL_PARTS);
    }

    @Inject(
            method = "setupAnimation(Lcom/tihyo/legends/client/events/SetupAnimationEvent;)V",
            at = @At("RETURN"),
            require = 0)
    private void mobends$endSpeedAnimation(CallbackInfo ci)
    {
        LegendsModCompat.endSuppressedWrites();
    }
}
