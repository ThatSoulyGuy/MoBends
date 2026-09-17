package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.ModernCompanionController;
import net.minecraft.world.entity.LivingEntity;

public class ModernCompanionData<E extends LivingEntity> extends HumanoidMobData<E>
{
    private final ModernCompanionController controller = new ModernCompanionController();

    public ModernCompanionData(E entity)
    {
        super(entity);
    }

    @Override
    public ModernCompanionController getController()
    {
        return controller;
    }
}
