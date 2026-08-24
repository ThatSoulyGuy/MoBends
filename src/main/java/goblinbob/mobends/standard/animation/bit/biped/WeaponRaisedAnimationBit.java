package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class WeaponRaisedAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    private static final String[] ACTIONS = new String[] { "weapon_raised" };

    private static final float PI = (float) Math.PI;
    private static final float TO_DEGREES = 180F / PI;

    private static final float RAISED_PITCH = -1.8849558F;
    private static final float ARM_SPLAY = 0.15707964F;
    private static final float SMOOTHNESS = 0.35F;

    @Override
    public String[] getActions(BipedEntityData<?> entityData)
    {
        return ACTIONS;
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final LivingEntity entity = data.getEntity();
        final float age = DataUpdateHandler.getTicks();
        final float swing = data.swingProgress.get();

        final float f = Mth.sin(swing * PI);
        final float f1 = Mth.sin((1.0F - (1.0F - swing) * (1.0F - swing)) * PI);

        final boolean rightHanded = AttackArms.attackingArm(data, entity) == HumanoidArm.RIGHT;

        final float raised = RAISED_PITCH + Mth.cos(age * 0.09F) * 0.15F + f * 2.2F - f1 * 0.4F;
        final float trailing = Mth.cos(age * 0.19F) * 0.5F + f * 1.2F - f1 * 0.4F;

        final float rightPitch = rightHanded ? raised : trailing;
        final float leftPitch = rightHanded ? trailing : raised;

        final float bobRoll = Mth.cos(age * 0.09F) * 0.05F + 0.05F;
        final float bobPitch = Mth.sin(age * 0.067F) * 0.05F;

        data.rightArm.rotation.setSmoothness(SMOOTHNESS)
                .orientX((rightPitch + bobPitch) * TO_DEGREES)
                .rotateY(ARM_SPLAY * TO_DEGREES)
                .rotateZ(bobRoll * TO_DEGREES);
        data.leftArm.rotation.setSmoothness(SMOOTHNESS)
                .orientX((leftPitch - bobPitch) * TO_DEGREES)
                .rotateY(-ARM_SPLAY * TO_DEGREES)
                .rotateZ(-bobRoll * TO_DEGREES);

        final IModelPart mainForeArm = rightHanded ? data.rightForeArm : data.leftForeArm;
        final IModelPart offForeArm = rightHanded ? data.leftForeArm : data.rightForeArm;

        mainForeArm.getRotation().setSmoothness(SMOOTHNESS).orientX(-25F + f * 25F);
        offForeArm.getRotation().setSmoothness(SMOOTHNESS).orientX(-10F);
    }
}
