package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * A cube specifically for armor rendering that can be sliced at a Y plane.
 * Based on the original MutatedBox but designed for 1.20.1 rendering.
 */
@OnlyIn(Dist.CLIENT)
public class ArmorCube
{
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int TOP = 2;
    public static final int BOTTOM = 3;
    public static final int FRONT = 4;
    public static final int BACK = 5;

    /**
     * Face visibility flags - each bit represents a face.
     */
    protected byte faceVisibilityFlag;

    /**
     * The 6 quads (faces) of this cube.
     */
    protected final ArmorQuad[] quads = new ArmorQuad[6];

    /**
     * Bounds of the cube in model units.
     */
    public float minX, minY, minZ;
    public float maxX, maxY, maxZ;

    /**
     * Whether this cube is mirrored.
     */
    public boolean mirror;

    /**
     * Create a cube from bounds and texture coordinates.
     */
    public ArmorCube(float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                     float inflation,
                     int texOffsetX, int texOffsetY,
                     float textureWidth, float textureHeight,
                     boolean mirror)
    {
        // Use actual dimensions for UV mapping
        this(minX, minY, minZ, maxX, maxY, maxZ, inflation,
             texOffsetX, texOffsetY, textureWidth, textureHeight, mirror,
             (int)(maxX - minX), (int)(maxY - minY), (int)(maxZ - minZ), 0);
    }

    /**
     * Create a cube with custom UV dimensions (for sliced limbs).
     * This allows the upper/lower portions to use the correct texture mapping.
     *
     * @param uvWidth The width to use for UV mapping (original limb width)
     * @param uvHeight The height to use for UV mapping (original limb height)
     * @param uvDepth The depth to use for UV mapping (original limb depth)
     * @param vOffset The V coordinate offset (0 for upper portion, portion height for lower)
     */
    public ArmorCube(float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                     float inflation,
                     int texOffsetX, int texOffsetY,
                     float textureWidth, float textureHeight,
                     boolean mirror,
                     int uvWidth, int uvHeight, int uvDepth, int vOffset)
    {
        this.mirror = mirror;
        this.faceVisibilityFlag = (byte) 0b111111;

        // Apply inflation
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

        // Scale to world units for rendering
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

        // Use provided UV dimensions and offset for proper texture mapping
        createQuadsWithOffset(rx1, ry1, rz1, rx2, ry2, rz2,
                             texOffsetX, texOffsetY, uvWidth, uvHeight, uvDepth,
                             textureWidth, textureHeight, mirror,
                             (int)(maxY - minY), vOffset);
    }

    /**
     * Create a cube with pre-calculated UV faces (used for sliced cubes).
     */
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

