package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.client.model.adaptive.AdaptiveHumanoidGeometry;
import net.minecraft.world.entity.LivingEntity;

public class VillagersRebornMutator<E extends LivingEntity> extends HumanoidMobMutator<E>
{
    public VillagersRebornMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    protected AdaptiveHumanoidGeometry.CaptureMode adaptiveLimbCaptureMode()
    {
        return AdaptiveHumanoidGeometry.CaptureMode.OWN_CUBES;
    }
}
