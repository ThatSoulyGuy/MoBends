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
import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import goblinbob.mobends.standard.client.model.armor.CapturedVertex;
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
        boolean hasBendsAnimation = entityData instanceof BipedEntityData
                && goblinbob.mobends.core.util.BenderHelper.isEntityAnimated(entity)
                && !goblinbob.mobends.compat.ModCompatManager.shouldDeferAnimation(entity);

        goblinbob.mobends.core.client.MoBendsRenderContext.beginArmorRender();
        try
        {
            renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.CHEST, packedLight, hasBendsAnimation, entityData);
            renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.LEGS, packedLight, hasBendsAnimation, entityData);
            renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.FEET, packedLight, hasBendsAnimation, entityData);
            renderArmorPiece(poseStack, bufferSource, entity, EquipmentSlot.HEAD, packedLight, hasBendsAnimation, entityData);
        }
        finally
        {
            goblinbob.mobends.core.client.MoBendsRenderContext.endArmorRender();
        }
    }

    @SuppressWarnings("unchecked")
    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource,
                                  E entity, EquipmentSlot slot, int packedLight,
                                  boolean hasBendsAnimation, EntityData<?> entityData)
    {
        ItemStack itemStack = entity.getItemBySlot(slot);
        if (itemStack.isEmpty()) return;

        if (isHiddenByFirstPersonView(entity, slot)) return;

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

        if (isCustomModel && shouldUseBends && entityData instanceof BipedEntityData<?>
                && isBendableGeoArmor(armorModel))
        {
            BipedEntityData<?> geoData = (BipedEntityData<?>) entityData;

            if (geoData instanceof PlayerData && PlayerPreviewer.isPreviewInProgress())
            {
                geoData = (BipedEntityData<?>) PlayerPreviewer.getPreviewData();
            }

            if (renderCapturedGeoArmor(poseStack, bufferSource, packedLight, armorModel, defaultModel, itemStack, geoData, slot))
            {
                return;
            }
        }

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

    private static boolean isBendableGeoArmor(Model armorModel)
    {
        try
        {
            Class<?> geoClass = Class.forName("software.bernie.geckolib.renderer.GeoArmorRenderer");
            if (!geoClass.isInstance(armorModel))
            {
                return false;
            }

            return geoClass.isInstance(armorModel);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Nullable
    private static ResourceLocation geoArmorTexture(Model armorModel)
    {
        try
        {
            Class<?> geoClass = Class.forName("software.bernie.geckolib.renderer.GeoArmorRenderer");
            Object animatable = geoClass.getMethod("getAnimatable").invoke(armorModel);
            if (animatable == null)
            {
                return null;
            }

            for (java.lang.reflect.Method method : geoClass.getMethods())
            {
                if ("getTextureLocation".equals(method.getName())
                        && method.getParameterCount() == 1
                        && method.getReturnType() == ResourceLocation.class)
                {
                    return (ResourceLocation) method.invoke(armorModel, animatable);
                }
            }
        }
        catch (Exception e)
        {
            return null;
        }

        return null;
    }

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
        ResourceLocation texture = geoArmorTexture(armorModel);
        if (texture == null)
        {
            return false;
        }

        float[] savedState = captureArmorPartState(defaultModel);
        boolean[] savedVisibility = captureArmorPartVisibility(defaultModel);

        applyArmorRestPose(defaultModel);

        java.util.List<goblinbob.mobends.standard.client.model.armor.CapturedVertex> vertices = new java.util.ArrayList<>();
        java.util.List<goblinbob.mobends.standard.client.model.armor.BoneRegion> regions = new java.util.ArrayList<>();
        java.util.List<goblinbob.mobends.standard.client.model.armor.BoneRegion> blendRegions = new java.util.ArrayList<>();
        java.util.List<Float> blendWeights = new java.util.ArrayList<>();

        java.util.Set<String> alwaysDrawn = captureAlwaysDrawn(armorModel, defaultModel, packedLight,
                savedVisibility, slot, vertices, regions, blendRegions, blendWeights);

        for (GeoPart part : GeoPart.values())
        {
            capturePart(armorModel, defaultModel, packedLight, part, savedVisibility, alwaysDrawn,
                    vertices, regions, blendRegions, blendWeights);
        }

        restoreArmorPartVisibility(defaultModel, savedVisibility);
        restoreArmorPartState(defaultModel, savedState);

        if (vertices.isEmpty())
        {
            return false;
        }

        VertexConsumer outputConsumer = (VertexConsumer) IModelRenderHelper.Holder.getHelper().getArmorFoilBuffer(
                bufferSource, RenderType.armorCutoutNoCull(texture), itemStack.hasFoil());

        rigidRenderer.renderTaggedVertices(poseStack, outputConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                bipedData, vertices, regions, blendRegions, blendWeights);

        return true;
    }

    private java.util.Set<String> captureAlwaysDrawn(Model armorModel, HumanoidModel<E> defaultModel,
                                                     int packedLight, boolean[] slotVisibility, EquipmentSlot slot,
                                                     java.util.List<goblinbob.mobends.standard.client.model.armor.CapturedVertex> outVertices,
                                                     java.util.List<goblinbob.mobends.standard.client.model.armor.BoneRegion> outRegions,
                                                     java.util.List<goblinbob.mobends.standard.client.model.armor.BoneRegion> outBlendRegions,
                                                     java.util.List<Float> outBlendWeights)
    {
        applyOnlyVisible(defaultModel, null, slotVisibility);

        CapturingVertexConsumer capture = rigidRenderer.getCaptureConsumer();
        PoseStack capturePoseStack = new PoseStack();

        final com.mojang.blaze3d.vertex.VertexConsumer previousCapture =
                goblinbob.mobends.standard.client.model.armor.ArmorCaptureContext.begin(capture);
        try
        {
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

        java.util.List<goblinbob.mobends.standard.client.model.armor.CapturedVertex> captured = capture.getVertices();

        if (captured.isEmpty())
        {
            return java.util.Collections.emptySet();
        }

        java.util.Set<String> keys = new java.util.HashSet<>();
        goblinbob.mobends.standard.client.model.armor.ArmorBoneAssignment assignment = new goblinbob.mobends.standard.client.model.armor.ArmorBoneAssignment();

        final boolean quadAligned = captured.size() % 4 == 0;

        for (CapturedVertex v : captured)
        {
            keys.add(vertexKey(v));
        }

        if (!quadAligned)
        {
            for (CapturedVertex v : captured)
            {
                BoneRegion region = assignment.assignVertexForSlot(v.x, v.y, v.z, slot);
                outVertices.add(v);
                outRegions.add(region);
                outBlendRegions.add(region);
                outBlendWeights.add(0.0F);
            }

            return keys;
        }

        for (int q = 0; q + 3 < captured.size(); q += 4)
        {
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
                                captured.get(q + 2), captured.get(q + 3)),
                        outVertices, outRegions, outBlendRegions, outBlendWeights);

                continue;
            }

            BoneRegion baseRegion = assignment.assignVertexForSlot(cx, cy, cz, slot);

            for (int i = q; i < q + 4; ++i)
            {
                outVertices.add(captured.get(i));
                outRegions.add(baseRegion);
                outBlendRegions.add(baseRegion);
                outBlendWeights.add(0.0F);
            }
        }

        return keys;
    }

    private static String vertexKey(goblinbob.mobends.standard.client.model.armor.CapturedVertex v)
    {
        return Float.floatToIntBits(v.x) + ":" + Float.floatToIntBits(v.y) + ":" + Float.floatToIntBits(v.z)
                + ":" + Float.floatToIntBits(v.u) + ":" + Float.floatToIntBits(v.v);
    }

    private void capturePart(Model armorModel, HumanoidModel<E> defaultModel, int packedLight, GeoPart part,
                             boolean[] slotVisibility, java.util.Set<String> excluded,
                             java.util.List<goblinbob.mobends.standard.client.model.armor.CapturedVertex> outVertices,
                             java.util.List<goblinbob.mobends.standard.client.model.armor.BoneRegion> outRegions,
                             java.util.List<goblinbob.mobends.standard.client.model.armor.BoneRegion> outBlendRegions,
                             java.util.List<Float> outBlendWeights)
    {
        applyOnlyVisible(defaultModel, part, slotVisibility);

        CapturingVertexConsumer capture = rigidRenderer.getCaptureConsumer();
        PoseStack capturePoseStack = new PoseStack();

        final com.mojang.blaze3d.vertex.VertexConsumer previousCapture =
                goblinbob.mobends.standard.client.model.armor.ArmorCaptureContext.begin(capture);
        try
        {
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

        java.util.List<goblinbob.mobends.standard.client.model.armor.CapturedVertex> captured = capture.getVertices();

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

                emitVertex(part, v, outVertices, outRegions, outBlendRegions, outBlendWeights);
            }

            return;
        }

        final float joint = jointPlane(part);

        for (int q = 0; q + 3 < captured.size(); q += 4)
        {
            if (excluded.contains(vertexKey(captured.get(q)))
                    && excluded.contains(vertexKey(captured.get(q + 1)))
                    && excluded.contains(vertexKey(captured.get(q + 2)))
                    && excluded.contains(vertexKey(captured.get(q + 3))))
            {
                continue;
            }

            java.util.List<java.util.List<CapturedVertex>> polys = java.util.Collections.singletonList(
                    java.util.Arrays.asList(captured.get(q), captured.get(q + 1),
                            captured.get(q + 2), captured.get(q + 3)));

            if (!Float.isNaN(joint))
            {
                polys = clipAll(polys, AXIS_Y, joint - JOINT_BLEND_BAND);
                polys = clipAll(polys, AXIS_Y, joint + JOINT_BLEND_BAND);
            }

            for (java.util.List<CapturedVertex> poly : polys)
            {
                emitPoly(part, poly, outVertices, outRegions, outBlendRegions, outBlendWeights);
            }
        }

    }

    private static boolean isSkirtQuad(float centroidY, EquipmentSlot slot)
    {
        return slot == EquipmentSlot.CHEST && centroidY > SKIRT_MIN_Y;
    }

    private void emitSkirtQuad(java.util.List<CapturedVertex> quad,
                               java.util.List<CapturedVertex> outVertices,
                               java.util.List<BoneRegion> outRegions,
                               java.util.List<BoneRegion> outBlendRegions,
                               java.util.List<Float> outBlendWeights)
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
                emitSkirtPoly(piece, left, outVertices, outRegions, outBlendRegions, outBlendWeights);
            }
        }
    }

    private void emitSkirtPoly(java.util.List<CapturedVertex> poly, boolean left,
                               java.util.List<CapturedVertex> outVertices,
                               java.util.List<BoneRegion> outRegions,
                               java.util.List<BoneRegion> outBlendRegions,
                               java.util.List<Float> outBlendWeights)
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
                emitSkirtVertex(poly.get(i), left, outVertices, outRegions, outBlendRegions, outBlendWeights);
            }

            return;
        }

        for (int k = 1; k + 1 < n; ++k)
        {
            emitSkirtVertex(poly.get(0), left, outVertices, outRegions, outBlendRegions, outBlendWeights);
            emitSkirtVertex(poly.get(k), left, outVertices, outRegions, outBlendRegions, outBlendWeights);
            emitSkirtVertex(poly.get(k + 1), left, outVertices, outRegions, outBlendRegions, outBlendWeights);
            emitSkirtVertex(poly.get(k + 1), left, outVertices, outRegions, outBlendRegions, outBlendWeights);
        }
    }

    private void emitSkirtVertex(CapturedVertex v, boolean left,
                                 java.util.List<CapturedVertex> outVertices,
                                 java.util.List<BoneRegion> outRegions,
                                 java.util.List<BoneRegion> outBlendRegions,
                                 java.util.List<Float> outBlendWeights)
    {
        outVertices.add(v);
        outRegions.add(left ? BoneRegion.LEFT_LEG_UPPER : BoneRegion.RIGHT_LEG_UPPER);
        outBlendRegions.add(left ? BoneRegion.LEFT_LEG_LOWER : BoneRegion.RIGHT_LEG_LOWER);
        outBlendWeights.add(skirtKneeBlend(v.y));
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

    private void emitPoly(GeoPart part, java.util.List<CapturedVertex> poly,
                          java.util.List<CapturedVertex> outVertices,
                          java.util.List<BoneRegion> outRegions,
                          java.util.List<BoneRegion> outBlendRegions,
                          java.util.List<Float> outBlendWeights)
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
                emitVertex(part, poly.get(i), outVertices, outRegions, outBlendRegions, outBlendWeights);
            }

            return;
        }

        for (int k = 1; k + 1 < n; ++k)
        {
            emitVertex(part, poly.get(0), outVertices, outRegions, outBlendRegions, outBlendWeights);
            emitVertex(part, poly.get(k), outVertices, outRegions, outBlendRegions, outBlendWeights);
            emitVertex(part, poly.get(k + 1), outVertices, outRegions, outBlendRegions, outBlendWeights);
            emitVertex(part, poly.get(k + 1), outVertices, outRegions, outBlendRegions, outBlendWeights);
        }
    }

    private void emitVertex(GeoPart part, CapturedVertex v,
                            java.util.List<CapturedVertex> outVertices,
                            java.util.List<BoneRegion> outRegions,
                            java.util.List<BoneRegion> outBlendRegions,
                            java.util.List<Float> outBlendWeights)
    {
        outVertices.add(v);
        outRegions.add(upperRegionFor(part));
        outBlendRegions.add(lowerRegionFor(part));
        outBlendWeights.add(jointBlend(part, v.y));
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
        float joint;

        switch (part)
        {
            case LEFT_ARM:
            case RIGHT_ARM:
                joint = ELBOW_Y;
                break;
            case LEFT_LEG:
            case RIGHT_LEG:
                joint = KNEE_Y;
                break;
            default:
                return 0.0F;
        }

        float t = (y - (joint - JOINT_BLEND_BAND)) / (2.0F * JOINT_BLEND_BAND);
        return Math.max(0.0F, Math.min(1.0F, t));
    }

    private static goblinbob.mobends.standard.client.model.armor.BoneRegion regionFor(GeoPart part, float y)
    {
        switch (part)
        {
            case HEAD:
                return goblinbob.mobends.standard.client.model.armor.BoneRegion.HEAD;
            case LEFT_ARM:
                return y <= ELBOW_Y ? goblinbob.mobends.standard.client.model.armor.BoneRegion.LEFT_ARM_UPPER : goblinbob.mobends.standard.client.model.armor.BoneRegion.LEFT_ARM_LOWER;
            case RIGHT_ARM:
                return y <= ELBOW_Y ? goblinbob.mobends.standard.client.model.armor.BoneRegion.RIGHT_ARM_UPPER : goblinbob.mobends.standard.client.model.armor.BoneRegion.RIGHT_ARM_LOWER;
            case LEFT_LEG:
                return y <= KNEE_Y ? goblinbob.mobends.standard.client.model.armor.BoneRegion.LEFT_LEG_UPPER : goblinbob.mobends.standard.client.model.armor.BoneRegion.LEFT_LEG_LOWER;
            case RIGHT_LEG:
                return y <= KNEE_Y ? goblinbob.mobends.standard.client.model.armor.BoneRegion.RIGHT_LEG_UPPER : goblinbob.mobends.standard.client.model.armor.BoneRegion.RIGHT_LEG_LOWER;
            case BODY:
            default:
                return goblinbob.mobends.standard.client.model.armor.BoneRegion.BODY;
        }
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

        return goblinbob.mobends.compat.FirstPersonModelCompat.showsVanillaHands(getParentModel());
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
            java.util.List<goblinbob.mobends.standard.client.model.armor.ImmersiveArmorsSupport.Layer> extendedLayers =
                    resolveExtendedArmorLayers(armorItem, slot);

            if (!extendedLayers.isEmpty())
            {
                renderExtendedArmorLayers(poseStack, bufferSource, packedLight, entity,
                        armorModel, slot, itemStack, bipedData, extendedLayers);
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
                    armorItem,
                    armorModel,
                    bipedData,
                    texture
            );

            renderArmorOverlayPass(poseStack, bufferSource, packedLight, entity, armorItem,
                    armorModel, slot, itemStack, bipedData);
        }
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
                                           int packedLight, E entity,
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
        }
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
                armorItem,
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
