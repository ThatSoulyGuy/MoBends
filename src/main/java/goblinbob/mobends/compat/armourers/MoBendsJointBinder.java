package goblinbob.mobends.compat.armourers;

import goblinbob.mobends.api.skeleton.IAnimatedSkeleton;
import goblinbob.mobends.api.skeleton.IBoneTransform;
import goblinbob.mobends.api.skeleton.MoBendsAPI;
import goblinbob.mobends.api.skeleton.MoBendsBone;
import moe.plushie.armourers_workshop.api.armature.IJointTransform;
import moe.plushie.armourers_workshop.api.core.math.IPoseStack;
import moe.plushie.armourers_workshop.core.armature.Joint;
import moe.plushie.armourers_workshop.core.armature.JointContext;
import moe.plushie.armourers_workshop.core.armature.JointModifier;
import moe.plushie.armourers_workshop.core.math.OpenQuaternionf;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IODataObject;

public class MoBendsJointBinder extends JointModifier
{
    private static final float MODEL_UNIT = 16.0F;

    private final String name;
    private final MoBendsBone bone;

    public MoBendsJointBinder(String name, IODataObject parameters)
    {
        this.name = name;
        this.bone = MoBendsBone.byName(name);
    }

    @Override
    public IJointTransform apply(IJointTransform transform, Joint joint, JointContext context)
    {
        if (this.bone == null || !this.bone.isForeLimb())
        {
            final var pose = context.poses().byPartName(this.name);
            if (pose == null)
            {
                return transform;
            }
            return poseStack ->
            {
                transform.apply(poseStack);
                pose.transform(poseStack);
            };
        }

        final MoBendsBone parentBone = this.bone.parent();
        if (parentBone == null)
        {
            return transform;
        }

        final var parentPose = context.poses().byPartName(parentBone.boneName());
        if (parentPose == null)
        {
            return transform;
        }

        final MoBendsBone foreLimb = this.bone;
        return poseStack ->
        {
            transform.apply(poseStack);
            parentPose.transform(poseStack);
            applyBone(foreLimb, poseStack);
        };
    }

    private static void applyBone(MoBendsBone bone, IPoseStack poseStack)
    {
        final IAnimatedSkeleton skeleton = MoBendsAPI.getRenderingSkeleton();
        if (skeleton == null)
        {
            return;
        }

        final IBoneTransform transform = skeleton.getBone(bone);
        if (transform == null)
        {
            return;
        }

        poseStack.translate(transform.positionX() / MODEL_UNIT,
                transform.positionY() / MODEL_UNIT,
                transform.positionZ() / MODEL_UNIT);
        poseStack.rotate(OpenQuaternionf.fromEulerAnglesXYZ(
                transform.rotationX(), transform.rotationY(), transform.rotationZ()));
    }
}
