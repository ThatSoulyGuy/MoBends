package goblinbob.mobends.standard.client.event;

import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.standard.mutators.PlayerMutator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderingEventHandler
{

    @SubscribeEvent
    public void beforeHandRender(RenderHandEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        Entity viewEntity = mc.getCameraEntity();

        if (!(viewEntity instanceof AbstractClientPlayer))
            return;

        AbstractClientPlayer player = (AbstractClientPlayer) viewEntity;

        if (!BenderHelper.isEntityAnimated(player))
        	return;

        PlayerRenderer renderPlayer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
        PlayerMutator mutator = (PlayerMutator) BenderHelper.getMutatorForRenderer(AbstractClientPlayer.class, renderPlayer);
        if (mutator != null)
            mutator.poseForFirstPersonView();
    }

}
