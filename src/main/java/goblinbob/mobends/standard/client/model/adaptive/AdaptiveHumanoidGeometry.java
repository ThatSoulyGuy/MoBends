package goblinbob.mobends.standard.client.model.adaptive;

import goblinbob.mobends.core.client.model.BendsMesh;
import goblinbob.mobends.standard.client.model.armor.CapturedVertex;
import goblinbob.mobends.standard.client.model.armor.JointDefinitions;
import goblinbob.mobends.standard.client.model.armor.JointPlane;
import goblinbob.mobends.standard.client.model.armor.QuadSlicer;
import goblinbob.mobends.standard.client.model.armor.SliceResult;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

import java.util.List;

public final class AdaptiveHumanoidGeometry
{
    private static final float SCALE = 1.0F / 16.0F;

    private static final float DEFAULT_BODY_HEIGHT = 12.0F;

    private static final float CROUCH_HEAD_Y = 4.2F;
    private static final float CROUCH_BODY_Y = 3.2F;
    private static final float CROUCH_ARM_Y = 3.2F;
    private static final float CROUCH_LEG_Y = 0.2F;
    private static final float CROUCH_LEG_Z = 4.0F;

    public final float[] bodyPivot = new float[3];
    public final float[] headPivot = new float[3];
    public final float[] leftArmPivot = new float[3];
    public final float[] rightArmPivot = new float[3];
    public final float[] leftForeArmPivot = new float[3];
    public final float[] rightForeArmPivot = new float[3];
    public final float[] leftLegPivot = new float[3];
    public final float[] rightLegPivot = new float[3];
    public final float[] leftForeLegPivot = new float[3];
    public final float[] rightForeLegPivot = new float[3];

    private float torsoBottom = DEFAULT_BODY_HEIGHT;

    public BendsMesh headMesh;
    public BendsMesh hatMesh;
    public BendsMesh bodyMesh;
    public BendsMesh leftArmMesh;
    public BendsMesh rightArmMesh;
    public BendsMesh leftForeArmMesh;
    public BendsMesh rightForeArmMesh;
    public BendsMesh leftLegMesh;
    public BendsMesh rightLegMesh;
    public BendsMesh leftForeLegMesh;
    public BendsMesh rightForeLegMesh;

    private float[] jointOverride = null;

    private AdaptiveHumanoidGeometry()
    {
    }

    public static AdaptiveHumanoidGeometry build(HumanoidModel<?> model)
    {
        return build(model, false);
    }

    public static AdaptiveHumanoidGeometry build(HumanoidModel<?> model, boolean includeChildren)
    {
        return build(model, includeChildren, null);
    }

    public static AdaptiveHumanoidGeometry build(HumanoidModel<?> model, boolean includeChildren,
                                                 float[] jointOverride)
    {
        if (model == null || model.head == null || model.body == null
                || model.leftArm == null || model.rightArm == null
                || model.leftLeg == null || model.rightLeg == null)
        {
            return null;
        }

        final PartCapture head = capture(model.head, includeChildren);
        final PartCapture hat = capture(model.hat, includeChildren);
        final PartCapture body = capture(model.body, includeChildren);
        final PartCapture leftArm = capture(model.leftArm, includeChildren);
        final PartCapture rightArm = capture(model.rightArm, includeChildren);
        final PartCapture leftLeg = capture(model.leftLeg, includeChildren);
        final PartCapture rightLeg = capture(model.rightLeg, includeChildren);

        final AdaptiveHumanoidGeometry geometry = new AdaptiveHumanoidGeometry();
        geometry.jointOverride = jointOverride;

        final float torsoBottom = body.isEmpty() ? DEFAULT_BODY_HEIGHT : body.baseMaxY;
        geometry.torsoBottom = torsoBottom;

        geometry.bodyPivot[0] = body.pivotX;
        geometry.bodyPivot[1] = body.pivotY + torsoBottom;
        geometry.bodyPivot[2] = body.pivotZ;

        geometry.headPivot[0] = head.pivotX - geometry.bodyPivot[0];
        geometry.headPivot[1] = head.pivotY - geometry.bodyPivot[1];
        geometry.headPivot[2] = head.pivotZ - geometry.bodyPivot[2];

        geometry.buildTorso(body, head, hat, torsoBottom);
        geometry.buildArm(leftArm, true);
        geometry.buildArm(rightArm, false);
        geometry.buildLeg(leftLeg, true);
        geometry.buildLeg(rightLeg, false);

        return geometry;
    }

