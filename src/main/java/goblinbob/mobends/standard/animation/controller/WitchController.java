package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.item.ToolAction;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.VillagerData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;

public class WitchController extends VillagerController
{
    protected final AnimationBit<BipedEntityData<?>> bitThrow = new ToolAction();

    @Override
    protected void performActionAnimations(VillagerData<?> data)
    {
        final LivingEntity entity = data.getEntity();

        if (entity instanceof Witch witch && witch.isDrinkingPotion())
        {
            this.layerHandAction.playOrContinueBit(bitDrink, data);
        }
        else if (entity.swinging)
        {
            this.layerHandAction.playOrContinueBit(bitThrow, data);
        }
        else
        {
            this.layerHandAction.clearAnimation();
        }

        this.layerHandAction.perform(data);
    }
}
