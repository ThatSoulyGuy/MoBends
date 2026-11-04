package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public final class LimbBending
{
    private static final float FACE_NUDGE = 0.0005F;
    private static final float CLIP_EPSILON = 1.0E-6F;

    private static final CapturedVertex[] UPPER_POLY = new CapturedVertex[16];
    private static final CapturedVertex[] LOWER_POLY = new CapturedVertex[16];
    private static final CapturedVertex[] MOVED = new CapturedVertex[16];
    private static final float[] UPPER_DIST = new float[16];
    private static final float[] LOWER_DIST = new float[16];
    private static final float[] SCRATCH_DIST = new float[16];

    private LimbBending()
    {
    }

    public static void emitRigid(List<CapturedVertex> vertices, VertexConsumer vertexConsumer)
    {
        final IEntityVertexHelper helper = IEntityVertexHelper.Holder.getHelper();
        if (helper == null)
        {
            return;
        }

        for (CapturedVertex vertex : vertices)
        {
            emit(helper, vertexConsumer, vertex);
        }
    }

    public static void emitAttached(List<CapturedVertex> vertices, Matrix4f limbPose, Matrix4f forePose,
                                    VertexConsumer vertexConsumer)
    {
        final IEntityVertexHelper helper = IEntityVertexHelper.Holder.getHelper();
        if (helper == null)
        {
            return;
        }

        final Matrix4f toLimb = new Matrix4f(limbPose).invert();
        final Matrix4f joint = new Matrix4f(toLimb).mul(forePose);
        final Vector3f pivot = joint.getTranslation(new Vector3f());
        final Matrix4f upperToLower = new Matrix4f(limbPose)
                .translate(pivot)
                .rotate(joint.getNormalizedRotation(new Quaternionf()))
                .translate(-pivot.x, -pivot.y, -pivot.z)
                .mul(toLimb);
        final Matrix3f upperToLowerNormal = new Matrix3f(upperToLower);

        for (CapturedVertex[] quad : ArmorPoseHelper.groupIntoQuads(vertices))
        {
            emitMoved(helper, vertexConsumer, quad, quad.length, upperToLower, upperToLowerNormal);
        }
    }

    public static void emitBent(List<CapturedVertex> vertices, Matrix4f limbPose, Matrix4f forePose,
                                VertexConsumer vertexConsumer)
    {
        final IEntityVertexHelper helper = IEntityVertexHelper.Holder.getHelper();
        if (helper == null)
        {
            return;
        }

        final Matrix4f toLimb = new Matrix4f(limbPose).invert();
        final Matrix4f joint = new Matrix4f(toLimb).mul(forePose);
        final Vector3f pivot = joint.getTranslation(new Vector3f());
        final Quaternionf bend = joint.getNormalizedRotation(new Quaternionf());

        final Vector3f upperNormal = bend.transform(new Vector3f(0.0F, 1.0F, 0.0F)).add(0.0F, 1.0F, 0.0F);
        if (upperNormal.lengthSquared() < 1.0E-6F)
        {
            upperNormal.set(0.0F, 1.0F, 0.0F);
        }
        upperNormal.normalize();
        final Vector3f lowerNormal = bend.transformInverse(new Vector3f(upperNormal));

        final Matrix4f upperToLower = new Matrix4f(limbPose)
                .translate(pivot)
                .rotate(bend)
                .translate(-pivot.x, -pivot.y, -pivot.z)
                .mul(toLimb);
        final Matrix3f upperToLowerNormal = new Matrix3f(upperToLower);
        final Matrix3f normalToLimb = new Matrix3f(limbPose).transpose();

        final Vector3f point = new Vector3f();
        final Vector3f normal = new Vector3f();

        for (CapturedVertex[] quad : ArmorPoseHelper.groupIntoQuads(vertices))
        {
            normal.set(0.0F, 0.0F, 0.0F);
            for (CapturedVertex vertex : quad)
            {
                normal.add(vertex.normalX, vertex.normalY, vertex.normalZ);
            }
            normalToLimb.transform(normal);
            if (normal.lengthSquared() > 0.0F)
            {
                normal.normalize();
            }
            final float upperNudge = FACE_NUDGE * normal.dot(upperNormal);
            final float lowerNudge = FACE_NUDGE * normal.dot(lowerNormal);

            boolean allUpper = true;
            boolean allLower = true;
            for (int i = 0; i < quad.length; ++i)
            {
                point.set(quad[i].x, quad[i].y, quad[i].z);
                toLimb.transformPosition(point);
                point.sub(pivot);
                UPPER_DIST[i] = point.dot(upperNormal) - upperNudge;
                LOWER_DIST[i] = point.dot(lowerNormal) - lowerNudge;
                allUpper &= UPPER_DIST[i] <= 0.0F;
                allLower &= LOWER_DIST[i] >= 0.0F;
            }

            if (allUpper)
            {
                emitPolygon(helper, vertexConsumer, quad, quad.length);
            }
            else
            {
                final int count = clipPolygon(quad, UPPER_DIST, quad.length, true, UPPER_POLY, SCRATCH_DIST);
                emitPolygon(helper, vertexConsumer, UPPER_POLY, count);
            }

            if (allLower)
            {
                emitMoved(helper, vertexConsumer, quad, quad.length, upperToLower, upperToLowerNormal);
            }
            else
            {
                final int count = clipPolygon(quad, LOWER_DIST, quad.length, false, LOWER_POLY, SCRATCH_DIST);
                emitMoved(helper, vertexConsumer, LOWER_POLY, count, upperToLower, upperToLowerNormal);
            }
        }
    }

    private static int clipPolygon(CapturedVertex[] polygon, float[] distance, int count, boolean keepNegative,
                                   CapturedVertex[] out, float[] outDistance)
    {
        int outCount = 0;
        for (int i = 0; i < count; ++i)
        {
            final int j = (i + 1) % count;
            final boolean insideI = keepNegative ? distance[i] <= 0.0F : distance[i] >= 0.0F;
            final boolean insideJ = keepNegative ? distance[j] <= 0.0F : distance[j] >= 0.0F;

            if (insideI)
            {
                out[outCount] = polygon[i];
                outDistance[outCount++] = distance[i];
            }
            if (insideI != insideJ)
            {
                final float t = distance[i] / (distance[i] - distance[j]);
                if (t > CLIP_EPSILON && t < 1.0F - CLIP_EPSILON)
                {
                    out[outCount] = lerp(polygon[i], polygon[j], t);
                    outDistance[outCount++] = 0.0F;
                }
            }
        }
        return outCount;
    }

    private static void emitMoved(IEntityVertexHelper helper, VertexConsumer vertexConsumer,
                                  CapturedVertex[] polygon, int count,
                                  Matrix4f upperToLower, Matrix3f upperToLowerNormal)
    {
        if (count < 3)
        {
            return;
        }

        for (int i = 0; i < count; ++i)
        {
            final CapturedVertex vertex = polygon[i];
            final Vector3f position = upperToLower.transformPosition(new Vector3f(vertex.x, vertex.y, vertex.z));
            final Vector3f normal = upperToLowerNormal.transform(new Vector3f(vertex.normalX, vertex.normalY, vertex.normalZ));
            if (normal.lengthSquared() > 0.0F)
            {
                normal.normalize();
            }
            MOVED[i] = new CapturedVertex(position.x, position.y, position.z,
                    vertex.red, vertex.green, vertex.blue, vertex.alpha,
                    vertex.u, vertex.v, vertex.overlayUV, vertex.lightmapUV,
                    normal.x, normal.y, normal.z);
        }
        emitPolygon(helper, vertexConsumer, MOVED, count);
    }

    private static CapturedVertex lerp(CapturedVertex a, CapturedVertex b, float t)
    {
        return new CapturedVertex(
                a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t,
                a.red, a.green, a.blue, a.alpha,
                a.u + (b.u - a.u) * t, a.v + (b.v - a.v) * t,
                a.overlayUV, a.lightmapUV,
                a.normalX, a.normalY, a.normalZ);
    }

    private static void emitPolygon(IEntityVertexHelper helper, VertexConsumer vertexConsumer,
                                    CapturedVertex[] vertices, int count)
    {
        if (count < 3)
        {
            return;
        }

        for (int start = 1; start + 1 < count; start += 2)
        {
            final int third = Math.min(start + 2, count - 1);
            emit(helper, vertexConsumer, vertices[0]);
            emit(helper, vertexConsumer, vertices[start]);
            emit(helper, vertexConsumer, vertices[start + 1]);
            emit(helper, vertexConsumer, vertices[third]);
        }
    }

    private static void emit(IEntityVertexHelper helper, VertexConsumer vertexConsumer, CapturedVertex vertex)
    {
        helper.emitVertex(vertexConsumer, vertex.x, vertex.y, vertex.z,
                packColor(vertex), vertex.u, vertex.v, vertex.overlayUV, vertex.lightmapUV,
                vertex.normalX, vertex.normalY, vertex.normalZ);
    }

    private static int packColor(CapturedVertex vertex)
    {
        return ((int) (vertex.alpha * 255.0F) & 0xFF) << 24
                | ((int) (vertex.red * 255.0F) & 0xFF) << 16
                | ((int) (vertex.green * 255.0F) & 0xFF) << 8
                | ((int) (vertex.blue * 255.0F) & 0xFF);
    }
}
