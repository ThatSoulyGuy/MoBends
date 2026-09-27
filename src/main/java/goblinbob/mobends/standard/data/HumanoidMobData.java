package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.HumanoidMobController;
import net.minecraft.world.entity.LivingEntity;

public class HumanoidMobData<E extends LivingEntity> extends BipedEntityData<E>
{
    private final HumanoidMobController<HumanoidMobData<?>> controller = new HumanoidMobController<>();

    public HumanoidMobData(E entity)
    {
        super(entity);
    }

    @Override
    public HumanoidMobController<HumanoidMobData<?>> getController()
    {
        return controller;
    }
}
