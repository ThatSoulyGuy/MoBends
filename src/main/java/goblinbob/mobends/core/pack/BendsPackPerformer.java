package goblinbob.mobends.core.pack;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;


public class BendsPackPerformer
{
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();


    public static final BendsPackPerformer INSTANCE = new BendsPackPerformer();

    public void performCurrentPack(EntityData<?> entityData, String animatedEntityKey)
    {
        final BendsPackData packData = PackDataProvider.INSTANCE.getAppliedData();
        if (packData == null)
        {
            return;
        }

        try
        {
            entityData.packAnimationState.update(entityData, packData, animatedEntityKey, DataUpdateHandler.ticksPerFrame);
        }
        catch (MalformedKumoTemplateException e)
        {
            LOGGER.error("Malformed bends pack animator; resetting the applied packs", e);
            PackManager.INSTANCE.resetAppliedPacks(true);
        }
    }

}
