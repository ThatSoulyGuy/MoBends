package goblinbob.mobends.core.kumo;

import goblinbob.mobends.lib.client.model.IAnimatedPart;
import goblinbob.mobends.lib.data.ILivingEntityAnimationData;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.vector.IVec3f;
import goblinbob.mobends.lib.math.vector.SmoothVector3f;
import goblinbob.mobends.lib.math.vector.Vec3f;

import java.util.HashMap;
import java.util.Map;

public class FakeAnimationData implements ILivingEntityAnimationData
{
    public static final class FakePart implements IAnimatedPart
    {
        private final Vec3f offset = new Vec3f();
        private final SmoothOrientation rotation = new SmoothOrientation();

        @Override
        public IVec3f getOffset()
        {
            return offset;
        }

        @Override
        public SmoothOrientation getRotation()
        {
            return rotation;
        }
    }

    private final Map<String, Object> parts = new HashMap<>();
    private final SmoothVector3f globalOffset = new SmoothVector3f();
    private final SmoothOrientation centerRotation = new SmoothOrientation();

    private float limbSwing;
    private boolean living = true;

    public FakeAnimationData withBones(String... names)
    {
        for (String name : names)
        {
            parts.put(name, new FakePart());
        }
        return this;
    }

    public FakeAnimationData withNonAnimatablePart(String name)
    {
        parts.put(name, new Object());
        return this;
    }

    public FakePart part(String name)
    {
        return (FakePart) parts.get(name);
    }

    public FakeAnimationData setLimbSwing(float value)
    {
        this.limbSwing = value;
        return this;
    }

    public FakeAnimationData setLiving(boolean value)
    {
        this.living = value;
        return this;
    }

    @Override
    public Object getPartForName(String name)
    {
        return parts.get(name);
    }

    @Override
    public SmoothVector3f getGlobalOffset()
    {
        return globalOffset;
    }

    @Override
    public SmoothOrientation getCenterRotation()
    {
        return centerRotation;
    }

    @Override public double getPositionX() { return 0; }
    @Override public double getPositionY() { return 0; }
    @Override public double getPositionZ() { return 0; }
    @Override public double getMotionX() { return 0; }
    @Override public double getMotionY() { return 0; }
    @Override public double getMotionZ() { return 0; }
    @Override public double getInterpolatedMotionX() { return 0; }
    @Override public double getInterpolatedMotionY() { return 0; }
    @Override public double getInterpolatedMotionZ() { return 0; }
    @Override public double getMotionMagnitude() { return 0; }
    @Override public double getXZMotionMagnitude() { return 0; }
    @Override public double getInterpolatedMotionMagnitude() { return 0; }
    @Override public double getInterpolatedXZMotionMagnitude() { return 0; }
    @Override public double getForwardMomentum() { return 0; }
    @Override public double getSidewaysMomentum() { return 0; }
    @Override public float getMovementAngle() { return 0; }
    @Override public float getLookAngle() { return 0; }
    @Override public boolean isOnGround() { return true; }
    @Override public boolean isStillHorizontally() { return true; }
    @Override public boolean isStrafing() { return false; }
    @Override public boolean isUnderwater() { return false; }
    @Override public boolean isSprinting() { return false; }
    @Override public boolean isLiving() { return living; }
    @Override public float getHealth() { return living ? 20F : 0F; }
    @Override public float getMaxHealth() { return living ? 20F : 0F; }

    @Override public float getLimbSwing() { return limbSwing; }
    @Override public float getLimbSwingAmount() { return 0; }
    @Override public float getSwingProgress() { return 0; }
    @Override public float getHeadYaw() { return 0; }
    @Override public float getHeadPitch() { return 0; }
    @Override public float getTicksInAir() { return 0; }
    @Override public float getTicksAfterTouchdown() { return 0; }
    @Override public float getTicksAfterAttack() { return 0; }
    @Override public float getTicksFalling() { return 0; }
    @Override public boolean isClimbing() { return false; }
    @Override public float getClimbingCycle() { return 0; }
    @Override public float getClimbingRotation() { return 0; }
    @Override public float getLedgeHeight() { return 0; }
    @Override public boolean isDrawingBow() { return false; }
}
