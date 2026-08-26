package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.McaVillagerController;
import net.minecraft.world.entity.LivingEntity;

public class McaVillagerData<E extends LivingEntity> extends BipedEntityData<E>
{
    private final McaVillagerController controller = new McaVillagerController();

    public McaVillagerData(E entity)
    {
        super(entity);
    }

    @Override
    public McaVillagerController getController()
    {
        return controller;
    }

    @Override
    public void onTicksRestart()
    {
    }
}
