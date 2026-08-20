package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.standard.main.ModStatics;
import goblinbob.mobends.standard.mutators.WolfMutator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class LayerWolfMisc
{

    private static final int TEXTURE_WIDTH = 8;
    private static final int TEXTURE_HEIGHT = 8;
    private static final float MODEL_SCALE = 1.0F / 16.0F;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MOUTH_INSIDE_ROTATION = 90;

    private static ResourceLocation texture;

    private LayerWolfMisc()
    {
    }

    private static ResourceLocation getTexture()
    {
        if (texture == null)
        {
            texture = ModStatics.getResource("textures/entity/wolf_misc.png");
        }
        return texture;
    }

    public static void render(PoseStack poseStack, WolfMutator mutator, MultiBufferSource bufferSource,
                              int packedLight, int packedOverlay)
    {
        if (mutator.wolfHeadMain == null || bufferSource == null)
        {
            return;
        }

        final VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTexture()));

        poseStack.pushPose();
        mutator.wolfHeadMain.applyCharacterTransformPoseStack(poseStack);

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(MOUTH_INSIDE_ROTATION));
        emitPlane(poseStack, vertexConsumer, packedLight, packedOverlay,
                -1.5F, -4.1F, -3.0F, 3.0F, 1.0F, true, 0, 0, 3, 4);
        poseStack.popPose();

        renderAttached(poseStack, vertexConsumer, packedLight, packedOverlay, mutator.nose,
                -1.5F, 1.0F, -4.0F, 3.0F, 4.0F, false, 0, 0, 3, 4);

        renderAttached(poseStack, vertexConsumer, packedLight, packedOverlay, mutator.mouth,
                -1.5F, 0.0F, -4.0F, 3.0F, 4.0F, true, 0, 0, 3, 4);

        renderAttached(poseStack, vertexConsumer, packedLight, packedOverlay, mutator.tongue,
                -1.5F, 0.0F, -4.0F, 3.0F, 6.0F, true, 3, 0, 6, 6);

        poseStack.popPose();
    }

    private static void renderAttached(PoseStack poseStack, VertexConsumer vertexConsumer,
                                       int packedLight, int packedOverlay, BendsModelPart part,
                                       float minX, float y, float minZ, float width, float length,
                                       boolean facingUp, int u0, int v0, int u1, int v1)
    {
        if (part == null)
        {
            return;
        }

        poseStack.pushPose();
        part.applyPreTransformPoseStack(poseStack);
        part.applyLocalTransformPoseStack(poseStack);
        emitPlane(poseStack, vertexConsumer, packedLight, packedOverlay,
                minX, y, minZ, width, length, facingUp, u0, v0, u1, v1);
        poseStack.popPose();
    }

    private static void emitPlane(PoseStack poseStack, VertexConsumer vertexConsumer,
                                  int packedLight, int packedOverlay,
                                  float minX, float y, float minZ, float width, float length,
                                  boolean facingUp, int u0, int v0, int u1, int v1)
    {
        final Matrix4f matrix = poseStack.last().pose();
        final Matrix3f normalMatrix = poseStack.last().normal();

        final float maxX = (minX + width) * MODEL_SCALE;
        final float maxZ = (minZ + length) * MODEL_SCALE;
        final float x = minX * MODEL_SCALE;
        final float z = minZ * MODEL_SCALE;
        final float height = y * MODEL_SCALE;

        final float minU = (float) u0 / TEXTURE_WIDTH;
        final float maxU = (float) u1 / TEXTURE_WIDTH;
        final float minV = (float) v0 / TEXTURE_HEIGHT;
        final float maxV = (float) v1 / TEXTURE_HEIGHT;

        final Vector3f normal = new Vector3f(0.0F, facingUp ? -1.0F : 1.0F, 0.0F);
        normal.mul(normalMatrix);

        emitVertex(vertexConsumer, matrix, normal, x, height, maxZ, minU, minV, packedLight, packedOverlay);
        emitVertex(vertexConsumer, matrix, normal, x, height, z, minU, maxV, packedLight, packedOverlay);
        emitVertex(vertexConsumer, matrix, normal, maxX, height, z, maxU, maxV, packedLight, packedOverlay);
        emitVertex(vertexConsumer, matrix, normal, maxX, height, maxZ, maxU, minV, packedLight, packedOverlay);
    }

    private static void emitVertex(VertexConsumer vertexConsumer, Matrix4f matrix, Vector3f normal,
                                   float x, float y, float z, float u, float v,
                                   int packedLight, int packedOverlay)
    {
        final float tx = matrix.m00() * x + matrix.m10() * y + matrix.m20() * z + matrix.m30();
        final float ty = matrix.m01() * x + matrix.m11() * y + matrix.m21() * z + matrix.m31();
        final float tz = matrix.m02() * x + matrix.m12() * y + matrix.m22() * z + matrix.m32();

        IEntityVertexHelper.Holder.getHelper().emitVertex(vertexConsumer,
                tx, ty, tz,
                WHITE,
                u, v,
                packedOverlay, packedLight,
                normal.x(), normal.y(), normal.z());
    }

}
