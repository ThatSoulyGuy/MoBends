package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.flux.ComputedDependencyHelper;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class FluxHandler
{

    @SubscribeEvent
    public void checkDirty(RenderFrameEvent.Pre event)
    {
        ComputedDependencyHelper.reevaluateDirty();
    }

}
