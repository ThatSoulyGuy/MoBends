package goblinbob.mobends.standard.client.model.adaptive;

import goblinbob.mobends.core.client.model.BendsMesh;
import goblinbob.mobends.standard.client.model.armor.CapturedVertex;
import goblinbob.mobends.standard.client.model.armor.JointDefinitions;
import goblinbob.mobends.standard.client.model.armor.JointPlane;
import goblinbob.mobends.standard.client.model.armor.QuadSlicer;
import goblinbob.mobends.standard.client.model.armor.SliceResult;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

import java.util.ArrayList;
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

    public BendsMesh bodyWearMesh;
    public BendsMesh leftArmWearMesh;
    public BendsMesh rightArmWearMesh;
    public BendsMesh leftForeArmWearMesh;
    public BendsMesh rightForeArmWearMesh;
    public BendsMesh leftLegWearMesh;
    public BendsMesh rightLegWearMesh;
    public BendsMesh leftForeLegWearMesh;
    public BendsMesh rightForeLegWearMesh;

    private float[] jointOverride = null;

    public boolean limbSubtreesBaked = false;

    public enum CaptureMode
    {
        OWN_CUBES,
        SUBTREE,
        OVERLAY
    }

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
        return build(model, includeChildren, jointOverride, null);
    }

    public static AdaptiveHumanoidGeometry build(HumanoidModel<?> model, boolean includeChildren,
                                                 float[] jointOverride, WearParts wear)
    {
        final CaptureMode mode = includeChildren ? CaptureMode.OVERLAY : CaptureMode.OWN_CUBES;
        return build(model, mode, mode, jointOverride, wear);
    }

    public static AdaptiveHumanoidGeometry build(HumanoidModel<?> model,
                                                 CaptureMode headMode, CaptureMode limbMode,
                                                 float[] jointOverride, WearParts wear)
    {
        if (model == null || model.head == null || model.body == null
                || model.leftArm == null || model.rightArm == null
                || model.leftLeg == null || model.rightLeg == null)
        {
            return null;
        }

        final PartCapture head = capture(model.head, headMode);
        final PartCapture hat = capture(model.hat, headMode);
        final PartCapture body = capture(model.body, limbMode);
        final PartCapture leftArm = capture(model.leftArm, limbMode);
        final PartCapture rightArm = capture(model.rightArm, limbMode);
        final PartCapture leftLeg = capture(model.leftLeg, limbMode);
        final PartCapture rightLeg = capture(model.rightLeg, limbMode);

        final AdaptiveHumanoidGeometry geometry = new AdaptiveHumanoidGeometry();
        geometry.jointOverride = jointOverride;
        geometry.limbSubtreesBaked = limbMode == CaptureMode.SUBTREE;

        final float torsoBottom = body.isEmpty() ? DEFAULT_BODY_HEIGHT : body.baseMaxY;
        geometry.torsoBottom = torsoBottom;

        geometry.bodyPivot[0] = body.pivotX;
        geometry.bodyPivot[1] = body.pivotY + torsoBottom;
        geometry.bodyPivot[2] = body.pivotZ;

        geometry.headPivot[0] = head.pivotX - geometry.bodyPivot[0];
        geometry.headPivot[1] = head.pivotY - geometry.bodyPivot[1];
        geometry.headPivot[2] = head.pivotZ - geometry.bodyPivot[2];

        geometry.buildTorso(body, head, hat, torsoBottom,
                capture(wear == null ? null : wear.body, limbMode));
        geometry.buildArm(leftArm, capture(wear == null ? null : wear.leftArm, limbMode), true);
        geometry.buildArm(rightArm, capture(wear == null ? null : wear.rightArm, limbMode), false);
        geometry.buildLeg(leftLeg, capture(wear == null ? null : wear.leftLeg, limbMode), true);
        geometry.buildLeg(rightLeg, capture(wear == null ? null : wear.rightLeg, limbMode), false);

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

    private void buildTorso(PartCapture body, PartCapture head, PartCapture hat, float torsoBottom,
                            PartCapture bodyWear)
    {
        bodyMesh = meshOf(body.quads, 0.0F, -torsoBottom, 0.0F);
        headMesh = meshOf(head.quads, 0.0F, 0.0F, 0.0F);
        hatMesh = meshOf(hat.quads, 0.0F, 0.0F, 0.0F);

        if (!bodyWear.isEmpty())
        {
            bodyWearMesh = meshOf(bodyWear.quads,
                    bodyWear.pivotX - body.pivotX,
                    bodyWear.pivotY - body.pivotY - torsoBottom,
                    bodyWear.pivotZ - body.pivotZ);
        }
    }

    private void buildArm(PartCapture arm, PartCapture wear, boolean isLeft)
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
        final List<SliceResult> wearSlices = wear.isEmpty() ? null
                : sliceAt(shiftOnto(wear, arm), JointDefinitions.createElbowPlane(splitY));

        if (isLeft)
        {
            leftArmMesh = slicedMesh(slices, true, 0.0F, 0.0F, 0.0F);
            leftForeArmMesh = slicedMesh(slices, false, 0.0F, -splitY, -hingeZ);

            if (wearSlices != null)
            {
                leftArmWearMesh = slicedMesh(wearSlices, true, 0.0F, 0.0F, 0.0F);
                leftForeArmWearMesh = slicedMesh(wearSlices, false, 0.0F, -splitY, -hingeZ);
            }
        }
        else
        {
            rightArmMesh = slicedMesh(slices, true, 0.0F, 0.0F, 0.0F);
            rightForeArmMesh = slicedMesh(slices, false, 0.0F, -splitY, -hingeZ);

            if (wearSlices != null)
            {
                rightArmWearMesh = slicedMesh(wearSlices, true, 0.0F, 0.0F, 0.0F);
                rightForeArmWearMesh = slicedMesh(wearSlices, false, 0.0F, -splitY, -hingeZ);
            }
        }
    }

    private void buildLeg(PartCapture leg, PartCapture wear, boolean isLeft)
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
        final List<SliceResult> wearSlices = wear.isEmpty() ? null
                : sliceAt(shiftOnto(wear, leg), JointDefinitions.createKneePlane(splitY));

        if (isLeft)
        {
            leftLegMesh = slicedMesh(slices, true, 0.0F, 0.0F, 0.0F);
            leftForeLegMesh = slicedMesh(slices, false, 0.0F, -splitY, -hingeZ);

            if (wearSlices != null)
            {
                leftLegWearMesh = slicedMesh(wearSlices, true, 0.0F, 0.0F, 0.0F);
                leftForeLegWearMesh = slicedMesh(wearSlices, false, 0.0F, -splitY, -hingeZ);
            }
        }
        else
        {
            rightLegMesh = slicedMesh(slices, true, 0.0F, 0.0F, 0.0F);
            rightForeLegMesh = slicedMesh(slices, false, 0.0F, -splitY, -hingeZ);

            if (wearSlices != null)
            {
                rightLegWearMesh = slicedMesh(wearSlices, true, 0.0F, 0.0F, 0.0F);
                rightForeLegWearMesh = slicedMesh(wearSlices, false, 0.0F, -splitY, -hingeZ);
            }
        }
    }

    private static List<CapturedVertex[]> shiftOnto(PartCapture wear, PartCapture base)
    {
        final float dx = (wear.pivotX - base.pivotX) * SCALE;
        final float dy = (wear.pivotY - base.pivotY) * SCALE;
        final float dz = (wear.pivotZ - base.pivotZ) * SCALE;

        if (dx == 0.0F && dy == 0.0F && dz == 0.0F)
        {
            return wear.quads;
        }

        final List<CapturedVertex[]> shifted = new ArrayList<>(wear.quads.size());

        for (CapturedVertex[] quad : wear.quads)
        {
            final CapturedVertex[] moved = new CapturedVertex[quad.length];

            for (int i = 0; i < quad.length; ++i)
            {
                final CapturedVertex vertex = quad[i];
                moved[i] = new CapturedVertex(vertex.x + dx, vertex.y + dy, vertex.z + dz,
                        vertex.red, vertex.green, vertex.blue, vertex.alpha,
                        vertex.u, vertex.v,
                        vertex.overlayUV, vertex.lightmapUV,
                        vertex.normalX, vertex.normalY, vertex.normalZ);
            }

            shifted.add(moved);
        }

        return shifted;
    }

    public static final class WearParts
    {
        private final ModelPart body, leftArm, rightArm, leftLeg, rightLeg;

        public WearParts(ModelPart body, ModelPart leftArm, ModelPart rightArm,
                         ModelPart leftLeg, ModelPart rightLeg)
        {
            this.body = body;
            this.leftArm = leftArm;
            this.rightArm = rightArm;
            this.leftLeg = leftLeg;
            this.rightLeg = rightLeg;
        }
    }

    private static PartCapture capture(ModelPart part, CaptureMode mode)
    {
        return switch (mode)
        {
            case OVERLAY -> PartCapture.ofOverlay(part);
            case SUBTREE -> PartCapture.ofSubtree(part);
            default -> PartCapture.ofOwnCubes(part);
        };
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