    /**
     * Create quads with proper UV mapping for sliced limbs.
     * @param actualHeight The actual height of this cube portion
     * @param vOffset The V offset within the full limb texture (0 for top, upper height for bottom)
     */
    protected void createQuadsWithOffset(float x1, float y1, float z1, float x2, float y2, float z2,
                                         int texU, int texV, int uvWidth, int uvHeight, int uvDepth,
                                         float textureWidth, float textureHeight, boolean mirror,
                                         int actualHeight, int vOffset)
    {
        // Create the 8 vertices
        ArmorVertex v000 = new ArmorVertex(x1, y1, z1, 0, 0);
        ArmorVertex v100 = new ArmorVertex(x2, y1, z1, 0, 0);
        ArmorVertex v110 = new ArmorVertex(x2, y2, z1, 0, 0);
        ArmorVertex v010 = new ArmorVertex(x1, y2, z1, 0, 0);
        ArmorVertex v001 = new ArmorVertex(x1, y1, z2, 0, 0);
        ArmorVertex v101 = new ArmorVertex(x2, y1, z2, 0, 0);
        ArmorVertex v111 = new ArmorVertex(x2, y2, z2, 0, 0);
        ArmorVertex v011 = new ArmorVertex(x1, y2, z2, 0, 0);

        // UV coordinates - use full limb dimensions for proper texture mapping
        int u = texU;
        int v = texV;

        // For side faces (left, right, front, back), offset V by vOffset and use actualHeight
        int sideVStart = v + uvDepth + vOffset;
        int sideVEnd = sideVStart + actualHeight;

        // Left face (-X): v000, v001, v011, v010
        quads[LEFT] = createQuad(new ArmorVertex[] {v000, v001, v011, v010},
                u, sideVStart, u + uvDepth, sideVEnd, textureWidth, textureHeight);

        // Right face (+X): v101, v100, v110, v111
        quads[RIGHT] = createQuad(new ArmorVertex[] {v101, v100, v110, v111},
                u + uvDepth + uvWidth, sideVStart, u + uvDepth + uvWidth + uvDepth, sideVEnd, textureWidth, textureHeight);

        // Top face (-Y): only show for upper portion (vOffset == 0)
        quads[TOP] = createQuad(new ArmorVertex[] {v101, v001, v000, v100},
                u + uvDepth, v, u + uvDepth + uvWidth, v + uvDepth, textureWidth, textureHeight);

        // Bottom face (+Y): only show for lower portion (vOffset > 0)
        quads[BOTTOM] = createQuad(new ArmorVertex[] {v110, v010, v011, v111},
                u + uvDepth + uvWidth, v, u + uvDepth + uvWidth + uvWidth, v + uvDepth, textureWidth, textureHeight);

        // Front face (-Z): v100, v000, v010, v110
        quads[FRONT] = createQuad(new ArmorVertex[] {v100, v000, v010, v110},
                u + uvDepth, sideVStart, u + uvDepth + uvWidth, sideVEnd, textureWidth, textureHeight);

        // Back face (+Z): v001, v101, v111, v011
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

    /**
     * Hide a specific face.
     */
    public void hideFace(int faceIndex)
    {
        faceVisibilityFlag &= ~(1 << faceIndex);
    }

    /**
     * Show a specific face.
     */
    public void showFace(int faceIndex)
    {
        faceVisibilityFlag |= (1 << faceIndex);
    }

    /**
     * Slice this cube at a Y plane, returning the lower portion.
     * This cube is modified to become the upper portion.
     * @param sliceY The Y coordinate to slice at (in model units)
     * @param textureWidth Texture width for UV recalculation
     * @param textureHeight Texture height for UV recalculation
     * @return The lower portion, or null if no slice was needed
     */
    public ArmorCube sliceAtY(float sliceY, float textureWidth, float textureHeight)
    {
        // Check if slice plane intersects this cube
        if (sliceY <= minY || sliceY >= maxY)
        {
            return null;
        }

        float originalHeight = maxY - minY;
        float upperHeight = sliceY - minY;
        float lowerHeight = maxY - sliceY;
        float upperRatio = upperHeight / originalHeight;
        float lowerRatio = lowerHeight / originalHeight;

        // Create lower cube quads by copying and adjusting
        ArmorQuad[] lowerQuads = new ArmorQuad[6];
        byte lowerVisibility = this.faceVisibilityFlag;

        // Scale to world units
        float scale = 1.0F / 16.0F;
        float sliceYWorld = sliceY * scale;
        float maxYWorld = maxY * scale;
        float minYWorld = minY * scale;

        // Adjust the quads for the lower portion
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

                // For side faces (LEFT, RIGHT, FRONT, BACK), we need to adjust Y and V
                if (i != TOP && i != BOTTOM)
                {
                    if (v.y < sliceYWorld)
                    {
                        // This vertex is in the upper part - move to slice plane
                        newY = sliceYWorld;
                        // Interpolate V coordinate
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

        // Now adjust this cube (upper portion)
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
                        // This vertex is in the lower part - move to slice plane
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

        // Hide bottom face on upper cube, top face on lower cube
        this.hideFace(BOTTOM);
        lowerVisibility &= ~(1 << TOP);

        // Update bounds
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

    /**
     * Render this cube to the vertex consumer.
     */
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

                    // Pack RGBA into single int for 1.21.1
                    int color = ((int)(alpha * 255.0F) << 24) | ((int)(red * 255.0F) << 16) | ((int)(green * 255.0F) << 8) | (int)(blue * 255.0F);
                    vertexConsumer.addVertex(tx, ty, tz)
                            .setColor(color)
                            .setUv(vertex.u, vertex.v)
                            .setOverlay(packedOverlay)
                            .setLight(packedLight)
                            .setNormal(normal.x(), normal.y(), normal.z());
                }
            }
            tempFlag >>= 1;
        }
    }

    /**
     * A vertex with position and UV coordinates.
     */
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

    /**
     * A quad (face) with 4 vertices and a normal.
     */
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
            // Save the original V coordinates before swapping
            // Position 1 should remain top (v1), position 3 should remain bottom (v2)
            float v1_original = vertices[1].v;  // Top V coordinate
            float v3_original = vertices[3].v;  // Bottom V coordinate

            // Swap vertices 1 and 3 to reverse winding order for back-face culling
            ArmorVertex temp = vertices[1];
            vertices[1] = vertices[3];
            vertices[3] = temp;

            // Restore V coordinates to maintain correct vertical texture mapping
            // This ensures the top of the geometry still maps to the top of the texture
            // while the U coordinates remain swapped for horizontal mirroring
            vertices[1] = vertices[1].withUV(vertices[1].u, v1_original);
            vertices[3] = vertices[3].withUV(vertices[3].u, v3_original);

            normalX = -normalX;
            normalY = -normalY;
            normalZ = -normalZ;
        }
    }
}
