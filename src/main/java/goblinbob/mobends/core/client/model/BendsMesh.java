package goblinbob.mobends.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Arrays;

public class BendsMesh
{
    private final float[] positions;
    private final float[] uvs;
    private final float[] normals;
    private final int vertexCount;

    public BendsMesh(float[] positions, float[] uvs, float[] normals)
    {
        this.positions = positions;
        this.uvs = uvs;
        this.normals = normals;
        this.vertexCount = positions.length / 3;
    }

    public boolean isEmpty()
    {
        return vertexCount == 0;
    }

    public void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer,
                        int packedLight, int packedOverlay, int color)
    {
        final Matrix4f matrix = pose.pose();
        final Matrix3f normalMatrix = pose.normal();
        final IEntityVertexHelper vertexHelper = IEntityVertexHelper.Holder.getHelper();

        for (int i = 0; i < vertexCount; ++i)
        {
            final int p = i * 3;
            final int t = i * 2;

            final float x = positions[p];
            final float y = positions[p + 1];
            final float z = positions[p + 2];

            final float nx = normals[p];
            final float ny = normals[p + 1];
            final float nz = normals[p + 2];

            final float tx = matrix.m00() * x + matrix.m10() * y + matrix.m20() * z + matrix.m30();
            final float ty = matrix.m01() * x + matrix.m11() * y + matrix.m21() * z + matrix.m31();
            final float tz = matrix.m02() * x + matrix.m12() * y + matrix.m22() * z + matrix.m32();

            final float tnx = normalMatrix.m00() * nx + normalMatrix.m10() * ny + normalMatrix.m20() * nz;
            final float tny = normalMatrix.m01() * nx + normalMatrix.m11() * ny + normalMatrix.m21() * nz;
            final float tnz = normalMatrix.m02() * nx + normalMatrix.m12() * ny + normalMatrix.m22() * nz;

            vertexHelper.emitVertex(vertexConsumer,
                    tx, ty, tz,
                    color,
                    uvs[t], uvs[t + 1],
                    packedOverlay, packedLight,
                    tnx, tny, tnz);
        }
    }

    public static class Builder
    {
        private float[] positions = new float[288];
        private float[] uvs = new float[192];
        private float[] normals = new float[288];
        private int count = 0;

        public void addVertex(float x, float y, float z, float u, float v, float nx, float ny, float nz)
        {
            if ((count + 1) * 3 > positions.length)
            {
                positions = Arrays.copyOf(positions, positions.length * 2);
                normals = Arrays.copyOf(normals, normals.length * 2);
                uvs = Arrays.copyOf(uvs, uvs.length * 2);
            }

            final int p = count * 3;
            final int t = count * 2;

            positions[p] = x;
            positions[p + 1] = y;
            positions[p + 2] = z;
            uvs[t] = u;
            uvs[t + 1] = v;
            normals[p] = nx;
            normals[p + 1] = ny;
            normals[p + 2] = nz;

            ++count;
        }

        public boolean isEmpty()
        {
            return count == 0;
        }

        public BendsMesh build()
        {
            return new BendsMesh(Arrays.copyOf(positions, count * 3),
                                 Arrays.copyOf(uvs, count * 2),
                                 Arrays.copyOf(normals, count * 3));
        }
    }
}
