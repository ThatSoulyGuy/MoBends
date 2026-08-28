package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.EatingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.RidingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SittingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SprintAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.StandAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.TradeOfferAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.WalkAnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.VillagerData;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

public class VillagerController implements IAnimationController<VillagerData<?>>
{
    protected HardAnimationLayer<VillagerData<?>> layerBase;

    protected AnimationBit<VillagerData<?>> bitStand, bitWalk, bitSprint, bitJump, bitRiding, bitSitting;

    protected final HardAnimationLayer<BipedEntityData<?>> layerHandAction = new HardAnimationLayer<>();

    protected final AnimationBit<BipedEntityData<?>> bitTradeOffer = new TradeOfferAnimationBit();
    protected final AnimationBit<BipedEntityData<?>> bitDrink = new EatingAnimationBit(HumanoidArm.RIGHT);

    public VillagerController()
    {
        this.layerBase = new HardAnimationLayer<>();
        this.bitStand = new StandAnimationBit<>();
        this.bitWalk = new WalkAnimationBit<>();
        this.bitSprint = new SprintAnimationBit<>();
        this.bitJump = new JumpAnimationBit<>();
        this.bitRiding = new RidingAnimationBit<>();
        this.bitSitting = new SittingAnimationBit<>();
    }

    @Override
    public void perform(VillagerData<?> data)
    {
        if (data.isRiding())
        {
            this.layerBase.playOrContinueBit(data.isRidingLivingEntity() ? bitRiding : bitSitting, data);
        }
        else if (!data.isOnGround() || data.getTicksAfterTouchdown() < 1)
        {
            this.layerBase.playOrContinueBit(bitJump, data);
        }
        else if (data.isStillHorizontally())
        {
            this.layerBase.playOrContinueBit(bitStand, data);
        }
        else if (data.isMovingAtSprintSpeed())
        {
            this.layerBase.playOrContinueBit(bitSprint, data);
        }
        else
        {
            this.layerBase.playOrContinueBit(bitWalk, data);
        }

        this.layerBase.perform(data);
        performActionAnimations(data);
    }

    protected void performActionAnimations(VillagerData<?> data)
    {
        final LivingEntity entity = data.getEntity();

        if (isConsumingItem(entity))
        {
            this.layerHandAction.playOrContinueBit(this.bitDrink, data);
        }
        else if (TradeOfferAnimationBit.getOfferedArm(entity) != null)
        {
            this.layerHandAction.playOrContinueBit(this.bitTradeOffer, data);
        }
        else
        {
            this.layerHandAction.clearAnimation();
        }

        this.layerHandAction.perform(data);
    }

    protected static boolean isConsumingItem(LivingEntity entity)
    {
        ItemStack itemStack = entity.getUseItem();

        if (itemStack.isEmpty())
        {
            itemStack = entity.getMainHandItem();
        }

        final UseAnim useAnim = itemStack.getUseAnimation();

        if (useAnim != UseAnim.DRINK && useAnim != UseAnim.EAT)
        {
            return false;
        }

        return entity.isUsingItem() || entity instanceof WanderingTrader;
    }
}
