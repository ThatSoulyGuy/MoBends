package goblinbob.mobends.mixin;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.compat.ModCompatManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererSwimMixin
{
    /**
     * Suppresses vanilla's swim rotation while Mo' Bends is posing the player.
     *
     * <p>The NeoForge copy of this class uses MixinExtras' {@code @ModifyExpressionValue}, which
     * stacks — two mods can both modify this call. {@code @Redirect} claims the call site
     * exclusively, so a second mod touching this {@code getSwimAmount} call loses, and because of
     * {@code require = 0} it loses quietly: a warning in the log and a silently vanilla swim pose.
     *
     * <p>Forge 1.20.1 does not ship MixinExtras and NeoForge does. Shading and bootstrapping it
     * here was tried and made the Forge client hang during mod scanning, so this stays on
     * {@code @Redirect} rather than trading a theoretical conflict for a real startup failure. If
     * this is revisited, the fix must be verified by launching the client — it compiles either
     * way, and a lost injection is silent.
     */
    @Redirect(
            method = "setupRotations",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getSwimAmount(F)F"),
            require = 0
    )
    private float mobends$suppressSwimRotation(AbstractClientPlayer entity, float partialTick)
    {
        EntityBender bender = EntityBenderRegistry.instance.getForEntity(entity);
        if (bender != null && bender.isAnimated() && !ModCompatManager.shouldDeferAnimation(entity)
                && !mobends$isCrawling(entity))
        {
            return 0.0F;
        }
        return entity.getSwimAmount(partialTick);
    }

    @Unique
    private static boolean mobends$isCrawling(AbstractClientPlayer entity)
    {
        return entity.isVisuallySwimming() && !entity.isInWater();
    }
}