    public void adoptRuntimePivots(HumanoidModel<?> model)
    {
        if (model == null || model.body == null || model.head == null
                || model.leftArm == null || model.rightArm == null
                || model.leftLeg == null || model.rightLeg == null)
        {
            return;
        }

        final boolean crouching = model.crouching;

        bodyPivot[0] = model.body.x;
        bodyPivot[1] = model.body.y - (crouching ? CROUCH_BODY_Y : 0.0F) + torsoBottom;
        bodyPivot[2] = model.body.z;

        relativeToBody(headPivot, model.head, crouching ? CROUCH_HEAD_Y : 0.0F, 0.0F);
        relativeToBody(leftArmPivot, model.leftArm, crouching ? CROUCH_ARM_Y : 0.0F, 0.0F);
        relativeToBody(rightArmPivot, model.rightArm, crouching ? CROUCH_ARM_Y : 0.0F, 0.0F);

        legPivot(leftLegPivot, model.leftLeg, crouching);
        legPivot(rightLegPivot, model.rightLeg, crouching);
    }

    private void relativeToBody(float[] pivot, ModelPart part, float crouchY, float crouchZ)
    {
        pivot[0] = part.x - bodyPivot[0];
        pivot[1] = part.y - crouchY - bodyPivot[1];
        pivot[2] = part.z - crouchZ - bodyPivot[2];
    }

    private static void legPivot(float[] pivot, ModelPart part, boolean crouching)
    {
        pivot[0] = part.x;
        pivot[1] = part.y - (crouching ? CROUCH_LEG_Y : 0.0F);
        pivot[2] = part.z - (crouching ? CROUCH_LEG_Z : 0.0F);
    }

    private void buildTorso(PartCapture body, PartCapture head, PartCapture hat, float torsoBottom)
    {
        bodyMesh = meshOf(body.quads, 0.0F, -torsoBottom, 0.0F);
        headMesh = meshOf(head.quads, 0.0F, 0.0F, 0.0F);
        hatMesh = meshOf(hat.quads, 0.0F, 0.0F, 0.0F);
    }

    private void buildArm(PartCapture arm, boolean isLeft)
    {
        final float[] pivot = isLeft ? leftArmPivot : rightArmPivot;
        pivot[0] = arm.pivotX - bodyPivot[0];
        pivot[1] = arm.pivotY - bodyPivot[1];
        pivot[2] = arm.pivotZ - bodyPivot[2];

        final float splitY = jointOverride != null ? jointOverride[0] : (arm.baseMinY + arm.baseMaxY) * 0.5F;
        final float hingeZ = jointOverride != null ? jointOverride[1] : arm.baseMaxZ;

        final float[] forePivot = isLeft ? leftForeArmPivot : rightForeArmPivot;
        forePivot[0] = 0.0F;
        forePivot[1] = splitY;
        forePivot[2] = hingeZ;

        final List<SliceResult> slices = sliceAt(arm.quads, JointDefinitions.createElbowPlane(splitY));

        if (isLeft)
        {
            leftArmMesh = slicedMesh(slices, true, 0.0F, 0.0F, 0.0F);
            leftForeArmMesh = slicedMesh(slices, false, 0.0F, -splitY, -hingeZ);
        }
        else
        {
            rightArmMesh = slicedMesh(slices, true, 0.0F, 0.0F, 0.0F);
            rightForeArmMesh = slicedMesh(slices, false, 0.0F, -splitY, -hingeZ);
        }
    }

