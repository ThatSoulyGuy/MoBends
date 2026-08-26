package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.VillagerController;
import net.minecraft.world.entity.LivingEntity;

public class VillagerData<E extends LivingEntity> extends BipedEntityData<E>
{
    private final VillagerController controller = new VillagerController();

    public VillagerData(E entity)
    {
        super(entity);
    }

    @Override
    public VillagerController getController()
    {
        return controller;
    }

    @Override
    public void onTicksRestart()
    {
    }
}
