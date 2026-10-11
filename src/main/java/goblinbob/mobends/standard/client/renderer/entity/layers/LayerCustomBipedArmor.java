package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.platform.armor.ArmorModelProviderHolder;
import goblinbob.mobends.api.rendering.IArmorLayerProvider;
import goblinbob.mobends.platform.armor.IArmorTextureProvider;
import goblinbob.mobends.api.rendering.IArmorHelper;
import goblinbob.mobends.api.rendering.IModelRenderHelper;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderingFacade;
import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import goblinbob.mobends.standard.client.model.armor.CapturedVertex;
import goblinbob.mobends.standard.client.model.armor.CapturingVertexConsumer;
import goblinbob.mobends.standard.client.model.armor.RigidArmorRenderer;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.main.ModConfig;
import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.previewer.PlayerPreviewer;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.model.EntityModel;
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

public class LayerCustomBipedArmor<E extends LivingEntity, M extends EntityModel<E>> extends RenderLayer<E, M>
{
    private static final java.util.Map<Class<?>, Boolean> SELF_RENDERING_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private final BipedMutator<?, E, M> mutator;
    private HumanoidArmorLayer<?, ?, ?> vanillaArmorLayer;

    private HumanoidModel<E> innerModel;
    private HumanoidModel<E> outerModel;

    private final ArmorRenderingFacade armorFacade = new ArmorRenderingFacade();

    private final goblinbob.mobends.standard.client.model.armor.TinkersArmorProxyModel tinkersProxy =
            new goblinbob.mobends.standard.client.model.armor.TinkersArmorProxyModel();

    private net.minecraft.client.model.ElytraModel<E> tinkersWings;

    @Deprecated
    private final RigidArmorRenderer rigidRenderer = new RigidArmorRenderer();

    public LayerCustomBipedArmor(LivingEntityRenderer<E, M> renderer, BipedMutator<?, E, M> mutator)
    {
        super(renderer);
        this.mutator = mutator;
    }

