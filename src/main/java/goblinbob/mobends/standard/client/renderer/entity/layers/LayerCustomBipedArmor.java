package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.mixin.armor.HumanoidArmorLayerAccessor;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderContext;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderingFacade;
import goblinbob.mobends.standard.client.model.armor.CapturingVertexConsumer;
import goblinbob.mobends.standard.client.model.armor.RigidArmorRenderer;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.main.ModConfig;
import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.previewer.PlayerPreviewer;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
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
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

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
     * Also extracts the inner and outer armor models from the vanilla layer.
     */
    @SuppressWarnings("unchecked")
    public void setVanillaArmorLayer(HumanoidArmorLayer<E, M, ?> vanillaLayer)
    {
        this.vanillaArmorLayer = vanillaLayer;

        // Extract armor models from vanilla layer using accessor mixin
        if (vanillaLayer instanceof HumanoidArmorLayerAccessor accessor)
        {
            this.innerModel = (HumanoidModel<E>) accessor.mobends$getInnerModel();
            this.outerModel = (HumanoidModel<E>) accessor.mobends$getOuterModel();
        }
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

        // Get the appropriate standard armor model
        boolean usesInnerModel = usesInnerModel(slot);
        HumanoidModel<E> standardModel = usesInnerModel ? getInnerModel() : getOuterModel();
        if (standardModel == null)
        {
            return;
        }

        // Set up model visibility for this slot
        M parentModel = getParentModel();
        parentModel.copyPropertiesTo(standardModel);
        setPartVisibility(standardModel, slot);

        // Query IClientItemExtensions for custom armor model
        // This is how Forge allows mods to provide custom 3D armor models (like GeckoLib)
        IClientItemExtensions extensions = IClientItemExtensions.of(itemStack);
        Model customModel = extensions.getGenericArmorModel(entity, itemStack, slot, standardModel);

        // Determine if this is a custom model (different from the standard HumanoidModel)
        boolean isCustomModel = customModel != standardModel && !(customModel instanceof HumanoidModel);

        // Use custom model if available, otherwise use standard
        Model modelToRender = customModel != null ? customModel : standardModel;

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

            // Render with the appropriate approach based on model type
            if (isCustomModel)
            {
                // Custom model - use Tier 2 rendering
                renderCustomModelArmor(poseStack, bufferSource, packedLight, entity, armorItem, modelToRender, slot, itemStack, bipedData);
            }
            else
            {
                // Standard HumanoidModel - use Tier 1 rendering (or legacy if Tier 1 fails)
                @SuppressWarnings("unchecked")
                HumanoidModel<E> humanoidModel = (HumanoidModel<E>) modelToRender;
                renderRigidArmor(poseStack, bufferSource, packedLight, entity, armorItem, humanoidModel, slot, itemStack, bipedData);
            }
        }
        else
        {
            // Fall back to vanilla rendering
            if (modelToRender instanceof HumanoidModel<?>)
            {
                @SuppressWarnings("unchecked")
                HumanoidModel<E> humanoidModel = (HumanoidModel<E>) modelToRender;
                renderVanillaArmor(poseStack, bufferSource, packedLight, entity, armorItem, humanoidModel, slot, itemStack);
            }
            else
            {
                // Custom model without Mo'Bends - render directly
                renderCustomModelVanilla(poseStack, bufferSource, packedLight, entity, armorItem, modelToRender, slot, itemStack);
            }
        }
    }

    /**
     * Render armor using the three-tier armor rendering system.
     *
     * Tier selection:
     * - Tier 1 (Transform Injection): ~85% of armor - vanilla/standard HumanoidModel
     * - Tier 2 (Model Interception): ~10% of armor - modded using ModelPart
     * - Tier 3 (Vertex Capture): ~5% of armor - exotic renderers
     */
    private void renderRigidArmor(PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, E entity, ArmorItem armorItem,
                                  HumanoidModel<E> armorModel, EquipmentSlot slot,
                                  ItemStack itemStack, BipedEntityData<?> bipedData)
    {
        // Check if this is dyeable armor (leather)
        if (armorItem instanceof DyeableLeatherItem dyeableItem)
        {
            // Get dye color and convert to RGB components (0.0-1.0)
            int color = dyeableItem.getColor(itemStack);
            float red = (float)(color >> 16 & 255) / 255.0F;
            float green = (float)(color >> 8 & 255) / 255.0F;
            float blue = (float)(color & 255) / 255.0F;

            // Render base texture with dye color
            ResourceLocation baseTexture = getArmorTexture(entity, itemStack, armorItem, slot, null);
            if (baseTexture != null)
            {
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
                        baseTexture,
                        red, green, blue
                );

                if (!rendered)
                {
                    renderLegacyRigidArmorWithColor(poseStack, bufferSource, packedLight, armorModel, slot, itemStack, bipedData, baseTexture, red, green, blue);
                }
            }

            // Render overlay texture without color (white)
            ResourceLocation overlayTexture = getArmorTexture(entity, itemStack, armorItem, slot, "overlay");
            if (overlayTexture != null)
            {
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
                        overlayTexture
                );

                if (!rendered)
                {
                    renderLegacyRigidArmor(poseStack, bufferSource, packedLight, armorModel, slot, itemStack, bipedData, overlayTexture);
                }
            }
        }
        else
        {
            // Standard armor - single texture, no color tint
            ResourceLocation texture = getArmorTexture(entity, itemStack, armorItem, slot, null);
            if (texture == null) return;

            // Use the three-tier facade for rendering
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

            // If facade didn't handle it, fall back to legacy approach
            if (!rendered)
            {
                renderLegacyRigidArmor(poseStack, bufferSource, packedLight, armorModel, slot, itemStack, bipedData, texture);
            }
        }
    }

    /**
     * Render custom 3D armor model with Mo'Bends animation.
     * Uses Tier 2 renderer for custom models (GeckoLib, custom 3D armor, etc.)
     */
    private void renderCustomModelArmor(PoseStack poseStack, MultiBufferSource bufferSource,
                                        int packedLight, E entity, ArmorItem armorItem,
                                        Model customModel, EquipmentSlot slot,
                                        ItemStack itemStack, BipedEntityData<?> bipedData)
    {
        // Get the texture for custom armor (uses Forge hook for custom textures)
        ResourceLocation texture = getArmorTexture(entity, itemStack, armorItem, slot, null);
        if (texture == null) return;

        // Build render context for the custom model
        ArmorRenderContext<E> context = ArmorRenderContext.<E>builder()
                .entity(entity)
                .entityData(bipedData)
                .slot(slot)
                .armorStack(itemStack)
                .poseStack(poseStack)
                .bufferSource(bufferSource)
                .packedLight(packedLight)
                .packedOverlay(OverlayTexture.NO_OVERLAY)
                .partialTicks(0)
                .armorModel(null) // Custom model, not HumanoidModel
                .armorColor(0xFFFFFFFF)
                .build();

        // Use Tier 2 renderer for custom models
        boolean rendered = armorFacade.getTier2Renderer().renderWithTexture(
                context, customModel, texture, itemStack.hasFoil());

        if (!rendered)
        {
            // Fallback: render the custom model directly without Mo'Bends transforms
            renderCustomModelVanilla(poseStack, bufferSource, packedLight, entity, armorItem, customModel, slot, itemStack);
        }
    }

    /**
     * Render custom 3D armor model without Mo'Bends animation (vanilla style).
     */
    private void renderCustomModelVanilla(PoseStack poseStack, MultiBufferSource bufferSource,
                                          int packedLight, E entity, ArmorItem armorItem,
                                          Model customModel, EquipmentSlot slot, ItemStack itemStack)
    {
        ResourceLocation texture = getArmorTexture(entity, itemStack, armorItem, slot, null);
        if (texture == null) return;

        poseStack.pushPose();

        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
                bufferSource, RenderType.armorCutoutNoCull(texture), false, itemStack.hasFoil());

        customModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                                   1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }

    /**
     * Legacy rigid armor rendering using vertex capture.
     * Used as fallback if the new three-tier system fails.
     */
    private void renderLegacyRigidArmor(PoseStack poseStack, MultiBufferSource bufferSource,
                                        int packedLight, HumanoidModel<E> armorModel, EquipmentSlot slot,
                                        ItemStack itemStack, BipedEntityData<?> bipedData, ResourceLocation texture)
    {
        renderLegacyRigidArmorWithColor(poseStack, bufferSource, packedLight, armorModel, slot, itemStack, bipedData, texture, 1.0f, 1.0f, 1.0f);
    }

    /**
     * Legacy rigid armor rendering with color tint.
     * Used for dyeable armor like leather.
     */
    private void renderLegacyRigidArmorWithColor(PoseStack poseStack, MultiBufferSource bufferSource,
                                                  int packedLight, HumanoidModel<E> armorModel, EquipmentSlot slot,
                                                  ItemStack itemStack, BipedEntityData<?> bipedData, ResourceLocation texture,
                                                  float red, float green, float blue)
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
        armorModel.renderToBuffer(capturePoseStack, captureConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                                  red, green, blue, 1.0F);

        // Restore original poses
        restoreModelPoses(armorModel, snapshot);

        // Now render the captured vertices with Mo' Bends transforms
        VertexConsumer outputConsumer = ItemRenderer.getArmorFoilBuffer(
                bufferSource, RenderType.armorCutoutNoCull(texture), false, itemStack.hasFoil());

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
        // Sync poses to vanilla model from our mutator
        if (mutator != null)
        {
            mutator.syncPosesToVanillaModel(armorModel);
        }

        // Check if this is dyeable armor (leather)
        if (armorItem instanceof DyeableLeatherItem dyeableItem)
        {
            // Get dye color and convert to RGB components (0.0-1.0)
            int color = dyeableItem.getColor(itemStack);
            float red = (float)(color >> 16 & 255) / 255.0F;
            float green = (float)(color >> 8 & 255) / 255.0F;
            float blue = (float)(color & 255) / 255.0F;

            // Render base texture with dye color
            ResourceLocation baseTexture = getArmorTexture(entity, itemStack, armorItem, slot, null);
            if (baseTexture != null)
            {
                VertexConsumer baseConsumer = ItemRenderer.getArmorFoilBuffer(
                        bufferSource, RenderType.armorCutoutNoCull(baseTexture), false, itemStack.hasFoil());
                armorModel.renderToBuffer(poseStack, baseConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                                          red, green, blue, 1.0F);
            }

            // Render overlay texture without color (white)
            ResourceLocation overlayTexture = getArmorTexture(entity, itemStack, armorItem, slot, "overlay");
            if (overlayTexture != null)
            {
                VertexConsumer overlayConsumer = ItemRenderer.getArmorFoilBuffer(
                        bufferSource, RenderType.armorCutoutNoCull(overlayTexture), false, itemStack.hasFoil());
                armorModel.renderToBuffer(poseStack, overlayConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                                          1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
        else
        {
            // Standard armor - single texture, no color tint
            ResourceLocation texture = getArmorTexture(entity, itemStack, armorItem, slot, null);
            if (texture == null) return;

            VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
                    bufferSource, RenderType.armorCutoutNoCull(texture), false, itemStack.hasFoil());

            armorModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                                      1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    /**
     * Get the armor texture for the given item and slot.
     * First tries the Forge hook on ArmorItem for custom textures,
     * then falls back to vanilla texture path logic.
     */
    private ResourceLocation getArmorTexture(E entity, ItemStack itemStack, ArmorItem armorItem, EquipmentSlot slot, @Nullable String overlay)
    {
        // Try Forge hook first - allows mods to provide custom armor textures
        String customTexture = armorItem.getArmorTexture(itemStack, entity, slot, overlay);
        if (customTexture != null)
        {
            return new ResourceLocation(customTexture);
        }

        // Fall back to vanilla texture location logic
        String material = armorItem.getMaterial().getName();
        String domain = "minecraft";
        String path = material;

        if (material.contains(":"))
        {
            String[] split = material.split(":", 2);
            domain = split[0];
            path = split[1];
        }

        int layer = usesInnerModel(slot) ? 2 : 1;
        String suffix = overlay == null ? "" : "_" + overlay;

        return new ResourceLocation(domain, "textures/models/armor/" + path + "_layer_" + layer + suffix + ".png");
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
