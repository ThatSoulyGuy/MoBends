package goblinbob.mobends.standard.animation.bit.biped.item;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.AxeStanceSprintAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.AxeSwingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.AxeSwingAnimationBit.Pose;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class AxeAction extends AnimationBit<BipedEntityData<?>>
{
    public static final List<AnimationBit<BipedEntityData<?>>> MOVES = List.of(
            new AxeSwingAnimationBit(new Pose(-170, 0, 25, -50, -10, 25, 30), new Pose(-15, 0, -10, -10, 25, -20, 0)),
            new AxeSwingAnimationBit(new Pose(-150, 20, 40, -45, -5, 30, 45), new Pose(-30, -30, -35, -10, 20, -40, 20)),
            new AxeSwingAnimationBit(new Pose(0, -100, 70, -50, 5, -40, 0), new Pose(0, 40, 80, -10, 15, 35, 0))
    );

    private static final float MIN_MOVE_INTERVAL = 4.0F;
    private static final float STANCE_TICKS = 60.0F;
    private static final float JUMP_AIRBORNE_TICKS = 14.0F;

    protected final HardAnimationLayer<BipedEntityData<?>> layerBase = new HardAnimationLayer<>();
    protected final AxeStanceSprintAnimationBit bitStanceSprint = new AxeStanceSprintAnimationBit();

    protected float lastTicksAfterAttack = 0.0F;
    protected float ticksSinceMove = 100.0F;
    protected int moveId = 0;

    private void nextMove(BipedEntityData<?> data)
    {
        final LivingEntity entity = data.getEntity();

        if (entity == Minecraft.getInstance().player)
        {
            this.layerBase.playBit(MOVES.get(moveId), data);
            moveId = (moveId + 1) % MOVES.size();
        }
        else
        {
            this.layerBase.playBit(MOVES.get((entity.tickCount / 6) % MOVES.size()), data);
        }
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final float ticksAfterAttack = data.getTicksAfterAnyAttack();

        ticksSinceMove += goblinbob.mobends.core.client.event.DataUpdateHandler.ticksPerFrame;

        if (ticksAfterAttack < lastTicksAfterAttack
                && (!goblinbob.mobends.compat.ModCompatManager.tracksPerHandAttacks() || ticksSinceMove >= MIN_MOVE_INTERVAL))
        {
            nextMove(data);
            ticksSinceMove = 0.0F;
        }
        lastTicksAfterAttack = ticksAfterAttack;

        if (ticksAfterAttack > 20)
        {
            moveId = 0;
        }

        if (ticksAfterAttack >= AxeSwingAnimationBit.DURATION)
        {
            final LivingEntity entity = data.getEntity();
            final boolean grounded = data.isOnGround();
            final boolean jumping = !grounded && data.getTicksInAir() <= JUMP_AIRBORNE_TICKS;

            if (ticksAfterAttack < STANCE_TICKS && entity.isSprinting() && (grounded || jumping))
            {
                this.layerBase.playOrContinueBit(this.bitStanceSprint, data);
            }
            else
            {
                this.layerBase.clearAnimation();
            }
        }

        this.layerBase.perform(data);
    }
}
