package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;

public class UnhappyAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    private static final float SHAKE_SPEED = 0.45F;
    private static final float SHAKE_AMOUNT = (float) Math.toDegrees(0.3F);
    private static final float LOOK_DOWN = (float) Math.toDegrees(0.4F);

    private static final float SMOOTHNESS = 1.0F;

    public static boolean isUnhappy(LivingEntity entity)
    {
        return entity instanceof AbstractVillager villager && villager.getUnhappyCounter() > 0;
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final LivingEntity entity = data.getEntity();

        if (entity == null)
        {
            return;
        }

        final float ageInTicks = entity.tickCount + DataUpdateHandler.partialTicks;

        data.head.rotation.setSmoothness(SMOOTHNESS)
                .orientX(LOOK_DOWN)
                .rotateY(data.headYaw.get())
                .rotateZ(SHAKE_AMOUNT * Mth.sin(SHAKE_SPEED * ageInTicks));
    }
}
