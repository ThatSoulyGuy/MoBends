package goblinbob.mobends.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;
import goblinbob.mobends.api.rendering.IPoseStack;
import goblinbob.mobends.api.rendering.IVertexConsumer;
import goblinbob.mobends.lib.math.physics.AABBox;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BendsCube
{
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int TOP = 2;
    public static final int BOTTOM = 3;
    public static final int FRONT = 4;
    public static final int BACK = 5;

    protected final byte faceVisibilityFlag;

    protected final BendsQuad[] quads = new BendsQuad[6];

    public final float minX, minY, minZ;
    public final float maxX, maxY, maxZ;

    public BendsCube(int texOffsetX, int texOffsetY,
                     float x, float y, float z,
                     int width, int height, int depth,
                     float inflation,
                     float textureWidth, float textureHeight,
                     boolean mirror)
    {
        this(texOffsetX, texOffsetY, x, y, z, width, height, depth, inflation,
             textureWidth, textureHeight, mirror, (byte) 0b111111, texOffsetX, texOffsetY);
    }

    public BendsCube(int texOffsetX, int texOffsetY,
                     float x, float y, float z,
                     int width, int height, int depth,
                     float inflation,
                     float textureWidth, float textureHeight,
                     boolean mirror,
                     byte faceVisibilityFlag)
    {
        this(texOffsetX, texOffsetY, x, y, z, width, height, depth, inflation,
             textureWidth, textureHeight, mirror, faceVisibilityFlag, texOffsetX, texOffsetY);
    }

    public BendsCube(int texOffsetX, int texOffsetY,
                     float x, float y, float z,
                     int width, int height, int depth,
                     float inflation,
                     float textureWidth, float textureHeight,
                     boolean mirror,
                     int bottomTexOffsetX, int bottomTexOffsetY)
    {
        this(texOffsetX, texOffsetY, x, y, z, width, height, depth, inflation,
             textureWidth, textureHeight, mirror, (byte) 0b111111, bottomTexOffsetX, bottomTexOffsetY);
    }

    public BendsCube(int texOffsetX, int texOffsetY,
                     float x, float y, float z,
                     int width, int height, int depth,
                     float inflation,
                     float textureWidth, float textureHeight,
                     boolean mirror,
                     byte faceVisibilityFlag,
                     int bottomTexOffsetX, int bottomTexOffsetY)
    {
        this.faceVisibilityFlag = faceVisibilityFlag;

        float bx1 = x - inflation;
        float by1 = y - inflation;
        float bz1 = z - inflation;
        float bx2 = x + width + inflation;
        float by2 = y + height + inflation;
        float bz2 = z + depth + inflation;

        this.minX = bx1;
        this.minY = by1;
        this.minZ = bz1;
        this.maxX = bx2;
        this.maxY = by2;
        this.maxZ = bz2;

        float scale = 1.0F / 16.0F;
        float x1 = bx1 * scale;
        float y1 = by1 * scale;
        float z1 = bz1 * scale;
        float x2 = bx2 * scale;
        float y2 = by2 * scale;
        float z2 = bz2 * scale;

        if (mirror)
        {
            float temp = x2;
            x2 = x1;
            x1 = temp;
        }

        BendsVertex v000 = new BendsVertex(x1, y1, z1, 0, 0);
        BendsVertex v100 = new BendsVertex(x2, y1, z1, 0, 0);
        BendsVertex v110 = new BendsVertex(x2, y2, z1, 0, 0);
        BendsVertex v010 = new BendsVertex(x1, y2, z1, 0, 0);
        BendsVertex v001 = new BendsVertex(x1, y1, z2, 0, 0);
        BendsVertex v101 = new BendsVertex(x2, y1, z2, 0, 0);
        BendsVertex v111 = new BendsVertex(x2, y2, z2, 0, 0);
        BendsVertex v011 = new BendsVertex(x1, y2, z2, 0, 0);

        int u = texOffsetX;
        int v = texOffsetY;

        quads[0] = createQuad(new BendsVertex[] {v101, v100, v110, v111},
                u + depth + width, v + depth,
                u + depth + width + depth, v + depth + height,
                textureWidth, textureHeight);

        quads[1] = createQuad(new BendsVertex[] {v000, v001, v011, v010},
                u, v + depth,
                u + depth, v + depth + height,
                textureWidth, textureHeight);

        quads[2] = createQuad(new BendsVertex[] {v101, v001, v000, v100},
                u + depth, v,
                u + depth + width, v + depth,
                textureWidth, textureHeight);

        quads[3] = createQuad(new BendsVertex[] {v110, v010, v011, v111},
                bottomTexOffsetX + depth + width, bottomTexOffsetY + depth,
                bottomTexOffsetX + depth + width + width, bottomTexOffsetY,
                textureWidth, textureHeight);

        quads[4] = createQuad(new BendsVertex[] {v100, v000, v010, v110},
                u + depth, v + depth,
                u + depth + width, v + depth + height,
                textureWidth, textureHeight);

        quads[5] = createQuad(new BendsVertex[] {v001, v101, v111, v011},
                u + depth + width + depth, v + depth,
                u + depth + width + depth + width, v + depth + height,
                textureWidth, textureHeight);

        if (mirror)
        {
            for (BendsQuad quad : quads)
            {
                quad.flipFace();
            }
        }
    }

    public BendsCube(float x0, float y0, float z0, float x1, float y1, float z1,
                     BoxFactory.TextureFace[] faces,
                     byte faceVisibilityFlag,
                     boolean mirror,
                     float textureWidth, float textureHeight)
    {
        this.faceVisibilityFlag = faceVisibilityFlag;

        this.minX = Math.min(x0, x1);
        this.minY = Math.min(y0, y1);
        this.minZ = Math.min(z0, z1);
        this.maxX = Math.max(x0, x1);
        this.maxY = Math.max(y0, y1);
        this.maxZ = Math.max(z0, z1);

        float scale = 1.0F / 16.0F;
        float sx1 = x0 * scale;
        float sy1 = y0 * scale;
        float sz1 = z0 * scale;
        float sx2 = x1 * scale;
        float sy2 = y1 * scale;
        float sz2 = z1 * scale;

        if (mirror)
        {
            float temp = sx2;
            sx2 = sx1;
            sx1 = temp;
        }

        BendsVertex v000 = new BendsVertex(sx1, sy1, sz1, 0, 0);
        BendsVertex v100 = new BendsVertex(sx2, sy1, sz1, 0, 0);
        BendsVertex v110 = new BendsVertex(sx2, sy2, sz1, 0, 0);
        BendsVertex v010 = new BendsVertex(sx1, sy2, sz1, 0, 0);
        BendsVertex v001 = new BendsVertex(sx1, sy1, sz2, 0, 0);
        BendsVertex v101 = new BendsVertex(sx2, sy1, sz2, 0, 0);
        BendsVertex v111 = new BendsVertex(sx2, sy2, sz2, 0, 0);
        BendsVertex v011 = new BendsVertex(sx1, sy2, sz2, 0, 0);

        quads[0] = createQuadFromFace(new BendsVertex[] {v101, v100, v110, v111}, faces[0], textureWidth, textureHeight);
        quads[1] = createQuadFromFace(new BendsVertex[] {v000, v001, v011, v010}, faces[1], textureWidth, textureHeight);
        quads[2] = createQuadFromFace(new BendsVertex[] {v101, v001, v000, v100}, faces[2], textureWidth, textureHeight);
        quads[3] = createQuadFromFace(new BendsVertex[] {v110, v010, v011, v111}, faces[3], textureWidth, textureHeight);
        quads[4] = createQuadFromFace(new BendsVertex[] {v100, v000, v010, v110}, faces[4], textureWidth, textureHeight);
        quads[5] = createQuadFromFace(new BendsVertex[] {v001, v101, v111, v011}, faces[5], textureWidth, textureHeight);

        if (mirror)
        {
            for (BendsQuad quad : quads)
            {
                quad.flipFace();
            }
        }
    }

    private BendsQuad createQuadFromFace(BendsVertex[] vertices, BoxFactory.TextureFace face,
                                          float textureWidth, float textureHeight)
    {
        if (face == null)
        {
            return new BendsQuad(vertices);
        }

        int uSize = face.uSize;
        int vSize = face.vSize;

        if (face.faceRotation == FaceRotation.CLOCKWISE || face.faceRotation == FaceRotation.COUNTER_CLOCKWISE)
        {
            uSize = face.vSize;
            vSize = face.uSize;
        }

        BendsQuad quad = createQuad(vertices,
                face.uPos, face.vPos,
                face.uPos + uSize, face.vPos + vSize,
                textureWidth, textureHeight);

        applyFaceRotation(quad, face.faceRotation);

        return quad;
    }

    private void applyFaceRotation(BendsQuad quad, FaceRotation rotation)
    {
        if (rotation == null || rotation == FaceRotation.IDENTITY)
            return;

        float[] uCoords = new float[4];
        float[] vCoords = new float[4];

        for (int i = 0; i < 4; ++i)
        {
            uCoords[i] = quad.vertices[i].u;
            vCoords[i] = quad.vertices[i].v;
        }

        int offset = 2;
        if (rotation == FaceRotation.CLOCKWISE)
            offset = 3;
        else if (rotation == FaceRotation.COUNTER_CLOCKWISE)
            offset = 1;

        for (int i = 0; i < 4; ++i)
        {
            quad.vertices[i] = quad.vertices[i].withUV(uCoords[(i + offset) % 4], vCoords[(i + offset) % 4]);
        }
    }

    private BendsQuad createQuad(BendsVertex[] vertices, int u1, int v1, int u2, int v2,
                                  float textureWidth, float textureHeight)
    {
        vertices[0] = vertices[0].withUV(u2 / textureWidth, v1 / textureHeight);
        vertices[1] = vertices[1].withUV(u1 / textureWidth, v1 / textureHeight);
        vertices[2] = vertices[2].withUV(u1 / textureWidth, v2 / textureHeight);
        vertices[3] = vertices[3].withUV(u2 / textureWidth, v2 / textureHeight);

        return new BendsQuad(vertices);
    }

    @Deprecated
    public void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer,
                        int packedLight, int packedOverlay,
                        float red, float green, float blue, float alpha)
    {
        int color = ((int)(alpha * 255.0F) << 24) |
                    ((int)(red * 255.0F) << 16) |
                    ((int)(green * 255.0F) << 8) |
                    (int)(blue * 255.0F);
        compile(pose, vertexConsumer, packedLight, packedOverlay, color);
    }

    public void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer,
                        int packedLight, int packedOverlay, int color)
    {
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        IEntityVertexHelper vertexHelper = IEntityVertexHelper.Holder.getHelper();

        byte tempFlag = this.faceVisibilityFlag;

        for (BendsQuad quad : quads)
        {
            if ((tempFlag & 1) == 1)
            {
                float nx = quad.normalX;
                float ny = quad.normalY;
                float nz = quad.normalZ;

                float tnx = normalMatrix.m00() * nx + normalMatrix.m10() * ny + normalMatrix.m20() * nz;
                float tny = normalMatrix.m01() * nx + normalMatrix.m11() * ny + normalMatrix.m21() * nz;
                float tnz = normalMatrix.m02() * nx + normalMatrix.m12() * ny + normalMatrix.m22() * nz;

                for (BendsVertex vertex : quad.vertices)
                {
                    float x = vertex.x;
                    float y = vertex.y;
                    float z = vertex.z;

                    float tx = matrix.m00() * x + matrix.m10() * y + matrix.m20() * z + matrix.m30();
                    float ty = matrix.m01() * x + matrix.m11() * y + matrix.m21() * z + matrix.m31();
                    float tz = matrix.m02() * x + matrix.m12() * y + matrix.m22() * z + matrix.m32();

                    vertexHelper.emitVertex(vertexConsumer,
                            tx, ty, tz,
                            color,
                            vertex.u, vertex.v,
                            packedOverlay, packedLight,
                            tnx, tny, tnz);
                }
            }
            tempFlag >>= 1;
        }
    }

    public void compileAbstracted(IPoseStack poseStack, IVertexConsumer vertexConsumer,
                                   int packedLight, int packedOverlay, int color)
    {
        float[] poseMatrix = new float[16];
        float[] normalMatrix = new float[9];
        poseStack.getPose(poseMatrix);
        poseStack.getNormal(normalMatrix);

        byte tempFlag = this.faceVisibilityFlag;

        for (BendsQuad quad : quads)
        {
            if ((tempFlag & 1) == 1)
            {
                float nx = quad.normalX;
                float ny = quad.normalY;
                float nz = quad.normalZ;

                float tnx = normalMatrix[0] * nx + normalMatrix[3] * ny + normalMatrix[6] * nz;
                float tny = normalMatrix[1] * nx + normalMatrix[4] * ny + normalMatrix[7] * nz;
                float tnz = normalMatrix[2] * nx + normalMatrix[5] * ny + normalMatrix[8] * nz;

                for (BendsVertex vertex : quad.vertices)
                {
                    float x = vertex.x;
                    float y = vertex.y;
                    float z = vertex.z;

                    float tx = poseMatrix[0] * x + poseMatrix[4] * y + poseMatrix[8] * z + poseMatrix[12];
                    float ty = poseMatrix[1] * x + poseMatrix[5] * y + poseMatrix[9] * z + poseMatrix[13];
                    float tz = poseMatrix[2] * x + poseMatrix[6] * y + poseMatrix[10] * z + poseMatrix[14];

                    vertexConsumer.addVertex(tx, ty, tz)
                            .setColor(color)
                            .setUv(vertex.u, vertex.v)
                            .setOverlay(packedOverlay)
                            .setLight(packedLight)
                            .setNormal(tnx, tny, tnz);
                }
            }
            tempFlag >>= 1;
        }
    }

    public boolean isFaceVisible(int faceIndex)
    {
        return ((faceVisibilityFlag >> faceIndex) & 1) == 1;
    }

    public AABBox createAABB()
    {
        return new AABBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static class BendsVertex
    {
        public final float x, y, z;
        public final float u, v;

        public BendsVertex(float x, float y, float z, float u, float v)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
        }

        public BendsVertex withUV(float u, float v)
        {
            return new BendsVertex(this.x, this.y, this.z, u, v);
        }
    }

    public static class BendsQuad
    {
        public final BendsVertex[] vertices;
        public float normalX, normalY, normalZ;

        public BendsQuad(BendsVertex[] vertices)
        {
            this.vertices = vertices;
            calculateNormal();
        }

        private void calculateNormal()
        {
            float ax = vertices[1].x - vertices[0].x;
            float ay = vertices[1].y - vertices[0].y;
            float az = vertices[1].z - vertices[0].z;

            float bx = vertices[2].x - vertices[0].x;
            float by = vertices[2].y - vertices[0].y;
            float bz = vertices[2].z - vertices[0].z;

            normalX = ay * bz - az * by;
            normalY = az * bx - ax * bz;
            normalZ = ax * by - ay * bx;

            float length = (float) Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
            if (length > 0)
            {
                normalX /= length;
                normalY /= length;
                normalZ /= length;
            }
        }

        public void flipFace()
        {
            BendsVertex temp = vertices[1];
            vertices[1] = vertices[3];
            vertices[3] = temp;

            normalX = -normalX;
            normalY = -normalY;
            normalZ = -normalZ;
        }
    }
}
