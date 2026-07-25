package goblinbob.mobends.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;
import goblinbob.mobends.api.rendering.IPoseStack;
import goblinbob.mobends.api.rendering.IVertexConsumer;
import goblinbob.mobends.lib.math.physics.AABBox;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * A cube/box for the new 1.20.1 rendering system.
 * Replaces MutatedBox and uses VertexConsumer for immediate-mode rendering
 * instead of the obsolete display lists.
 */
public class BendsCube
{
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int TOP = 2;
    public static final int BOTTOM = 3;
    public static final int FRONT = 4;
    public static final int BACK = 5;

    /**
     * Face visibility flag - each bit represents a face.
     */
    protected final byte faceVisibilityFlag;

    /**
     * The 6 quads (faces) of this cube.
     */
    protected final BendsQuad[] quads = new BendsQuad[6];

    /**
     * Bounds of the cube for collision detection.
     */
    public final float minX, minY, minZ;
    public final float maxX, maxY, maxZ;

    /**
     * Create a cube with standard box layout.
     */
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

    /**
     * Create a cube with face visibility control.
     */
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

    /**
     * Create a cube whose bottom face samples a separate texture offset, used for split
     * limb segments so the hand/foot underside maps to the real arm/leg bottom.
     */
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

    /**
     * Create a cube with face visibility control and a bottom-face texture offset override.
     */
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

        // Calculate bounds in model units (for collision)
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

        // Scale to world units (divide by 16) for rendering
        // This matches vanilla Minecraft's coordinate system
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

        // Create the 8 vertices (now in world scale)
        BendsVertex v000 = new BendsVertex(x1, y1, z1, 0, 0);
        BendsVertex v100 = new BendsVertex(x2, y1, z1, 0, 0);
        BendsVertex v110 = new BendsVertex(x2, y2, z1, 0, 0);
        BendsVertex v010 = new BendsVertex(x1, y2, z1, 0, 0);
        BendsVertex v001 = new BendsVertex(x1, y1, z2, 0, 0);
        BendsVertex v101 = new BendsVertex(x2, y1, z2, 0, 0);
        BendsVertex v111 = new BendsVertex(x2, y2, z2, 0, 0);
        BendsVertex v011 = new BendsVertex(x1, y2, z2, 0, 0);

        // Calculate UV coordinates based on Minecraft's standard cube unwrap
        int u = texOffsetX;
        int v = texOffsetY;

        // Create the 6 quads with proper UV mapping
        // Face order: Right(+X), Left(-X), Top(+Y), Bottom(-Y), Front(+Z), Back(-Z)

        // Right face (+X side): vertices v101, v100, v110, v111
        quads[0] = createQuad(new BendsVertex[] {v101, v100, v110, v111},
                u + depth + width, v + depth,
                u + depth + width + depth, v + depth + height,
                textureWidth, textureHeight);

        // Left face (-X side): vertices v000, v001, v011, v010
        quads[1] = createQuad(new BendsVertex[] {v000, v001, v011, v010},
                u, v + depth,
                u + depth, v + depth + height,
                textureWidth, textureHeight);

        // Top face (-Y side): vertices v101, v001, v000, v100
        quads[2] = createQuad(new BendsVertex[] {v101, v001, v000, v100},
                u + depth, v,
                u + depth + width, v + depth,
                textureWidth, textureHeight);

        // Bottom face (+Y side): vertices v110, v010, v011, v111
        quads[3] = createQuad(new BendsVertex[] {v110, v010, v011, v111},
                bottomTexOffsetX + depth + width, bottomTexOffsetY,
                bottomTexOffsetX + depth + width + width, bottomTexOffsetY + depth,
                textureWidth, textureHeight);

        // Front face (-Z side): vertices v100, v000, v010, v110
        quads[4] = createQuad(new BendsVertex[] {v100, v000, v010, v110},
                u + depth, v + depth,
                u + depth + width, v + depth + height,
                textureWidth, textureHeight);

        // Back face (+Z side): vertices v001, v101, v111, v011
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

    private BendsQuad createQuad(BendsVertex[] vertices, int u1, int v1, int u2, int v2,
                                  float textureWidth, float textureHeight)
    {
        // Assign UV coordinates to vertices
        vertices[0] = vertices[0].withUV(u2 / textureWidth, v1 / textureHeight);
        vertices[1] = vertices[1].withUV(u1 / textureWidth, v1 / textureHeight);
        vertices[2] = vertices[2].withUV(u1 / textureWidth, v2 / textureHeight);
        vertices[3] = vertices[3].withUV(u2 / textureWidth, v2 / textureHeight);

        return new BendsQuad(vertices);
    }

