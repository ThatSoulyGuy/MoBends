package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import goblinbob.mobends.core.client.model.BendsCube;
import goblinbob.mobends.core.client.model.BendsModelPart;
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
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

public class LayerCustomHeldItem<E extends LivingEntity, M extends net.minecraft.client.model.EntityModel<E>> extends RenderLayer<E, M>
{

    private final BipedMutator<?, E, M> mutator;

    private RenderLayer<E, M> fallbackLayer;

    public LayerCustomHeldItem(LivingEntityRenderer<E, M> renderer, BipedMutator<?, E, M> mutator)
    {
        super(renderer);
        this.mutator = mutator;
    }

    public void setFallbackLayer(RenderLayer<E, M> fallbackLayer)
    {
        this.fallbackLayer = fallbackLayer;
    }

    private boolean canPositionHands(E entity)
    {
        return this.getParentModel() instanceof ArmedModel
                || (this.isDrivenByMoBends(entity) && this.getCustomForeArm(HumanoidArm.RIGHT) != null);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       E entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (goblinbob.mobends.compat.FirstPersonModelCompat.isRenderingFirstPersonBody(entity)
                && this.getParentModel() instanceof HumanoidModel<?> humanoidParent
                && goblinbob.mobends.compat.FirstPersonModelCompat.showsVanillaHands(humanoidParent))
        {
            return;
        }

        if (!this.canPositionHands(entity))
        {
            if (this.fallbackLayer != null)
            {
                this.fallbackLayer.render(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount,
                        partialTicks, ageInTicks, netHeadYaw, headPitch);
            }

            return;
        }

        boolean rightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
        ItemStack mainHandItem = rightHanded ? entity.getMainHandItem() : entity.getOffhandItem();
        ItemStack offHandItem = rightHanded ? entity.getOffhandItem() : entity.getMainHandItem();

        if (!mainHandItem.isEmpty() || !offHandItem.isEmpty())
        {
            poseStack.pushPose();

            if (this.getParentModel().young && !this.isDrivenByMoBends(entity))
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
            if (goblinbob.mobends.compat.NotEnoughAnimationsCompat.renderHeldItem(
                    entity, this.getParentModel(), itemStack, arm, poseStack, bufferSource, packedLight))
            {
                return;
            }

            if (this.renderSpyglassOnHead(entity, itemStack, arm, poseStack, bufferSource, packedLight))
            {
                return;
            }

            poseStack.pushPose();

            this.translateToHand(arm, entity, poseStack);

            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            boolean leftHanded = arm == HumanoidArm.LEFT;
            this.translateToGrip(arm, entity, itemStack, displayContext, poseStack);

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    entity, itemStack, displayContext, leftHanded,
                    poseStack, bufferSource, entity.level(), packedLight,
                    LivingEntityRenderer.getOverlayCoords(entity, 0.0F), entity.getId());

            poseStack.popPose();
        }
    }

    private boolean renderSpyglassOnHead(E entity, ItemStack itemStack, HumanoidArm arm,
                                         PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        if (itemStack.getUseAnimation() != UseAnim.SPYGLASS
                || entity.getUseItem() != itemStack
                || entity.swingTime != 0)
        {
            return false;
        }

        if (mutator == null || !mutator.shouldRenderCustom())
        {
            return false;
        }

        final BendsModelPart body = mutator.getBody();
        final BendsModelPart head = mutator.getHead();
        if (body == null || head == null)
        {
            return false;
        }

        final float scale = 1.0F / 16.0F;

        poseStack.pushPose();

        applyBoneTransform(poseStack, body, scale, false);
        applyBoneTransform(poseStack, head, scale, false);

        poseStack.translate(0.0F, -0.25F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);

        poseStack.translate((arm == HumanoidArm.LEFT ? -2.5F : 2.5F) / 16.0F, -0.0625F, 0.0F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                entity, itemStack, ItemDisplayContext.HEAD, false,
                poseStack, bufferSource, entity.level(), packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F), entity.getId());

        poseStack.popPose();
        return true;
    }

    protected void translateToGrip(HumanoidArm arm, E entity, ItemStack itemStack,
                                   ItemDisplayContext displayContext, PoseStack poseStack)
    {
        float vanillaGripX = arm == HumanoidArm.LEFT ? 1.0F : -1.0F;
        BendsModelPart foreArm = this.getCustomForeArm(arm);

        if (foreArm != null && !foreArm.getCubes().isEmpty())
        {
            float minX = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;

            for (BendsCube cube : foreArm.getCubes())
            {
                minX = Math.min(minX, cube.minX);
                maxX = Math.max(maxX, cube.maxX);
            }

            float centredGripX = (minX + maxX) * 0.5F;
            float ownOffset = Math.abs(this.getDisplayOffsetX(entity, itemStack, displayContext));
            float blend = 1.0F - Math.min(1.0F, ownOffset);
            float gripX = vanillaGripX + (centredGripX - vanillaGripX) * blend;

            poseStack.translate(-gripX / 16.0F, 0.125F, -0.625F);
            return;
        }

        poseStack.translate(-vanillaGripX / 16.0F, 0.125F, -0.625F);
    }

    private float getDisplayOffsetX(E entity, ItemStack itemStack, ItemDisplayContext displayContext)
    {
        try
        {
            BakedModel model = Minecraft.getInstance().getItemRenderer()
                    .getModel(itemStack, entity.level(), entity, entity.getId());
            return model.getTransforms().getTransform(displayContext).translation.x() * 16.0F;
        }
        catch (Exception e)
        {
            return 0.0F;
        }
    }

    private boolean isDrivenByMoBends(E entity)
    {
        return goblinbob.mobends.core.util.BenderHelper.isEntityAnimated(entity)
                && !goblinbob.mobends.compat.ModCompatManager.shouldDeferAnimation(entity)
                && (mutator == null || mutator.shouldRenderCustom());
    }

    private BendsModelPart getCustomForeArm(HumanoidArm arm)
    {
        if (mutator == null || !mutator.shouldRenderCustom() || mutator.getBody() == null)
        {
            return null;
        }

        BendsModelPart armPart = arm == HumanoidArm.RIGHT ? mutator.getRightArm() : mutator.getLeftArm();
        BendsModelPart foreArm = arm == HumanoidArm.RIGHT ? mutator.getRightForeArm() : mutator.getLeftForeArm();

        return armPart != null && foreArm != null ? foreArm : null;
    }

    private static void applyBoneTransform(PoseStack poseStack, BendsModelPart bone, float scale,
                                           boolean includeOffset)
    {
        if (bone.globalOffset.x != 0 || bone.globalOffset.y != 0 || bone.globalOffset.z != 0)
        {
            poseStack.translate(bone.globalOffset.x * scale, bone.globalOffset.y * scale, bone.globalOffset.z * scale);
        }

        poseStack.translate(bone.position.x * scale, bone.position.y * scale, bone.position.z * scale);

        if (includeOffset && (bone.offset.x != 0 || bone.offset.y != 0 || bone.offset.z != 0))
        {
            poseStack.translate(bone.offset.x * scale, bone.offset.y * scale, bone.offset.z * scale);
        }

        if (bone.preRotationScale.x != 1.0F || bone.preRotationScale.y != 1.0F || bone.preRotationScale.z != 1.0F)
        {
            poseStack.scale(bone.preRotationScale.x, bone.preRotationScale.y, bone.preRotationScale.z);
        }

        GlHelper.rotate(poseStack, bone.rotation.getSmooth());

        if (bone.scale.x != 1.0F || bone.scale.y != 1.0F || bone.scale.z != 1.0F)
        {
            poseStack.scale(bone.scale.x, bone.scale.y, bone.scale.z);
        }
    }

    protected void translateToHand(HumanoidArm arm, E entity, PoseStack poseStack)
    {
        BendsModelPart foreArm = this.isDrivenByMoBends(entity) ? this.getCustomForeArm(arm) : null;

        if (foreArm != null)
        {
            BendsModelPart body = mutator.getBody();
            BendsModelPart armPart = arm == HumanoidArm.RIGHT ? mutator.getRightArm() : mutator.getLeftArm();

            float scale = 1.0F / 16.0F;

            applyBoneTransform(poseStack, body, scale, true);
            applyBoneTransform(poseStack, armPart, scale, true);
            applyBoneTransform(poseStack, foreArm, scale, true);

            poseStack.translate(0, -4.0F * scale, -2.0F * scale);

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

        M model = this.getParentModel();
        if (model instanceof ArmedModel armedModel)
        {
            armedModel.translateToHand(arm, poseStack);
        }

        EntityData<?> entityData = EntityDatabase.instance.get(entity);
        if (entityData instanceof BipedEntityData<?> bipedData && this.isDrivenByMoBends(entity))
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
