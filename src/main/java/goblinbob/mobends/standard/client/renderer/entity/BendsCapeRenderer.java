package goblinbob.mobends.standard.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.player.IPlayerSkinProvider;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;
import goblinbob.mobends.standard.data.PlayerData;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BendsCapeRenderer
{

    public static final int MODEL_WIDTH = 10;
    public static final int MODEL_LENGTH = 16;
    public static final int MODEL_DEPTH = 1;
    public static final int SLAB_AMOUNT = 16;

    public static final ResourceLocation CAPE_TEXTURE = ModStatics.getResource("textures/cape.png");

    public Slab[] slabs;

    public BendsCapeRenderer()
    {
        this.slabs = new Slab[SLAB_AMOUNT];
        for (int i = 0; i < SLAB_AMOUNT; i++)
        {
            this.slabs[i] = new Slab(i * MODEL_LENGTH / SLAB_AMOUNT);
            if (i > 0)
                this.slabs[i - 1].setChildSlab(this.slabs[i]);
        }
        this.slabs[0].rotationPointY = 0;
    }

    public void applyAnimation(PlayerData playerData)
    {
        double phase = playerData.getCapeWavePhase();

        for (int i = 0; i < SLAB_AMOUNT; i++)
        {
            float waveSpeed = 0.2f;
            float waveFrequency = 7.2f;
            float waveOffset = ((float) i) / SLAB_AMOUNT;
            float magnitude = (80.0f / SLAB_AMOUNT * (0.7f + (float) i / SLAB_AMOUNT));
            this.slabs[i].setRotateAngle((float) (Math.cos((phase) * waveSpeed + waveOffset * waveFrequency) * magnitude));
        }
        this.slabs[0].rotate(-10.0f);
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay)
    {
        this.slabs[0].render(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F / 16.0F);
    }

    public void render(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource,
                       int packedLight, net.minecraft.client.player.AbstractClientPlayer player, float scale)
    {
        IPlayerSkinProvider skinProvider = IPlayerSkinProvider.Holder.getProvider();
        net.minecraft.resources.ResourceLocation capeTexture = skinProvider != null ? (ResourceLocation) skinProvider.getCapeTexture(player) : null;
        if (capeTexture != null)
        {
            net.minecraft.client.renderer.RenderType renderType =
                net.minecraft.client.renderer.RenderType.entitySolid(capeTexture);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
            int packedOverlay = net.minecraft.client.renderer.entity.LivingEntityRenderer.getOverlayCoords(player, 0.0F);
            this.slabs[0].render(poseStack, vertexConsumer, packedLight, packedOverlay, scale);
        }
    }

    static class Slab
    {

        float rotateAngle;

        public float textureWidth = 64;
        public float textureHeight = 32;
        private Slab childSlab;
        public boolean showModel;
        public boolean isHidden;
        public int offsetX;
        public int offsetY;
        public int offsetZ;
        public int rotationPointX;
        public int rotationPointY;
        public int rotationPointZ;
        public int hingeOffset = 0;

        private final CapeVertex[] vertexPositions;
        private final CapeQuad[] quadList;
        public final float posX1;
        public final float posY1;
        public final float posZ1;
        public final float posX2;
        public final float posY2;
        public final float posZ2;

        public Slab(int texV)
        {
            this.textureWidth = 64.0F;
            this.textureHeight = 32.0F;
            this.showModel = true;
            this.rotationPointX = 0;
            this.rotationPointY = MODEL_LENGTH / SLAB_AMOUNT;
            this.rotationPointZ = 0;
            this.offsetX = -MODEL_WIDTH / 2;
            this.offsetY = 0;
            this.offsetZ = 0;
            int slabLength = MODEL_LENGTH / SLAB_AMOUNT;
            this.posX1 = this.offsetX;
            this.posY1 = this.offsetY;
            this.posZ1 = this.offsetZ;
            this.posX2 = this.offsetX + MODEL_WIDTH;
            this.posY2 = this.offsetY + slabLength;
            this.posZ2 = this.offsetZ + MODEL_DEPTH;
            int texU = 0;

            this.vertexPositions = new CapeVertex[8];
            this.quadList = new CapeQuad[6];

            CapeVertex v0 = new CapeVertex(posX1, posY1, posZ1, 0.0F, 0.0F);
            CapeVertex v1 = new CapeVertex(posX2, posY1, posZ1, 0.0F, 8.0F);
            CapeVertex v2 = new CapeVertex(posX2, posY2, posZ1, 8.0F, 8.0F);
            CapeVertex v3 = new CapeVertex(posX1, posY2, posZ1, 8.0F, 0.0F);
            CapeVertex v4 = new CapeVertex(posX1, posY1, posZ2, 0.0F, 0.0F);
            CapeVertex v5 = new CapeVertex(posX2, posY1, posZ2, 0.0F, 8.0F);
            CapeVertex v6 = new CapeVertex(posX2, posY2, posZ2, 8.0F, 8.0F);
            CapeVertex v7 = new CapeVertex(posX1, posY2, posZ2, 8.0F, 0.0F);

            this.vertexPositions[0] = v0;
            this.vertexPositions[1] = v1;
            this.vertexPositions[2] = v2;
            this.vertexPositions[3] = v3;
            this.vertexPositions[4] = v4;
            this.vertexPositions[5] = v5;
            this.vertexPositions[6] = v6;
            this.vertexPositions[7] = v7;

            this.quadList[0] = new CapeQuad(new CapeVertex[] { v5, v1, v2, v6 },
                    texU + MODEL_DEPTH + MODEL_WIDTH, texV + MODEL_DEPTH,
                    texU + MODEL_DEPTH + MODEL_WIDTH + MODEL_DEPTH, texV + MODEL_DEPTH + slabLength,
                    textureWidth, textureHeight);
            this.quadList[1] = new CapeQuad(new CapeVertex[] { v0, v4, v7, v3 },
                    texU, texV + MODEL_DEPTH,
                    texU + MODEL_DEPTH, texV + MODEL_DEPTH + slabLength,
                    textureWidth, textureHeight);
            this.quadList[2] = new CapeQuad(new CapeVertex[] { v5, v4, v0, v1 },
                    texU + MODEL_DEPTH, texV,
                    texU + MODEL_DEPTH + MODEL_WIDTH, texV + MODEL_DEPTH,
                    textureWidth, textureHeight);
            this.quadList[3] = new CapeQuad(new CapeVertex[] { v2, v3, v7, v6 },
                    texU + MODEL_DEPTH + MODEL_WIDTH, texV + MODEL_DEPTH,
                    texU + MODEL_DEPTH + MODEL_WIDTH + MODEL_WIDTH, texV,
                    textureWidth, textureHeight);
            this.quadList[4] = new CapeQuad(new CapeVertex[] { v1, v0, v3, v2 },
                    texU + MODEL_DEPTH, texV + MODEL_DEPTH,
                    texU + MODEL_DEPTH + MODEL_WIDTH, texV + MODEL_DEPTH + slabLength,
                    textureWidth, textureHeight);
            this.quadList[5] = new CapeQuad(new CapeVertex[] { v4, v5, v6, v7 },
                    texU + MODEL_DEPTH + MODEL_WIDTH + MODEL_DEPTH, texV + MODEL_DEPTH,
                    texU + MODEL_DEPTH + MODEL_WIDTH + MODEL_DEPTH + MODEL_WIDTH, texV + MODEL_DEPTH + slabLength,
                    textureWidth, textureHeight);
        }

        public void rotate(float f)
        {
            this.setRotateAngle(this.rotateAngle + f);
        }

        public Slab setChildSlab(Slab slab)
        {
            this.childSlab = slab;
            return this;
        }

        public void setRotateAngle(float rotateAngle)
        {
            this.rotateAngle = rotateAngle;
            this.hingeOffset = this.rotateAngle < 0 ? MODEL_DEPTH : 0;
        }

        public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float scale)
        {
            if (!this.isHidden && this.showModel)
            {
                poseStack.pushPose();

                poseStack.translate(this.rotationPointX * scale, this.rotationPointY * scale, (this.rotationPointZ + this.hingeOffset) * scale);
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(this.rotateAngle));
                poseStack.translate(0, 0, -this.hingeOffset * scale);

                PoseStack.Pose pose = poseStack.last();
                Matrix4f matrix = pose.pose();
                Matrix3f normal = pose.normal();

                for (CapeQuad quad : this.quadList)
                {
                    quad.render(matrix, normal, vertexConsumer, packedLight, packedOverlay, scale);
                }

                if (this.childSlab != null)
                {
                    this.childSlab.render(poseStack, vertexConsumer, packedLight, packedOverlay, scale);
                }

                poseStack.popPose();
            }
        }
    }

    static class CapeVertex
    {
        public final float x, y, z;
        public float u, v;

        public CapeVertex(float x, float y, float z, float u, float v)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
        }

        public CapeVertex remap(float u, float v)
        {
            return new CapeVertex(this.x, this.y, this.z, u, v);
        }
    }

    static class CapeQuad
    {
        public final CapeVertex[] vertices;
        public final float normalX, normalY, normalZ;

        public CapeQuad(CapeVertex[] vertices, int u1, int v1, int u2, int v2, float texWidth, float texHeight)
        {
            this.vertices = new CapeVertex[4];
            float uScale = 1.0F / texWidth;
            float vScale = 1.0F / texHeight;

            this.vertices[0] = vertices[0].remap(u2 * uScale, v1 * vScale);
            this.vertices[1] = vertices[1].remap(u1 * uScale, v1 * vScale);
            this.vertices[2] = vertices[2].remap(u1 * uScale, v2 * vScale);
            this.vertices[3] = vertices[3].remap(u2 * uScale, v2 * vScale);

            Vector3f v0 = new Vector3f(vertices[1].x - vertices[0].x, vertices[1].y - vertices[0].y, vertices[1].z - vertices[0].z);
            Vector3f v1Vec = new Vector3f(vertices[2].x - vertices[0].x, vertices[2].y - vertices[0].y, vertices[2].z - vertices[0].z);
            Vector3f normal = v0.cross(v1Vec).normalize();
            this.normalX = normal.x;
            this.normalY = normal.y;
            this.normalZ = normal.z;
        }

        private static final Vector3f transformedNormal = new Vector3f();
        private static final Vector3f transformedPosition = new Vector3f();

        public void render(Matrix4f matrix, Matrix3f normalMatrix, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float scale)
        {
            transformedNormal.set(normalX, normalY, normalZ).mul(normalMatrix);

            IEntityVertexHelper vertexHelper = IEntityVertexHelper.Holder.getHelper();

            for (CapeVertex vertex : this.vertices)
            {
                transformedPosition.set(vertex.x * scale, vertex.y * scale, vertex.z * scale).mulPosition(matrix);

                int color = 0xFFFFFFFF;
                vertexHelper.emitVertex(vertexConsumer, transformedPosition.x, transformedPosition.y, transformedPosition.z,
                        color, vertex.u, vertex.v,
                        packedOverlay, packedLight,
                        transformedNormal.x, transformedNormal.y, transformedNormal.z);
            }
        }
    }
}
