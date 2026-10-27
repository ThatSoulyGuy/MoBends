package goblinbob.mobends.mixin.mobplayeranimator;

import goblinbob.mobends.compat.BetterCombatCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "me.Thelnfamous1.mobplayeranimator.api.PlayerAnimatorHelper", remap = false)
public class PlayerAnimatorHelperMixin
{
    @Inject(method = "applyBodyRotations", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mobends$skipBodyRotations(CallbackInfo ci)
    {
        if (BetterCombatCompat.skipsMobBodyRotations())
        {
            ci.cancel();
        }
    }
}
