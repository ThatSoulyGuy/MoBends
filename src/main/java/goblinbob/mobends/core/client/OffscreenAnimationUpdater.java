package goblinbob.mobends.core.client;

import goblinbob.mobends.compat.ModCompatManager;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;

public final class OffscreenAnimationUpdater
{
    private static boolean playerRendered = false;

    private OffscreenAnimationUpdater()
    {
    }

    public static void markPlayerRendered()
    {
        playerRendered = true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void updateIfNotRendered(float partialTicks)
    {
        final boolean wasRendered = playerRendered;
        playerRendered = false;

        if (wasRendered)
        {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();
        final AbstractClientPlayer player = mc.player;

        if (player == null || player.isSpectator())
        {
            return;
        }

        final EntityBender bender = EntityBenderRegistry.instance.getForEntity(player);
        if (bender == null || !bender.isAnimated())
        {
            return;
        }

        if (ModCompatManager.shouldDeferAnimation(player))
        {
            return;
        }

        final EntityRenderer<?> renderer = mc.getEntityRenderDispatcher().getRenderer(player);
        if (!(renderer instanceof LivingEntityRenderer))
        {
            return;
        }

        bender.applyMutation((LivingEntityRenderer) renderer, player, partialTicks);
    }
}
