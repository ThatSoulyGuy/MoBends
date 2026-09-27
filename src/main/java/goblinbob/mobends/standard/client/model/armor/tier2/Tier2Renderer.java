package goblinbob.mobends.standard.client.model.armor.tier2;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IModelRenderHelper;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.client.model.armor.*;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.function.Function;

public class Tier2Renderer
{
    private final CapturingVertexConsumer limbCapture = new CapturingVertexConsumer();
    private final QuadSlicer quadSlicer = new QuadSlicer();

    private int currentArmorColor = 0xFFFFFFFF;

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
        RenderType renderType = renderTypeProvider.apply(texture);
        VertexConsumer vertexConsumer = context.getBufferSource().getBuffer(renderType);
        renderWithConsumer(context, model, vertexConsumer);
    }

    private <E extends LivingEntity> void renderWithConsumer(
            ArmorRenderContext<E> context,
            Model model,
            VertexConsumer vertexConsumer)
    {
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
            ArmorPoseHelper.renderSplitArm(poseStack, vertexConsumer, leftArmPart, entityData, true, packedLight, packedOverlay, 0F, currentArmorColor, limbCapture, quadSlicer);
        }

        ModelPart rightArmPart = findPartByName(root, "right_arm", "rightArm", "RightArm");
        if (rightArmPart != null)
        {
            ArmorPoseHelper.renderSplitArm(poseStack, vertexConsumer, rightArmPart, entityData, false, packedLight, packedOverlay, 0F, currentArmorColor, limbCapture, quadSlicer);
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
            ArmorPoseHelper.renderSplitLeg(poseStack, vertexConsumer, leftLegPart, entityData, true, packedLight, packedOverlay, currentArmorColor, limbCapture, quadSlicer);
        }

        ModelPart rightLegPart = findPartByName(root, "right_leg", "rightLeg", "RightLeg");
        if (rightLegPart != null)
        {
            ArmorPoseHelper.renderSplitLeg(poseStack, vertexConsumer, rightLegPart, entityData, false, packedLight, packedOverlay, currentArmorColor, limbCapture, quadSlicer);
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
            ArmorPoseHelper.renderSplitLeg(poseStack, vertexConsumer, leftLegPart, entityData, true, packedLight, packedOverlay, currentArmorColor, limbCapture, quadSlicer);
        }

        ModelPart rightLegPart = findPartByName(root, "right_leg", "rightLeg", "RightLeg");
        if (rightLegPart != null)
        {
            ArmorPoseHelper.renderSplitLeg(poseStack, vertexConsumer, rightLegPart, entityData, false, packedLight, packedOverlay, currentArmorColor, limbCapture, quadSlicer);
        }
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
}
