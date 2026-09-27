package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.NpcBipedController;
import net.minecraft.world.entity.LivingEntity;

public class CustomNpcData<E extends LivingEntity> extends BipedEntityData<E>
{
    private final NpcBipedController controller = new NpcBipedController(goblinbob.mobends.compat.CustomNpcsCompat::isSitting);

    public CustomNpcData(E entity)
    {
        super(entity);
    }

    @Override
    public NpcBipedController getController()
    {
        return controller;
    }
}
