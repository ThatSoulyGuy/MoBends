package goblinbob.mobends.standard.animation.bit.biped.item;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.*;
import goblinbob.mobends.standard.animation.bit.biped.AttackStanceAnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.List;

public class SwordAction extends AnimationBit<BipedEntityData<?>>
{
    protected final HardAnimationLayer<BipedEntityData<?>> layerBase = new HardAnimationLayer<>();
    protected final AttackStanceAnimationBit bitAttackStance = new AttackStanceAnimationBit();
    protected final AttackStanceSprintAnimationBit bitAttackStanceSprint = new AttackStanceSprintAnimationBit();

    protected float lastTicksAfterAttack = 0.0F;
    protected float ticksSinceMove = 100.0F;
    protected int moveId = 0;

    private static final float MIN_MOVE_INTERVAL = 4.0F;

    private static final List<AnimationBit<BipedEntityData<?>>> bits = Arrays.asList(
            new AttackSlashUpAnimationBit(),
            new AttackSlashDownAnimationBit(),
            new AttackSlashInwardAnimationBit(),
            new AttackSlashOutwardAnimationBit(),
            new AttackWhirlSlashAnimationBit()
    );

    public SwordAction(HumanoidArm ignoredHandSide)
    {

    }

    private boolean canStartNextMove()
    {
        if (!goblinbob.mobends.compat.ModCompatManager.tracksPerHandAttacks())
        {
            return true;
        }

        return ticksSinceMove >= MIN_MOVE_INTERVAL;
    }

    private void nextMove(BipedEntityData<?> entityData)
    {
        LivingEntity entity = entityData.getEntity();
        boolean isLocal = entity == Minecraft.getInstance().player;

        boolean spinAllowed = isLocal ? ModConfig.performSpinAttack : ModConfig.mobsCanSpin;
        int moveCount = spinAllowed ? bits.size() : bits.size() - 1;

        if (!isLocal)
        {
            moveId = (entity.tickCount / 6) % moveCount;
        }
        else if (moveId >= moveCount)
        {
            moveId = 0;
        }

        AnimationBit<BipedEntityData<?>> bit = bits.get(moveId);

        if (bit != null)
        {
            this.layerBase.playBit(bit, entityData);
        }
        else
        {
            this.layerBase.clearAnimation();
        }

        if (isLocal)
        {
            moveId = (moveId + 1) % moveCount;
        }
    }

    @Override
    public void perform(BipedEntityData<?> entityData)
    {
        float ticksAfterAttack = entityData.getTicksAfterAnyAttack();

        ticksSinceMove += goblinbob.mobends.core.client.event.DataUpdateHandler.ticksPerFrame;

        if (ticksAfterAttack < lastTicksAfterAttack && canStartNextMove())
        {
            nextMove(entityData);
            ticksSinceMove = 0.0F;
        }
        lastTicksAfterAttack = ticksAfterAttack;

        LivingEntity entity = entityData.getEntity();

        int comboClearTime = 20;

        if (ticksAfterAttack > comboClearTime)
        {
            moveId = 0;
        }

        if (ticksAfterAttack < 10)
        {
        }
        else if (ticksAfterAttack < 60 && entityData.isOnGround())
        {
            if (entity.isSprinting())
            {
                this.layerBase.playOrContinueBit(this.bitAttackStanceSprint, entityData);
            }
            else if (entityData.isStillHorizontally())
            {
                this.layerBase.playOrContinueBit(this.bitAttackStance, entityData);
            }
            else
            {
                this.layerBase.clearAnimation();
            }
        }
        else
        {
            this.layerBase.clearAnimation();
        }

        this.layerBase.perform(entityData);
    }
}