    private void buildLeg(PartCapture leg, boolean isLeft)
    {
        final float[] pivot = isLeft ? leftLegPivot : rightLegPivot;
        pivot[0] = leg.pivotX;
        pivot[1] = leg.pivotY;
        pivot[2] = leg.pivotZ;

        final float splitY = jointOverride != null ? jointOverride[2] : (leg.baseMinY + leg.baseMaxY) * 0.5F;
        final float hingeZ = jointOverride != null ? jointOverride[3] : leg.baseMinZ;

        final float[] forePivot = isLeft ? leftForeLegPivot : rightForeLegPivot;
        forePivot[0] = 0.0F;
        forePivot[1] = splitY;
        forePivot[2] = hingeZ;

        final List<SliceResult> slices = sliceAt(leg.quads, JointDefinitions.createKneePlane(splitY));

        if (isLeft)
        {
            leftLegMesh = slicedMesh(slices, true, 0.0F, 0.0F, 0.0F);
            leftForeLegMesh = slicedMesh(slices, false, 0.0F, -splitY, -hingeZ);
        }
        else
        {
            rightLegMesh = slicedMesh(slices, true, 0.0F, 0.0F, 0.0F);
            rightForeLegMesh = slicedMesh(slices, false, 0.0F, -splitY, -hingeZ);
        }
    }

    private static PartCapture capture(ModelPart part, boolean overlay)
    {
        return overlay ? PartCapture.ofOverlay(part) : PartCapture.ofOwnCubes(part);
    }

    private static List<SliceResult> sliceAt(List<CapturedVertex[]> quads, JointPlane plane)
    {
        return new QuadSlicer().sliceAll(quads, plane);
    }

    private static BendsMesh meshOf(List<CapturedVertex[]> quads,
                                    float offsetX, float offsetY, float offsetZ)
    {
        if (quads.isEmpty())
        {
            return null;
        }

        final BendsMesh.Builder builder = new BendsMesh.Builder();
        final float dx = offsetX * SCALE;
        final float dy = offsetY * SCALE;
        final float dz = offsetZ * SCALE;

        for (CapturedVertex[] quad : quads)
        {
            for (CapturedVertex vertex : quad)
            {
                builder.addVertex(vertex.x + dx, vertex.y + dy, vertex.z + dz,
                        vertex.u, vertex.v,
                        vertex.normalX, vertex.normalY, vertex.normalZ);
            }
        }

        return builder.isEmpty() ? null : builder.build();
    }

    private static BendsMesh slicedMesh(List<SliceResult> slices, boolean upper,
                                        float offsetX, float offsetY, float offsetZ)
    {
        final BendsMesh.Builder builder = new BendsMesh.Builder();
        final float dx = offsetX * SCALE;
        final float dy = offsetY * SCALE;
        final float dz = offsetZ * SCALE;

        for (SliceResult slice : slices)
        {
            final List<SliceResult.SlicedVertex> vertices = upper
                    ? slice.getUpperVertices()
                    : slice.getLowerVertices();

            final int count = vertices.size();
            if (count < 3)
            {
                continue;
            }

            if (count == 4)
            {
                for (SliceResult.SlicedVertex vertex : vertices)
                {
                    addVertex(builder, vertex, dx, dy, dz);
                }
                continue;
            }

            for (int i = 1; i < count - 1; ++i)
            {
                addVertex(builder, vertices.get(0), dx, dy, dz);
                addVertex(builder, vertices.get(i), dx, dy, dz);
                addVertex(builder, vertices.get(i + 1), dx, dy, dz);
                addVertex(builder, vertices.get(i + 1), dx, dy, dz);
            }
        }

        return builder.isEmpty() ? null : builder.build();
    }

    private static void addVertex(BendsMesh.Builder builder, SliceResult.SlicedVertex vertex,
                                  float dx, float dy, float dz)
    {
        builder.addVertex(vertex.x + dx, vertex.y + dy, vertex.z + dz,
                vertex.u, vertex.v,
                vertex.normalX, vertex.normalY, vertex.normalZ);
    }
}
