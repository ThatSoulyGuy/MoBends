package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderingFacade;
import goblinbob.mobends.standard.client.model.armor.CapturingVertexConsumer;
import goblinbob.mobends.standard.client.model.armor.RigidArmorRenderer;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.main.ModConfig;
import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.previewer.PlayerPreviewer;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;

/**
 * Custom armor layer that renders armor with Mo' Bends animation.
 * Uses the rigid body approach: capture vertices, assign to bones by spatial position,
 * and render with Mo' Bends transforms.
 *
 * This approach works with ANY armor - vanilla, modded, custom models, etc.
 */
@OnlyIn(Dist.CLIENT)
public class LayerCustomBipedArmor<E extends LivingEntity, M extends HumanoidModel<E>> extends RenderLayer<E, M>
{
    private final BipedMutator<?, E, M> mutator;
    private HumanoidArmorLayer<E, M, ?> vanillaArmorLayer;

    /**
     * Standard armor models for rendering.
     */
    private HumanoidModel<E> innerModel;
    private HumanoidModel<E> outerModel;

    /**
     * The three-tier armor rendering facade.
     * Handles tier selection and delegates to appropriate renderer.
     */
    private final ArmorRenderingFacade armorFacade = new ArmorRenderingFacade();

    /**
     * The rigid armor renderer for capture-assign-render approach.
     * @deprecated Use armorFacade instead. Kept for backward compatibility.
     */
    @Deprecated
    private final RigidArmorRenderer rigidRenderer = new RigidArmorRenderer();

    public LayerCustomBipedArmor(LivingEntityRenderer<E, M> renderer, BipedMutator<?, E, M> mutator)
    {
        super(renderer);
        this.mutator = mutator;
    }

    /**
     * Set the vanilla armor layer reference for texture access.
     */
    public void setVanillaArmorLayer(HumanoidArmorLayer<E, M, ?> vanillaLayer)
    {
        this.vanillaArmorLayer = vanillaLayer;
    }

    /**
     * Set the armor models to use for rendering.
     */
    @SuppressWarnings("unchecked")
    public void setArmorModels(HumanoidModel<?> innerModel, HumanoidModel<?> outerModel)
    {
        this.innerModel = (HumanoidModel<E>) innerModel;
        this.outerModel = (HumanoidModel<E>) outerModel;
    }

