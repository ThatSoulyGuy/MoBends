package goblinbob.mobends.standard.animation.bit.player;

import goblinbob.mobends.compat.ZiplineCompat;
import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.HumanoidArm;

public class ZiplineArmAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    private static final float ARM_PITCH = -180.0F;
    private static final float ELBOW_BEND = 12.0F;
    private static final float PIVOT_DROP = 3.0F;
    private static final float PIVOT_SPEED = 0.35F;
    private static final float TOOL_RAISE = 80.0F;
    private static final float TOOL_LEAN = 12.0F;
    private static final float TOOL_SPIN = 90.0F;

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final HumanoidArm arm = ZiplineCompat.getZipliningArm(data.getEntity());

        settlePivot(data.rightArm, arm == HumanoidArm.RIGHT);
        settlePivot(data.leftArm, arm == HumanoidArm.LEFT);

        if (arm == null)
        {
            return;
        }

        final float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        final ModelPartTransform upperArm = arm == HumanoidArm.RIGHT ? data.rightArm : data.leftArm;
        final ModelPartTransform foreArm = arm == HumanoidArm.RIGHT ? data.rightForeArm : data.leftForeArm;
        final SmoothOrientation toolRotation = arm == HumanoidArm.RIGHT
                ? data.renderRightItemRotation
                : data.renderLeftItemRotation;

        upperArm.rotation.setSmoothness(0.45F).orientX(ARM_PITCH + ELBOW_BEND);
        foreArm.rotation.setSmoothness(0.45F).orientX(-ELBOW_BEND);
        toolRotation.setSmoothness(0.45F).orientX(TOOL_RAISE)
                .rotateZ(-TOOL_LEAN * side)
                .rotateY(TOOL_SPIN * side);
    }

    private static void settlePivot(ModelPartTransform arm, boolean raised)
    {
        final float target = raised ? PIVOT_DROP : 0.0F;
        final float step = Math.min(1.0F, PIVOT_SPEED * DataUpdateHandler.ticksPerFrame);
        float y = arm.offset.y + (target - arm.offset.y) * step;

        if (Math.abs(y - target) < 0.01F)
        {
            y = target;
        }

        arm.offset.set(arm.offset.x, y, arm.offset.z);
    }
}
