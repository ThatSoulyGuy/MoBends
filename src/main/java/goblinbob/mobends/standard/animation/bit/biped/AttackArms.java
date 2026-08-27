package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.SwordItem;

public final class AttackArms
{
    private AttackArms()
    {
    }

    public static HumanoidArm offArm(HumanoidArm arm)
    {
        return arm == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public static HumanoidArm attackingArm(BipedEntityData<?> data, LivingEntity living)
    {
        final HumanoidArm mainArm = living.getMainArm();

        if (!goblinbob.mobends.compat.ModCompatManager.tracksPerHandAttacks())
        {
            return mainArm;
        }

        return data.getTicksAfterOffHandAttack() < data.getTicksAfterAttack()
                ? offArm(mainArm)
                : mainArm;
    }

    public static boolean isDualWielding(BipedEntityData<?> data)
    {
        return goblinbob.mobends.compat.ModCompatManager.tracksPerHandAttacks()
                && data.isOffHandAttacking()
                && data.getTicksAfterAttack() < 10.0F;
    }

    public static InteractionHand handOf(LivingEntity living, HumanoidArm arm)
    {
        return arm == living.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    private static boolean holdsSword(LivingEntity living, HumanoidArm arm)
    {
        return living.getItemInHand(handOf(living, arm)).getItem() instanceof SwordItem;
    }

    public static void resetTrails(BipedEntityData<?> data)
    {
        data.swordTrail.reset();
        data.offHandSwordTrail.reset();
    }

    public static void emitTrails(BipedEntityData<?> data, LivingEntity living, HumanoidArm attackArm,
                                  float attackTicks, boolean dualWielding)
    {
        if (attackTicks < 4F && holdsSword(living, attackArm))
        {
            data.swordTrail.add(data, attackArm);
        }

        if (!dualWielding)
        {
            return;
        }

        final HumanoidArm other = offArm(attackArm);

        if (ticksAfterAttack(data, living, other) < 4F && holdsSword(living, other))
        {
            data.offHandSwordTrail.add(data, other);
        }
    }

    public static float ticksAfterAttack(BipedEntityData<?> data, LivingEntity living, HumanoidArm arm)
    {
        if (!goblinbob.mobends.compat.ModCompatManager.tracksPerHandAttacks())
        {
            return data.getTicksAfterAttack();
        }

        return arm == living.getMainArm()
                ? data.getTicksAfterAttack()
                : data.getTicksAfterOffHandAttack();
    }
}
