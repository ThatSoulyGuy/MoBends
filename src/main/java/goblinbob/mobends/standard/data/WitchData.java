package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.WitchController;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.AABB;

public class WitchData<E extends LivingEntity> extends VillagerData<E>
{
    private static final double THROW_SEARCH_RADIUS = 3.0D;
    private static final int THROW_DETECT_AGE = 1;

    private final WitchController controller = new WitchController();

    public WitchData(E entity)
    {
        super(entity);
    }

    @Override
    public WitchController getController()
    {
        return controller;
    }

    @Override
    public void updateClient()
    {
        super.updateClient();

        final E entity = getEntity();
        if (entity == null || entity.level() == null)
        {
            return;
        }

        final AABB area = entity.getBoundingBox().inflate(THROW_SEARCH_RADIUS);
        final boolean thrown = !entity.level()
                .getEntitiesOfClass(ThrownPotion.class, area,
                        potion -> potion.tickCount <= THROW_DETECT_AGE && potion.getOwner() == entity)
                .isEmpty();

        if (thrown)
        {
            entity.swing(InteractionHand.MAIN_HAND);
        }
    }
}
