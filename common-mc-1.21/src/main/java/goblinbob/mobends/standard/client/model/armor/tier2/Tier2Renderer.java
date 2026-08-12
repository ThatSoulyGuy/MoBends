package goblinbob.mobends.standard.client.model.armor.tier2;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IModelRenderHelper;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.client.model.armor.*;
import goblinbob.mobends.standard.client.model.armor.cache.CacheManager;
import goblinbob.mobends.standard.client.model.armor.tier.RenderTier;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class Tier2Renderer
{
    private final CapturingVertexConsumer limbCapture = new CapturingVertexConsumer();
    private final QuadSlicer quadSlicer = new QuadSlicer();

    private long renderCount = 0;

    private int currentArmorColor = 0xFFFFFFFF;

    public Tier2Renderer()
    {
    }

    public RenderTier getTier()
    {
        return RenderTier.TIER_2_MODEL_INTERCEPTION;
    }

    public <E extends LivingEntity> boolean render(ArmorRenderContext<E> context, Model model)
    {
        if (context == null || model == null || context.getEntityData() == null)
        {
            return false;
        }

        return false;
    }

    public <E extends LivingEntity> boolean renderWithTexture(
            ArmorRenderContext<E> context,
            Model model,
            ResourceLocation texture,
            boolean hasFoil)
    {
        if (context == null || model == null || context.getEntityData() == null || texture == null)
        {
            return false;
        }

        try
        {
            VertexConsumer vertexConsumer = (VertexConsumer) IModelRenderHelper.Holder.getHelper().getArmorFoilBuffer(
                    context.getBufferSource(),
                    RenderType.armorCutoutNoCull(texture),
                    hasFoil);

            renderWithConsumer(context, model, vertexConsumer);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public <E extends LivingEntity> void render(
            ArmorRenderContext<E> context,
            Model model,
            ResourceLocation texture,
            Function<ResourceLocation, RenderType> renderTypeProvider)
    {
        if (context.getEntityData() == null)
        {
            renderVanilla(context, model, texture, renderTypeProvider);
            return;
        }

        RenderType renderType = renderTypeProvider.apply(texture);
        VertexConsumer vertexConsumer = context.getBufferSource().getBuffer(renderType);
        renderWithConsumer(context, model, vertexConsumer);
    }

    private <E extends LivingEntity> void renderWithConsumer(
            ArmorRenderContext<E> context,
            Model model,
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
        int packedLight = context.getPackedLight();
        int packedOverlay = context.getPackedOverlay();

        ModelPart root = getModelRoot(model);
        if (root == null)
        {
            renderVanillaFallback(context, model, vertexConsumer);
            return;
        }

        poseStack.pushPose();

        switch (slot)
        {
            case HEAD:
                renderHead(poseStack, vertexConsumer, root, entityData, packedLight, packedOverlay);
                break;
            case CHEST:
                renderChest(poseStack, vertexConsumer, root, entityData, packedLight, packedOverlay);
                break;
            case LEGS:
                renderLegs(poseStack, vertexConsumer, root, entityData, packedLight, packedOverlay);
                break;
            case FEET:
                renderFeet(poseStack, vertexConsumer, root, entityData, packedLight, packedOverlay);
                break;
        }

        poseStack.popPose();

        CacheManager.getInstance().recordCacheAssistedRender();
    }

    @Nullable
    private ModelPart getModelRoot(Model model)
    {
        try
        {
            for (String fieldName : new String[]{"root", "body", "main"})
            {
                try
                {
                    java.lang.reflect.Field field = model.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (value instanceof ModelPart)
                    {
                        return (ModelPart) value;
                    }
                }
                catch (NoSuchFieldException ignored)
                {
                }
            }

            for (java.lang.reflect.Field field : model.getClass().getDeclaredFields())
            {
                if (ModelPart.class.isAssignableFrom(field.getType()))
                {
                    field.setAccessible(true);
                    return (ModelPart) field.get(model);
                }
            }
        }
        catch (Exception e)
        {
        }
        return null;
    }

    @Nullable
    private ModelPart findPartByName(ModelPart root, String... names)
    {
        for (String name : names)
        {
            try
            {
                ModelPart child = root.getChild(name);
                if (child != null)
                {
                    return child;
                }
            }
            catch (Exception ignored)
            {
            }
        }
        return null;
    }

    private void renderHead(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart root,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay)
    {
        ModelPart headPart = findPartByName(root, "head", "Head");
        if (headPart == null)
        {
            renderPartWithTransform(poseStack, vertexConsumer, root, entityData.body, entityData.head,
                    packedLight, packedOverlay);
            return;
        }

        poseStack.pushPose();
        ArmorPoseHelper.applyBodyTransformWithPivot(poseStack, entityData);
        ArmorPoseHelper.applyPartTransform(poseStack, entityData.head, true);
        ArmorPoseHelper.renderPartAtOrigin(headPart, poseStack, vertexConsumer, packedLight, packedOverlay);
        poseStack.popPose();

        ModelPart hatPart = findPartByName(root, "hat", "Hat");
        if (hatPart != null)
        {
            poseStack.pushPose();
            ArmorPoseHelper.applyBodyTransformWithPivot(poseStack, entityData);
            ArmorPoseHelper.applyPartTransform(poseStack, entityData.head, true);
            ArmorPoseHelper.renderPartAtOrigin(hatPart, poseStack, vertexConsumer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private void renderChest(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart root,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay)
    {
        ModelPart bodyPart = findPartByName(root, "body", "Body", "torso", "Torso");
        if (bodyPart != null)
        {
            poseStack.pushPose();
            ArmorPoseHelper.applyBodyTransformWithPivot(poseStack, entityData);
            ArmorPoseHelper.renderPartAtOrigin(bodyPart, poseStack, vertexConsumer, packedLight, packedOverlay);
            poseStack.popPose();
        }

        ModelPart leftArmPart = findPartByName(root, "left_arm", "leftArm", "LeftArm");
        if (leftArmPart != null)
        {
            renderSplitArm(poseStack, vertexConsumer, leftArmPart, entityData, true, packedLight, packedOverlay);
        }

        ModelPart rightArmPart = findPartByName(root, "right_arm", "rightArm", "RightArm");
        if (rightArmPart != null)
        {
            renderSplitArm(poseStack, vertexConsumer, rightArmPart, entityData, false, packedLight, packedOverlay);
        }
    }

    private void renderLegs(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart root,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay)
    {
        ModelPart bodyPart = findPartByName(root, "body", "Body", "torso", "Torso");
        if (bodyPart != null)
        {
            poseStack.pushPose();
            ArmorPoseHelper.applyBodyTransformWithPivot(poseStack, entityData);
            ArmorPoseHelper.renderPartAtOrigin(bodyPart, poseStack, vertexConsumer, packedLight, packedOverlay);
            poseStack.popPose();
        }

        ModelPart leftLegPart = findPartByName(root, "left_leg", "leftLeg", "LeftLeg");
        if (leftLegPart != null)
        {
            renderSplitLeg(poseStack, vertexConsumer, leftLegPart, entityData, true, packedLight, packedOverlay);
        }

        ModelPart rightLegPart = findPartByName(root, "right_leg", "rightLeg", "RightLeg");
        if (rightLegPart != null)
        {
            renderSplitLeg(poseStack, vertexConsumer, rightLegPart, entityData, false, packedLight, packedOverlay);
        }
    }

    private void renderFeet(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart root,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay)
    {
        ModelPart leftLegPart = findPartByName(root, "left_leg", "leftLeg", "LeftLeg");
        if (leftLegPart != null)
        {
            renderSplitLeg(poseStack, vertexConsumer, leftLegPart, entityData, true, packedLight, packedOverlay);
        }

        ModelPart rightLegPart = findPartByName(root, "right_leg", "rightLeg", "RightLeg");
        if (rightLegPart != null)
        {
            renderSplitLeg(poseStack, vertexConsumer, rightLegPart, entityData, false, packedLight, packedOverlay);
        }
    }

    private void renderSplitArm(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart armPart,
            BipedEntityData<?> entityData,
            boolean isLeft,
            int packedLight,
            int packedOverlay)
    {
        ModelPartTransform upperArm = isLeft ? entityData.leftArm : entityData.rightArm;
        ModelPartTransform foreArm = isLeft ? entityData.leftForeArm : entityData.rightForeArm;
        JointPlane elbowPlane = JointDefinitions.getElbow(isLeft);

        limbCapture.clear();
        PoseStack captureStack = new PoseStack();
        float[] storage = new float[6];
        ArmorPoseHelper.resetPartToOrigin(armPart, storage);
        armPart.render(captureStack, limbCapture, packedLight, packedOverlay);
        ArmorPoseHelper.restorePartFromStorage(armPart, storage);

        List<CapturedVertex> vertices = limbCapture.getVertices();
        if (vertices.isEmpty())
        {
            return;
        }

        List<CapturedVertex[]> quads = ArmorPoseHelper.groupIntoQuads(vertices);
        List<SliceResult> sliceResults = quadSlicer.sliceAll(quads, elbowPlane);

        LimbInflation upperInflation = LimbInflation.of(vertices, LimbInflation.ARM_INFLATION);
        LimbInflation lowerInflation = upperInflation.plus(LimbInflation.LOWER_LIMB_INFLATION_STEP);

        poseStack.pushPose();
        ArmorPoseHelper.applyPartTransform(poseStack, entityData.body, true);
        ArmorPoseHelper.applyPartTransform(poseStack, upperArm, true);
        ArmorPoseHelper.renderSlicedVertices(poseStack, vertexConsumer, sliceResults, true, 0, 0, 0, packedLight, packedOverlay, currentArmorColor, upperInflation);
        poseStack.popPose();

        float foreArmOffsetX = -foreArm.position.x * ArmorPoseHelper.SCALE;
        float foreArmOffsetY = -foreArm.position.y * ArmorPoseHelper.SCALE;
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
            ModelPart legPart,
            BipedEntityData<?> entityData,
            boolean isLeft,
            int packedLight,
            int packedOverlay)
    {
        ModelPartTransform upperLeg = isLeft ? entityData.leftLeg : entityData.rightLeg;
        ModelPartTransform lowerLeg = isLeft ? entityData.leftForeLeg : entityData.rightForeLeg;
        JointPlane kneePlane = JointDefinitions.getKnee(isLeft);

        limbCapture.clear();
        PoseStack captureStack = new PoseStack();
        float[] storage = new float[6];
        ArmorPoseHelper.resetPartToOrigin(legPart, storage);
        legPart.render(captureStack, limbCapture, packedLight, packedOverlay);
        ArmorPoseHelper.restorePartFromStorage(legPart, storage);

        List<CapturedVertex> vertices = limbCapture.getVertices();
        if (vertices.isEmpty())
        {
            return;
        }

        List<CapturedVertex[]> quads = ArmorPoseHelper.groupIntoQuads(vertices);
        List<SliceResult> sliceResults = quadSlicer.sliceAll(quads, kneePlane);

        float vanillaLegX = storage[0];

        LimbInflation upperInflation = LimbInflation.of(vertices, LimbInflation.LEG_INFLATION);
        LimbInflation lowerInflation = LimbInflation.of(vertices, LimbInflation.LEG_INFLATION + LimbInflation.LOWER_LIMB_INFLATION_STEP);

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

    private void renderPartWithTransform(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart part,
            ModelPartTransform bodyTransform,
            ModelPartTransform partTransform,
            int packedLight,
            int packedOverlay)
    {
        poseStack.pushPose();
        if (bodyTransform != null)
        {
            ArmorPoseHelper.applyPartTransform(poseStack, bodyTransform, true);
        }
        if (partTransform != null)
        {
            ArmorPoseHelper.applyPartTransform(poseStack, partTransform, true);
        }
        ArmorPoseHelper.renderPartAtOrigin(part, poseStack, vertexConsumer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private <E extends LivingEntity> void renderVanillaFallback(
            ArmorRenderContext<E> context,
            Model model,
            VertexConsumer vertexConsumer)
    {
        PoseStack poseStack = context.getPoseStack();
        poseStack.pushPose();
        IModelRenderHelper.Holder.getHelper().renderModelToBuffer(
                model,
                poseStack,
                vertexConsumer,
                context.getPackedLight(),
                context.getPackedOverlay(),
                context.getArmorColor()
        );
        poseStack.popPose();
    }

    private <E extends LivingEntity> void renderVanilla(
            ArmorRenderContext<E> context,
            Model model,
            ResourceLocation texture,
            Function<ResourceLocation, RenderType> renderTypeProvider)
    {
        PoseStack poseStack = context.getPoseStack();
        MultiBufferSource bufferSource = context.getBufferSource();

        poseStack.pushPose();

        RenderType renderType = renderTypeProvider.apply(texture);
        IModelRenderHelper.Holder.getHelper().renderModelToBuffer(
                model,
                poseStack,
                bufferSource.getBuffer(renderType),
                context.getPackedLight(),
                context.getPackedOverlay(),
                context.getArmorColor()
        );

        poseStack.popPose();
    }

    public long getRenderCount()
    {
        return renderCount;
    }

    public void resetStats()
    {
        renderCount = 0;
    }

    public String getStats()
    {
        return String.format("Tier2Renderer: %d renders", renderCount);
    }
}
