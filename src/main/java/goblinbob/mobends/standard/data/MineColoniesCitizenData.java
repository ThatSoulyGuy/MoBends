package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.MineColoniesCitizenController;
import net.minecraft.world.entity.LivingEntity;

public class MineColoniesCitizenData<E extends LivingEntity> extends HumanoidMobData<E>
{
    private final MineColoniesCitizenController controller = new MineColoniesCitizenController();

    public MineColoniesCitizenData(E entity)
    {
        super(entity);
    }

    @Override
    public MineColoniesCitizenController getController()
    {
        return controller;
    }
}
