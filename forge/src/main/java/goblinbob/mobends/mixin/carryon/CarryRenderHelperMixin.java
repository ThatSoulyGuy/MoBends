package goblinbob.mobends.mixin.carryon;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.compat.CarryOnCompat;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "tschipp.carryon.client.render.CarryRenderHelper", remap = false)
public class CarryRenderHelperMixin
{
    @Inject(method = "applyGeneralTransformations", at = @At("RETURN"), require = 0)
    private static void mobends$followAnimatedHands(Player player, float partialTicks, PoseStack matrix, CallbackInfo ci)
    {
        CarryOnCompat.applyAnchor(player, matrix);
    }
}
