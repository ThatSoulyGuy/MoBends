package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.armor.ArmorModelProviderHolder;
import goblinbob.mobends.api.rendering.IArmorLayerProvider;
import goblinbob.mobends.api.armor.IArmorTextureProvider;
import goblinbob.mobends.api.rendering.IArmorHelper;
import goblinbob.mobends.api.rendering.IModelRenderHelper;
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
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class LayerCustomBipedArmor<E extends LivingEntity, M extends HumanoidModel<E>> extends RenderLayer<E, M>
{
    private static final java.util.Map<Class<?>, Boolean> SELF_RENDERING_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private final BipedMutator<?, E, M> mutator;
    private HumanoidArmorLayer<E, M, ?> vanillaArmorLayer;

    private HumanoidModel<E> innerModel;
    private HumanoidModel<E> outerModel;

    private final ArmorRenderingFacade armorFacade = new ArmorRenderingFacade();

    @Deprecated
    private final RigidArmorRenderer rigidRenderer = new RigidArmorRenderer();

    public LayerCustomBipedArmor(LivingEntityRenderer<E, M> renderer, BipedMutator<?, E, M> mutator)
    {
        super(renderer);
        this.mutator = mutator;
    }

    public void setVanillaArmorLayer(HumanoidArmorLayer<E, M, ?> vanillaLayer)
    {
        this.vanillaArmorLayer = vanillaLayer;
    }

    @SuppressWarnings("unchecked")
    public void setArmorModels(HumanoidModel<?> innerModel, HumanoidModel<?> outerModel)
    {
        this.innerModel = (HumanoidModel<E>) innerModel;
        this.outerModel = (HumanoidModel<E>) outerModel;
    }

    public void initArmor()
    {
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       E entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        EntityData<?> entityData = EntityDatabase.instance.get(entity);
        boolean hasBendsAnimation = entityData instanceof BipedEntityData;

        renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.CHEST, packedLight, hasBendsAnimation, entityData);
        renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.LEGS, packedLight, hasBendsAnimation, entityData);
        renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.FEET, packedLight, hasBendsAnimation, entityData);
        renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.HEAD, packedLight, hasBendsAnimation, entityData);
    }

    @SuppressWarnings("unchecked")
    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource,
                                  E entity, EquipmentSlot slot, int packedLight,
                                  boolean hasBendsAnimation, EntityData<?> entityData)
    {
        ItemStack itemStack = entity.getItemBySlot(slot);
        if (itemStack.isEmpty()) return;

        if (!(itemStack.getItem() instanceof ArmorItem armorItem)) return;
        if (armorItem.getEquipmentSlot() != slot) return;

        boolean usesInnerModel = usesInnerModel(slot);
        HumanoidModel<E> defaultModel = usesInnerModel ? getInnerModel() : getOuterModel();
        if (defaultModel == null)
        {
            return;
        }

        M parentModel = getParentModel();
        parentModel.copyPropertiesTo(defaultModel);
        defaultModel.young = false;
        setPartVisibility(defaultModel, slot);

        Model customModel = ArmorModelProviderHolder.getProvider()
                .getCustomArmorModel(entity, itemStack, slot, defaultModel);

        boolean isCustomModel = (customModel != null && customModel != defaultModel);
        Model armorModel = isCustomModel ? customModel : defaultModel;

        if (isCustomModel && armorModel instanceof HumanoidModel<?>)
        {
            HumanoidModel<E> humanoidCustom = (HumanoidModel<E>) armorModel;
            parentModel.copyPropertiesTo(humanoidCustom);
            humanoidCustom.young = false;
            setPartVisibility(humanoidCustom, slot);
        }

        boolean shouldUseBends = hasBendsAnimation && !ModConfig.shouldKeepArmorAsVanilla(armorItem);

        if (isCustomModel && isSelfRenderingModel(armorModel))
        {
            if (mutator != null)
            {
                mutator.syncPosesToVanillaModel(
                        armorModel instanceof HumanoidModel<?> selfDrawn ? selfDrawn : defaultModel);
            }

            renderSelfDrawnArmor(poseStack, bufferSource, packedLight, entity, armorItem, armorModel, slot, itemStack);
            return;
        }

        if (shouldUseBends && entityData instanceof BipedEntityData<?>)
        {
            BipedEntityData<?> bipedData = (BipedEntityData<?>) entityData;

            if (bipedData instanceof PlayerData && PlayerPreviewer.isPreviewInProgress())
            {
                bipedData = (BipedEntityData<?>) PlayerPreviewer.getPreviewData();
            }

            renderRigidArmor(poseStack, bufferSource, packedLight, entity, armorItem, armorModel, slot, itemStack, bipedData, isCustomModel);
        }
        else
        {
            renderVanillaArmor(poseStack, bufferSource, packedLight, entity, armorItem, armorModel, slot, itemStack);
        }
    }

    private void renderRigidArmor(PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, E entity, ArmorItem armorItem,
                                  Model armorModel, EquipmentSlot slot,
                                  ItemStack itemStack, BipedEntityData<?> bipedData,
                                  boolean isCustomModel)
    {
        if (isCustomModel)
        {
            IArmorLayerProvider layerProvider = IArmorLayerProvider.Holder.getProvider();
            final boolean[] anyRendered = {false};

            if (layerProvider != null)
            {
                layerProvider.forEachLayer(armorItem, layer -> {
                    ResourceLocation texture = resolveArmorTexture(armorItem, itemStack, entity, slot, layer, null);
                    if (texture == null)
                    {
                        return;
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
                        anyRendered[0] = true;
                    }
                });
            }

            if (!anyRendered[0])
            {
                ResourceLocation fallbackTexture = getArmorTexture(armorItem, itemStack, entity, slot, null);
                if (fallbackTexture != null)
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
                            fallbackTexture
                    );

                    if (!rendered)
                    {
                        renderLegacyRigidArmor(poseStack, bufferSource, packedLight, armorModel, slot, itemStack, bipedData, fallbackTexture);
                    }
                }
            }
        }
        else
        {
            ResourceLocation texture = getArmorTexture(armorItem, itemStack, entity, slot, null);
            if (texture == null)
            {
                return;
            }

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

    private void renderLegacyRigidArmor(PoseStack poseStack, MultiBufferSource bufferSource,
                                        int packedLight, Model armorModel, EquipmentSlot slot,
                                        ItemStack itemStack, BipedEntityData<?> bipedData, ResourceLocation texture)
    {
        if (!(armorModel instanceof HumanoidModel<?> humanoidModel))
        {
            return;
        }

        ModelPoseSnapshot snapshot = saveModelPoses(humanoidModel);

        resetToRestPose(humanoidModel);

        CapturingVertexConsumer captureConsumer = rigidRenderer.getCaptureConsumer();

        PoseStack capturePoseStack = new PoseStack();
        IModelRenderHelper.Holder.getHelper().renderModelToBuffer(armorModel, capturePoseStack, captureConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        restoreModelPoses(humanoidModel, snapshot);

        VertexConsumer outputConsumer = (VertexConsumer) IModelRenderHelper.Holder.getHelper().getArmorFoilBuffer(
                bufferSource, RenderType.armorCutoutNoCull(texture), itemStack.hasFoil());

        rigidRenderer.renderCapturedVertices(poseStack, outputConsumer, packedLight, OverlayTexture.NO_OVERLAY, bipedData);
    }

    private void resetToRestPose(HumanoidModel<?> model)
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
    }

    private ModelPoseSnapshot saveModelPoses(HumanoidModel<?> model)
    {
        return new ModelPoseSnapshot(model);
    }

    private void restoreModelPoses(HumanoidModel<?> model, ModelPoseSnapshot snapshot)
    {
        snapshot.restore(model);
    }

    private void renderVanillaArmor(PoseStack poseStack, MultiBufferSource bufferSource,
                                    int packedLight, E entity, ArmorItem armorItem,
                                    Model armorModel, EquipmentSlot slot, ItemStack itemStack)
    {
        ResourceLocation texture = getArmorTexture(armorItem, itemStack, entity, slot, null);
        if (texture == null) return;

        VertexConsumer vertexConsumer = (VertexConsumer) IModelRenderHelper.Holder.getHelper().getArmorFoilBuffer(
                bufferSource, RenderType.armorCutoutNoCull(texture), itemStack.hasFoil());

        if (mutator != null && armorModel instanceof HumanoidModel<?> humanoidModel)
        {
            mutator.syncPosesToVanillaModel(humanoidModel);
        }

        IModelRenderHelper.Holder.getHelper().renderModelToBuffer(armorModel, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }

    private void renderSelfDrawnArmor(PoseStack poseStack, MultiBufferSource bufferSource,
                                      int packedLight, E entity, ArmorItem armorItem,
                                      Model armorModel, EquipmentSlot slot, ItemStack itemStack)
    {
        ResourceLocation texture = getArmorTexture(armorItem, itemStack, entity, slot, null);
        if (texture == null)
        {
            return;
        }

        VertexConsumer vertexConsumer = (VertexConsumer) IModelRenderHelper.Holder.getHelper().getArmorFoilBuffer(
                bufferSource, RenderType.armorCutoutNoCull(texture), itemStack.hasFoil());

        IModelRenderHelper.Holder.getHelper().renderModelToBuffer(armorModel, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }

    private static boolean isSelfRenderingModel(Model model)
    {
        Class<?> modelClass = model.getClass();

        Boolean cached = SELF_RENDERING_CACHE.get(modelClass);
        if (cached != null)
        {
            return cached;
        }

        boolean selfRendering = false;

        for (Class<?> current = modelClass;
             current != null && current != HumanoidModel.class && current != Model.class && Model.class.isAssignableFrom(current);
             current = current.getSuperclass())
        {
            for (java.lang.reflect.Method method : current.getDeclaredMethods())
            {
                String name = method.getName();
                if (("renderToBuffer".equals(name) || "m_7695_".equals(name)) && method.getParameterCount() >= 5)
                {
                    selfRendering = true;
                    break;
                }
            }

            if (selfRendering)
            {
                break;
            }
        }

        SELF_RENDERING_CACHE.put(modelClass, selfRendering);
        return selfRendering;
    }

    private ResourceLocation getArmorTexture(ArmorItem armorItem, ItemStack itemStack, E entity, EquipmentSlot slot, @Nullable String overlay)
    {
        return resolveArmorTexture(armorItem, itemStack, entity, slot, null, overlay);
    }

    private ResourceLocation resolveArmorTexture(ArmorItem armorItem, ItemStack itemStack, E entity, EquipmentSlot slot,
                                                 @Nullable Object layer, @Nullable String overlay)
    {
        boolean isInnerModel = usesInnerModel(slot);

        IArmorTextureProvider textureProvider = IArmorTextureProvider.Holder.getProvider();
        ResourceLocation customTexture = textureProvider.getArmorTexture(armorItem, itemStack, entity, slot, layer, isInnerModel);
        if (customTexture != null)
        {
            return customTexture;
        }

        IArmorHelper helper = IArmorHelper.Holder.getHelper();
        String materialName = helper != null ? helper.getArmorMaterialName(armorItem) : "leather";

        String domain = "minecraft";
        String path = materialName;
        int colonIndex = materialName.indexOf(':');
        if (colonIndex >= 0)
        {
            domain = materialName.substring(0, colonIndex);
            path = materialName.substring(colonIndex + 1);
        }

        int layerIndex = isInnerModel ? 2 : 1;
        String suffix = overlay == null ? "" : "_" + overlay;

        return goblinbob.mobends.core.util.ResourceLocationFactory.create(domain, "textures/models/armor/" + path + "_layer_" + layerIndex + suffix + ".png");
    }

    private boolean usesInnerModel(EquipmentSlot slot)
    {
        return slot == EquipmentSlot.LEGS;
    }

    private HumanoidModel<E> getInnerModel()
    {
        return innerModel;
    }

    private HumanoidModel<E> getOuterModel()
    {
        return outerModel;
    }

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

    public ArmorRenderingFacade getArmorFacade()
    {
        return armorFacade;
    }

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
