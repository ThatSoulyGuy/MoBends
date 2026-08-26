package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.EatingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.item.ToolAction;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.VillagerData;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;

public class WitchController extends VillagerController
{
    protected final HardAnimationLayer<BipedEntityData<?>> layerAction = new HardAnimationLayer<>();

    protected final AnimationBit<BipedEntityData<?>> bitThrow = new ToolAction(HumanoidArm.RIGHT);
    protected final AnimationBit<BipedEntityData<?>> bitDrink = new EatingAnimationBit(HumanoidArm.RIGHT);

    @Override
    protected void performActionAnimations(VillagerData<?> data)
    {
        final LivingEntity entity = data.getEntity();

        if (entity instanceof Witch witch && witch.isDrinkingPotion())
        {
            this.layerAction.playOrContinueBit(bitDrink, data);
        }
        else if (entity.swinging)
        {
            this.layerAction.playOrContinueBit(bitThrow, data);
        }
        else
        {
            this.layerAction.clearAnimation();
        }

        this.layerAction.perform(data);
    }
}
