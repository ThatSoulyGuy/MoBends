package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.HumanoidMobController;
import net.minecraft.world.entity.LivingEntity;

public class HumanoidMobData<E extends LivingEntity> extends BipedEntityData<E>
{
    private final HumanoidMobController controller = new HumanoidMobController();

    public HumanoidMobData(E entity)
    {
        super(entity);
    }

    @Override
    public HumanoidMobController getController()
    {
        return controller;
    }

    @Override
    public void onTicksRestart()
    {
    }
}
