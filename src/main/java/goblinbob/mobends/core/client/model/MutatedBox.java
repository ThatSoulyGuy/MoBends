package goblinbob.mobends.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.math.physics.AABBox;
import goblinbob.mobends.core.math.vector.IVec3fRead;
import goblinbob.mobends.core.math.vector.Vec3f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Custom box implementation for Mo' Bends animations.
 * Updated for Minecraft 1.20.1 - no longer extends ModelBox (which doesn't exist).
 * Uses immediate-mode rendering via VertexConsumer instead of display lists.
 */
public class MutatedBox
{
    // We just need 6 bits (6 faces)
    protected final byte faceVisibilityFlag;

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int TOP = 2;
    public static final int BOTTOM = 3;
    public static final int FRONT = 4;
    public static final int BACK = 5;

    // Box bounds for collision detection
    public final float posX1, posY1, posZ1;
    public final float posX2, posY2, posZ2;

    // Custom vertex structure
    protected final MutatedVertex[] vertices = new MutatedVertex[8];
    protected final MutatedQuad[] quads = new MutatedQuad[6];

    /**
     * Internal vertex class for storing position and texture coordinates
     */
    public static class MutatedVertex
    {
        public float x, y, z;
        public float u, v;

        public MutatedVertex(float x, float y, float z, float u, float v)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
        }

        public MutatedVertex remap(float u, float v)
        {
            return new MutatedVertex(this.x, this.y, this.z, u, v);
        }
    }

    /**
     * Internal quad class for storing 4 vertices and a normal
     */
    public static class MutatedQuad
    {
        public final MutatedVertex[] vertices;
        public float normalX, normalY, normalZ;

        public MutatedQuad(MutatedVertex[] vertices, float u1, float v1, float u2, float v2, float textureWidth, float textureHeight)
        {
            this.vertices = new MutatedVertex[4];
            float uScale = 1.0F / textureWidth;
            float vScale = 1.0F / textureHeight;

            this.vertices[0] = vertices[0].remap(u2 * uScale, v1 * vScale);
            this.vertices[1] = vertices[1].remap(u1 * uScale, v1 * vScale);
            this.vertices[2] = vertices[2].remap(u1 * uScale, v2 * vScale);
            this.vertices[3] = vertices[3].remap(u2 * uScale, v2 * vScale);

            calculateNormal();
        }

        public MutatedQuad(MutatedVertex[] vertices)
        {
            this.vertices = vertices;
            calculateNormal();
        }

        private void calculateNormal()
        {
            // Calculate face normal from first 3 vertices
            float dx1 = vertices[1].x - vertices[0].x;
            float dy1 = vertices[1].y - vertices[0].y;
            float dz1 = vertices[1].z - vertices[0].z;
            float dx2 = vertices[2].x - vertices[1].x;
            float dy2 = vertices[2].y - vertices[1].y;
            float dz2 = vertices[2].z - vertices[1].z;

            // Cross product
            normalX = dy1 * dz2 - dz1 * dy2;
            normalY = dz1 * dx2 - dx1 * dz2;
            normalZ = dx1 * dy2 - dy1 * dx2;

            // Normalize
            float length = (float) Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
            if (length > 0.0F)
            {
                normalX /= length;
                normalY /= length;
                normalZ /= length;
            }
        }

        public void flipFace()
        {
            MutatedVertex[] newVertices = new MutatedVertex[this.vertices.length];
            for (int i = 0; i < this.vertices.length; ++i)
            {
                newVertices[i] = this.vertices[this.vertices.length - i - 1];
            }
            System.arraycopy(newVertices, 0, this.vertices, 0, this.vertices.length);
            normalX = -normalX;
            normalY = -normalY;
            normalZ = -normalZ;
        }

        public void render(PoseStack.Pose pose, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha)
        {
            Matrix4f matrix = pose.pose();
            Matrix3f normalMatrix = pose.normal();

            for (MutatedVertex vertex : this.vertices)
            {
                // Transform position
                Vector3f pos = new Vector3f(vertex.x, vertex.y, vertex.z);
                pos.mulPosition(matrix);

                // Transform normal
                Vector3f normal = new Vector3f(normalX, normalY, normalZ);
                normal.mul(normalMatrix);

                vertexConsumer.vertex(pos.x, pos.y, pos.z,
                        red, green, blue, alpha,
                        vertex.u, vertex.v,
                        packedOverlay, packedLight,
                        normal.x, normal.y, normal.z);
            }
        }
    }

    public MutatedBox(ModelPart renderer, IVec3fRead min, IVec3fRead max, BoxFactory.TextureFace[] faces, byte faceVisibilityFlag)
    {
        this.faceVisibilityFlag = faceVisibilityFlag;

        float x0 = min.getX();
        float y0 = min.getY();
        float z0 = min.getZ();
        float x1 = max.getX();
        float y1 = max.getY();
        float z1 = max.getZ();

        // Store bounds
        this.posX1 = Math.min(x0, x1);
        this.posY1 = Math.min(y0, y1);
        this.posZ1 = Math.min(z0, z1);
        this.posX2 = Math.max(x0, x1);
        this.posY2 = Math.max(y0, y1);
        this.posZ2 = Math.max(z0, z1);

        if (renderer != null && renderer.mirror)
        {
            float temp = x1;
            x1 = x0;
            x0 = temp;
        }

        // Create 8 corner vertices
        vertices[0] = new MutatedVertex(x0, y0, z0, 0.0F, 0.0F);
        vertices[1] = new MutatedVertex(x1, y0, z0, 0.0F, 8.0F);
        vertices[2] = new MutatedVertex(x1, y1, z0, 8.0F, 8.0F);
        vertices[3] = new MutatedVertex(x0, y1, z0, 8.0F, 0.0F);
        vertices[4] = new MutatedVertex(x0, y0, z1, 0.0F, 0.0F);
        vertices[5] = new MutatedVertex(x1, y0, z1, 0.0F, 8.0F);
        vertices[6] = new MutatedVertex(x1, y1, z1, 8.0F, 8.0F);
        vertices[7] = new MutatedVertex(x0, y1, z1, 8.0F, 0.0F);

        float textureWidth = renderer != null ? 64 : 64;
        float textureHeight = renderer != null ? 64 : 64;

        // Create quads for each face
        quads[0] = createQuadFromFace(new MutatedVertex[]{vertices[5], vertices[1], vertices[2], vertices[6]}, faces[0], textureWidth, textureHeight);
        quads[1] = createQuadFromFace(new MutatedVertex[]{vertices[0], vertices[4], vertices[7], vertices[3]}, faces[1], textureWidth, textureHeight);
        quads[2] = createQuadFromFace(new MutatedVertex[]{vertices[5], vertices[4], vertices[0], vertices[1]}, faces[2], textureWidth, textureHeight);
        quads[3] = createQuadFromFace(new MutatedVertex[]{vertices[2], vertices[3], vertices[7], vertices[6]}, faces[3], textureWidth, textureHeight);
        quads[4] = createQuadFromFace(new MutatedVertex[]{vertices[1], vertices[0], vertices[3], vertices[2]}, faces[4], textureWidth, textureHeight);
        quads[5] = createQuadFromFace(new MutatedVertex[]{vertices[4], vertices[5], vertices[6], vertices[7]}, faces[5], textureWidth, textureHeight);

        if (renderer != null && renderer.mirror)
        {
            for (MutatedQuad quad : quads)
            {
                if (quad != null) quad.flipFace();
            }
        }
    }

    public MutatedBox(ModelPart modelRenderer, int texU, int texV, float x, float y, float z, int width, int height, int length, float inflation, boolean mirrored, byte faceVisibilityFlag)
    {
        this.faceVisibilityFlag = faceVisibilityFlag;

        float f4 = x + (float) width;
        float f5 = y + (float) height;
        float f6 = z + (float) length;

        // Store original bounds before inflation
        this.posX1 = x;
        this.posY1 = y;
        this.posZ1 = z;
        this.posX2 = f4;
        this.posY2 = f5;
        this.posZ2 = f6;

        x -= inflation;
        y -= inflation;
        z -= inflation;
        f4 += inflation;
        f5 += inflation;
        f6 += inflation;

        if (mirrored)
        {
            float f7 = f4;
            f4 = x;
            x = f7;
        }

        float textureWidth = modelRenderer != null ? 64 : 64;
        float textureHeight = modelRenderer != null ? 64 : 64;

        // Create 8 corner vertices
        vertices[0] = new MutatedVertex(x, y, z, 0.0F, 0.0F);
        vertices[1] = new MutatedVertex(f4, y, z, 0.0F, 8.0F);
        vertices[2] = new MutatedVertex(f4, f5, z, 8.0F, 8.0F);
        vertices[3] = new MutatedVertex(x, f5, z, 8.0F, 0.0F);
        vertices[4] = new MutatedVertex(x, y, f6, 0.0F, 0.0F);
        vertices[5] = new MutatedVertex(f4, y, f6, 0.0F, 8.0F);
        vertices[6] = new MutatedVertex(f4, f5, f6, 8.0F, 8.0F);
        vertices[7] = new MutatedVertex(x, f5, f6, 8.0F, 0.0F);

        // Create quads for each face with proper UV mapping
        quads[0] = new MutatedQuad(new MutatedVertex[]{vertices[5], vertices[1], vertices[2], vertices[6]},
                texU + length + width, texV + length, texU + length + width + length, texV + length + height, textureWidth, textureHeight);
        quads[1] = new MutatedQuad(new MutatedVertex[]{vertices[0], vertices[4], vertices[7], vertices[3]},
                texU, texV + length, texU + length, texV + length + height, textureWidth, textureHeight);
        quads[2] = new MutatedQuad(new MutatedVertex[]{vertices[5], vertices[4], vertices[0], vertices[1]},
                texU + length, texV, texU + length + width, texV + length, textureWidth, textureHeight);
        quads[3] = new MutatedQuad(new MutatedVertex[]{vertices[2], vertices[3], vertices[7], vertices[6]},
                texU + length + width, texV + length, texU + length + width + width, texV, textureWidth, textureHeight);
        quads[4] = new MutatedQuad(new MutatedVertex[]{vertices[1], vertices[0], vertices[3], vertices[2]},
                texU + length, texV + length, texU + length + width, texV + length + height, textureWidth, textureHeight);
        quads[5] = new MutatedQuad(new MutatedVertex[]{vertices[4], vertices[5], vertices[6], vertices[7]},
                texU + length + width + length, texV + length, texU + length + width + length + width, texV + length + height, textureWidth, textureHeight);

        if (mirrored)
        {
            for (MutatedQuad quad : quads)
            {
                if (quad != null) quad.flipFace();
            }
        }
    }

    public MutatedBox(ModelPart modelRenderer, int texU, int texV, float x, float y, float z, int width, int height, int length, float inflation, boolean mirrored)
    {
        this(modelRenderer, texU, texV, x, y, z, width, height, length, inflation, mirrored, (byte) 0b111111);
    }

    public MutatedBox(ModelPart modelRenderer, int texU, int texV, float x, float y, float z, int width, int height, int length, float inflation)
    {
        this(modelRenderer, texU, texV, x, y, z, width, height, length, inflation, modelRenderer != null && modelRenderer.mirror, (byte) 0b111111);
    }

    private MutatedQuad createQuadFromFace(MutatedVertex[] verts, BoxFactory.TextureFace face, float textureWidth, float textureHeight)
    {
        if (face == null)
        {
            return new MutatedQuad(verts);
        }

        int uSize = face.uSize;
        int vSize = face.vSize;

        if (face.faceRotation == FaceRotation.CLOCKWISE || face.faceRotation == FaceRotation.COUNTER_CLOCKWISE)
        {
            uSize = face.vSize;
            vSize = face.uSize;
        }

        MutatedQuad quad = new MutatedQuad(verts, face.uPos, face.vPos, face.uPos + uSize, face.vPos + vSize, textureWidth, textureHeight);
        applyFaceRotation(quad, face.faceRotation);

        return quad;
    }

    private void applyFaceRotation(MutatedQuad quad, FaceRotation rotation)
    {
        if (rotation == null || rotation == FaceRotation.IDENTITY)
            return;

        float[] uCoords = new float[]{
            quad.vertices[0].u,
            quad.vertices[1].u,
            quad.vertices[2].u,
            quad.vertices[3].u,
        };

        float[] vCoords = new float[]{
            quad.vertices[0].v,
            quad.vertices[1].v,
            quad.vertices[2].v,
            quad.vertices[3].v,
        };

        int offset = 2;
        if (rotation == FaceRotation.CLOCKWISE)
            offset = 3;
        else if (rotation == FaceRotation.COUNTER_CLOCKWISE)
            offset = 1;

        for (int i = 0; i < 4; ++i)
        {
            quad.vertices[i].u = uCoords[(i + offset) % 4];
            quad.vertices[i].v = vCoords[(i + offset) % 4];
        }
    }

    /**
     * Render this box to the given VertexConsumer.
     * This is the 1.20.1 rendering method replacing the old BufferBuilder approach.
     */
    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                      float red, float green, float blue, float alpha)
    {
        byte tempFlag = this.faceVisibilityFlag;
        PoseStack.Pose pose = poseStack.last();

        for (MutatedQuad quad : quads)
        {
            if (quad != null && (tempFlag & 1) == 1)
            {
                quad.render(pose, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            }
            tempFlag >>= 1;
        }
    }

    /**
     * Render at default scale - convenience method for compatibility.
     */
    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay)
    {
        render(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public boolean isFaceVisible(int faceIndex)
    {
        return ((faceVisibilityFlag >> faceIndex) & 1) == 1;
    }

    public AABBox createAABB()
    {
        return new AABBox(this.posX1, this.posY1, this.posZ1, this.posX2, this.posY2, this.posZ2);
    }
}
