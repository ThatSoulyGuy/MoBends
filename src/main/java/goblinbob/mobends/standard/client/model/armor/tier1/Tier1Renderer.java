package goblinbob.mobends.standard.client.model.armor.tier1;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;
import goblinbob.mobends.api.rendering.IModelRenderHelper;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.util.GlHelper;
import goblinbob.mobends.standard.client.model.armor.ArmorPoseHelper;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderContext;
import goblinbob.mobends.standard.client.model.armor.CapturedVertex;
import goblinbob.mobends.standard.client.model.armor.CapturingVertexConsumer;
import goblinbob.mobends.standard.client.model.armor.JointDefinitions;
import goblinbob.mobends.standard.client.model.armor.JointPlane;
import goblinbob.mobends.standard.client.model.armor.LimbInflation;
import goblinbob.mobends.standard.client.model.armor.QuadSlicer;
import goblinbob.mobends.standard.client.model.armor.SliceResult;
import goblinbob.mobends.standard.client.model.armor.cache.CacheManager;
import goblinbob.mobends.standard.client.model.armor.tier.RenderTier;
import goblinbob.mobends.standard.client.model.armor.tier2.Tier2Renderer;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
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

    private long renderCount = 0;
    private long fallbackCount = 0;

    private int currentArmorColor = 0xFFFFFFFF;

    public Tier1Renderer()
    {
        this.tier2Fallback = new Tier2Renderer();
    }

    public RenderTier getTier()
    {
        return RenderTier.TIER_1_TRANSFORM_INJECTION;
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
            fallbackCount++;
            tier2Fallback.render(context, model, texture, renderTypeProvider);
            return;
        }

        if (context.getEntityData() == null)
        {
            renderVanilla(context, humanoidModel, texture, renderTypeProvider);
            return;
        }

        renderCount++;

        currentArmorColor = context.getArmorColor();

        BipedEntityData<?> entityData = context.getEntityData();
        PoseStack poseStack = context.getPoseStack();
        MultiBufferSource bufferSource = context.getBufferSource();
        EquipmentSlot slot = context.getSlot();

        configureVisibility(humanoidModel, slot);

        RenderType renderType = renderTypeProvider.apply(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);


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

        CacheManager.getInstance().recordCacheAssistedRender();
    }

    private <E extends LivingEntity> void renderInternal(
            ArmorRenderContext<E> context,
            HumanoidModel<?> humanoidModel,
            VertexConsumer vertexConsumer)
    {
        if (context.getEntityData() == null)
        {
            return;
        }

        renderCount++;

        currentArmorColor = context.getArmorColor();

        BipedEntityData<?> entityData = context.getEntityData();
        PoseStack poseStack = context.getPoseStack();
        EquipmentSlot slot = context.getSlot();

        configureVisibility(humanoidModel, slot);


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

        CacheManager.getInstance().recordCacheAssistedRender();
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

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        for (CapturedVertex v : vertices)
        {
            float tx = matrix.m00() * v.x + matrix.m10() * v.y + matrix.m20() * v.z + matrix.m30();
            float ty = matrix.m01() * v.x + matrix.m11() * v.y + matrix.m21() * v.z + matrix.m31();
            float tz = matrix.m02() * v.x + matrix.m12() * v.y + matrix.m22() * v.z + matrix.m32();

            float nx = normal.m00() * v.normalX + normal.m10() * v.normalY + normal.m20() * v.normalZ;
            float ny = normal.m01() * v.normalX + normal.m11() * v.normalY + normal.m21() * v.normalZ;
            float nz = normal.m02() * v.normalX + normal.m12() * v.normalY + normal.m22() * v.normalZ;

            float tintR = ((currentArmorColor >> 16) & 0xFF) / 255.0F;
            float tintG = ((currentArmorColor >> 8) & 0xFF) / 255.0F;
            float tintB = (currentArmorColor & 0xFF) / 255.0F;
            float tintA = ((currentArmorColor >> 24) & 0xFF) / 255.0F;

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

        renderSplitArm(poseStack, vertexConsumer, model, entityData, true, packedLight, packedOverlay, isSlimArms);

        renderSplitArm(poseStack, vertexConsumer, model, entityData, false, packedLight, packedOverlay, isSlimArms);

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

        renderSplitLeg(poseStack, vertexConsumer, model, entityData, true, packedLight, packedOverlay);

        renderSplitLeg(poseStack, vertexConsumer, model, entityData, false, packedLight, packedOverlay);

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

        renderSplitLeg(poseStack, vertexConsumer, model, entityData, true, packedLight, packedOverlay);

        renderSplitLeg(poseStack, vertexConsumer, model, entityData, false, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private static final boolean ENABLE_LIMB_SLICING = true;

    private static final float SLIM_ARM_Y_OFFSET = 0.5f * ArmorPoseHelper.SCALE;

    private static final float ARM_INFLATION = LimbInflation.ARM_INFLATION;
    private static final float LEG_INFLATION = LimbInflation.LEG_INFLATION;
    private static final float LOWER_LIMB_INFLATION_STEP = LimbInflation.LOWER_LIMB_INFLATION_STEP;

    private void renderSplitArm(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            HumanoidModel<?> model,
            BipedEntityData<?> entityData,
            boolean isLeft,
            int packedLight,
            int packedOverlay,
            boolean isSlimArms)
    {
        ModelPart armPart = isLeft ? model.leftArm : model.rightArm;
        if (armPart == null || !armPart.visible)
        {
            return;
        }

        ModelPartTransform upperArm = isLeft ? entityData.leftArm : entityData.rightArm;

        if (!ENABLE_LIMB_SLICING)
        {
            poseStack.pushPose();
            ArmorPoseHelper.applyPartTransform(poseStack, entityData.body, true);
            ArmorPoseHelper.applyPartTransform(poseStack, upperArm, true);
            if (isSlimArms)
            {
                poseStack.translate(0, -SLIM_ARM_Y_OFFSET, 0);
            }
            ArmorPoseHelper.renderPartAtOrigin(armPart, poseStack, vertexConsumer, packedLight, packedOverlay);
            poseStack.popPose();
            return;
        }

        ModelPartTransform foreArm = isLeft ? entityData.leftForeArm : entityData.rightForeArm;
        JointPlane elbowPlane = JointDefinitions.getElbow(isLeft);

        limbCapture.clear();
        PoseStack captureStack = new PoseStack();
        resetPartToOrigin(armPart);
        armPart.render(captureStack, limbCapture, packedLight, packedOverlay);
        restorePartFromCapture(armPart);

        List<CapturedVertex> vertices = limbCapture.getVertices();
        if (vertices.isEmpty())
        {
            return;
        }

        List<CapturedVertex[]> quads = ArmorPoseHelper.groupIntoQuads(vertices);
        List<SliceResult> sliceResults = quadSlicer.sliceAll(quads, elbowPlane);

        LimbInflation upperInflation = LimbInflation.of(vertices, ARM_INFLATION);
        LimbInflation lowerInflation = upperInflation.plus(LOWER_LIMB_INFLATION_STEP);

        float slimArmOffset = isSlimArms ? -SLIM_ARM_Y_OFFSET : 0;

        poseStack.pushPose();
        ArmorPoseHelper.applyPartTransform(poseStack, entityData.body, true);
        ArmorPoseHelper.applyPartTransform(poseStack, upperArm, true);
        ArmorPoseHelper.renderSlicedVertices(poseStack, vertexConsumer, sliceResults, true, 0, slimArmOffset, 0, packedLight, packedOverlay, currentArmorColor, upperInflation);
        poseStack.popPose();

        float foreArmOffsetX = -foreArm.position.x * ArmorPoseHelper.SCALE;
        float foreArmOffsetY = -foreArm.position.y * ArmorPoseHelper.SCALE + slimArmOffset;
        float foreArmOffsetZ = -foreArm.position.z * ArmorPoseHelper.SCALE;
        poseStack.pushPose();
        ArmorPoseHelper.applyPartTransform(poseStack, entityData.body, true);
        ArmorPoseHelper.applyPartTransform(poseStack, upperArm, true);
        ArmorPoseHelper.applyPartTransform(poseStack, foreArm, true);
        ArmorPoseHelper.renderSlicedVertices(poseStack, vertexConsumer, sliceResults, false, foreArmOffsetX, foreArmOffsetY, foreArmOffsetZ, packedLight, packedOverlay, currentArmorColor, lowerInflation);
        poseStack.popPose();
    }

    private void renderSplitLeg(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            HumanoidModel<?> model,
            BipedEntityData<?> entityData,
            boolean isLeft,
            int packedLight,
            int packedOverlay)
    {
        ModelPart legPart = isLeft ? model.leftLeg : model.rightLeg;
        if (legPart == null || !legPart.visible)
        {
            return;
        }

        ModelPartTransform upperLeg = isLeft ? entityData.leftLeg : entityData.rightLeg;

        float vanillaLegX = legPart.x;

        if (!ENABLE_LIMB_SLICING)
        {
            poseStack.pushPose();
            ArmorPoseHelper.applyLegTransform(poseStack, upperLeg, vanillaLegX);
            ArmorPoseHelper.renderPartAtOrigin(legPart, poseStack, vertexConsumer, packedLight, packedOverlay);
            poseStack.popPose();
            return;
        }

        ModelPartTransform lowerLeg = isLeft ? entityData.leftForeLeg : entityData.rightForeLeg;
        JointPlane kneePlane = JointDefinitions.getKnee(isLeft);

        limbCapture.clear();
        PoseStack captureStack = new PoseStack();
        resetPartToOrigin(legPart);
        legPart.render(captureStack, limbCapture, packedLight, packedOverlay);
        restorePartFromCapture(legPart);

        List<CapturedVertex> vertices = limbCapture.getVertices();
        if (vertices.isEmpty())
        {
            return;
        }

        List<CapturedVertex[]> quads = ArmorPoseHelper.groupIntoQuads(vertices);
        List<SliceResult> sliceResults = quadSlicer.sliceAll(quads, kneePlane);


        LimbInflation upperInflation = LimbInflation.of(vertices, LEG_INFLATION);
        LimbInflation lowerInflation = LimbInflation.of(vertices, LEG_INFLATION + LOWER_LIMB_INFLATION_STEP);

        poseStack.pushPose();
        ArmorPoseHelper.applyLegTransform(poseStack, upperLeg, vanillaLegX);
        ArmorPoseHelper.renderSlicedVertices(poseStack, vertexConsumer, sliceResults, true, 0, 0, 0, packedLight, packedOverlay, currentArmorColor, upperInflation);
        poseStack.popPose();

        float lowerLegOffsetX = -lowerLeg.position.x * ArmorPoseHelper.SCALE;
        float lowerLegOffsetY = -lowerLeg.position.y * ArmorPoseHelper.SCALE;
        float lowerLegOffsetZ = -lowerLeg.position.z * ArmorPoseHelper.SCALE;
        poseStack.pushPose();
        ArmorPoseHelper.applyLegTransform(poseStack, upperLeg, vanillaLegX);
        ArmorPoseHelper.applyPartTransform(poseStack, lowerLeg, true);
        ArmorPoseHelper.renderSlicedVertices(poseStack, vertexConsumer, sliceResults, false, lowerLegOffsetX, lowerLegOffsetY, lowerLegOffsetZ, packedLight, packedOverlay, currentArmorColor, lowerInflation);
        poseStack.popPose();
    }

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

        ModelPartTransform body = entityData.body;
        if (body == null)
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

        float scale = 1.0f / 16.0f;

        if (body.globalOffset.x != 0 || body.globalOffset.y != 0 || body.globalOffset.z != 0)
        {
            poseStack.translate(
                body.globalOffset.x * scale,
                body.globalOffset.y * scale,
                body.globalOffset.z * scale
            );
        }

        float offsetScale = body.offsetScale;

        poseStack.translate(
            body.position.x * scale * offsetScale,
            body.position.y * scale * offsetScale,
            body.position.z * scale * offsetScale
        );

        if (body.offset.x != 0 || body.offset.y != 0 || body.offset.z != 0)
        {
            poseStack.translate(
                body.offset.x * scale * offsetScale,
                body.offset.y * scale * offsetScale,
                body.offset.z * scale * offsetScale
            );
        }

        GlHelper.rotate(poseStack, body.rotation.getSmooth());

        poseStack.translate(
            -body.position.x * scale * offsetScale,
            -body.position.y * scale * offsetScale,
            -body.position.z * scale * offsetScale
        );

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

        poseStack.popPose();
    }

    private void configureVisibility(HumanoidModel<?> model, EquipmentSlot slot)
    {
        model.head.visible = false;
        model.hat.visible = false;
        model.body.visible = false;
        model.rightArm.visible = false;
        model.leftArm.visible = false;
        model.rightLeg.visible = false;
        model.leftLeg.visible = false;

        switch (slot)
        {
            case HEAD:
                model.head.visible = true;
                model.hat.visible = true;
                break;
            case CHEST:
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
                break;
            case LEGS:
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
                break;
            case FEET:
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
                break;
        }
    }

    private <E extends LivingEntity> void renderVanilla(
            ArmorRenderContext<E> context,
            HumanoidModel<?> model,
            ResourceLocation texture,
            Function<ResourceLocation, RenderType> renderTypeProvider)
    {
        PoseStack poseStack = context.getPoseStack();
        MultiBufferSource bufferSource = context.getBufferSource();

        poseStack.pushPose();

        configureVisibility(model, context.getSlot());

        RenderType renderType = renderTypeProvider.apply(texture);
        IModelRenderHelper.Holder.getHelper().renderModelToBuffer(
                model,
                poseStack,
                bufferSource.getBuffer(renderType),
                context.getPackedLight(),
                context.getPackedOverlay(),
                0xFFFFFFFF
        );

        poseStack.popPose();
    }

    public Tier2Renderer getTier2Fallback()
    {
        return tier2Fallback;
    }

    public long getRenderCount()
    {
        return renderCount;
    }

    public long getFallbackCount()
    {
        return fallbackCount;
    }

    public float getFallbackRate()
    {
        return renderCount > 0 ? (float) fallbackCount / renderCount : 0;
    }

    public void resetStats()
    {
        renderCount = 0;
        fallbackCount = 0;
    }

    public String getStats()
    {
        return String.format("Tier1Renderer: %d renders, %d fallbacks (%.1f%% fallback rate)",
                renderCount, fallbackCount, getFallbackRate() * 100);
    }

    private static class PartStateStorage
    {
        private float headX, headY, headZ, headXRot, headYRot, headZRot;
        private float hatX, hatY, hatZ, hatXRot, hatYRot, hatZRot;
        private float bodyX, bodyY, bodyZ, bodyXRot, bodyYRot, bodyZRot;
        private float leftArmX, leftArmY, leftArmZ, leftArmXRot, leftArmYRot, leftArmZRot;
        private float rightArmX, rightArmY, rightArmZ, rightArmXRot, rightArmYRot, rightArmZRot;
        private float leftLegX, leftLegY, leftLegZ, leftLegXRot, leftLegYRot, leftLegZRot;
        private float rightLegX, rightLegY, rightLegZ, rightLegXRot, rightLegYRot, rightLegZRot;

        void store(HumanoidModel<?> model)
        {
            storePart(model.head);
            headX = model.head.x; headY = model.head.y; headZ = model.head.z;
            headXRot = model.head.xRot; headYRot = model.head.yRot; headZRot = model.head.zRot;

            hatX = model.hat.x; hatY = model.hat.y; hatZ = model.hat.z;
            hatXRot = model.hat.xRot; hatYRot = model.hat.yRot; hatZRot = model.hat.zRot;

            bodyX = model.body.x; bodyY = model.body.y; bodyZ = model.body.z;
            bodyXRot = model.body.xRot; bodyYRot = model.body.yRot; bodyZRot = model.body.zRot;

            leftArmX = model.leftArm.x; leftArmY = model.leftArm.y; leftArmZ = model.leftArm.z;
            leftArmXRot = model.leftArm.xRot; leftArmYRot = model.leftArm.yRot; leftArmZRot = model.leftArm.zRot;

            rightArmX = model.rightArm.x; rightArmY = model.rightArm.y; rightArmZ = model.rightArm.z;
            rightArmXRot = model.rightArm.xRot; rightArmYRot = model.rightArm.yRot; rightArmZRot = model.rightArm.zRot;

            leftLegX = model.leftLeg.x; leftLegY = model.leftLeg.y; leftLegZ = model.leftLeg.z;
            leftLegXRot = model.leftLeg.xRot; leftLegYRot = model.leftLeg.yRot; leftLegZRot = model.leftLeg.zRot;

            rightLegX = model.rightLeg.x; rightLegY = model.rightLeg.y; rightLegZ = model.rightLeg.z;
            rightLegXRot = model.rightLeg.xRot; rightLegYRot = model.rightLeg.yRot; rightLegZRot = model.rightLeg.zRot;
        }

        void restore(HumanoidModel<?> model)
        {
            model.head.x = headX; model.head.y = headY; model.head.z = headZ;
            model.head.xRot = headXRot; model.head.yRot = headYRot; model.head.zRot = headZRot;

            model.hat.x = hatX; model.hat.y = hatY; model.hat.z = hatZ;
            model.hat.xRot = hatXRot; model.hat.yRot = hatYRot; model.hat.zRot = hatZRot;

            model.body.x = bodyX; model.body.y = bodyY; model.body.z = bodyZ;
            model.body.xRot = bodyXRot; model.body.yRot = bodyYRot; model.body.zRot = bodyZRot;

            model.leftArm.x = leftArmX; model.leftArm.y = leftArmY; model.leftArm.z = leftArmZ;
            model.leftArm.xRot = leftArmXRot; model.leftArm.yRot = leftArmYRot; model.leftArm.zRot = leftArmZRot;

            model.rightArm.x = rightArmX; model.rightArm.y = rightArmY; model.rightArm.z = rightArmZ;
            model.rightArm.xRot = rightArmXRot; model.rightArm.yRot = rightArmYRot; model.rightArm.zRot = rightArmZRot;

            model.leftLeg.x = leftLegX; model.leftLeg.y = leftLegY; model.leftLeg.z = leftLegZ;
            model.leftLeg.xRot = leftLegXRot; model.leftLeg.yRot = leftLegYRot; model.leftLeg.zRot = leftLegZRot;

            model.rightLeg.x = rightLegX; model.rightLeg.y = rightLegY; model.rightLeg.z = rightLegZ;
            model.rightLeg.xRot = rightLegXRot; model.rightLeg.yRot = rightLegYRot; model.rightLeg.zRot = rightLegZRot;
        }

        private void storePart(ModelPart part)
        {
        }
    }
}