    /**
     * Compile and render this cube to the vertex consumer.
     *
     * @deprecated Use the overload with int color parameter for 1.21.1+
     */
    @Deprecated
    public void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer,
                        int packedLight, int packedOverlay,
                        float red, float green, float blue, float alpha)
    {
        // Convert RGBA floats to packed ARGB int (range 0.0-1.0 to 0-255)
        int color = ((int)(alpha * 255.0F) << 24) |
                    ((int)(red * 255.0F) << 16) |
                    ((int)(green * 255.0F) << 8) |
                    (int)(blue * 255.0F);
        compile(pose, vertexConsumer, packedLight, packedOverlay, color);
    }

    /**
     * Compile and render this cube to the vertex consumer (1.21.1+).
     */
    public void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer,
                        int packedLight, int packedOverlay, int color)
    {
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        byte tempFlag = this.faceVisibilityFlag;

        for (BendsQuad quad : quads)
        {
            // Check if this face is visible
            if ((tempFlag & 1) == 1)
            {
                // Calculate transformed normal
                Vector3f normal = new Vector3f(quad.normalX, quad.normalY, quad.normalZ);
                normal.mul(normalMatrix);

                for (BendsVertex vertex : quad.vertices)
                {
                    // Transform vertex position
                    float x = vertex.x;
                    float y = vertex.y;
                    float z = vertex.z;

                    // Apply the transformation matrix
                    float tx = matrix.m00() * x + matrix.m10() * y + matrix.m20() * z + matrix.m30();
                    float ty = matrix.m01() * x + matrix.m11() * y + matrix.m21() * z + matrix.m31();
                    float tz = matrix.m02() * x + matrix.m12() * y + matrix.m22() * z + matrix.m32();

                    IEntityVertexHelper.Holder.getHelper().emitVertex(vertexConsumer,
                            tx, ty, tz,
                            color,
                            vertex.u, vertex.v,
                            packedOverlay, packedLight,
                            normal.x(), normal.y(), normal.z());
                }
            }
            tempFlag >>= 1;
        }
    }

    /**
     * Compile and render this cube using abstracted types (cross-version compatible).
     * This method uses IPoseStack and IVertexConsumer for platform-agnostic rendering.
     *
     * @param poseStack The abstracted pose stack
     * @param vertexConsumer The abstracted vertex consumer
     * @param packedLight The packed light value
     * @param packedOverlay The packed overlay value
     * @param color The packed ARGB color
     */
    public void compileAbstracted(IPoseStack poseStack, IVertexConsumer vertexConsumer,
                                   int packedLight, int packedOverlay, int color)
    {
        // Get matrices from the abstracted pose stack
        float[] poseMatrix = new float[16];
        float[] normalMatrix = new float[9];
        poseStack.getPose(poseMatrix);
        poseStack.getNormal(normalMatrix);

        byte tempFlag = this.faceVisibilityFlag;

        for (BendsQuad quad : quads)
        {
            // Check if this face is visible
            if ((tempFlag & 1) == 1)
            {
                // Calculate transformed normal using 3x3 normal matrix
                float nx = quad.normalX;
                float ny = quad.normalY;
                float nz = quad.normalZ;

                // Transform normal: normalMatrix is in column-major order (3x3)
                // [m00, m10, m20, m01, m11, m21, m02, m12, m22]
                float tnx = normalMatrix[0] * nx + normalMatrix[3] * ny + normalMatrix[6] * nz;
                float tny = normalMatrix[1] * nx + normalMatrix[4] * ny + normalMatrix[7] * nz;
                float tnz = normalMatrix[2] * nx + normalMatrix[5] * ny + normalMatrix[8] * nz;

                for (BendsVertex vertex : quad.vertices)
                {
                    // Transform vertex position using 4x4 pose matrix (column-major)
                    float x = vertex.x;
                    float y = vertex.y;
                    float z = vertex.z;

                    float tx = poseMatrix[0] * x + poseMatrix[4] * y + poseMatrix[8] * z + poseMatrix[12];
                    float ty = poseMatrix[1] * x + poseMatrix[5] * y + poseMatrix[9] * z + poseMatrix[13];
                    float tz = poseMatrix[2] * x + poseMatrix[6] * y + poseMatrix[10] * z + poseMatrix[14];

                    // Output vertex using abstracted consumer
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

    /**
     * Check if a specific face is visible.
     */
    public boolean isFaceVisible(int faceIndex)
    {
        return ((faceVisibilityFlag >> faceIndex) & 1) == 1;
    }

    /**
     * Create an axis-aligned bounding box for this cube.
     */
    public AABBox createAABB()
    {
        return new AABBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * A vertex with position and UV coordinates.
     */
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

    /**
     * A quad (face) with 4 vertices and a normal.
     */
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
            // Calculate normal from first 3 vertices
            float ax = vertices[1].x - vertices[0].x;
            float ay = vertices[1].y - vertices[0].y;
            float az = vertices[1].z - vertices[0].z;

            float bx = vertices[2].x - vertices[0].x;
            float by = vertices[2].y - vertices[0].y;
            float bz = vertices[2].z - vertices[0].z;

            // Cross product
            normalX = ay * bz - az * by;
            normalY = az * bx - ax * bz;
            normalZ = ax * by - ay * bx;

            // Normalize
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
            // Reverse vertex order
            BendsVertex temp = vertices[1];
            vertices[1] = vertices[3];
            vertices[3] = temp;

            // Flip normal
            normalX = -normalX;
            normalY = -normalY;
            normalZ = -normalZ;
        }
    }
}
