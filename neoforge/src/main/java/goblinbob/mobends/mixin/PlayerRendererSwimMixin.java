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
    /**
     * Suppresses vanilla's swim rotation while Mo' Bends is posing the player.
     *
     * <p>{@code @ModifyExpressionValue} rather than {@code @Redirect}: a redirect claims a call
     * site exclusively, so a second mod touching this same {@code getSwimAmount} call would lose
     * — and because the injector carries {@code require = 0}, it would lose <em>quietly</em>, as a
     * warning in the log and a silently vanilla swim pose. This form stacks: each mod sees the
     * previous one's value and returns its own.
     *
     * <p>{@code original} is the value vanilla computed; the trailing parameter captures the
     * target method's first argument.
     */
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
