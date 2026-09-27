package goblinbob.mobends.standard.client.model.armor.tier1;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;
import goblinbob.mobends.api.rendering.IModelRenderHelper;
import goblinbob.mobends.standard.client.model.armor.ArmorPoseHelper;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderContext;
import goblinbob.mobends.standard.client.model.armor.CapturedVertex;
import goblinbob.mobends.standard.client.model.armor.CapturingVertexConsumer;
import goblinbob.mobends.standard.client.model.armor.QuadSlicer;
import goblinbob.mobends.standard.client.model.armor.tier2.Tier2Renderer;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.Function;

public class Tier1Renderer
{
    private final Tier2Renderer tier2Fallback;

    private final CapturingVertexConsumer limbCapture = new CapturingVertexConsumer();
    private final QuadSlicer quadSlicer = new QuadSlicer();

    private int currentArmorColor = 0xFFFFFFFF;

    public Tier1Renderer()
    {
        this.tier2Fallback = new Tier2Renderer();
    }

    public <E extends LivingEntity> boolean renderWithTexture(
            ArmorRenderContext<E> context,
            HumanoidModel<?> model,
            ResourceLocation texture,
            boolean hasFoil)
    {
        if (context == null || model == null || context.getEntityData() == null || texture == null)
        {
            return false;
        }

        try
        {
            renderWithFoil(context, model, texture, hasFoil);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public <E extends LivingEntity> boolean renderWithConsumer(
            ArmorRenderContext<E> context,
            HumanoidModel<?> model,
            VertexConsumer vertexConsumer)
    {
        if (context == null || model == null || vertexConsumer == null || context.getEntityData() == null)
        {
            return false;
        }

        try
        {
            renderInternal(context, model, vertexConsumer);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private <E extends LivingEntity> void renderWithFoil(
            ArmorRenderContext<E> context,
            HumanoidModel<?> model,
            ResourceLocation texture,
            boolean hasFoil)
    {
        VertexConsumer vertexConsumer = (VertexConsumer) IModelRenderHelper.Holder.getHelper().getArmorFoilBuffer(
                context.getBufferSource(),
                RenderType.armorCutoutNoCull(texture),
                hasFoil);

        renderInternal(context, model, vertexConsumer);
    }

    public <E extends LivingEntity> void render(
            ArmorRenderContext<E> context,
            Model model,
            ResourceLocation texture,
            Function<ResourceLocation, RenderType> renderTypeProvider)
    {
        if (!(model instanceof HumanoidModel<?> humanoidModel))
        {
            tier2Fallback.render(context, model, texture, renderTypeProvider);
            return;
        }

        renderInternal(context, humanoidModel, context.getBufferSource().getBuffer(renderTypeProvider.apply(texture)));
    }

    private <E extends LivingEntity> void renderInternal(
            ArmorRenderContext<E> context,
            HumanoidModel<?> humanoidModel,
            VertexConsumer vertexConsumer)
    {
        currentArmorColor = context.getArmorColor();

        BipedEntityData<?> entityData = context.getEntityData();
        PoseStack poseStack = context.getPoseStack();
        EquipmentSlot slot = context.getSlot();

        humanoidModel.head.visible = false;
        humanoidModel.hat.visible = false;
        humanoidModel.body.visible = false;
        humanoidModel.rightArm.visible = false;
        humanoidModel.leftArm.visible = false;
        humanoidModel.rightLeg.visible = false;
        humanoidModel.leftLeg.visible = false;
        ArmorPoseHelper.showSlotParts(humanoidModel, slot);


        boolean isSlimArms = context.isSlimArms();

        switch (slot)
        {
            case HEAD:
                renderHead(poseStack, vertexConsumer, humanoidModel, entityData, context.getPackedLight(), context.getPackedOverlay());
                break;
            case CHEST:
                renderChest(poseStack, vertexConsumer, humanoidModel, entityData, context.getPackedLight(), context.getPackedOverlay(), isSlimArms);
                break;
            case LEGS:
                renderLegs(poseStack, vertexConsumer, humanoidModel, entityData, context.getPackedLight(), context.getPackedOverlay());
                break;
            case FEET:
                renderFeet(poseStack, vertexConsumer, humanoidModel, entityData, context.getPackedLight(), context.getPackedOverlay());
                break;
        }
    }

    private void renderHead(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            HumanoidModel<?> model,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay)
    {
        poseStack.pushPose();

        if (model.head != null && model.head.visible)
        {
            renderCapturedPart(poseStack, vertexConsumer, model.head, entityData, true, packedLight, packedOverlay);
        }

        if (model.hat != null && model.hat.visible)
        {
            renderCapturedPart(poseStack, vertexConsumer, model.hat, entityData, true, packedLight, packedOverlay);
        }

        poseStack.popPose();
    }

    private void renderCapturedPart(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart part,
            BipedEntityData<?> entityData,
            boolean isHead,
            int packedLight,
            int packedOverlay)
    {
        if (part == null || !part.visible)
        {
            return;
        }

        limbCapture.clear();
        PoseStack captureStack = new PoseStack();
        resetPartToOrigin(part);
        part.render(captureStack, limbCapture, packedLight, packedOverlay);
        restorePartFromCapture(part);

        List<CapturedVertex> vertices = limbCapture.getVertices();
        if (vertices.isEmpty())
        {
            return;
        }

        poseStack.pushPose();
        ArmorPoseHelper.applyPartTransform(poseStack, entityData.body, true);

        if (isHead)
        {
            ArmorPoseHelper.applyPartTransform(poseStack, entityData.head, true);
        }

        emitCaptured(poseStack, vertices, vertexConsumer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void renderChest(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            HumanoidModel<?> model,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay,
            boolean isSlimArms)
    {
        poseStack.pushPose();

        if (model.body != null && model.body.visible)
        {
            renderBodyWithPivotRotation(poseStack, vertexConsumer, model.body, entityData, packedLight, packedOverlay);
        }

        float slimArmOffset = isSlimArms ? -SLIM_ARM_Y_OFFSET : 0;

        ArmorPoseHelper.renderSplitArm(poseStack, vertexConsumer, model.leftArm, entityData, true, packedLight, packedOverlay, slimArmOffset, currentArmorColor, limbCapture, quadSlicer);

        ArmorPoseHelper.renderSplitArm(poseStack, vertexConsumer, model.rightArm, entityData, false, packedLight, packedOverlay, slimArmOffset, currentArmorColor, limbCapture, quadSlicer);

        poseStack.popPose();
    }

    private void renderLegs(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            HumanoidModel<?> model,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay)
    {
        poseStack.pushPose();

        if (model.body != null && model.body.visible)
        {
            renderBodyWithPivotRotation(poseStack, vertexConsumer, model.body, entityData, packedLight, packedOverlay);
        }

        ArmorPoseHelper.renderSplitLeg(poseStack, vertexConsumer, model.leftLeg, entityData, true, packedLight, packedOverlay, currentArmorColor, limbCapture, quadSlicer);

        ArmorPoseHelper.renderSplitLeg(poseStack, vertexConsumer, model.rightLeg, entityData, false, packedLight, packedOverlay, currentArmorColor, limbCapture, quadSlicer);

        poseStack.popPose();
    }

    private void renderFeet(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            HumanoidModel<?> model,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay)
    {
        poseStack.pushPose();

        ArmorPoseHelper.renderSplitLeg(poseStack, vertexConsumer, model.leftLeg, entityData, true, packedLight, packedOverlay, currentArmorColor, limbCapture, quadSlicer);

        ArmorPoseHelper.renderSplitLeg(poseStack, vertexConsumer, model.rightLeg, entityData, false, packedLight, packedOverlay, currentArmorColor, limbCapture, quadSlicer);

        poseStack.popPose();
    }

    private static final float SLIM_ARM_Y_OFFSET = 0.5f * ArmorPoseHelper.SCALE;

    private float capturedX, capturedY, capturedZ;
    private float capturedXRot, capturedYRot, capturedZRot;

    private void resetPartToOrigin(ModelPart part)
    {
        capturedX = part.x;
        capturedY = part.y;
        capturedZ = part.z;
        capturedXRot = part.xRot;
        capturedYRot = part.yRot;
        capturedZRot = part.zRot;

        part.x = 0;
        part.y = 0;
        part.z = 0;
        part.xRot = 0;
        part.yRot = 0;
        part.zRot = 0;
    }

    private void restorePartFromCapture(ModelPart part)
    {
        part.x = capturedX;
        part.y = capturedY;
        part.z = capturedZ;
        part.xRot = capturedXRot;
        part.yRot = capturedYRot;
        part.zRot = capturedZRot;
    }

    private void renderBodyWithPivotRotation(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart part,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay)
    {
        if (part == null || !part.visible)
        {
            return;
        }

        if (entityData.body == null)
        {
            return;
        }

        limbCapture.clear();
        PoseStack captureStack = new PoseStack();
        resetPartToOrigin(part);
        part.render(captureStack, limbCapture, packedLight, packedOverlay);
        restorePartFromCapture(part);

        List<CapturedVertex> vertices = limbCapture.getVertices();
        if (vertices.isEmpty())
        {
            return;
        }

        poseStack.pushPose();
        ArmorPoseHelper.applyBodyTransformWithPivot(poseStack, entityData);
        emitCaptured(poseStack, vertices, vertexConsumer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private void emitCaptured(
            PoseStack poseStack,
            List<CapturedVertex> vertices,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay)
    {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        float tintR = ((currentArmorColor >> 16) & 0xFF) / 255.0F;
        float tintG = ((currentArmorColor >> 8) & 0xFF) / 255.0F;
        float tintB = (currentArmorColor & 0xFF) / 255.0F;
        float tintA = ((currentArmorColor >> 24) & 0xFF) / 255.0F;

        for (CapturedVertex v : vertices)
        {
            float tx = matrix.m00() * v.x + matrix.m10() * v.y + matrix.m20() * v.z + matrix.m30();
            float ty = matrix.m01() * v.x + matrix.m11() * v.y + matrix.m21() * v.z + matrix.m31();
            float tz = matrix.m02() * v.x + matrix.m12() * v.y + matrix.m22() * v.z + matrix.m32();

            float nx = normal.m00() * v.normalX + normal.m10() * v.normalY + normal.m20() * v.normalZ;
            float ny = normal.m01() * v.normalX + normal.m11() * v.normalY + normal.m21() * v.normalZ;
            float nz = normal.m02() * v.normalX + normal.m12() * v.normalY + normal.m22() * v.normalZ;

            int color = ((int)(v.alpha * tintA * 255.0F) << 24) |
                        ((int)(v.red * tintR * 255.0F) << 16) |
                        ((int)(v.green * tintG * 255.0F) << 8) |
                        (int)(v.blue * tintB * 255.0F);
            IEntityVertexHelper.Holder.getHelper().emitVertex(vertexConsumer,
                    tx, ty, tz,
                    color,
                    v.u, v.v,
                    packedOverlay, packedLight,
                    nx, ny, nz);
        }
    }
}
