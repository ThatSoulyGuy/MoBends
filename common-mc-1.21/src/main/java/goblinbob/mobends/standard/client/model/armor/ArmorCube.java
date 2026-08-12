package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ArmorCube
{
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int TOP = 2;
    public static final int BOTTOM = 3;
    public static final int FRONT = 4;
    public static final int BACK = 5;

    protected byte faceVisibilityFlag;

    protected final ArmorQuad[] quads = new ArmorQuad[6];

    public float minX, minY, minZ;
    public float maxX, maxY, maxZ;

    public boolean mirror;

    public ArmorCube(float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                     float inflation,
                     int texOffsetX, int texOffsetY,
                     float textureWidth, float textureHeight,
                     boolean mirror)
    {
        this(minX, minY, minZ, maxX, maxY, maxZ, inflation,
             texOffsetX, texOffsetY, textureWidth, textureHeight, mirror,
             (int)(maxX - minX), (int)(maxY - minY), (int)(maxZ - minZ), 0);
    }

    public ArmorCube(float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                     float inflation,
                     int texOffsetX, int texOffsetY,
                     float textureWidth, float textureHeight,
                     boolean mirror,
                     int uvWidth, int uvHeight, int uvDepth, int vOffset)
    {
        this.mirror = mirror;
        this.faceVisibilityFlag = (byte) 0b111111;

        float x1 = minX - inflation;
        float y1 = minY - inflation;
        float z1 = minZ - inflation;
        float x2 = maxX + inflation;
        float y2 = maxY + inflation;
        float z2 = maxZ + inflation;

        this.minX = x1;
        this.minY = y1;
        this.minZ = z1;
        this.maxX = x2;
        this.maxY = y2;
        this.maxZ = z2;

        float scale = 1.0F / 16.0F;
        float rx1 = x1 * scale;
        float ry1 = y1 * scale;
        float rz1 = z1 * scale;
        float rx2 = x2 * scale;
        float ry2 = y2 * scale;
        float rz2 = z2 * scale;

        if (mirror)
        {
            float temp = rx2;
            rx2 = rx1;
            rx1 = temp;
        }

        createQuadsWithOffset(rx1, ry1, rz1, rx2, ry2, rz2,
                             texOffsetX, texOffsetY, uvWidth, uvHeight, uvDepth,
                             textureWidth, textureHeight, mirror,
                             (int)(maxY - minY), vOffset);
    }

    public ArmorCube(float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                     ArmorQuad[] quads, byte faceVisibilityFlag, boolean mirror)
    {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.faceVisibilityFlag = faceVisibilityFlag;
        this.mirror = mirror;
        System.arraycopy(quads, 0, this.quads, 0, 6);
    }

    protected void createQuads(float x1, float y1, float z1, float x2, float y2, float z2,
                               int texU, int texV, int width, int height, int depth,
                               float textureWidth, float textureHeight, boolean mirror)
    {
        createQuadsWithOffset(x1, y1, z1, x2, y2, z2, texU, texV, width, height, depth,
                             textureWidth, textureHeight, mirror, height, 0);
    }

    protected void createQuadsWithOffset(float x1, float y1, float z1, float x2, float y2, float z2,
                                         int texU, int texV, int uvWidth, int uvHeight, int uvDepth,
                                         float textureWidth, float textureHeight, boolean mirror,
                                         int actualHeight, int vOffset)
    {
        ArmorVertex v000 = new ArmorVertex(x1, y1, z1, 0, 0);
        ArmorVertex v100 = new ArmorVertex(x2, y1, z1, 0, 0);
        ArmorVertex v110 = new ArmorVertex(x2, y2, z1, 0, 0);
        ArmorVertex v010 = new ArmorVertex(x1, y2, z1, 0, 0);
        ArmorVertex v001 = new ArmorVertex(x1, y1, z2, 0, 0);
        ArmorVertex v101 = new ArmorVertex(x2, y1, z2, 0, 0);
        ArmorVertex v111 = new ArmorVertex(x2, y2, z2, 0, 0);
        ArmorVertex v011 = new ArmorVertex(x1, y2, z2, 0, 0);

        int u = texU;
        int v = texV;

        int sideVStart = v + uvDepth + vOffset;
        int sideVEnd = sideVStart + actualHeight;

        quads[LEFT] = createQuad(new ArmorVertex[] {v000, v001, v011, v010},
                u, sideVStart, u + uvDepth, sideVEnd, textureWidth, textureHeight);

        quads[RIGHT] = createQuad(new ArmorVertex[] {v101, v100, v110, v111},
                u + uvDepth + uvWidth, sideVStart, u + uvDepth + uvWidth + uvDepth, sideVEnd, textureWidth, textureHeight);

        quads[TOP] = createQuad(new ArmorVertex[] {v101, v001, v000, v100},
                u + uvDepth, v, u + uvDepth + uvWidth, v + uvDepth, textureWidth, textureHeight);

        quads[BOTTOM] = createQuad(new ArmorVertex[] {v110, v010, v011, v111},
                u + uvDepth + uvWidth, v, u + uvDepth + uvWidth + uvWidth, v + uvDepth, textureWidth, textureHeight);

        quads[FRONT] = createQuad(new ArmorVertex[] {v100, v000, v010, v110},
                u + uvDepth, sideVStart, u + uvDepth + uvWidth, sideVEnd, textureWidth, textureHeight);

        quads[BACK] = createQuad(new ArmorVertex[] {v001, v101, v111, v011},
                u + uvDepth + uvWidth + uvDepth, sideVStart, u + uvDepth + uvWidth + uvDepth + uvWidth, sideVEnd, textureWidth, textureHeight);

        if (mirror)
        {
            for (ArmorQuad quad : quads)
            {
                if (quad != null) quad.flipFace();
            }
        }
    }

    protected ArmorQuad createQuad(ArmorVertex[] vertices, int u1, int v1, int u2, int v2,
                                   float textureWidth, float textureHeight)
    {
        vertices[0] = vertices[0].withUV(u2 / textureWidth, v1 / textureHeight);
        vertices[1] = vertices[1].withUV(u1 / textureWidth, v1 / textureHeight);
        vertices[2] = vertices[2].withUV(u1 / textureWidth, v2 / textureHeight);
        vertices[3] = vertices[3].withUV(u2 / textureWidth, v2 / textureHeight);

        return new ArmorQuad(vertices);
    }

    public void hideFace(int faceIndex)
    {
        faceVisibilityFlag &= ~(1 << faceIndex);
    }

    public void showFace(int faceIndex)
    {
        faceVisibilityFlag |= (1 << faceIndex);
    }

    public ArmorCube sliceAtY(float sliceY, float textureWidth, float textureHeight)
    {
        if (sliceY <= minY || sliceY >= maxY)
        {
            return null;
        }

        float originalHeight = maxY - minY;
        float upperHeight = sliceY - minY;
        float lowerHeight = maxY - sliceY;
        float upperRatio = upperHeight / originalHeight;
        float lowerRatio = lowerHeight / originalHeight;

        ArmorQuad[] lowerQuads = new ArmorQuad[6];
        byte lowerVisibility = this.faceVisibilityFlag;

        float scale = 1.0F / 16.0F;
        float sliceYWorld = sliceY * scale;
        float maxYWorld = maxY * scale;
        float minYWorld = minY * scale;

        for (int i = 0; i < 6; i++)
        {
            if (quads[i] == null) continue;

            ArmorVertex[] newVerts = new ArmorVertex[4];
            ArmorVertex[] origVerts = quads[i].vertices;

            for (int j = 0; j < 4; j++)
            {
                ArmorVertex v = origVerts[j];
                float newY = v.y;
                float newV = v.v;

                if (i != TOP && i != BOTTOM)
                {
                    if (v.y < sliceYWorld)
                    {
                        newY = sliceYWorld;
                        float t = (sliceYWorld - minYWorld) / (maxYWorld - minYWorld);
                        float origVTop = getMinVForFace(i);
                        float origVBot = getMaxVForFace(i);
                        newV = origVTop + t * (origVBot - origVTop);
                    }
                }

                newVerts[j] = new ArmorVertex(v.x, newY, v.z, v.u, newV);
            }

            lowerQuads[i] = new ArmorQuad(newVerts);
            lowerQuads[i].normalX = quads[i].normalX;
            lowerQuads[i].normalY = quads[i].normalY;
            lowerQuads[i].normalZ = quads[i].normalZ;
        }

        for (int i = 0; i < 6; i++)
        {
            if (quads[i] == null) continue;

            ArmorVertex[] origVerts = quads[i].vertices;

            for (int j = 0; j < 4; j++)
            {
                ArmorVertex v = origVerts[j];

                if (i != TOP && i != BOTTOM)
                {
                    if (v.y > sliceYWorld)
                    {
                        float newY = sliceYWorld;
                        float t = (sliceYWorld - minYWorld) / (maxYWorld - minYWorld);
                        float origVTop = getMinVForFace(i);
                        float origVBot = getMaxVForFace(i);
                        float newV = origVTop + t * (origVBot - origVTop);
                        origVerts[j] = new ArmorVertex(v.x, newY, v.z, v.u, newV);
                    }
                }
            }
        }

        this.hideFace(BOTTOM);
        lowerVisibility &= ~(1 << TOP);

        float oldMaxY = this.maxY;
        this.maxY = sliceY;

        return new ArmorCube(minX, sliceY, minZ, maxX, oldMaxY, maxZ,
                            lowerQuads, lowerVisibility, mirror);
    }

    private float getMinVForFace(int faceIndex)
    {
        if (quads[faceIndex] == null) return 0;
        float minV = Float.MAX_VALUE;
        for (ArmorVertex v : quads[faceIndex].vertices)
        {
            if (v.v < minV) minV = v.v;
        }
        return minV;
    }

    private float getMaxVForFace(int faceIndex)
    {
        if (quads[faceIndex] == null) return 0;
        float maxV = Float.MIN_VALUE;
        for (ArmorVertex v : quads[faceIndex].vertices)
        {
            if (v.v > maxV) maxV = v.v;
        }
        return maxV;
    }

    public void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer,
                        int packedLight, int packedOverlay,
                        float red, float green, float blue, float alpha)
    {
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        byte tempFlag = this.faceVisibilityFlag;

        for (ArmorQuad quad : quads)
        {
            if (quad != null && (tempFlag & 1) == 1)
            {
                Vector3f normal = new Vector3f(quad.normalX, quad.normalY, quad.normalZ);
                normal.mul(normalMatrix);

                for (ArmorVertex vertex : quad.vertices)
                {
                    float x = vertex.x;
                    float y = vertex.y;
                    float z = vertex.z;

                    float tx = matrix.m00() * x + matrix.m10() * y + matrix.m20() * z + matrix.m30();
                    float ty = matrix.m01() * x + matrix.m11() * y + matrix.m21() * z + matrix.m31();
                    float tz = matrix.m02() * x + matrix.m12() * y + matrix.m22() * z + matrix.m32();

                    int color = ((int)(alpha * 255.0F) << 24) | ((int)(red * 255.0F) << 16) | ((int)(green * 255.0F) << 8) | (int)(blue * 255.0F);
                    IEntityVertexHelper.Holder.getHelper().emitVertex(vertexConsumer,
                            tx, ty, tz, color, vertex.u, vertex.v,
                            packedOverlay, packedLight, normal.x(), normal.y(), normal.z());
                }
            }
            tempFlag >>= 1;
        }
    }

    public static class ArmorVertex
    {
        public final float x, y, z;
        public final float u, v;

        public ArmorVertex(float x, float y, float z, float u, float v)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
        }

        public ArmorVertex withUV(float u, float v)
        {
            return new ArmorVertex(this.x, this.y, this.z, u, v);
        }
    }

    public static class ArmorQuad
    {
        public final ArmorVertex[] vertices;
        public float normalX, normalY, normalZ;

        public ArmorQuad(ArmorVertex[] vertices)
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
            float v1_original = vertices[1].v;
            float v3_original = vertices[3].v;

            ArmorVertex temp = vertices[1];
            vertices[1] = vertices[3];
            vertices[3] = temp;

            vertices[1] = vertices[1].withUV(vertices[1].u, v1_original);
            vertices[3] = vertices[3].withUV(vertices[3].u, v3_original);

            normalX = -normalX;
            normalY = -normalY;
            normalZ = -normalZ;
        }
    }
}
