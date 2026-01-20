package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.flux.ComputedDependencyHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FluxHandler
{

    @SubscribeEvent
    public void checkDirty(TickEvent.RenderTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            return;
        }

        ComputedDependencyHelper.reevaluateDirty();
    }

}
