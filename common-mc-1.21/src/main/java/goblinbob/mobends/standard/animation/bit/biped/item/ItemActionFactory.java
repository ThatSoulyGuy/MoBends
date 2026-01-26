package goblinbob.mobends.standard.animation.bit.biped.item;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import net.minecraft.world.entity.HumanoidArm;

@FunctionalInterface
public interface ItemActionFactory<T extends AnimationBit<?>>
{
    T create(HumanoidArm actionHand);
}
