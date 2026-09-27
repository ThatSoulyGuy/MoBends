package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.NpcBipedController;
import net.minecraft.world.entity.LivingEntity;

public class McaVillagerData<E extends LivingEntity> extends BipedEntityData<E>
{
    private final NpcBipedController controller = new NpcBipedController(e -> false);

    public McaVillagerData(E entity)
    {
        super(entity);
    }

    @Override
    public NpcBipedController getController()
    {
        return controller;
    }
}
