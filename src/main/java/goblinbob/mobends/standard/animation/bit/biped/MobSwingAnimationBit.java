package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.standard.animation.bit.biped.item.BipedActionController;
import goblinbob.mobends.standard.animation.bit.biped.item.AxeAction;
import goblinbob.mobends.standard.animation.bit.biped.item.StabAction;
import goblinbob.mobends.standard.animation.bit.player.PunchAnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

import java.util.Arrays;
import java.util.List;

public class MobSwingAnimationBit extends AnimationBit<BipedEntityData<?>>
{

    private static final float SWING_TICKS = 6.0F;

    public static boolean isSwinging(BipedEntityData<?> data)
    {
        return data.getTicksAfterAnyAttack() < SWING_TICKS
                || BipedActionController.getItemAttackAction(data.getEntity().getMainHandItem().getItem())
                        == AttackActionType.STAB
                && StabAction.stabProgress(data, data.getEntity().getMainArm()) < 1.0F;
    }

    public static boolean canPerform(LivingEntity entity)
    {
        return entity != null
                && BipedActionController.getItemAttackAction(entity.getMainHandItem().getItem())
                        != AttackActionType.FISTS;
    }

    private static final List<AnimationBit<BipedEntityData<?>>> SLASHES = Arrays.asList(
            new AttackSlashUpAnimationBit(),
            new AttackSlashDownAnimationBit(),
            new AttackSlashInwardAnimationBit(),
            new AttackSlashOutwardAnimationBit()
    );

    private static final AnimationBit<BipedEntityData<?>> SPIN = new AttackWhirlSlashAnimationBit();

    private static final AnimationBit<BipedEntityData<?>> STAB = new StabAction();

    private static final List<AnimationBit<BipedEntityData<?>>> PUNCHES = Arrays.asList(
            new PunchAnimationBit(HumanoidArm.LEFT),
            new PunchAnimationBit(HumanoidArm.RIGHT)
    );

    private final HardAnimationLayer<BipedEntityData<?>> layer = new HardAnimationLayer<>();


    @Override
    public void onPlay(BipedEntityData<?> data)
    {
        if (!canPerform(data.getEntity()))
        {
            this.layer.playBit(PUNCHES.get(Math.floorMod(data.getEntity().tickCount / 6, PUNCHES.size())), data);
            return;
        }

        final AttackActionType attackAction = BipedActionController.getItemAttackAction(data.getEntity().getMainHandItem().getItem());
        if (attackAction == AttackActionType.STAB || attackAction == AttackActionType.AXE)
        {
            this.layer.playBit(attackAction == AttackActionType.STAB
                    ? STAB
                    : AxeAction.MOVES.get(Math.floorMod(data.getEntity().tickCount / 6, AxeAction.MOVES.size())), data);
            return;
        }

        final int variantCount = ModConfig.mobsCanSpin ? SLASHES.size() + 1 : SLASHES.size();
        final int variant = Math.floorMod(data.getEntity().tickCount / 6, variantCount);

        this.layer.playBit(variant < SLASHES.size() ? SLASHES.get(variant) : SPIN, data);
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        this.layer.perform(data);
    }
}
