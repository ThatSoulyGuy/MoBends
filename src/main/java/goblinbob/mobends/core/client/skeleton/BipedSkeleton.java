package goblinbob.mobends.core.client.skeleton;

import goblinbob.mobends.api.skeleton.IAnimatedSkeleton;
import goblinbob.mobends.api.skeleton.IBoneTransform;
import goblinbob.mobends.api.skeleton.MoBendsBone;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.standard.mutators.BipedMutator;

public class BipedSkeleton implements IAnimatedSkeleton
{
    private final BipedMutator<?, ?, ?> mutator;

    public BipedSkeleton(BipedMutator<?, ?, ?> mutator)
    {
        this.mutator = mutator;
    }

    @Override
    public boolean hasBone(MoBendsBone bone)
    {
        return resolve(bone) != null;
    }

    @Override
    public IBoneTransform getBone(MoBendsBone bone)
    {
        final BendsModelPart part = resolve(bone);
        if (part == null)
        {
            return null;
        }
        return new BoneTransform(part, this.mutator.getPartEulerAngles(part));
    }

    private BendsModelPart resolve(MoBendsBone bone)
    {
        switch (bone)
        {
            case HEAD: return this.mutator.getHead();
            case BODY: return this.mutator.getBody();
            case LEFT_ARM: return this.mutator.getLeftArm();
            case LEFT_FORE_ARM: return this.mutator.getLeftForeArm();
            case RIGHT_ARM: return this.mutator.getRightArm();
            case RIGHT_FORE_ARM: return this.mutator.getRightForeArm();
            case LEFT_LEG: return this.mutator.getLeftLeg();
            case LEFT_FORE_LEG: return this.mutator.getLeftForeLeg();
            case RIGHT_LEG: return this.mutator.getRightLeg();
            case RIGHT_FORE_LEG: return this.mutator.getRightForeLeg();
            default: return null;
        }
    }

    private static final class BoneTransform implements IBoneTransform
    {
        private final BendsModelPart part;
        private final float[] euler;

        private BoneTransform(BendsModelPart part, float[] euler)
        {
            this.part = part;
            this.euler = euler;
        }

        @Override
        public float positionX()
        {
            return this.part.position.x + this.part.offset.x;
        }

        @Override
        public float positionY()
        {
            return this.part.position.y + this.part.offset.y;
        }

        @Override
        public float positionZ()
        {
            return this.part.position.z + this.part.offset.z;
        }

        @Override
        public float rotationX()
        {
            return this.euler[0];
        }

        @Override
        public float rotationY()
        {
            return this.euler[1];
        }

        @Override
        public float rotationZ()
        {
            return this.euler[2];
        }

        @Override
        public boolean visible()
        {
            return this.part.isShowing();
        }
    }
}
