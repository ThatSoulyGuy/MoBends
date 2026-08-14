package goblinbob.mobends.standard.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomBipedArmor;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomHeldItem;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

public abstract class BipedMutator<D extends BipedEntityData<E>,
                                   E extends LivingEntity,
                                   M extends HumanoidModel<E>>
                                  extends Mutator<D, E, M>
{
    protected BendsModelPart body;
    protected BendsModelPart head;
    protected BendsModelPart headwear;
    protected BendsModelPart leftArm;
    protected BendsModelPart rightArm;
    protected BendsModelPart leftForeArm;
    protected BendsModelPart rightForeArm;
    protected BendsModelPart leftLeg;
    protected BendsModelPart rightLeg;
    protected BendsModelPart leftForeLeg;
    protected BendsModelPart rightForeLeg;

    protected BendsModelPart outerBody;
    protected BendsModelPart outerHead;
    protected BendsModelPart outerLeftArm;
    protected BendsModelPart outerRightArm;
    protected BendsModelPart outerLeftForeArm;
    protected BendsModelPart outerRightForeArm;
    protected BendsModelPart outerLeftLeg;
    protected BendsModelPart outerRightLeg;
    protected BendsModelPart outerLeftForeLeg;
    protected BendsModelPart outerRightForeLeg;

    protected ModelPart extraHeadPart;

    protected float babyHeadScale = 1.0F;

    protected ModelPart vanillaBody;
    protected ModelPart vanillaHead;
    protected ModelPart vanillaHat;
    protected ModelPart vanillaLeftArm;
    protected ModelPart vanillaRightArm;
    protected ModelPart vanillaLeftLeg;
    protected ModelPart vanillaRightLeg;

    private VanillaPartState vanillaBodyState;
    private VanillaPartState vanillaHeadState;
    private VanillaPartState vanillaHatState;
    private VanillaPartState vanillaLeftArmState;
    private VanillaPartState vanillaRightArmState;
    private VanillaPartState vanillaLeftLegState;
    private VanillaPartState vanillaRightLegState;

    protected LayerCustomBipedArmor<E, M> layerArmor;
    protected HumanoidArmorLayer<E, M, ?> layerArmorVanilla;
    protected LayerCustomHeldItem<E, M> layerHeldItem;
    protected ItemInHandLayer<E, M> layerHeldItemVanilla;
    protected CustomHeadLayer<E, M> layerCustomHead;
    protected CustomHeadLayer<E, M> layerCustomHeadVanilla;

    public BipedMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public void storeVanillaModel(M model)
    {
        this.vanillaBody = model.body;
        this.vanillaHead = model.head;
        this.vanillaHat = model.hat;
        this.vanillaLeftArm = model.leftArm;
        this.vanillaRightArm = model.rightArm;
        this.vanillaLeftLeg = model.leftLeg;
        this.vanillaRightLeg = model.rightLeg;

        this.vanillaBodyState = VanillaPartState.capture(model.body);
        this.vanillaHeadState = VanillaPartState.capture(model.head);
        this.vanillaHatState = VanillaPartState.capture(model.hat);
        this.vanillaLeftArmState = VanillaPartState.capture(model.leftArm);
        this.vanillaRightArmState = VanillaPartState.capture(model.rightArm);
        this.vanillaLeftLegState = VanillaPartState.capture(model.leftLeg);
        this.vanillaRightLegState = VanillaPartState.capture(model.rightLeg);
    }

    @Override
    public void applyVanillaModel(M model)
    {
        if (model == null)
            return;

        VanillaPartState.restore(this.vanillaBodyState, model.body);
        VanillaPartState.restore(this.vanillaHeadState, model.head);
        VanillaPartState.restore(this.vanillaHatState, model.hat);
        VanillaPartState.restore(this.vanillaLeftArmState, model.leftArm);
        VanillaPartState.restore(this.vanillaRightArmState, model.rightArm);
        VanillaPartState.restore(this.vanillaLeftLegState, model.leftLeg);
        VanillaPartState.restore(this.vanillaRightLegState, model.rightLeg);

        this.vanillaPositionsStored = false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void swapLayer(LivingEntityRenderer<E, M> renderer, int index, boolean isModelVanilla)
    {
        RenderLayer<E, M> layer = layerRenderers.get(index);
        if (layer instanceof HumanoidArmorLayer)
        {
            HumanoidArmorLayer<E, M, ?> vanillaArmor = (HumanoidArmorLayer<E, M, ?>) layer;
            if (isModelVanilla)
                this.layerArmorVanilla = vanillaArmor;

            this.layerArmor = new LayerCustomBipedArmor<>(renderer, this);
            this.layerArmor.setVanillaArmorLayer(vanillaArmor);

            try
            {
                net.minecraft.client.model.geom.ModelLayerLocation innerLocation =
                    net.minecraft.client.model.geom.ModelLayers.PLAYER_INNER_ARMOR;
                net.minecraft.client.model.geom.ModelLayerLocation outerLocation =
                    net.minecraft.client.model.geom.ModelLayers.PLAYER_OUTER_ARMOR;

                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                HumanoidModel<?> innerModel = new HumanoidModel<>(mc.getEntityModels().bakeLayer(innerLocation));
                HumanoidModel<?> outerModel = new HumanoidModel<>(mc.getEntityModels().bakeLayer(outerLocation));

                this.layerArmor.setArmorModels(innerModel, outerModel);
            }
            catch (Exception e)
            {
            }

            layerRenderers.set(index, this.layerArmor);
        }
        else if (layer instanceof ItemInHandLayer)
        {
            this.layerHeldItem = new LayerCustomHeldItem<>(renderer, this);
            if (isModelVanilla)
                this.layerHeldItemVanilla = (ItemInHandLayer<E, M>) layer;
            layerRenderers.set(index, this.layerHeldItem);
        }
        else if (layer instanceof CustomHeadLayer)
        {
            if (isModelVanilla)
                this.layerCustomHeadVanilla = (CustomHeadLayer<E, M>) layer;
        }
    }

    @Override
    public void deswapLayer(LivingEntityRenderer<E, M> renderer, int index)
    {
        RenderLayer<E, M> layer = layerRenderers.get(index);
        if (layer instanceof LayerCustomBipedArmor && this.layerArmorVanilla != null)
        {
            layerRenderers.set(index, this.layerArmorVanilla);
        }
        else if (layer instanceof LayerCustomHeldItem && this.layerHeldItemVanilla != null)
        {
            layerRenderers.set(index, this.layerHeldItemVanilla);
        }
        else if (layer == this.layerCustomHead && this.layerCustomHeadVanilla != null)
        {
            layerRenderers.set(index, this.layerCustomHeadVanilla);
        }
    }

    protected void createHeadParts(float scaleFactor)
    {
        head = new BendsModelPart(0, 0)
                .setTextureSize(64, 64)
                .setPosition(0.0F, -12.0F, 0.0F);
        head.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor);
        body.addChild(head);

        headwear = new BendsModelPart(32, 0)
                .setTextureSize(64, 64);
        headwear.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor + 0.5F);
        head.addChild(headwear);
    }

    protected void createOuterHeadParts(float scaleFactor, float outerOffset)
    {
        outerHead = new BendsModelPart(0, 0)
                .setTextureSize(64, 64)
                .setPosition(0.0F, -12.0F, 0.0F);
        outerHead.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor + outerOffset);
        outerBody.addChild(outerHead);
    }

    @Override
    public boolean createParts(M original, float scaleFactor)
    {
        body = new BendsModelPart(16, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12.0F, 0.0F);
        body.addCube(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor);

        createHeadParts(scaleFactor);

        int armWidth = 4;
        float armY = -10F;

        leftArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 64)
                .setPosition(5.0F, armY, 0.0F)
                .setMirror(true);
        leftArm.developBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        body.addChild(leftArm);

        rightArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 64)
                .setPosition(-5.0F, armY, 0.0F);
        rightArm.developBox(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        body.addChild(rightArm);

        leftForeArm = new BendsModelPart(40, 22)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 4.0F, 2.0F)
                .setMirror(true);
        leftForeArm.developBox(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        leftArm.addChild(leftForeArm);

        rightForeArm = new BendsModelPart(40, 22)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 4.0F, 2.0F);
        rightForeArm.developBox(-armWidth + 1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        rightArm.addChild(rightForeArm);

        rightLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12F, 0F);
        rightLeg.addCube(-3.9F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);

        leftLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12.0F, 0.0F)
                .setMirror(true);
        leftLeg.addCube(-0.1F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);

        leftForeLeg = new BendsModelPart(0, 22)
                .setTextureSize(64, 64)
                .setPosition(0, 6.0F, -2.0F)
                .setMirror(true);
        leftForeLeg.developBox(-0.1F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        leftLeg.addChild(leftForeLeg);

        rightForeLeg = new BendsModelPart(0, 22)
                .setTextureSize(64, 64)
                .setPosition(0, 6.0F, -2.0F);
        rightForeLeg.developBox(-3.9F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        rightLeg.addChild(rightForeLeg);

        createOuterParts(scaleFactor);

        return true;
    }

    protected void createOuterParts(float scaleFactor)
    {
        final float outerOffset = 0.25F;
        final float limbWearHeight = (6F + 2 * scaleFactor + 0.5F) - 0.25F;
        int armWidth = 4;
        float armY = -10F;

        outerBody = new BendsModelPart(16, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12.0F, 0.0F);
        outerBody.addCube(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor + outerOffset);

        createOuterHeadParts(scaleFactor, outerOffset);

        outerLeftArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 64)
                .setPosition(5.0F, armY, 0.0F)
                .setMirror(true);
        outerLeftArm.developBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .inflate(0.0025F, 0F, 0.0025F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        outerBody.addChild(outerLeftArm);

        outerRightArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 64)
                .setPosition(-5.0F, armY, 0.0F);
        outerRightArm.developBox(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .inflate(0.0025F, 0F, 0.0025F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        outerBody.addChild(outerRightArm);

        outerLeftForeArm = new BendsModelPart(40, 22)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 4.0F, 2.0F)
                .setMirror(true);
        outerLeftForeArm.developBox(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        outerLeftArm.addChild(outerLeftForeArm);

        outerRightForeArm = new BendsModelPart(40, 22)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 4.0F, 2.0F);
        outerRightForeArm.developBox(-armWidth + 1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        outerRightArm.addChild(outerRightForeArm);

        outerRightLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12F, 0F);
        outerRightLeg.developBox(-3.9F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .hideFace(BoxSide.BOTTOM)
                .create();

        outerLeftLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12.0F, 0.0F)
                .setMirror(true);
        outerLeftLeg.developBox(-0.1F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .hideFace(BoxSide.BOTTOM)
                .create();

        outerLeftForeLeg = new BendsModelPart(0, 22)
                .setTextureSize(64, 64)
                .setPosition(0, 6.0F, -2.0F)
                .setMirror(true);
        outerLeftForeLeg.developBox(-0.1F, 0.0F, 0.0F, 4, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        outerLeftLeg.addChild(outerLeftForeLeg);

        outerRightForeLeg = new BendsModelPart(0, 22)
                .setTextureSize(64, 64)
                .setPosition(0, 6.0F, -2.0F);
        outerRightForeLeg.developBox(-3.9F, 0.0F, 0.0F, 4, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        outerRightLeg.addChild(outerRightForeLeg);
    }

    @Override
    public void syncUpWithData(D data)
    {
        head.syncUp(data.head);
        body.syncUp(data.body);
        leftArm.syncUp(data.leftArm);
        rightArm.syncUp(data.rightArm);
        leftLeg.syncUp(data.leftLeg);
        rightLeg.syncUp(data.rightLeg);
        leftForeArm.syncUp(data.leftForeArm);
        rightForeArm.syncUp(data.rightForeArm);
        leftForeLeg.syncUp(data.leftForeLeg);
        rightForeLeg.syncUp(data.rightForeLeg);
    }

    @Override
    public boolean isModelVanilla(M model)
    {
        return this.body == null;
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof HumanoidModel);
    }

    @Override
    public boolean shouldRenderCustom()
    {
        return this.body != null;
    }

    @Override
    public void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                              int packedLight, int packedOverlay, int color)
    {
        applyBabyHeadScale();
        syncConcealmentFromVanillaModel();

        if (body != null)
        {
            body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }

        if (extraHeadPart != null && extraHeadPart.visible && head != null && head.isShowing())
        {
            poseStack.pushPose();
            head.applyCharacterTransformPoseStack(poseStack);
            extraHeadPart.render(poseStack, vertexConsumer, packedLight, packedOverlay);
            poseStack.popPose();
        }

        if (leftLeg != null)
        {
            leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
        if (rightLeg != null)
        {
            rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
    }

    protected void syncConcealmentFromVanillaModel()
    {
        final HumanoidModel<?> model = MoBendsRenderContext.getCurrentVanillaModel();
        if (model == null)
        {
            return;
        }

        clearConcealment();

        applyConcealment(head, model.head);
        applyConcealment(body, model.body);
        applyConcealment(leftArm, model.leftArm);
        applyConcealment(leftForeArm, model.leftArm);
        applyConcealment(rightArm, model.rightArm);
        applyConcealment(rightForeArm, model.rightArm);
        applyConcealment(leftLeg, model.leftLeg);
        applyConcealment(leftForeLeg, model.leftLeg);
        applyConcealment(rightLeg, model.rightLeg);
        applyConcealment(rightForeLeg, model.rightLeg);

        applySkinConcealment(model);
    }

    private void applySkinConcealment(HumanoidModel<?> model)
    {
        if (!goblinbob.mobends.compat.ArmourersWorkshopCompat.isModLoaded())
        {
            return;
        }

        concealIfSkinned(head, model.head);
        concealIfSkinned(body, model.body);
        concealIfSkinned(leftArm, model.leftArm);
        concealIfSkinned(leftForeArm, model.leftArm);
        concealIfSkinned(rightArm, model.rightArm);
        concealIfSkinned(rightForeArm, model.rightArm);
        concealIfSkinned(leftLeg, model.leftLeg);
        concealIfSkinned(leftForeLeg, model.leftLeg);
        concealIfSkinned(rightLeg, model.rightLeg);
        concealIfSkinned(rightForeLeg, model.rightLeg);

        syncOuterConcealment(model);
    }

    protected void clearConcealment()
    {
        clearConcealed(head);
        clearConcealed(body);
        clearConcealed(leftArm);
        clearConcealed(leftForeArm);
        clearConcealed(rightArm);
        clearConcealed(rightForeArm);
        clearConcealed(leftLeg);
        clearConcealed(leftForeLeg);
        clearConcealed(rightLeg);
        clearConcealed(rightForeLeg);
        clearConcealed(headwear);
        clearConcealed(outerHead);
        clearConcealed(outerBody);
        clearConcealed(outerLeftArm);
        clearConcealed(outerLeftForeArm);
        clearConcealed(outerRightArm);
        clearConcealed(outerRightForeArm);
        clearConcealed(outerLeftLeg);
        clearConcealed(outerLeftForeLeg);
        clearConcealed(outerRightLeg);
        clearConcealed(outerRightForeLeg);
    }

    protected static void clearConcealed(BendsModelPart part)
    {
        if (part != null)
        {
            part.concealed = false;
        }
    }

    protected void syncOuterConcealment(HumanoidModel<?> model)
    {
        concealWith(headwear, head, model.hat);
        concealWith(outerHead, head, model.hat);
        concealWith(outerBody, body, model.body);
        concealWith(outerLeftArm, leftArm, model.leftArm);
        concealWith(outerLeftForeArm, leftForeArm, model.leftArm);
        concealWith(outerRightArm, rightArm, model.rightArm);
        concealWith(outerRightForeArm, rightForeArm, model.rightArm);
        concealWith(outerLeftLeg, leftLeg, model.leftLeg);
        concealWith(outerLeftForeLeg, leftForeLeg, model.leftLeg);
        concealWith(outerRightLeg, rightLeg, model.rightLeg);
        concealWith(outerRightForeLeg, rightForeLeg, model.rightLeg);
    }

    protected static void concealIfSkinned(BendsModelPart part, ModelPart modelPart)
    {
        if (part == null || modelPart == null)
        {
            return;
        }
        if (goblinbob.mobends.compat.armourers.AWHiddenParts.isHidden(modelPart))
        {
            part.concealed = true;
        }
    }

    protected static void concealWith(BendsModelPart part, BendsModelPart basePart, ModelPart overlayPart)
    {
        if (part == null)
        {
            return;
        }
        if (basePart != null && basePart.concealed)
        {
            part.concealed = true;
            return;
        }
        concealIfSkinned(part, overlayPart);
    }

    private static void applyConcealment(BendsModelPart part, ModelPart modelPart)
    {
        if (part == null || modelPart == null)
        {
            return;
        }
        part.concealed = !modelPart.visible;
    }

    public void setBabyHeadScale(float scale)
    {
        this.babyHeadScale = scale;
    }

    protected void applyBabyHeadScale()
    {
        if (head != null)
        {
            head.scale.set(babyHeadScale, babyHeadScale, babyHeadScale);
        }
    }

    public boolean hasOuterParts()
    {
        return outerBody != null;
    }

    public void renderOuter(PoseStack poseStack, VertexConsumer vertexConsumer,
                            int packedLight, int packedOverlay, int color)
    {
        if (!hasOuterParts())
        {
            return;
        }
        syncOuterFromBase();

        outerBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        outerLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        outerRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void syncOuterFromBase()
    {
        copyAnimatedState(body, outerBody);
        copyAnimatedState(head, outerHead);
        copyAnimatedState(leftArm, outerLeftArm);
        copyAnimatedState(rightArm, outerRightArm);
        copyAnimatedState(leftForeArm, outerLeftForeArm);
        copyAnimatedState(rightForeArm, outerRightForeArm);
        copyAnimatedState(leftLeg, outerLeftLeg);
        copyAnimatedState(rightLeg, outerRightLeg);
        copyAnimatedState(leftForeLeg, outerLeftForeLeg);
        copyAnimatedState(rightForeLeg, outerRightForeLeg);
    }

    private static void copyAnimatedState(BendsModelPart src, BendsModelPart dst)
    {
        if (src == null || dst == null) return;
        dst.position.set(src.position);
        dst.offset.set(src.offset);
        dst.scale.set(src.scale);
        dst.offsetScale = src.offsetScale;
        dst.globalOffset.set(src.globalOffset);
        dst.rotation.set(src.rotation);
        dst.visible = src.visible;
        dst.hidden = src.hidden;
    }

    public BendsModelPart getBody() { return body; }
    public BendsModelPart getHead() { return head; }
    public BendsModelPart getLeftArm() { return leftArm; }
    public BendsModelPart getRightArm() { return rightArm; }
    public BendsModelPart getLeftForeArm() { return leftForeArm; }
    public BendsModelPart getRightForeArm() { return rightForeArm; }
    public BendsModelPart getLeftLeg() { return leftLeg; }
    public BendsModelPart getRightLeg() { return rightLeg; }
    public BendsModelPart getLeftForeLeg() { return leftForeLeg; }
    public BendsModelPart getRightForeLeg() { return rightForeLeg; }

    private float[] vanillaBodyPos, vanillaHeadPos, vanillaLeftArmPos, vanillaRightArmPos,
                    vanillaLeftLegPos, vanillaRightLegPos;
    private boolean vanillaPositionsStored = false;

    public void syncPosesToVanillaModel(HumanoidModel<?> model)
    {
        if (model == null) return;

        if (!vanillaPositionsStored)
        {
            vanillaBodyPos = new float[]{model.body.x, model.body.y, model.body.z};
            vanillaHeadPos = new float[]{model.head.x, model.head.y, model.head.z};
            vanillaLeftArmPos = new float[]{model.leftArm.x, model.leftArm.y, model.leftArm.z};
            vanillaRightArmPos = new float[]{model.rightArm.x, model.rightArm.y, model.rightArm.z};
            vanillaLeftLegPos = new float[]{model.leftLeg.x, model.leftLeg.y, model.leftLeg.z};
            vanillaRightLegPos = new float[]{model.rightLeg.x, model.rightLeg.y, model.rightLeg.z};
            vanillaPositionsStored = true;
        }

        Quaternion bodyRotation = body.rotation.getSmooth();
        float bodyPivotX = body.globalOffset.x + (body.position.x + body.offset.x) * body.offsetScale;
        float bodyPivotY = body.globalOffset.y + (body.position.y + body.offset.y) * body.offsetScale;
        float bodyPivotZ = body.globalOffset.z + (body.position.z + body.offset.z) * body.offsetScale;

        float[] bodyNeck = rotateVectorByQuaternion(bodyRotation, 0.0F, -12.0F, 0.0F);
        model.body.x = bodyPivotX + bodyNeck[0];
        model.body.y = bodyPivotY + bodyNeck[1];
        model.body.z = bodyPivotZ + bodyNeck[2];
        float[] bodyEuler = quaternionToEulerXYZ(bodyRotation);
        model.body.xRot = bodyEuler[0];
        model.body.yRot = bodyEuler[1];
        model.body.zRot = bodyEuler[2];
        model.body.visible = model.body.visible && body.isShowingIgnoringConcealment();

        syncBodyChildToModelPart(head, model.head, bodyPivotX, bodyPivotY, bodyPivotZ, bodyRotation);
        syncBodyChildToModelPart(leftArm, model.leftArm, bodyPivotX, bodyPivotY, bodyPivotZ, bodyRotation);
        syncBodyChildToModelPart(rightArm, model.rightArm, bodyPivotX, bodyPivotY, bodyPivotZ, bodyRotation);

        syncPartToModelPart(leftLeg, model.leftLeg, vanillaLeftLegPos);
        syncPartToModelPart(rightLeg, model.rightLeg, vanillaRightLegPos);

        model.head.xScale = babyHeadScale;
        model.head.yScale = babyHeadScale;
        model.head.zScale = babyHeadScale;

        if (model.hat != null && head != null)
        {
            model.hat.xScale = babyHeadScale;
            model.hat.yScale = babyHeadScale;
            model.hat.zScale = babyHeadScale;
            model.hat.visible = model.hat.visible && head.isShowingIgnoringConcealment();
            model.hat.x = model.head.x;
            model.hat.y = model.head.y;
            model.hat.z = model.head.z;
            model.hat.xRot = model.head.xRot;
            model.hat.yRot = model.head.yRot;
            model.hat.zRot = model.head.zRot;
        }
    }

    private void syncPartToModelPart(BendsModelPart bendsPart, ModelPart modelPart, float[] vanillaPos)
    {
        if (bendsPart == null || modelPart == null) return;

        if (vanillaPos != null)
        {
            modelPart.x = vanillaPos[0] + bendsPart.offset.x;
            modelPart.y = vanillaPos[1] + bendsPart.offset.y;
            modelPart.z = vanillaPos[2] + bendsPart.offset.z;
        }

        Quaternion q = bendsPart.rotation.getSmooth();
        float[] euler = quaternionToEulerXYZ(q);
        modelPart.xRot = euler[0];
        modelPart.yRot = euler[1];
        modelPart.zRot = euler[2];

        modelPart.visible = modelPart.visible && bendsPart.isShowingIgnoringConcealment();
    }

    private void syncBodyChildToModelPart(BendsModelPart child, ModelPart modelPart,
                                          float bodyPivotX, float bodyPivotY, float bodyPivotZ,
                                          Quaternion bodyRotation)
    {
        if (child == null || modelPart == null) return;
        float[] pivot = new float[3];
        Quaternion rotation = composeChildWorld(bodyPivotX, bodyPivotY, bodyPivotZ, bodyRotation, child, pivot);
        setEndModelPart(modelPart, pivot, rotation, modelPart.visible && child.isShowingIgnoringConcealment());
    }

    private static Quaternion composeChildWorld(float parentPivotX, float parentPivotY, float parentPivotZ,
                                                Quaternion parentRotation, BendsModelPart child, float[] outPivot)
    {
        float lx = (child.position.x + child.offset.x) * child.offsetScale;
        float ly = (child.position.y + child.offset.y) * child.offsetScale;
        float lz = (child.position.z + child.offset.z) * child.offsetScale;
        float[] rotated = rotateVectorByQuaternion(parentRotation, lx, ly, lz);
        outPivot[0] = parentPivotX + rotated[0];
        outPivot[1] = parentPivotY + rotated[1];
        outPivot[2] = parentPivotZ + rotated[2];
        return Quaternion.mul(parentRotation, child.rotation.getSmooth(), new Quaternion());
    }

    private static void setEndModelPart(ModelPart modelPart, float[] pivot, Quaternion rotation, boolean visible)
    {
        modelPart.x = pivot[0];
        modelPart.y = pivot[1];
        modelPart.z = pivot[2];
        float[] euler = quaternionToEulerXYZ(rotation);
        modelPart.xRot = euler[0];
        modelPart.yRot = euler[1];
        modelPart.zRot = euler[2];
        modelPart.visible = visible;
    }

    private static float[] rotateVectorByQuaternion(Quaternion q, float x, float y, float z)
    {
        float tx = 2.0F * (q.y * z - q.z * y);
        float ty = 2.0F * (q.z * x - q.x * z);
        float tz = 2.0F * (q.x * y - q.y * x);
        return new float[]{
                x + q.w * tx + (q.y * tz - q.z * ty),
                y + q.w * ty + (q.z * tx - q.x * tz),
                z + q.w * tz + (q.x * ty - q.y * tx)
        };
    }

    private static final float[] ZERO_EULER = {0, 0, 0};

    public float[] getPartEulerAngles(BendsModelPart part)
    {
        if (part == null) return ZERO_EULER;
        return quaternionToEulerXYZ(part.rotation.getSmooth());
    }

    public float[] getLeftForeArmEulerAngles() { return getPartEulerAngles(leftForeArm); }
    public float[] getRightForeArmEulerAngles() { return getPartEulerAngles(rightForeArm); }
    public float[] getLeftForeLegEulerAngles() { return getPartEulerAngles(leftForeLeg); }
    public float[] getRightForeLegEulerAngles() { return getPartEulerAngles(rightForeLeg); }

    private static float[] quaternionToEulerXYZ(Quaternion q)
    {
        float[] euler = new float[3];

        float sinX = 2.0f * (q.w * q.x + q.y * q.z);
        float cosX = 1.0f - 2.0f * (q.x * q.x + q.y * q.y);
        euler[0] = (float) Math.atan2(sinX, cosX);

        float sinY = 2.0f * (q.w * q.y - q.z * q.x);
        if (Math.abs(sinY) >= 1.0f)
        {
            euler[1] = (float) Math.copySign(Math.PI / 2, sinY);
        }
        else
        {
            euler[1] = (float) Math.asin(sinY);
        }

        float sinZ = 2.0f * (q.w * q.z + q.x * q.y);
        float cosZ = 1.0f - 2.0f * (q.y * q.y + q.z * q.z);
        euler[2] = (float) Math.atan2(sinZ, cosZ);

        return euler;
    }

    private static final class VanillaPartState
    {
        private final float x, y, z;
        private final float xRot, yRot, zRot;
        private final float xScale, yScale, zScale;
        private final boolean visible, skipDraw;

        private VanillaPartState(ModelPart part)
        {
            this.x = part.x;
            this.y = part.y;
            this.z = part.z;
            this.xRot = part.xRot;
            this.yRot = part.yRot;
            this.zRot = part.zRot;
            this.xScale = part.xScale;
            this.yScale = part.yScale;
            this.zScale = part.zScale;
            this.visible = part.visible;
            this.skipDraw = part.skipDraw;
        }

        private static VanillaPartState capture(ModelPart part)
        {
            return part != null ? new VanillaPartState(part) : null;
        }

        private static void restore(VanillaPartState state, ModelPart part)
        {
            if (state == null || part == null)
                return;

            part.x = state.x;
            part.y = state.y;
            part.z = state.z;
            part.xRot = state.xRot;
            part.yRot = state.yRot;
            part.zRot = state.zRot;
            part.xScale = state.xScale;
            part.yScale = state.yScale;
            part.zScale = state.zScale;
            part.visible = state.visible;
            part.skipDraw = state.skipDraw;
        }
    }

}
