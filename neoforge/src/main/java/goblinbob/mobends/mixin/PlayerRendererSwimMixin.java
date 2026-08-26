package goblinbob.mobends.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.compat.ModCompatManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererSwimMixin
{
    @ModifyExpressionValue(
            method = "setupRotations",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getSwimAmount(F)F"),
            require = 0
    )
    private float mobends$suppressSwimRotation(float original, AbstractClientPlayer entity)
    {
        EntityBender bender = EntityBenderRegistry.instance.getForEntity(entity);
        if (bender != null && bender.isAnimated() && !ModCompatManager.shouldDeferAnimation(entity)
                && !mobends$isCrawling(entity))
        {
            return 0.0F;
        }
        return original;
    }

    @Unique
    private static boolean mobends$isCrawling(AbstractClientPlayer entity)
    {
        return (entity.isVisuallySwimming() && !entity.isInWater())
                || goblinbob.mobends.compat.CrawlCompat.isCrawling(entity);
    }
}
