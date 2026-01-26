package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.core.util.GlHelper;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LayerCustomHeldItem<E extends LivingEntity, M extends HumanoidModel<E>> extends RenderLayer<E, M>
{

    private final BipedMutator<?, E, M> mutator;

    public LayerCustomHeldItem(LivingEntityRenderer<E, M> renderer, BipedMutator<?, E, M> mutator)
    {
        super(renderer);
        this.mutator = mutator;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       E entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        boolean rightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
        ItemStack mainHandItem = rightHanded ? entity.getMainHandItem() : entity.getOffhandItem();
        ItemStack offHandItem = rightHanded ? entity.getOffhandItem() : entity.getMainHandItem();

        if (!mainHandItem.isEmpty() || !offHandItem.isEmpty())
        {
            poseStack.pushPose();

            if (this.getParentModel().young)
            {
                poseStack.translate(0.0F, 0.75F, 0.0F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }

            this.renderHeldItem(entity, mainHandItem, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    HumanoidArm.RIGHT, poseStack, bufferSource, packedLight);
            this.renderHeldItem(entity, offHandItem, ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                    HumanoidArm.LEFT, poseStack, bufferSource, packedLight);

            poseStack.popPose();
        }
    }

    private void renderHeldItem(E entity, ItemStack itemStack, ItemDisplayContext displayContext,
                                HumanoidArm arm, PoseStack poseStack,
                                MultiBufferSource bufferSource, int packedLight)
    {
        if (!itemStack.isEmpty())
        {
            poseStack.pushPose();

            if (entity.isCrouching())
            {
                poseStack.translate(0.0F, 0.2F, 0.0F);
            }

            // Translate to the hand position
            this.translateToHand(arm, entity, poseStack);

            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            boolean leftHanded = arm == HumanoidArm.LEFT;
            poseStack.translate((float)(leftHanded ? -1 : 1) / 16.0F, 0.125F, -0.625F);

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    entity, itemStack, displayContext, leftHanded,
                    poseStack, bufferSource, entity.level(), packedLight,
                    LivingEntityRenderer.getOverlayCoords(entity, 0.0F), entity.getId());

            poseStack.popPose();
        }
    }

    /**
     * Translates to the hand position with Mo' Bends custom animation transforms applied.
     * This replicates the original Mo' Bends postRenderArm behavior:
     * 1. arm.applyCharacterTransform (body + arm transforms)
     * 2. arm.applyPostTransform -> forearm.propagateTransform (forearm local + postOffset)
     * 3. Item rotation adjustment with translate/rotate/untranslate
     */
    protected void translateToHand(HumanoidArm arm, E entity, PoseStack poseStack)
    {
        // Apply the full Mo' Bends transform chain matching the original postRenderArm behavior
        if (mutator != null && mutator.shouldRenderCustom())
        {
            var body = mutator.getBody();
            var armPart = arm == HumanoidArm.RIGHT ? mutator.getRightArm() : mutator.getLeftArm();
            var foreArm = arm == HumanoidArm.RIGHT ? mutator.getRightForeArm() : mutator.getLeftForeArm();

            if (body != null && armPart != null && foreArm != null)
            {
                float scale = 1.0F / 16.0F;

                // === arm.applyCharacterTransform ===
                // Apply body transform (position + offset + rotation)
                poseStack.translate(body.position.x * scale, body.position.y * scale, body.position.z * scale);
                if (body.offset.x != 0 || body.offset.y != 0 || body.offset.z != 0)
                {
                    poseStack.translate(body.offset.x * scale, body.offset.y * scale, body.offset.z * scale);
                }
                GlHelper.rotate(poseStack, body.rotation.getSmooth());

                // Apply arm transform (position + offset + rotation)
                poseStack.translate(armPart.position.x * scale, armPart.position.y * scale, armPart.position.z * scale);
                if (armPart.offset.x != 0 || armPart.offset.y != 0 || armPart.offset.z != 0)
                {
                    poseStack.translate(armPart.offset.x * scale, armPart.offset.y * scale, armPart.offset.z * scale);
                }
                GlHelper.rotate(poseStack, armPart.rotation.getSmooth());

                // === arm.applyPostTransform -> forearm.propagateTransform ===
                // Apply forearm local transform (position + offset + rotation)
                poseStack.translate(foreArm.position.x * scale, foreArm.position.y * scale, foreArm.position.z * scale);
                if (foreArm.offset.x != 0 || foreArm.offset.y != 0 || foreArm.offset.z != 0)
                {
                    poseStack.translate(foreArm.offset.x * scale, foreArm.offset.y * scale, foreArm.offset.z * scale);
                }
                GlHelper.rotate(poseStack, foreArm.rotation.getSmooth());

                // Apply forearm postOffset (0, -4, -2) - this positions the held item correctly
                // In the original, this was set via ModelPartPostOffset.setPostOffset(0, -4F, -2F)
                poseStack.translate(0, -4.0F * scale, -2.0F * scale);

                // === Item rotation adjustment ===
                // Apply custom item rotation with translate/rotate/untranslate pattern
                EntityData<?> entityData = EntityDatabase.instance.get(entity);
                if (entityData instanceof BipedEntityData<?> bipedData)
                {
                    SmoothOrientation itemRotation = arm == HumanoidArm.RIGHT
                            ? bipedData.renderRightItemRotation
                            : bipedData.renderLeftItemRotation;

                    poseStack.translate(0, 8.0F * scale, 0);
                    GlHelper.rotate(poseStack, itemRotation.getSmooth());
                    poseStack.translate(0, -8.0F * scale, 0);
                }

                return;
            }
        }

        // Fallback to vanilla transform if Mo' Bends parts not available
        M model = this.getParentModel();
        ((ArmedModel) model).translateToHand(arm, poseStack);

        // Apply Mo' Bends custom item rotation
        EntityData<?> entityData = EntityDatabase.instance.get(entity);
        if (entityData instanceof BipedEntityData<?> bipedData)
        {
            SmoothOrientation itemRotation = arm == HumanoidArm.RIGHT
                    ? bipedData.renderRightItemRotation
                    : bipedData.renderLeftItemRotation;

            poseStack.translate(0, 8F * 0.0625F, 0);
            GlHelper.rotate(poseStack, itemRotation.getSmooth());
            poseStack.translate(0, -8F * 0.0625F, 0);
        }
    }
}