    public void setVanillaArmorLayer(HumanoidArmorLayer<?, ?, ?> vanillaLayer)
    {
        this.vanillaArmorLayer = vanillaLayer;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private HumanoidModel<E> getHumanoidParentModel()
    {
        final M parentModel = getParentModel();
        final HumanoidModel<?> view = mutator != null ? mutator.humanoidViewOf(parentModel) : null;

        if (view != null)
        {
            return (HumanoidModel<E>) view;
        }

        return parentModel instanceof HumanoidModel<?> ? (HumanoidModel<E>) parentModel : null;
    }

    private void copyParentProperties(HumanoidModel<E> target)
    {
        final HumanoidModel<E> humanoidParent = getHumanoidParentModel();

        if (humanoidParent != null)
        {
            humanoidParent.copyPropertiesTo(target);
            return;
        }

        final M parentModel = getParentModel();

        if (parentModel != null)
        {
            parentModel.copyPropertiesTo(target);
        }
    }

    @SuppressWarnings("unchecked")
    public void setArmorModels(HumanoidModel<?> innerModel, HumanoidModel<?> outerModel)
    {
        this.innerModel = (HumanoidModel<E>) innerModel;
        this.outerModel = (HumanoidModel<E>) outerModel;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       E entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (goblinbob.mobends.compat.ModernCompanionsCompat.shouldSkipArmorLayer(vanillaArmorLayer, entity)
                || goblinbob.mobends.compat.AntarchyCompat.isArmorHidden(entity))
        {
            return;
        }

        if (goblinbob.mobends.compat.MineColoniesCompat.shouldRenderVanillaArmorLayer(vanillaArmorLayer, entity))
        {
            renderVanillaArmorLayer(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount,
                    partialTicks, ageInTicks, netHeadYaw, headPitch);
            return;
        }

        EntityData<?> entityData = EntityDatabase.instance.get(entity);
        boolean hasBendsAnimation = entityData instanceof BipedEntityData
                && goblinbob.mobends.core.util.BenderHelper.isEntityAnimated(entity)
                && !goblinbob.mobends.compat.ModCompatManager.shouldDeferAnimation(entity)
                && !goblinbob.mobends.compat.BetterCombatCompat.shouldYieldModel(entity);

        goblinbob.mobends.core.client.MoBendsRenderContext.beginArmorRender();
        poseStack.pushPose();

        if (hasBendsAnimation)
        {
            poseStack.last().pose().scaleLocal(DEPTH_BIAS_SCALE);
        }

        try
        {
            renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.CHEST, packedLight, hasBendsAnimation, entityData);
            renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.LEGS, packedLight, hasBendsAnimation, entityData);
            renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.FEET, packedLight, hasBendsAnimation, entityData);
            renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.HEAD, packedLight, hasBendsAnimation, entityData);
        }
        finally
        {
            poseStack.popPose();
            goblinbob.mobends.core.client.MoBendsRenderContext.endArmorRender();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void renderVanillaArmorLayer(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                         E entity, float limbSwing, float limbSwingAmount,
                                         float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        ((HumanoidArmorLayer) vanillaArmorLayer).render(poseStack, bufferSource, packedLight, entity,
                limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
    }

    @SuppressWarnings("unchecked")
    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource,
                                  E entity, EquipmentSlot slot, int packedLight,
                                  boolean hasBendsAnimation, EntityData<?> entityData)
    {
        ItemStack itemStack = goblinbob.mobends.compat.MineColoniesCompat.displayArmor(entity, slot);
        if (itemStack == null)
        {
            itemStack = entity.getItemBySlot(slot);
        }
        if (itemStack.isEmpty()) return;

        final Object previousRuneColor =
                goblinbob.mobends.compat.QuarkColorRunesCompat.beginItem(itemStack);

        try
        {
            renderArmorPieceContents(poseStack, bufferSource, entity, slot, packedLight,
                    hasBendsAnimation, entityData, itemStack);
        }
        finally
        {
            goblinbob.mobends.compat.QuarkColorRunesCompat.endItem(previousRuneColor);
        }
    }

    private void renderArmorPieceContents(PoseStack poseStack, MultiBufferSource bufferSource,
                                          E entity, EquipmentSlot slot, int packedLight,
                                          boolean hasBendsAnimation, EntityData<?> entityData,
                                          ItemStack itemStack)
    {
        if (isHiddenByFirstPersonView(entity, slot)) return;

        if (!(itemStack.getItem() instanceof ArmorItem armorItem)) return;
        if (armorItem.getEquipmentSlot() != slot) return;

        if (goblinbob.mobends.compat.WearableBackpacksCompat.isBackpackItem(itemStack)) return;

        boolean usesInnerModel = usesInnerModel(slot);
        HumanoidModel<E> defaultModel = usesInnerModel ? innerModel : outerModel;
        if (defaultModel == null)
        {
            return;
        }

        copyParentProperties(defaultModel);
        defaultModel.young = false;
        setPartVisibility(defaultModel, slot);

        Model customModel = ArmorModelProviderHolder.getProvider()
                .getCustomArmorModel(entity, itemStack, slot, defaultModel);
        final Model providerModel = customModel;

        if (customModel == null || customModel == defaultModel)
        {
            final Model uranusModel = goblinbob.mobends.compat.UranusCompat.getArmorModel(entity, itemStack, slot, defaultModel);

            if (uranusModel != null)
            {
                customModel = uranusModel;
            }
        }

        if (customModel == null || customModel == defaultModel)
        {
            final Model armorApiModel = goblinbob.mobends.compat.ArmorModelApiCompat.getArmorModel(itemStack, slot);

            if (armorApiModel != null)
            {
                customModel = armorApiModel;
            }
        }

        if (customModel == null || customModel == defaultModel)
        {
            final Model azureModel = goblinbob.mobends.compat.AzureLibCompat.getArmorModel(entity, itemStack, slot, defaultModel);

            if (azureModel != null)
            {
                customModel = azureModel;
            }
        }

        if (customModel == null || customModel == defaultModel)
        {
            Model geoModel = goblinbob.mobends.standard.client.model.armor.GeckoLibArmorSupport
                    .getArmorRenderer(entity, itemStack, slot, defaultModel);

            if (geoModel != null)
            {
                customModel = geoModel;
            }
        }
        else
        {
            goblinbob.mobends.standard.client.model.armor.GeckoLibArmorSupport
                    .prepare(customModel, entity, itemStack, slot, defaultModel);
        }

        final goblinbob.mobends.standard.client.model.armor.PalladiumSupport.Armor palladiumArmor =
                goblinbob.mobends.standard.client.model.armor.PalladiumSupport.resolve(itemStack, entity, slot);

        if (palladiumArmor != null && palladiumArmor.model != null)
        {
            customModel = palladiumArmor.model;
        }

        boolean isCustomModel = (customModel != null && customModel != defaultModel);
        Model armorModel = isCustomModel ? customModel : defaultModel;

        if (isCustomModel && armorModel instanceof HumanoidModel<?>)
        {
            HumanoidModel<E> humanoidCustom = (HumanoidModel<E>) armorModel;
            copyParentProperties(humanoidCustom);
            humanoidCustom.young = false;

            if (armorModel != providerModel || isBendableGeoArmor(armorModel)
                    || goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport.isTinkersArmorModel(armorModel))
            {
                setPartVisibility(humanoidCustom, slot);
            }
        }

        boolean shouldUseBends = hasBendsAnimation && !ModConfig.shouldKeepArmorAsVanilla(armorItem)
                && (mutator == null || mutator.shouldRenderCustom());

        if (entity instanceof net.minecraft.world.entity.player.Player
                && goblinbob.mobends.compat.ColdSweatCompat.isChameleonArmor(itemStack))
        {
            renderChameleonArmor(poseStack, bufferSource, packedLight, entity, slot, itemStack,
                    shouldUseBends && entityData instanceof BipedEntityData<?>
                            ? previewAware((BipedEntityData<?>) entityData)
                            : null);
            return;
        }

        if (palladiumArmor != null)
        {
            renderPalladiumArmor(poseStack, bufferSource, packedLight, entity, slot, itemStack,
                    armorModel, palladiumArmor,
                    shouldUseBends && entityData instanceof BipedEntityData<?>
                            ? (BipedEntityData<?>) entityData
                            : null);
            return;
        }

        if (isCustomModel
                && goblinbob.mobends.standard.client.model.armor.LegendsArmorSupport.isSuitModel(armorModel)
                && goblinbob.mobends.standard.client.model.armor.LegendsArmorSupport.isSuitItem(itemStack))
        {
            renderLegendsArmor(poseStack, bufferSource, packedLight, entity, slot, itemStack, armorItem, armorModel,
                    shouldUseBends && entityData instanceof BipedEntityData<?>
                            ? (BipedEntityData<?>) entityData
                            : null);
            return;
        }


        if (isCustomModel && shouldUseBends && entityData instanceof BipedEntityData<?>
                && isBendableGeoArmor(armorModel))
        {
            BipedEntityData<?> geoData = previewAware((BipedEntityData<?>) entityData);

            if (renderCapturedGeoArmor(poseStack, bufferSource, packedLight, armorModel, defaultModel, itemStack, geoData, slot))
            {
                return;
            }
        }

        final boolean isTinkersArmor = isCustomModel
                && goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport.isTinkersArmorModel(armorModel);

        if (isTinkersArmor && slot != EquipmentSlot.HEAD && shouldUseBends && entityData instanceof BipedEntityData<?>)
        {
            BipedEntityData<?> tinkersData = previewAware((BipedEntityData<?>) entityData);

            if (renderTinkersArmor(poseStack, bufferSource, packedLight, entity, slot, itemStack,
                    armorModel, defaultModel, tinkersData))
            {
                return;
            }
        }

        if (isCustomModel && isSelfRenderingModel(armorModel))
        {
            if (!isTinkersArmor && shouldUseBends && entityData instanceof BipedEntityData<?>
                    && !isBendableGeoArmor(armorModel) && armorModel instanceof HumanoidModel<?> selfDrawnHumanoid)
            {
                if (drawsNothing(armorModel, packedLight)) return;

                final BipedEntityData<?> selfDrawnData = previewAware((BipedEntityData<?>) entityData);

                renderRigidArmor(poseStack, bufferSource, packedLight, entity, armorItem, armorModel, slot, itemStack,
                        selfDrawnData, true);
                final ResourceLocation selfDrawnTexture = getArmorTexture(armorItem, itemStack, entity, slot, null);

                if (selfDrawnTexture != null)
                {
                    renderExtraParts(poseStack, bufferSource, packedLight, entity, selfDrawnHumanoid, slot, itemStack,
                            selfDrawnData, (VertexConsumer) IModelRenderHelper.Holder.getHelper().getArmorFoilBuffer(
                                    bufferSource, RenderType.armorCutoutNoCull(selfDrawnTexture), itemStack.hasFoil()),
                            null);
                }

                final VertexConsumer selfDrawnTrim = getTrimBuffer(bufferSource, entity, armorItem, itemStack, slot);

                if (selfDrawnTrim != null)
                {
                    renderExtraParts(poseStack, bufferSource, packedLight, entity, selfDrawnHumanoid, slot, itemStack,
                            selfDrawnData, selfDrawnTrim, 0xFFFFFFFF);
                }

                final ResourceLocation glowTexture = goblinbob.mobends.compat.CrysisCompat.getGlowTexture(armorModel);

                if (glowTexture != null)
                {
                    final PoseStack glowPose = new PoseStack();
                    glowPose.last().pose().scaling(DEPTH_BIAS_SCALE).mul(poseStack.last().pose());
                    glowPose.last().normal().set(poseStack.last().normal());

                    armorFacade.renderArmorLayer(glowPose, bufferSource, packedLight, entity, slot, itemStack,
                            armorModel, selfDrawnData, glowTexture,
                            goblinbob.mobends.compat.CrysisCompat.getGlowColor(armorModel),
                            RenderType::entityTranslucentEmissive);
                }
                return;
            }

            if (mutator != null && !goblinbob.mobends.compat.BetterCombatCompat.shouldYieldModel(entity))
            {
                mutator.syncPosesToVanillaModel(
                        armorModel instanceof HumanoidModel<?> selfDrawn ? selfDrawn : defaultModel);
            }

            BipedEntityData<?> skullData = null;

            if (isTinkersArmor && shouldUseBends && entityData instanceof BipedEntityData<?>)
            {
                skullData = previewAware((BipedEntityData<?>) entityData);
            }

            final net.minecraft.client.model.SkullModelBase skull = skullData != null
                    ? goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport.getSkullModel(armorModel)
                    : null;

            if (skull != null)
            {
                goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport.setSkullModel(armorModel, null);
            }

            try
            {
                renderVanillaArmor(poseStack, bufferSource, packedLight, entity, armorItem, armorModel, slot, itemStack, false);
            }
            finally
            {
                if (skull != null)
                {
                    goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport.setSkullModel(armorModel, skull);
                }
            }

            if (skull != null)
            {
                renderTinkersSkull(poseStack, bufferSource, packedLight, itemStack, armorModel, skull, skullData);
            }

            return;
        }

        if (shouldUseBends && entityData instanceof BipedEntityData<?>)
        {
            BipedEntityData<?> bipedData = previewAware((BipedEntityData<?>) entityData);

            renderRigidArmor(poseStack, bufferSource, packedLight, entity, armorItem, armorModel, slot, itemStack, bipedData, isCustomModel);
        }
        else
        {
            renderVanillaArmor(poseStack, bufferSource, packedLight, entity, armorItem, armorModel, slot, itemStack, true);
        }
    }

    private static BipedEntityData<?> previewAware(BipedEntityData<?> data)
    {
        return data instanceof PlayerData && PlayerPreviewer.isPreviewInProgress()
                ? (BipedEntityData<?>) PlayerPreviewer.getPreviewData()
                : data;
    }

    private boolean renderTinkersArmor(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                       E entity, EquipmentSlot slot, ItemStack itemStack,
                                       Model tinkersModel, HumanoidModel<E> defaultModel,
                                       BipedEntityData<?> bipedData)
    {
        final java.util.List<?> layers =
                goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport.getLayers(tinkersModel);

        if (layers.isEmpty())
        {
            return false;
        }

        final Object registryAccess =
                goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport.getRegistryAccess(tinkersModel);
        final Object armorType =
                goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport.textureTypeFor(slot);
        final Object wingsType =
                goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport.wingsTextureType();
        final boolean drawWings = slot == EquipmentSlot.CHEST
                && goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport.hasWings(tinkersModel);

        boolean armorGlint = itemStack.hasFoil();
        boolean wingGlint = armorGlint;
        boolean rendered = false;

        for (Object supplier : layers)
        {
            final Object texture = goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport
                    .getArmorTexture(supplier, itemStack, armorType, registryAccess);

            if (texture != null)
            {
                tinkersProxy.bind((ps, vertexConsumer, light, overlay, color) ->
                        armorFacade.renderArmorIntoConsumer(ps, bufferSource, vertexConsumer, light, overlay,
                                entity, slot, itemStack, defaultModel, bipedData, color));

                if (goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport.renderTexture(
                        texture, tinkersProxy, poseStack, bufferSource, packedLight,
                        OverlayTexture.NO_OVERLAY, armorGlint))
                {
                    rendered = true;
                    armorGlint = false;
                }
            }

            if (!drawWings)
            {
                continue;
            }

            final Object wingTexture = goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport
                    .getArmorTexture(supplier, itemStack, wingsType, registryAccess);

            if (wingTexture == null)
            {
                continue;
            }

            final net.minecraft.client.model.ElytraModel<E> wingsModel = getTinkersWings(entity);

            tinkersProxy.bind((ps, vertexConsumer, light, overlay, color) ->
                    renderTinkersWings(ps, vertexConsumer, wingsModel, bipedData, light, overlay, color));

            if (goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport.renderTexture(
                    wingTexture, tinkersProxy, poseStack, bufferSource, packedLight,
                    OverlayTexture.NO_OVERLAY, wingGlint))
            {
                rendered = true;
                wingGlint = false;
            }
        }

        tinkersProxy.bind(null);

        return rendered;
    }

    private net.minecraft.client.model.ElytraModel<E> getTinkersWings(E entity)
    {
        if (tinkersWings == null)
        {
            tinkersWings = new net.minecraft.client.model.ElytraModel<>(
                    net.minecraft.client.Minecraft.getInstance().getEntityModels()
                            .bakeLayer(net.minecraft.client.model.geom.ModelLayers.ELYTRA));
        }

        tinkersWings.young = false;
        tinkersWings.riding = false;
        tinkersWings.attackTime = 0.0F;
        tinkersWings.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        return tinkersWings;
    }

    private void renderTinkersWings(PoseStack poseStack, VertexConsumer vertexConsumer,
                                    net.minecraft.client.model.ElytraModel<E> wingsModel,
                                    BipedEntityData<?> bipedData, int packedLight, int packedOverlay, int color)
    {
        poseStack.pushPose();

        bipedData.body.applyCharacterTransform(poseStack, 0.0625F);
        poseStack.translate(0.0F, -0.75F, 0.125F);

        IModelRenderHelper.Holder.getHelper().renderModelToBuffer(wingsModel, poseStack, vertexConsumer,
                packedLight, packedOverlay, color);

        poseStack.popPose();
    }

    private void renderTinkersSkull(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                    ItemStack itemStack, Model tinkersModel,
                                    net.minecraft.client.model.SkullModelBase skull,
                                    BipedEntityData<?> bipedData)
    {
        final ResourceLocation texture =
                goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport.getSkullTexture(tinkersModel);

        if (texture == null)
        {
            return;
        }

        final int color =
                goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport.getSkullColor(tinkersModel);

        poseStack.pushPose();

        goblinbob.mobends.standard.client.model.armor.ArmorPoseHelper
                .applyPartTransform(poseStack, bipedData.body, true);
        goblinbob.mobends.standard.client.model.armor.ArmorPoseHelper
                .applyPartTransform(poseStack, bipedData.head, true);
        poseStack.scale(1.115F, 1.115F, 1.115F);

        skull.setupAnim(goblinbob.mobends.standard.client.model.armor.TinkersArmorSupport
                .getSkullWalkAnimation(tinkersModel), 0.0F, 0.0F);

        final IModelRenderHelper renderHelper = IModelRenderHelper.Holder.getHelper();
        final VertexConsumer vertexConsumer = (VertexConsumer) renderHelper.getArmorFoilBuffer(
                bufferSource, RenderType.entityCutoutNoCullZOffset(texture), itemStack.hasFoil());

        renderHelper.renderModelToBuffer(skull, poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY, color == -1 ? 0xFFFFFFFF : color);

        poseStack.popPose();
    }

    private static boolean isBendableGeoArmor(Model armorModel)
    {
        if (goblinbob.mobends.compat.AzureLibCompat.isArmorModel(armorModel))
        {
            return true;
        }

        try
        {
            return Class.forName("software.bernie.geckolib.renderer.GeoArmorRenderer").isInstance(armorModel);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Nullable
    private static Object invokeNoArg(Object target, String name)
    {
        try
        {
            return target.getClass().getMethod(name).invoke(target);
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }

    @Nullable
    private static ResourceLocation geoArmorTexture(Model armorModel, ItemStack itemStack)
    {
        if (goblinbob.mobends.compat.AzureLibCompat.isArmorModel(armorModel))
        {
            return goblinbob.mobends.compat.AzureLibCompat.getTexture(itemStack);
        }

        try
        {
            Object animatable = invokeNoArg(armorModel, "getAnimatable");

            if (animatable == null)
            {
                Object current = invokeNoArg(armorModel, "getCurrentStack");
                if (current instanceof ItemStack currentStack && !currentStack.isEmpty())
                {
                    animatable = currentStack.getItem();
                }
            }

            if (animatable == null && itemStack != null && !itemStack.isEmpty())
            {
                animatable = itemStack.getItem();
            }

            if (animatable == null)
            {
                return null;
            }

            for (java.lang.reflect.Method method : armorModel.getClass().getMethods())
            {
                if ("getTextureLocation".equals(method.getName())
                        && method.getParameterCount() == 1
                        && method.getReturnType() == ResourceLocation.class)
                {
                    try
                    {
                        ResourceLocation resolved = (ResourceLocation) method.invoke(armorModel, animatable);
                        if (resolved != null)
                        {
                            return resolved;
                        }
                    }
                    catch (Throwable ignored)
                    {
                    }
                }
            }

        }
        catch (Exception e)
        {
            return null;
        }

        return null;
    }

    private static final float DEPTH_BIAS_SCALE = 0.9997F;

    private static final float ELBOW_Y = 6.0F / 16.0F;
    private static final float KNEE_Y = 18.0F / 16.0F;
    private static final float JOINT_BLEND_BAND = 2.0F / 16.0F;
    private static final float SKIRT_MIN_Y = 13.0F / 16.0F;
    private static final float SKIRT_KNEE_FOLLOW = 0.75F;

    private enum GeoPart
    {
        HEAD, BODY, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG
    }

    private boolean renderCapturedGeoArmor(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                           Model armorModel, HumanoidModel<E> defaultModel,
                                           ItemStack itemStack, BipedEntityData<?> bipedData, EquipmentSlot slot)
    {
        ResourceLocation texture = geoArmorTexture(armorModel, itemStack);
        if (texture == null)
        {
            return false;
        }

        goblinbob.mobends.standard.client.model.armor.ArmorCaptureContext.clearEmissive();

        float[] savedState = captureArmorPartState(defaultModel);
        boolean[] savedVisibility = captureArmorPartVisibility(defaultModel);

        applyArmorRestPose(defaultModel);

        java.util.Map<RenderType, TypedGeometry> geometry = new java.util.LinkedHashMap<>();

        java.util.Set<String> alwaysDrawn = captureAlwaysDrawn(armorModel, defaultModel, packedLight,
                savedVisibility, slot, geometry);

        for (GeoPart part : GeoPart.values())
        {
            capturePart(armorModel, defaultModel, packedLight, part, savedVisibility, alwaysDrawn, geometry);
        }

        restoreArmorPartVisibility(defaultModel, savedVisibility);
        restoreArmorPartState(defaultModel, savedState);

        final java.util.List<RenderType> emissiveTypes =
                goblinbob.mobends.standard.client.model.armor.ArmorCaptureContext.drainEmissive();

        TypedGeometry primary = null;

        for (TypedGeometry candidate : geometry.values())
        {
            if (!candidate.vertices.isEmpty())
            {
                primary = candidate;
                break;
            }
        }

        if (primary == null)
        {
            return false;
        }

        for (java.util.Map.Entry<RenderType, TypedGeometry> entry : geometry.entrySet())
        {
            final TypedGeometry bucket = entry.getValue();

            if (bucket.vertices.isEmpty())
            {
                continue;
            }

            final RenderType renderType = bucket == primary || entry.getKey() == null
                    ? RenderType.armorCutoutNoCull(texture)
                    : entry.getKey();

            VertexConsumer outputConsumer = (VertexConsumer) IModelRenderHelper.Holder.getHelper().getArmorFoilBuffer(
                    bufferSource, renderType, itemStack.hasFoil());

            rigidRenderer.renderTaggedVertices(poseStack, outputConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                    bipedData, bucket.vertices, bucket.regions, bucket.blendRegions, bucket.blendWeights);
        }

        if (!emissiveTypes.isEmpty())
        {
            final PoseStack emissivePose = new PoseStack();
            emissivePose.last().pose().scaling(DEPTH_BIAS_SCALE).mul(poseStack.last().pose());
            emissivePose.last().normal().set(poseStack.last().normal());

            for (RenderType emissiveType : emissiveTypes)
            {
                rigidRenderer.renderTaggedVertices(emissivePose, bufferSource.getBuffer(emissiveType),
                        packedLight, OverlayTexture.NO_OVERLAY,
                        bipedData, primary.vertices, primary.regions, primary.blendRegions, primary.blendWeights);
            }
        }

        return true;
    }

    private static class TypedGeometry
    {
        final java.util.List<goblinbob.mobends.standard.client.model.armor.CapturedVertex> vertices = new java.util.ArrayList<>();
        final java.util.List<goblinbob.mobends.standard.client.model.armor.BoneRegion> regions = new java.util.ArrayList<>();
        final java.util.List<goblinbob.mobends.standard.client.model.armor.BoneRegion> blendRegions = new java.util.ArrayList<>();
        final java.util.List<Float> blendWeights = new java.util.ArrayList<>();

        void add(CapturedVertex v, BoneRegion region, BoneRegion blendRegion, float blendWeight)
        {
            vertices.add(v);
            regions.add(region);
            blendRegions.add(blendRegion);
            blendWeights.add(blendWeight);
        }
    }

    private java.util.Set<String> captureAlwaysDrawn(Model armorModel, HumanoidModel<E> defaultModel,
                                                     int packedLight, boolean[] slotVisibility, EquipmentSlot slot,
                                                     java.util.Map<RenderType, TypedGeometry> geometry)
    {
        java.util.Map<RenderType, java.util.List<goblinbob.mobends.standard.client.model.armor.CapturedVertex>> capturedByType =
                captureGeo(armorModel, defaultModel, null, slotVisibility, packedLight);

        if (capturedByType.isEmpty())
        {
            return java.util.Collections.emptySet();
        }

        final java.util.Map<RenderType, java.util.List<CapturedVertex>> drawn = new java.util.LinkedHashMap<>(capturedByType);
        final java.util.Map<RenderType, GeoPart[]> owners = probeOwners(armorModel, defaultModel, slotVisibility, packedLight, drawn);

        java.util.Set<String> keys = new java.util.HashSet<>();

        for (java.util.Map.Entry<RenderType, java.util.List<CapturedVertex>> entry : drawn.entrySet())
        {
            emitAlwaysDrawn(entry.getValue(), owners.get(entry.getKey()), slot, keys,
                    geometry.computeIfAbsent(entry.getKey(), key -> new TypedGeometry()));
        }

        return keys;
    }

    private static final float PROBE_OFFSET = 64.0F;

    private static final GeoPart[] PROBE_ORDER = {
            GeoPart.BODY, GeoPart.HEAD, GeoPart.LEFT_ARM, GeoPart.RIGHT_ARM, GeoPart.LEFT_LEG, GeoPart.RIGHT_LEG
    };

    private java.util.Map<RenderType, GeoPart[]> probeOwners(Model armorModel, HumanoidModel<E> defaultModel,
                                                             boolean[] slotVisibility, int packedLight,
                                                             java.util.Map<RenderType, java.util.List<CapturedVertex>> drawn)
    {
        final java.util.Map<RenderType, GeoPart[]> owners = new java.util.HashMap<>();

        for (GeoPart part : PROBE_ORDER)
        {
            final ModelPart base = basePart(defaultModel, part);
            final float restX = base.x;
            base.x = restX + PROBE_OFFSET;

            try
            {
                for (java.util.Map.Entry<RenderType, java.util.List<CapturedVertex>> entry
                        : captureGeo(armorModel, defaultModel, null, slotVisibility, packedLight).entrySet())
                {
                    final java.util.List<CapturedVertex> rest = drawn.get(entry.getKey());
                    final java.util.List<CapturedVertex> moved = entry.getValue();

                    if (rest == null || rest.size() != moved.size())
                    {
                        continue;
                    }

                    final GeoPart[] typeOwners = owners.computeIfAbsent(entry.getKey(), key -> new GeoPart[rest.size()]);

                    for (int i = 0; i < moved.size(); ++i)
                    {
                        if (followsProbe(rest.get(i), moved.get(i)))
                        {
                            typeOwners[i] = part;
                        }
                    }
                }
            }
            finally
            {
                base.x = restX;
            }
        }

        return owners;
    }

    private static ModelPart basePart(HumanoidModel<?> model, GeoPart part)
    {
        switch (part)
        {
            case HEAD: return model.head;
            case LEFT_ARM: return model.leftArm;
            case RIGHT_ARM: return model.rightArm;
            case LEFT_LEG: return model.leftLeg;
            case RIGHT_LEG: return model.rightLeg;
            case BODY:
            default: return model.body;
        }
    }

    private static boolean followsProbe(CapturedVertex rest, CapturedVertex probed)
    {
        final float dx = probed.x - rest.x;
        final float dy = probed.y - rest.y;
        final float dz = probed.z - rest.z;

        return dx * dx + dy * dy + dz * dz > 1.0F;
    }

    private java.util.Map<RenderType, java.util.List<CapturedVertex>> captureGeo(Model armorModel, HumanoidModel<E> defaultModel,
                                                                              @Nullable GeoPart part, boolean[] slotVisibility,
                                                                              int packedLight)
    {
        applyOnlyVisible(defaultModel, part, slotVisibility);

        CapturingVertexConsumer capture = rigidRenderer.getCaptureConsumer();
        PoseStack capturePoseStack = new PoseStack();

        final com.mojang.blaze3d.vertex.VertexConsumer previousCapture =
                goblinbob.mobends.standard.client.model.armor.ArmorCaptureContext.begin(capture);
        try
        {
            goblinbob.mobends.standard.client.model.armor.GeckoLibArmorSupport.reprepare(armorModel);

            IModelRenderHelper.Holder.getHelper().renderModelToBuffer(armorModel, capturePoseStack, capture,
                    packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        }
        catch (Throwable ignored)
        {
        }
        finally
        {
            goblinbob.mobends.standard.client.model.armor.ArmorCaptureContext.end(previousCapture);
        }

        return capture.getVerticesByType();
    }

    private void emitAlwaysDrawn(java.util.List<goblinbob.mobends.standard.client.model.armor.CapturedVertex> captured,
                                 @Nullable GeoPart[] owners, EquipmentSlot slot,
                                 java.util.Set<String> keys, TypedGeometry out)
    {
        goblinbob.mobends.standard.client.model.armor.ArmorBoneAssignment assignment = new goblinbob.mobends.standard.client.model.armor.ArmorBoneAssignment();

        final boolean quadAligned = captured.size() % 4 == 0;

        for (CapturedVertex v : captured)
        {
            keys.add(vertexKey(v));
        }

        if (!quadAligned)
        {
            for (int i = 0; i < captured.size(); ++i)
            {
                final CapturedVertex v = captured.get(i);
                final GeoPart owner = owners == null ? null : owners[i];

                if (owner != null)
                {
                    emitVertex(owner, v, jointBlend(owner, v.y), out);
                    continue;
                }

                final BoneRegion region = assignment.assignVertexForSlot(v.x, v.y, v.z, slot);
                out.add(v, region, region, 0.0F);
            }

            return;
        }

        for (int q = 0; q + 3 < captured.size(); q += 4)
        {
            final GeoPart owner = owners == null ? null : owners[q];

            if (owner != null)
            {
                emitPartQuad(owner, java.util.Arrays.asList(captured.get(q), captured.get(q + 1),
                        captured.get(q + 2), captured.get(q + 3)), out);
                continue;
            }

            float cx = 0.0F, cy = 0.0F, cz = 0.0F;

            for (int i = q; i < q + 4; ++i)
            {
                cx += captured.get(i).x * 0.25F;
                cy += captured.get(i).y * 0.25F;
                cz += captured.get(i).z * 0.25F;
            }

            if (isSkirtQuad(cy, slot))
            {
                emitSkirtQuad(java.util.Arrays.asList(captured.get(q), captured.get(q + 1),
                                captured.get(q + 2), captured.get(q + 3)), out);

                continue;
            }

            BoneRegion baseRegion = assignment.assignVertexForSlot(cx, cy, cz, slot);

            for (int i = q; i < q + 4; ++i)
            {
                out.add(captured.get(i), baseRegion, baseRegion, 0.0F);
            }
        }
    }

    private static String vertexKey(goblinbob.mobends.standard.client.model.armor.CapturedVertex v)
    {
        return Float.floatToIntBits(v.x) + ":" + Float.floatToIntBits(v.y) + ":" + Float.floatToIntBits(v.z)
                + ":" + Float.floatToIntBits(v.u) + ":" + Float.floatToIntBits(v.v);
    }

    private void capturePart(Model armorModel, HumanoidModel<E> defaultModel, int packedLight, GeoPart part,
                             boolean[] slotVisibility, java.util.Set<String> excluded,
                             java.util.Map<RenderType, TypedGeometry> geometry)
    {
        for (java.util.Map.Entry<RenderType, java.util.List<goblinbob.mobends.standard.client.model.armor.CapturedVertex>> entry
                : captureGeo(armorModel, defaultModel, part, slotVisibility, packedLight).entrySet())
        {
            emitPart(part, entry.getValue(), excluded,
                    geometry.computeIfAbsent(entry.getKey(), key -> new TypedGeometry()));
        }
    }

    private void emitPart(GeoPart part, java.util.List<goblinbob.mobends.standard.client.model.armor.CapturedVertex> captured,
                          java.util.Set<String> excluded, TypedGeometry out)
    {
        if (captured.isEmpty())
        {
            return;
        }

        final boolean quadAligned = captured.size() % 4 == 0;

        if (!quadAligned)
        {
            for (CapturedVertex v : captured)
            {
                if (excluded.contains(vertexKey(v)))
                {
                    continue;
                }

                emitVertex(part, v, jointBlend(part, v.y), out);
            }

            return;
        }

        for (int q = 0; q + 3 < captured.size(); q += 4)
        {
            if (excluded.contains(vertexKey(captured.get(q)))
                    && excluded.contains(vertexKey(captured.get(q + 1)))
                    && excluded.contains(vertexKey(captured.get(q + 2)))
                    && excluded.contains(vertexKey(captured.get(q + 3))))
            {
                continue;
            }

            emitPartQuad(part, java.util.Arrays.asList(captured.get(q), captured.get(q + 1),
                    captured.get(q + 2), captured.get(q + 3)), out);
        }

    }

    private void emitPartQuad(GeoPart part, java.util.List<CapturedVertex> quad, TypedGeometry out)
    {
        final float joint = jointPlane(part);

        java.util.List<java.util.List<CapturedVertex>> polys = java.util.Collections.singletonList(quad);

        if (!Float.isNaN(joint))
        {
            polys = clipAll(polys, AXIS_Y, joint);
        }

        for (java.util.List<CapturedVertex> poly : polys)
        {
            final float weight = pieceJointBlend(joint, poly);
            emitFan(poly, v -> emitVertex(part, v, weight, out));
        }
    }

    private static boolean isSkirtQuad(float centroidY, EquipmentSlot slot)
    {
        return slot == EquipmentSlot.CHEST && centroidY > SKIRT_MIN_Y;
    }

    private void emitSkirtQuad(java.util.List<CapturedVertex> quad, TypedGeometry out)
    {
        for (java.util.List<CapturedVertex> half
                : clipAll(java.util.Collections.singletonList(quad), AXIS_X, 0.0F))
        {
            float cx = 0.0F;

            for (CapturedVertex v : half)
            {
                cx += v.x;
            }

            final boolean left = cx >= 0.0F;

            java.util.List<java.util.List<CapturedVertex>> pieces =
                    java.util.Collections.singletonList(half);

            pieces = clipAll(pieces, AXIS_Y, KNEE_Y - JOINT_BLEND_BAND);
            pieces = clipAll(pieces, AXIS_Y, KNEE_Y + JOINT_BLEND_BAND);

            for (java.util.List<CapturedVertex> piece : pieces)
            {
                emitFan(piece, v -> emitSkirtVertex(v, left, out));
            }
        }
    }

    private static void emitFan(java.util.List<CapturedVertex> poly, java.util.function.Consumer<CapturedVertex> emitter)
    {
        final int n = poly.size();

        if (n < 3)
        {
            return;
        }

        if (n == 4)
        {
            for (int i = 0; i < 4; ++i)
            {
                emitter.accept(poly.get(i));
            }

            return;
        }

        for (int k = 1; k + 1 < n; ++k)
        {
            emitter.accept(poly.get(0));
            emitter.accept(poly.get(k));
            emitter.accept(poly.get(k + 1));
            emitter.accept(poly.get(k + 1));
        }
    }

    private void emitSkirtVertex(CapturedVertex v, boolean left, TypedGeometry out)
    {
        out.add(v,
                left ? BoneRegion.LEFT_LEG_UPPER : BoneRegion.RIGHT_LEG_UPPER,
                left ? BoneRegion.LEFT_LEG_LOWER : BoneRegion.RIGHT_LEG_LOWER,
                skirtKneeBlend(v.y));
    }

    private static float skirtKneeBlend(float y)
    {
        float t = (y - (KNEE_Y - JOINT_BLEND_BAND)) / (2.0F * JOINT_BLEND_BAND);
        return SKIRT_KNEE_FOLLOW * Math.max(0.0F, Math.min(1.0F, t));
    }

    private static float jointPlane(GeoPart part)
    {
        switch (part)
        {
            case LEFT_ARM:
            case RIGHT_ARM:
                return ELBOW_Y;
            case LEFT_LEG:
            case RIGHT_LEG:
                return KNEE_Y;
            default:
                return Float.NaN;
        }
    }

    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;

    private static float axisValue(CapturedVertex v, int axis)
    {
        return axis == AXIS_X ? v.x : v.y;
    }

    private static java.util.List<java.util.List<CapturedVertex>> clipAll(
            java.util.List<java.util.List<CapturedVertex>> polys, int axis, float plane)
    {
        java.util.List<java.util.List<CapturedVertex>> result = new java.util.ArrayList<>();

        for (java.util.List<CapturedVertex> poly : polys)
        {
            clip(poly, axis, plane, result);
        }

        return result;
    }

    private static void clip(java.util.List<CapturedVertex> poly, int axis, float plane,
                             java.util.List<java.util.List<CapturedVertex>> out)
    {
        final int n = poly.size();

        java.util.List<CapturedVertex> above = new java.util.ArrayList<>(n + 2);
        java.util.List<CapturedVertex> below = new java.util.ArrayList<>(n + 2);

        boolean crossed = false;

        for (int i = 0; i < n; ++i)
        {
            CapturedVertex cur = poly.get(i);
            CapturedVertex next = poly.get((i + 1) % n);

            boolean curAbove = axisValue(cur, axis) > plane;
            boolean nextAbove = axisValue(next, axis) > plane;

            if (curAbove)
            {
                above.add(cur);
            }
            else
            {
                below.add(cur);
            }

            if (curAbove != nextAbove)
            {
                float a = axisValue(cur, axis);
                float b = axisValue(next, axis);

                CapturedVertex mid = lerpVertex(cur, next, (plane - a) / (b - a));
                above.add(mid);
                below.add(mid);
                crossed = true;
            }
        }

        if (!crossed || above.size() < 3 || below.size() < 3)
        {
            out.add(poly);
            return;
        }

        out.add(above);
        out.add(below);
    }

    private static CapturedVertex lerpVertex(CapturedVertex a, CapturedVertex b, float t)
    {
        return new CapturedVertex(
                a.x + (b.x - a.x) * t,
                a.y + (b.y - a.y) * t,
                a.z + (b.z - a.z) * t,
                a.red + (b.red - a.red) * t,
                a.green + (b.green - a.green) * t,
                a.blue + (b.blue - a.blue) * t,
                a.alpha + (b.alpha - a.alpha) * t,
                a.u + (b.u - a.u) * t,
                a.v + (b.v - a.v) * t,
                a.overlayUV,
                a.lightmapUV,
                a.normalX + (b.normalX - a.normalX) * t,
                a.normalY + (b.normalY - a.normalY) * t,
                a.normalZ + (b.normalZ - a.normalZ) * t);
    }

    private static float pieceJointBlend(float joint, java.util.List<CapturedVertex> poly)
    {
        if (Float.isNaN(joint) || poly.isEmpty())
        {
            return 0.0F;
        }

        float centroidY = 0.0F;

        for (CapturedVertex v : poly)
        {
            centroidY += v.y;
        }

        return centroidY / poly.size() > joint ? 1.0F : 0.0F;
    }

    private void emitVertex(GeoPart part, CapturedVertex v, float weight, TypedGeometry out)
    {
        out.add(v, upperRegionFor(part), lowerRegionFor(part), weight);
    }

    private static goblinbob.mobends.standard.client.model.armor.BoneRegion upperRegionFor(GeoPart part)
    {
        switch (part)
        {
            case HEAD: return goblinbob.mobends.standard.client.model.armor.BoneRegion.HEAD;
            case LEFT_ARM: return goblinbob.mobends.standard.client.model.armor.BoneRegion.LEFT_ARM_UPPER;
            case RIGHT_ARM: return goblinbob.mobends.standard.client.model.armor.BoneRegion.RIGHT_ARM_UPPER;
            case LEFT_LEG: return goblinbob.mobends.standard.client.model.armor.BoneRegion.LEFT_LEG_UPPER;
            case RIGHT_LEG: return goblinbob.mobends.standard.client.model.armor.BoneRegion.RIGHT_LEG_UPPER;
            case BODY:
            default: return goblinbob.mobends.standard.client.model.armor.BoneRegion.BODY;
        }
    }

    private static goblinbob.mobends.standard.client.model.armor.BoneRegion lowerRegionFor(GeoPart part)
    {
        switch (part)
        {
            case LEFT_ARM: return goblinbob.mobends.standard.client.model.armor.BoneRegion.LEFT_ARM_LOWER;
            case RIGHT_ARM: return goblinbob.mobends.standard.client.model.armor.BoneRegion.RIGHT_ARM_LOWER;
            case LEFT_LEG: return goblinbob.mobends.standard.client.model.armor.BoneRegion.LEFT_LEG_LOWER;
            case RIGHT_LEG: return goblinbob.mobends.standard.client.model.armor.BoneRegion.RIGHT_LEG_LOWER;
            default: return upperRegionFor(part);
        }
    }

    private static float jointBlend(GeoPart part, float y)
    {
        return y > jointPlane(part) ? 1.0F : 0.0F;
    }

    private static void applyOnlyVisible(HumanoidModel<?> model, GeoPart part, boolean[] slotVisibility)
    {
        setVisible(model.head, part == GeoPart.HEAD && slotVisibility[0]);
        setVisible(model.hat, part == GeoPart.HEAD && slotVisibility[1]);
        setVisible(model.body, part == GeoPart.BODY && slotVisibility[2]);
        setVisible(model.leftArm, part == GeoPart.LEFT_ARM && slotVisibility[3]);
        setVisible(model.rightArm, part == GeoPart.RIGHT_ARM && slotVisibility[4]);
        setVisible(model.leftLeg, part == GeoPart.LEFT_LEG && slotVisibility[5]);
        setVisible(model.rightLeg, part == GeoPart.RIGHT_LEG && slotVisibility[6]);
    }

    private static void setVisible(ModelPart part, boolean visible)
    {
        if (part != null)
        {
            part.visible = visible;
        }
    }

    private static boolean[] captureArmorPartVisibility(HumanoidModel<?> model)
    {
        ModelPart[] parts = armorParts(model);
        boolean[] state = new boolean[parts.length];

        for (int i = 0; i < parts.length; ++i)
        {
            state[i] = parts[i] != null && parts[i].visible;
        }

        return state;
    }

    private static void restoreArmorPartVisibility(HumanoidModel<?> model, boolean[] state)
    {
        ModelPart[] parts = armorParts(model);

        for (int i = 0; i < parts.length; ++i)
        {
            if (parts[i] != null)
            {
                parts[i].visible = state[i];
            }
        }
    }

    private static ModelPart[] armorParts(HumanoidModel<?> model)
    {
        return new ModelPart[] {
                model.head, model.hat, model.body,
                model.leftArm, model.rightArm,
                model.leftLeg, model.rightLeg
        };
    }

    private static float[] captureArmorPartState(HumanoidModel<?> model)
    {
        ModelPart[] parts = armorParts(model);
        float[] state = new float[parts.length * 6];

        for (int i = 0; i < parts.length; ++i)
        {
            ModelPart part = parts[i];
            if (part == null) continue;

            int base = i * 6;
            state[base] = part.x;
            state[base + 1] = part.y;
            state[base + 2] = part.z;
            state[base + 3] = part.xRot;
            state[base + 4] = part.yRot;
            state[base + 5] = part.zRot;
        }

        return state;
    }

    private static void restoreArmorPartState(HumanoidModel<?> model, float[] state)
    {
        ModelPart[] parts = armorParts(model);

        for (int i = 0; i < parts.length; ++i)
        {
            ModelPart part = parts[i];
            if (part == null) continue;

            int base = i * 6;
            part.x = state[base];
            part.y = state[base + 1];
            part.z = state[base + 2];
            part.xRot = state[base + 3];
            part.yRot = state[base + 4];
            part.zRot = state[base + 5];
        }
    }

    private static void applyArmorRestPose(HumanoidModel<?> model)
    {
        setRestPose(model.head, 0.0F, 0.0F, 0.0F);
        setRestPose(model.hat, 0.0F, 0.0F, 0.0F);
        setRestPose(model.body, 0.0F, 0.0F, 0.0F);
        setRestPose(model.leftArm, 5.0F, 2.0F, 0.0F);
        setRestPose(model.rightArm, -5.0F, 2.0F, 0.0F);
        setRestPose(model.leftLeg, 1.9F, 12.0F, 0.0F);
        setRestPose(model.rightLeg, -1.9F, 12.0F, 0.0F);
    }

    private static void setRestPose(ModelPart part, float x, float y, float z)
    {
        if (part == null) return;

        part.x = x;
        part.y = y;
        part.z = z;
        part.xRot = 0.0F;
        part.yRot = 0.0F;
        part.zRot = 0.0F;
    }

    private boolean isHiddenByFirstPersonView(E entity, EquipmentSlot slot)
    {
        if (!goblinbob.mobends.compat.FirstPersonModelCompat.isRenderingFirstPersonBody(entity))
        {
            return false;
        }

        if (slot == EquipmentSlot.HEAD)
        {
            return true;
        }

        if (slot != EquipmentSlot.CHEST)
        {
            return false;
        }

        if (entity instanceof net.minecraft.client.player.LocalPlayer localPlayer && localPlayer.isSwimming())
        {
            return true;
        }

        return goblinbob.mobends.compat.FirstPersonModelCompat.showsVanillaHands(getHumanoidParentModel());
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

            renderTrim(poseStack, bufferSource, packedLight, entity, armorItem, armorModel, slot, itemStack, bipedData);
        }
        else
        {
            java.util.List<goblinbob.mobends.standard.client.model.armor.ImmersiveArmorsSupport.Layer> extendedLayers =
                    resolveExtendedArmorLayers(armorItem, slot);


            if (!extendedLayers.isEmpty())
            {
                renderExtendedArmorLayers(poseStack, bufferSource, packedLight, entity, armorItem,
                        armorModel, slot, itemStack, bipedData, extendedLayers);

                if (armorModel instanceof HumanoidModel<?> humanoidModel)
                {
                    if (mutator != null && !goblinbob.mobends.compat.BetterCombatCompat.shouldYieldModel(entity))
                    {
                        mutator.syncPosesToVanillaModel(humanoidModel);
                    }

                    goblinbob.mobends.standard.client.model.armor.ImmersiveArmorsSupport.renderDecorations(
                            armorItem, slot, poseStack, bufferSource, packedLight, entity, itemStack,
                            goblinbob.mobends.core.client.event.DataUpdateHandler.partialTicks, humanoidModel);
                }
                return;
            }

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
                    armorModel,
                    bipedData,
                    texture
            );

            renderArmorOverlayPass(poseStack, bufferSource, packedLight, entity, armorItem,
                    armorModel, slot, itemStack, bipedData);

            renderTrim(poseStack, bufferSource, packedLight, entity, armorItem, armorModel, slot, itemStack, bipedData);
        }
    }

    private void renderTrim(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, E entity,
                            ArmorItem armorItem, Model armorModel, EquipmentSlot slot, ItemStack itemStack,
                            @Nullable BipedEntityData<?> bipedData)
    {
        final VertexConsumer trimConsumer = getTrimBuffer(bufferSource, entity, armorItem, itemStack, slot);

        if (trimConsumer == null)
        {
            return;
        }

        if (bipedData == null)
        {
            IModelRenderHelper.Holder.getHelper().renderModelToBuffer(armorModel, poseStack, trimConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        }
        else if (armorModel instanceof HumanoidModel<?> humanoidModel)
        {
            armorFacade.renderArmorIntoConsumer(poseStack, bufferSource, trimConsumer, packedLight,
                    OverlayTexture.NO_OVERLAY, entity, slot, itemStack, humanoidModel, bipedData, 0xFFFFFFFF);
        }
    }

    @Nullable
    private VertexConsumer getTrimBuffer(MultiBufferSource bufferSource, E entity, ArmorItem armorItem,
                                         ItemStack itemStack, EquipmentSlot slot)
    {
        final boolean armorApi = goblinbob.mobends.compat.ArmorModelApiCompat.isArmor(itemStack);

        //? if >=1.21 {
        /*final net.minecraft.world.item.armortrim.ArmorTrim trim =
                itemStack.get(net.minecraft.core.component.DataComponents.TRIM);
        if (trim == null) return null;
        final RenderType renderType = net.minecraft.client.renderer.Sheets.armorTrimsSheet(trim.pattern().value().decal());
        *///?} else {
        final net.minecraft.world.item.armortrim.ArmorTrim trim = net.minecraft.world.item.armortrim.ArmorTrim
                .getTrim(entity.level().registryAccess(), itemStack).orElse(null);
        if (trim == null) return null;
        final RenderType renderType = net.minecraft.client.renderer.Sheets.armorTrimsSheet();
        //?}

        final net.minecraft.client.renderer.texture.TextureAtlas atlas = net.minecraft.client.Minecraft.getInstance()
                .getModelManager().getAtlas(net.minecraft.client.renderer.Sheets.ARMOR_TRIMS_SHEET);
        final net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = armorApi
                ? goblinbob.mobends.compat.ArmorModelApiCompat.getTrimSprite(itemStack, trim, atlas)
                : atlas.getSprite(usesInnerModel(slot)
                        ? trim.innerTexture(armorItem.getMaterial())
                        : trim.outerTexture(armorItem.getMaterial()));

        return sprite == null ? null : sprite.wrap(bufferSource.getBuffer(renderType));
    }

    private java.util.List<goblinbob.mobends.standard.client.model.armor.ImmersiveArmorsSupport.Layer>
        resolveExtendedArmorLayers(ArmorItem armorItem, EquipmentSlot slot)
    {
        if (!goblinbob.mobends.standard.client.model.armor.ImmersiveArmorsSupport.isAvailable())
        {
            return java.util.Collections.emptyList();
        }

        IArmorHelper helper = IArmorHelper.Holder.getHelper();
        String materialName = helper != null ? helper.getArmorMaterialName(armorItem) : null;

        if (materialName == null)
        {
            return java.util.Collections.emptyList();
        }

        int colonIndex = materialName.indexOf(':');
        if (colonIndex >= 0)
        {
            materialName = materialName.substring(colonIndex + 1);
        }

        return goblinbob.mobends.standard.client.model.armor.ImmersiveArmorsSupport.getLayers(
                armorItem, slot, materialName);
    }

    private void renderExtendedArmorLayers(PoseStack poseStack, MultiBufferSource bufferSource,
                                           int packedLight, E entity, ArmorItem armorItem,
                                           Model armorModel, EquipmentSlot slot,
                                           ItemStack itemStack, BipedEntityData<?> bipedData,
                                           java.util.List<goblinbob.mobends.standard.client.model.armor.ImmersiveArmorsSupport.Layer> layers)
    {
        goblinbob.mobends.api.rendering.IArmorColorProvider colorProvider =
                goblinbob.mobends.api.rendering.IArmorColorProvider.Holder.getProvider();

        for (goblinbob.mobends.standard.client.model.armor.ImmersiveArmorsSupport.Layer layer : layers)
        {
            if (armorModel instanceof HumanoidModel<?> parentModel)
            {
                copyModelProperties(parentModel, layer.model);
            }

            layer.model.setAllVisible(false);
            goblinbob.mobends.standard.client.model.armor.ArmorPoseHelper.showSlotParts(layer.model, slot);

            java.util.function.Function<ResourceLocation, RenderType> renderTypeProvider =
                    layer.translucent ? RenderType::entityTranslucent
                            : layer.glowing ? texture -> RenderType.entityCutoutNoCull(texture, false)
                            : RenderType::armorCutoutNoCull;

            Integer tint = 0xFFFFFFFF;

            if (layer.colored && colorProvider != null)
            {
                int dyed = colorProvider.getDyedColor(itemStack);
                if (dyed != -1)
                {
                    tint = 0xFF000000 | dyed;
                }
            }


            armorFacade.renderArmorLayer(poseStack, bufferSource, packedLight, entity, slot,
                    itemStack, layer.model, bipedData, layer.texture, tint, renderTypeProvider);

            if (layer.colored && overlayTextureExists(layer.overlayTexture))
            {
                armorFacade.renderArmorLayer(poseStack, bufferSource, packedLight, entity, slot,
                        itemStack, layer.model, bipedData, layer.overlayTexture, 0xFFFFFFFF,
                        renderTypeProvider);
            }

            final VertexConsumer trimConsumer = getTrimBuffer(bufferSource, entity, armorItem, itemStack, slot);

            if (trimConsumer != null)
            {
                armorFacade.renderArmorIntoConsumer(poseStack, bufferSource, trimConsumer, packedLight,
                        OverlayTexture.NO_OVERLAY, entity, slot, itemStack, layer.model, bipedData, 0xFFFFFFFF);
            }
        }
    }

    private void renderPalladiumArmor(PoseStack poseStack, MultiBufferSource bufferSource,
                                      int packedLight, E entity, EquipmentSlot slot, ItemStack itemStack,
                                      Model armorModel,
                                      goblinbob.mobends.standard.client.model.armor.PalladiumSupport.Armor palladiumArmor,
                                      @Nullable BipedEntityData<?> bipedData)
    {
        final java.util.function.Function<ResourceLocation, RenderType> renderTypeProvider =
                RenderType::entityTranslucent;

        final int tint = goblinbob.mobends.standard.client.model.armor.PalladiumSupport.getDyeColor(itemStack);

        if (bipedData != null)
        {
            BipedEntityData<?> data = previewAware(bipedData);

            armorFacade.renderArmorLayer(poseStack, bufferSource, packedLight, entity, slot,
                    itemStack, armorModel, data, palladiumArmor.texture, tint, renderTypeProvider);

            if (palladiumArmor.overlayTexture != null && overlayTextureExists(palladiumArmor.overlayTexture))
            {
                armorFacade.renderArmorLayer(poseStack, bufferSource, packedLight, entity, slot,
                        itemStack, armorModel, data, palladiumArmor.overlayTexture, 0xFFFFFFFF,
                        renderTypeProvider);
            }

            return;
        }

        if (mutator != null && armorModel instanceof HumanoidModel<?> humanoidModel)
        {
            mutator.syncPosesToVanillaModel(humanoidModel);
        }

        renderPalladiumPass(poseStack, bufferSource, packedLight, itemStack, armorModel,
                palladiumArmor.texture, renderTypeProvider, tint);

        if (palladiumArmor.overlayTexture != null && overlayTextureExists(palladiumArmor.overlayTexture))
        {
            renderPalladiumPass(poseStack, bufferSource, packedLight, itemStack, armorModel,
                    palladiumArmor.overlayTexture, renderTypeProvider, 0xFFFFFFFF);
        }
    }

    private void renderChameleonArmor(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                      E entity, EquipmentSlot slot, ItemStack itemStack,
                                      @Nullable BipedEntityData<?> bipedData)
    {
        final HumanoidModel<E> model = goblinbob.mobends.compat.ColdSweatCompat.getChameleonModel(entity, itemStack, slot);

        if (model == null)
        {
            return;
        }

        copyParentProperties(model);
        model.young = false;
        setPartVisibility(model, slot);

        final double factor = goblinbob.mobends.compat.ColdSweatCompat.getAdaptiveFactor(itemStack);
        final ResourceLocation baseTexture = goblinbob.mobends.compat.ColdSweatCompat.getChameleonTexture(slot, "");
        final ResourceLocation overlayTexture =
                goblinbob.mobends.compat.ColdSweatCompat.getChameleonTexture(slot, factor < 0 ? "_blue" : "_red");
        final int overlayColor = ((int) (Math.min(Math.abs(factor), 1.0D) * 255.0D) << 24) | 0xFFFFFF;

        if (bipedData != null)
        {
            armorFacade.renderArmorLayer(poseStack, bufferSource, packedLight, entity, slot, itemStack,
                    model, bipedData, baseTexture, 0xFFFFFFFF, RenderType::entityTranslucent);
            armorFacade.renderArmorLayer(poseStack, bufferSource, packedLight, entity, slot, itemStack,
                    model, bipedData, overlayTexture, overlayColor, RenderType::entityTranslucent);

            if (itemStack.hasFoil())
            {
                armorFacade.renderArmorIntoConsumer(poseStack, bufferSource,
                        bufferSource.getBuffer(RenderType.armorEntityGlint()), packedLight, OverlayTexture.NO_OVERLAY,
                        entity, slot, itemStack, model, bipedData, 0xFFFFFFFF);
            }
            return;
        }

        if (mutator != null && !goblinbob.mobends.compat.BetterCombatCompat.shouldYieldModel(entity))
        {
            mutator.syncPosesToVanillaModel(model);
        }

        renderPalladiumPass(poseStack, bufferSource, packedLight, itemStack, model, baseTexture,
                RenderType::entityTranslucent, 0xFFFFFFFF);
        IModelRenderHelper.Holder.getHelper().renderModelToBuffer(model, poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucent(overlayTexture)), packedLight,
                OverlayTexture.NO_OVERLAY, overlayColor);
    }

    private final goblinbob.mobends.standard.client.model.armor.CapturingVertexConsumer legendsVisibilityPass =
            new goblinbob.mobends.standard.client.model.armor.CapturingVertexConsumer(true);

    private void renderLegendsArmor(PoseStack poseStack, MultiBufferSource bufferSource,
                                    int packedLight, E entity, EquipmentSlot slot, ItemStack itemStack,
                                    ArmorItem armorItem, Model armorModel,
                                    @Nullable BipedEntityData<?> bipedData)
    {
        final ResourceLocation texture = getArmorTexture(armorItem, itemStack, entity, slot, null);
        if (texture == null)
        {
            return;
        }

        final boolean shiny = goblinbob.mobends.standard.client.model.armor.LegendsArmorSupport.isShiny(itemStack);

        final java.util.function.Function<ResourceLocation, RenderType> renderTypeProvider = shiny
                ? goblinbob.mobends.standard.client.model.armor.LegendsArmorSupport::shinyRenderType
                : RenderType::entityCutoutNoCull;

        final int tint = shiny
                ? goblinbob.mobends.standard.client.model.armor.LegendsArmorSupport.getShinyColor(itemStack)
                : 0xFFFFFFFF;

        if (bipedData != null)
        {
            BipedEntityData<?> data = previewAware(bipedData);

            refreshLegendsPartVisibility(armorModel, packedLight);

            armorFacade.renderArmorLayer(poseStack, bufferSource, packedLight, entity, slot,
                    itemStack, armorModel, data, texture, tint, renderTypeProvider);
            return;
        }

        if (mutator != null && armorModel instanceof HumanoidModel<?> humanoidModel)
        {
            mutator.syncPosesToVanillaModel(humanoidModel);
        }

        renderPalladiumPass(poseStack, bufferSource, packedLight, itemStack, armorModel,
                texture, renderTypeProvider, tint);
    }

    private void refreshLegendsPartVisibility(Model armorModel, int packedLight)
    {
        legendsVisibilityPass.clear();

        IModelRenderHelper.Holder.getHelper().renderModelToBuffer(armorModel, new PoseStack(),
                legendsVisibilityPass, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        legendsVisibilityPass.clear();
    }

    private final CapturingVertexConsumer selfDrawnProbe = new CapturingVertexConsumer();

    private boolean drawsNothing(Model armorModel, int packedLight)
    {
        selfDrawnProbe.clear();
        IModelRenderHelper.Holder.getHelper().renderModelToBuffer(armorModel, new PoseStack(),
                selfDrawnProbe, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        final boolean empty = selfDrawnProbe.getVertices().isEmpty();
        selfDrawnProbe.clear();
        return empty;
    }

    private void renderPalladiumPass(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                     ItemStack itemStack, Model armorModel, ResourceLocation texture,
                                     java.util.function.Function<ResourceLocation, RenderType> renderTypeProvider,
                                     int color)
    {
        VertexConsumer vertexConsumer = (VertexConsumer) IModelRenderHelper.Holder.getHelper().getArmorFoilBuffer(
                bufferSource, renderTypeProvider.apply(texture), itemStack.hasFoil());

        IModelRenderHelper.Holder.getHelper().renderModelToBuffer(armorModel, poseStack, vertexConsumer,
                packedLight, OverlayTexture.NO_OVERLAY, color);
    }

    private static void copyModelProperties(HumanoidModel<?> source, HumanoidModel<?> target)
    {
        target.young = source.young;
        target.riding = source.riding;
        target.crouching = source.crouching;
        target.attackTime = source.attackTime;
        target.rightArmPose = source.rightArmPose;
        target.leftArmPose = source.leftArmPose;
    }

    private static final java.util.Map<ResourceLocation, Boolean> OVERLAY_PRESENCE_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();

    private void renderArmorOverlayPass(PoseStack poseStack, MultiBufferSource bufferSource,
                                        int packedLight, E entity, ArmorItem armorItem,
                                        Model armorModel, EquipmentSlot slot,
                                        ItemStack itemStack, BipedEntityData<?> bipedData)
    {
        ResourceLocation overlayTexture = getArmorTexture(armorItem, itemStack, entity, slot, "overlay");

        if (overlayTexture == null || !overlayTextureExists(overlayTexture))
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
                armorModel,
                bipedData,
                overlayTexture,
                0xFFFFFFFF
        );
    }

    private static boolean overlayTextureExists(ResourceLocation texture)
    {
        return OVERLAY_PRESENCE_CACHE.computeIfAbsent(texture, location -> {
            try
            {
                return net.minecraft.client.Minecraft.getInstance().getResourceManager()
                        .getResource(location).isPresent();
            }
            catch (Throwable t)
            {
                return Boolean.FALSE;
            }
        });
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
                                    Model armorModel, EquipmentSlot slot, ItemStack itemStack, boolean syncPose)
    {
        ResourceLocation texture = getArmorTexture(armorItem, itemStack, entity, slot, null);
        if (texture == null) return;

        VertexConsumer vertexConsumer = (VertexConsumer) IModelRenderHelper.Holder.getHelper().getArmorFoilBuffer(
                bufferSource, RenderType.armorCutoutNoCull(texture), itemStack.hasFoil());

        if (syncPose && mutator != null && armorModel instanceof HumanoidModel<?> humanoidModel
                && !goblinbob.mobends.compat.BetterCombatCompat.shouldYieldModel(entity))
        {
            mutator.syncPosesToVanillaModel(humanoidModel);
        }

        IModelRenderHelper.Holder.getHelper().renderModelToBuffer(armorModel, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        renderTrim(poseStack, bufferSource, packedLight, entity, armorItem, armorModel, slot, itemStack, null);
    }

    private static final java.util.Map<Class<?>, java.util.List<java.lang.reflect.Field>> MODEL_PART_FIELDS =
            new java.util.concurrent.ConcurrentHashMap<>();

    private final CapturingVertexConsumer extraLimbCapture = new CapturingVertexConsumer();
    private final goblinbob.mobends.standard.client.model.armor.QuadSlicer extraLimbSlicer =
            new goblinbob.mobends.standard.client.model.armor.QuadSlicer();

    private void renderExtraParts(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, E entity,
                                  HumanoidModel<?> armorModel, EquipmentSlot slot, ItemStack itemStack,
                                  BipedEntityData<?> bipedData, VertexConsumer vertexConsumer, @Nullable Integer colorOverride)
    {
        final ModelPart[] limbs = {armorModel.leftLeg, armorModel.rightLeg, armorModel.leftArm, armorModel.rightArm};
        final java.util.List<ModelPart> limbExtras = new java.util.ArrayList<>();
        final java.util.List<Integer> limbIndices = new java.util.ArrayList<>();

        for (ModelPart part : findExtraParts(armorModel))
        {
            for (int i = 0; i < limbs.length; ++i)
            {
                if (samePose(part, limbs[i]))
                {
                    limbExtras.add(part);
                    limbIndices.add(i);
                    break;
                }
            }
        }

        if (mutator != null && !goblinbob.mobends.compat.BetterCombatCompat.shouldYieldModel(entity))
        {
            mutator.syncPosesToVanillaModel(armorModel);
        }

        final java.util.List<ModelPart> drawnElsewhere = new java.util.ArrayList<>(limbExtras);
        java.util.Collections.addAll(drawnElsewhere, armorParts(armorModel));
        final java.util.List<ModelPart> skipped = drawnElsewhere.stream()
                .filter(java.util.Objects::nonNull).flatMap(ModelPart::getAllParts).toList();
        final boolean[] skipDraws = new boolean[skipped.size()];

        for (int i = 0; i < skipped.size(); ++i)
        {
            skipDraws[i] = skipped.get(i).skipDraw;
            skipped.get(i).skipDraw = true;
        }

        try
        {
            IModelRenderHelper.Holder.getHelper().renderModelToBuffer(armorModel, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        }
        finally
        {
            for (int i = skipped.size() - 1; i >= 0; --i)
            {
                skipped.get(i).skipDraw = skipDraws[i];
            }
        }

        if (limbExtras.isEmpty())
        {
            return;
        }

        final goblinbob.mobends.standard.client.model.armor.ArmorRenderContext<E> context =
                new goblinbob.mobends.standard.client.model.armor.ArmorRenderContext<>(entity, bipedData, slot, itemStack,
                        poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, colorOverride);
        final int color = context.getArmorColor();
        final float slimArmOffset = context.isSlimArms()
                ? -0.5F * goblinbob.mobends.standard.client.model.armor.ArmorPoseHelper.SCALE
                : 0.0F;

        for (int i = 0; i < limbExtras.size(); ++i)
        {
            final int limb = limbIndices.get(i);

            if (limb < 2)
            {
                goblinbob.mobends.standard.client.model.armor.ArmorPoseHelper.renderSplitLeg(poseStack, vertexConsumer,
                        limbExtras.get(i), bipedData, limb == 0, packedLight, OverlayTexture.NO_OVERLAY, color,
                        extraLimbCapture, extraLimbSlicer);
            }
            else
            {
                goblinbob.mobends.standard.client.model.armor.ArmorPoseHelper.renderSplitArm(poseStack, vertexConsumer,
                        limbExtras.get(i), bipedData, limb == 2, packedLight, OverlayTexture.NO_OVERLAY, slimArmOffset,
                        color, extraLimbCapture, extraLimbSlicer);
            }
        }
    }

    private static java.util.List<ModelPart> findExtraParts(HumanoidModel<?> model)
    {
        final java.util.Set<ModelPart> nested = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());

        for (ModelPart part : armorParts(model))
        {
            part.getAllParts().forEach(nested::add);
        }

        final java.util.List<ModelPart> candidates = new java.util.ArrayList<>();

        for (java.lang.reflect.Field field : modelPartFields(model.getClass()))
        {
            try
            {
                final ModelPart part = (ModelPart) field.get(model);

                if (part != null && part.visible && !nested.contains(part) && !candidates.contains(part))
                {
                    candidates.add(part);
                }
            }
            catch (Throwable ignored)
            {
            }
        }

        for (ModelPart part : candidates)
        {
            part.getAllParts().filter(child -> child != part).forEach(nested::add);
        }

        candidates.removeIf(nested::contains);
        return candidates;
    }

    private static java.util.List<java.lang.reflect.Field> modelPartFields(Class<?> modelClass)
    {
        return MODEL_PART_FIELDS.computeIfAbsent(modelClass, type -> {
            final java.util.List<java.lang.reflect.Field> fields = new java.util.ArrayList<>();

            for (Class<?> current = type; current != null && current != HumanoidModel.class; current = current.getSuperclass())
            {
                for (java.lang.reflect.Field field : current.getDeclaredFields())
                {
                    if (field.getType() == ModelPart.class && !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                    {
                        try
                        {
                            field.setAccessible(true);
                            fields.add(field);
                        }
                        catch (Throwable ignored)
                        {
                        }
                    }
                }
            }

            return fields;
        });
    }

    private static boolean samePose(ModelPart a, ModelPart b)
    {
        return a.x == b.x && a.y == b.y && a.z == b.z
                && a.xRot == b.xRot && a.yRot == b.yRot && a.zRot == b.zRot;
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
        final ResourceLocation armorApiTexture = goblinbob.mobends.compat.ArmorModelApiCompat.getTexture(itemStack);

        if (armorApiTexture != null)
        {
            return overlay == null ? armorApiTexture : null;
        }

        boolean isInnerModel = usesInnerModel(slot);

        IArmorTextureProvider textureProvider = IArmorTextureProvider.Holder.getProvider();
        ResourceLocation customTexture = textureProvider.getArmorTexture(armorItem, itemStack, entity, slot, layer, overlay, isInnerModel);

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

    private void setPartVisibility(HumanoidModel<E> model, EquipmentSlot slot)
    {
        model.setAllVisible(false);
        goblinbob.mobends.standard.client.model.armor.ArmorPoseHelper.showSlotParts(model, slot);
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
