package goblinbob.mobends.core.util;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.lib.math.Quaternion;
import org.joml.Quaternionf;

public class GlHelper
{
    private static final Quaternionf ROTATION_SCRATCH = new Quaternionf();

    public static void rotate(PoseStack poseStack, Quaternion quaternionIn)
    {
        if (quaternionIn == null) return;

        if (quaternionIn.lengthSquared() < 1.0E-6F) return;

        poseStack.mulPose(ROTATION_SCRATCH.set(quaternionIn.x, quaternionIn.y, quaternionIn.z, quaternionIn.w));
    }
}