    /**
     * For backward compatibility - creates armor models if not set.
     */
    public void initArmor()
    {
        // Armor models will be set from the vanilla layer or created on demand
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       E entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        // Check if entity has Mo' Bends animation data
        EntityData<?> entityData = EntityDatabase.instance.get(entity);
        boolean hasBendsAnimation = entityData instanceof BipedEntityData;

        // Render each armor slot
        renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.CHEST, packedLight, hasBendsAnimation, entityData);
        renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.LEGS, packedLight, hasBendsAnimation, entityData);
        renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.FEET, packedLight, hasBendsAnimation, entityData);
        renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.HEAD, packedLight, hasBendsAnimation, entityData);
    }

    /**
     * Render a single armor piece.
     */
    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource,
                                  E entity, EquipmentSlot slot, int packedLight,
                                  boolean hasBendsAnimation, EntityData<?> entityData)
    {
        ItemStack itemStack = entity.getItemBySlot(slot);
        if (itemStack.isEmpty()) return;

        if (!(itemStack.getItem() instanceof ArmorItem armorItem)) return;
        if (armorItem.getEquipmentSlot() != slot) return;

        // Get the default armor model (inner or outer based on slot)
        boolean usesInnerModel = usesInnerModel(slot);
        HumanoidModel<E> defaultModel = usesInnerModel ? getInnerModel() : getOuterModel();
        if (defaultModel == null)
        {
            return;
        }

        // Set up model visibility for this slot
        M parentModel = getParentModel();
        parentModel.copyPropertiesTo(defaultModel);
        setPartVisibility(defaultModel, slot);

        // Query NeoForge's IClientItemExtensions for custom armor models
        // This is the standard way mods provide custom 3D armor models
        @SuppressWarnings("unchecked")
        HumanoidModel<E> customModel = (HumanoidModel<E>) IClientItemExtensions.of(itemStack)
                .getHumanoidArmorModel(entity, itemStack, slot, defaultModel);

        // Determine if this is a custom model or the default vanilla model
        // If the returned model is the same as the default, use vanilla Tier 1 rendering
        // If it's a different model (custom), use that model instead
        boolean isCustomModel = (customModel != defaultModel);
        HumanoidModel<E> armorModel = isCustomModel ? customModel : defaultModel;

        // Check if we should use Mo' Bends rendering
        boolean shouldUseBends = hasBendsAnimation && !ModConfig.shouldKeepArmorAsVanilla(armorItem);

        if (shouldUseBends && entityData instanceof BipedEntityData<?>)
        {
            BipedEntityData<?> bipedData = (BipedEntityData<?>) entityData;

            // Handle previewer
            if (bipedData instanceof PlayerData && PlayerPreviewer.isPreviewInProgress())
            {
                bipedData = (BipedEntityData<?>) PlayerPreviewer.getPreviewData();
            }

            // Render with rigid body approach
            // Pass isCustomModel flag to determine rendering strategy
            renderRigidArmor(poseStack, bufferSource, packedLight, entity, armorItem, armorModel, slot, itemStack, bipedData, isCustomModel);
        }
        else
        {
            // Fall back to vanilla rendering
            renderVanillaArmor(poseStack, bufferSource, packedLight, entity, armorItem, armorModel, slot, itemStack);
        }
    }

    /**
     * Render armor using the three-tier armor rendering system.
     *
     * Tier selection:
     * - Tier 1 (Transform Injection): vanilla/standard HumanoidModel (uses default texture paths)
     * - Tier 2 (Model Interception): modded custom models (uses custom texture paths from mod)
     *
     * @param isCustomModel true if the armor uses a custom model from IClientItemExtensions
     */
    private void renderRigidArmor(PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, E entity, ArmorItem armorItem,
                                  HumanoidModel<E> armorModel, EquipmentSlot slot,
                                  ItemStack itemStack, BipedEntityData<?> bipedData,
                                  boolean isCustomModel)
    {
        boolean isInnerModel = usesInnerModel(slot);
        ArmorMaterial material = armorItem.getMaterial().value();

        if (isCustomModel)
        {
            // Custom armor model - use NeoForge's getArmorTexture() to get custom textures
            boolean anyRendered = false;
            for (ArmorMaterial.Layer layer : material.layers())
            {
                ResourceLocation texture = armorItem.getArmorTexture(itemStack, entity, slot, layer, isInnerModel);
                if (texture == null)
                {
                    continue;
                }

                boolean rendered = armorFacade.renderArmor(
                        poseStack,
                        bufferSource,
                        packedLight,
                        entity,
                        slot,
                        itemStack,
                        armorItem,
                        armorModel,
                        bipedData,
                        texture
                );

                if (rendered)
                {
                    anyRendered = true;
                }
            }

            // Fallback for custom models if nothing rendered
            if (!anyRendered)
            {
                ResourceLocation fallbackTexture = getArmorTexture(armorItem, slot, null);
                if (fallbackTexture != null)
                {
                    renderLegacyRigidArmor(poseStack, bufferSource, packedLight, armorModel, slot, itemStack, bipedData, fallbackTexture);
                }
            }
        }
        else
        {
            // Vanilla armor model - use standard vanilla texture path
            ResourceLocation texture = getArmorTexture(armorItem, slot, null);
            if (texture == null)
            {
                return;
            }

            // Use Tier 1 rendering for vanilla armor
            armorFacade.renderArmor(
                    poseStack,
                    bufferSource,
                    packedLight,
                    entity,
                    slot,
                    itemStack,
                    armorItem,
                    armorModel,
                    bipedData,
                    texture
            );
        }
    }

    /**
     * Legacy rigid armor rendering using vertex capture.
     * Used as fallback if the new three-tier system fails.
     */
    private void renderLegacyRigidArmor(PoseStack poseStack, MultiBufferSource bufferSource,
                                        int packedLight, HumanoidModel<E> armorModel, EquipmentSlot slot,
                                        ItemStack itemStack, BipedEntityData<?> bipedData, ResourceLocation texture)
    {
        // Save current model poses
        ModelPoseSnapshot snapshot = saveModelPoses(armorModel);

        // Reset model to rest pose for capture
        resetToRestPose(armorModel);

        // Capture vertices by rendering to our capturing consumer
        CapturingVertexConsumer captureConsumer = rigidRenderer.getCaptureConsumer();

        // Render the armor model to capture its vertices
        // We use an identity pose stack since we want rest-pose vertices
        PoseStack capturePoseStack = new PoseStack();
        armorModel.renderToBuffer(capturePoseStack, captureConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        // Restore original poses
        restoreModelPoses(armorModel, snapshot);

        // Now render the captured vertices with Mo' Bends transforms
        VertexConsumer outputConsumer = ItemRenderer.getArmorFoilBuffer(
                bufferSource, RenderType.armorCutoutNoCull(texture), itemStack.hasFoil());

        rigidRenderer.renderCapturedVertices(poseStack, outputConsumer, packedLight, OverlayTexture.NO_OVERLAY, bipedData);
    }

    /**
     * Reset the armor model to rest pose (no rotations/translations).
     */
    private void resetToRestPose(HumanoidModel<E> model)
    {
        resetPart(model.head);
        resetPart(model.hat);
        resetPart(model.body);
        resetPart(model.leftArm);
        resetPart(model.rightArm);
        resetPart(model.leftLeg);
        resetPart(model.rightLeg);
    }

    private void resetPart(ModelPart part)
    {
        part.xRot = 0;
        part.yRot = 0;
        part.zRot = 0;
        // Keep the default positions (x, y, z) as they define the part's origin
    }

    /**
     * Save the current model poses.
     */
    private ModelPoseSnapshot saveModelPoses(HumanoidModel<E> model)
    {
        return new ModelPoseSnapshot(model);
    }

    /**
     * Restore saved model poses.
     */
    private void restoreModelPoses(HumanoidModel<E> model, ModelPoseSnapshot snapshot)
    {
        snapshot.restore(model);
    }

    /**
     * Render armor using vanilla method.
     */
    private void renderVanillaArmor(PoseStack poseStack, MultiBufferSource bufferSource,
                                    int packedLight, E entity, ArmorItem armorItem,
                                    HumanoidModel<E> armorModel, EquipmentSlot slot, ItemStack itemStack)
    {
        // Get the armor texture
        ResourceLocation texture = getArmorTexture(armorItem, slot, null);
        if (texture == null) return;

        // Get the render type
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
                bufferSource, RenderType.armorCutoutNoCull(texture), itemStack.hasFoil());

        // Sync poses to vanilla model from our mutator
        if (mutator != null)
        {
            mutator.syncPosesToVanillaModel(armorModel);
        }

        // Render using vanilla model
        armorModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }

    /**
     * Get the armor texture for the given item and slot.
     */
    private ResourceLocation getArmorTexture(ArmorItem armorItem, EquipmentSlot slot, @Nullable String overlay)
    {
        // Use vanilla texture location logic
        // In 1.21.1, getMaterial() returns Holder<ArmorMaterial>
        ResourceLocation materialLocation = armorItem.getMaterial().unwrapKey()
                .map(key -> key.location())
                .orElse(ResourceLocation.withDefaultNamespace("leather"));

        String domain = materialLocation.getNamespace();
        String path = materialLocation.getPath();

        int layer = usesInnerModel(slot) ? 2 : 1;
        String suffix = overlay == null ? "" : "_" + overlay;

        return ResourceLocation.fromNamespaceAndPath(domain, "textures/models/armor/" + path + "_layer_" + layer + suffix + ".png");
    }

    /**
     * Check if the slot uses the inner armor model.
     */
    private boolean usesInnerModel(EquipmentSlot slot)
    {
        return slot == EquipmentSlot.LEGS;
    }

    /**
     * Get the inner armor model.
     */
    private HumanoidModel<E> getInnerModel()
    {
        return innerModel;
    }

    /**
     * Get the outer armor model.
     */
    private HumanoidModel<E> getOuterModel()
    {
        return outerModel;
    }

    /**
     * Set part visibility for the given slot.
     */
    private void setPartVisibility(HumanoidModel<E> model, EquipmentSlot slot)
    {
        model.setAllVisible(false);

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
            default:
                break;
        }
    }

    /**
     * Get the armor rendering facade for statistics and debugging.
     */
    public ArmorRenderingFacade getArmorFacade()
    {
        return armorFacade;
    }

    /**
     * Snapshot of model part poses for save/restore.
     */
    private static class ModelPoseSnapshot
    {
        private final float headXRot, headYRot, headZRot;
        private final float hatXRot, hatYRot, hatZRot;
        private final float bodyXRot, bodyYRot, bodyZRot;
        private final float leftArmXRot, leftArmYRot, leftArmZRot;
        private final float rightArmXRot, rightArmYRot, rightArmZRot;
        private final float leftLegXRot, leftLegYRot, leftLegZRot;
        private final float rightLegXRot, rightLegYRot, rightLegZRot;

        public ModelPoseSnapshot(HumanoidModel<?> model)
        {
            headXRot = model.head.xRot; headYRot = model.head.yRot; headZRot = model.head.zRot;
            hatXRot = model.hat.xRot; hatYRot = model.hat.yRot; hatZRot = model.hat.zRot;
            bodyXRot = model.body.xRot; bodyYRot = model.body.yRot; bodyZRot = model.body.zRot;
            leftArmXRot = model.leftArm.xRot; leftArmYRot = model.leftArm.yRot; leftArmZRot = model.leftArm.zRot;
            rightArmXRot = model.rightArm.xRot; rightArmYRot = model.rightArm.yRot; rightArmZRot = model.rightArm.zRot;
            leftLegXRot = model.leftLeg.xRot; leftLegYRot = model.leftLeg.yRot; leftLegZRot = model.leftLeg.zRot;
            rightLegXRot = model.rightLeg.xRot; rightLegYRot = model.rightLeg.yRot; rightLegZRot = model.rightLeg.zRot;
        }

        public void restore(HumanoidModel<?> model)
        {
            model.head.xRot = headXRot; model.head.yRot = headYRot; model.head.zRot = headZRot;
            model.hat.xRot = hatXRot; model.hat.yRot = hatYRot; model.hat.zRot = hatZRot;
            model.body.xRot = bodyXRot; model.body.yRot = bodyYRot; model.body.zRot = bodyZRot;
            model.leftArm.xRot = leftArmXRot; model.leftArm.yRot = leftArmYRot; model.leftArm.zRot = leftArmZRot;
            model.rightArm.xRot = rightArmXRot; model.rightArm.yRot = rightArmYRot; model.rightArm.zRot = rightArmZRot;
            model.leftLeg.xRot = leftLegXRot; model.leftLeg.yRot = leftLegYRot; model.leftLeg.zRot = leftLegZRot;
            model.rightLeg.xRot = rightLegXRot; model.rightLeg.yRot = rightLegYRot; model.rightLeg.zRot = rightLegZRot;
        }
    }
}
