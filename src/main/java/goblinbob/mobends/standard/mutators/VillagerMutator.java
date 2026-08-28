package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomHeldItem;
import goblinbob.mobends.standard.data.VillagerData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class VillagerMutator<E extends LivingEntity>
        extends BipedMutator<VillagerData<E>, E, VillagerModel<E>>
{
    private static final float ARM_X = 5.0F;
    private static final float ARM_Y = -10.0F;
    private static final float ARM_Z = 0.0F;

    private static final float ARM_WIDTH = 4.0F;

    private static final float BODY_HALF_WIDTH = 4.0F;
    private static final float ROBE_INFLATE = 0.5F;
    private static final float SHOULDER_OVERLAP = 0.05F;

    private static final float ARM_INNER_X = BODY_HALF_WIDTH + ROBE_INFLATE - SHOULDER_OVERLAP;

    private static final int FOREARM_LENGTH = 4;
    private static final int HAND_EXPOSED = 2;

    private static final int HAND_TEX_U = 42;
    private static final int HAND_TEX_V = 38;


    private static final int TRIM_RED_U = 3;
    private static final int TRIM_RED_V = 8;
    private static final int TRIM_YELLOW_U = 18;
    private static final int TRIM_YELLOW_V = 41;
    private static final float TRIM_INFLATE = 0.02F;

    private static final boolean HAT_RIM_ENABLED = true;
    private static final int GLOVE_LENGTH = 4;

    private static final int ROBE_TEX_U = 0;
    private static final int ROBE_TEX_V = 38;
    private static final int ROBE_WIDTH = 8;
    private static final int ROBE_DEPTH = 6;
    private static final int ROBE_TORSO_HEIGHT = 12;
    private static final int SKIRT_OVERLAP = 3;
    private static final int SKIRT_HEIGHT = 8 + SKIRT_OVERLAP;
    private static final float SKIRT_TUCK = 0.05F;

    private static final float SKIRT_FOLLOW = 1.0F;
    private static final float SKIRT_MAX_FOLD = 90.0F;
    private static final float SKIRT_MAX_LIFT = 20.0F;

    protected int textureHeight()
    {
        return 64;
    }

    protected int handTexU()
    {
        return HAND_TEX_U;
    }

    protected int handTexV()
    {
        return HAND_TEX_V;
    }

    protected static float armBoxX(boolean left)
    {
        return left
                ? ARM_INNER_X - ARM_X
                : ARM_X - ARM_INNER_X - ARM_WIDTH;
    }

    private final Map<VillagerModel<?>, HumanoidModel<?>> views = new IdentityHashMap<>();

    private static BendsModelPart lastOuterHand;
    private static BendsModelPart lastWristTrim;
    private static BendsModelPart lastSkirt;
    private BendsModelPart outerLeftHand, outerRightHand;
    private BendsModelPart leftWristTrim, rightWristTrim;
    private BendsModelPart skirt, outerSkirt;

    public VillagerMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public void swapLayer(LivingEntityRenderer<E, VillagerModel<E>> renderer, int index, boolean isModelVanilla)
    {
        RenderLayer<E, VillagerModel<E>> layer = layerRenderers.get(index);

        if (layer instanceof CrossedArmsItemLayer)
        {
            LayerCustomHeldItem<E, VillagerModel<E>> heldItem = new LayerCustomHeldItem<>(renderer, this);
            heldItem.setFallbackLayer(layer);

            this.layerHeldItem = heldItem;
            this.originalLayers.put(index, layer);
            layerRenderers.set(index, heldItem);
            return;
        }

        super.swapLayer(renderer, index, isModelVanilla);
    }

    @Override
    public HumanoidModel<?> humanoidViewOf(EntityModel<?> model)
    {
        if (!(model instanceof VillagerModel<?> villagerModel))
        {
            return null;
        }

        return views.computeIfAbsent(villagerModel, VillagerMutator::buildView);
    }

    private static HumanoidModel<?> buildView(VillagerModel<?> model)
    {
        final ModelPart root = model.root();
        final ModelPart head = root.getChild("head");

        final Map<String, ModelPart> parts = new HashMap<>();
        parts.put("head", head);
        parts.put("hat", head.getChild("hat"));
        parts.put("body", root.getChild("body"));
        parts.put("right_arm", detachedPart());
        parts.put("left_arm", detachedPart());
        parts.put("right_leg", root.getChild("right_leg"));
        parts.put("left_leg", root.getChild("left_leg"));

        return new HumanoidModel<LivingEntity>(new ModelPart(Collections.emptyList(), parts));
    }

    private static ModelPart detachedPart()
    {
        return new ModelPart(Collections.emptyList(), Collections.emptyMap());
    }

    @Override
    public boolean createParts(VillagerModel<E> original, float scaleFactor)
    {
        body = buildBody(scaleFactor);
        skirt = lastSkirt;
        head = buildHead(scaleFactor, false);
        body.addChild(head);

        rightArm = buildArm(scaleFactor, false);
        rightForeArm = buildForeArm(scaleFactor, false, false);
        rightWristTrim = lastWristTrim;
        rightArm.addChild(rightForeArm);
        body.addChild(rightArm);

        leftArm = buildArm(scaleFactor, true);
        leftForeArm = buildForeArm(scaleFactor, true, false);
        leftWristTrim = lastWristTrim;
        leftArm.addChild(leftForeArm);
        body.addChild(leftArm);

        rightLeg = buildLeg(scaleFactor, false);
        rightForeLeg = buildForeLeg(scaleFactor, false);
        rightLeg.addChild(rightForeLeg);

        leftLeg = buildLeg(scaleFactor, true);
        leftForeLeg = buildForeLeg(scaleFactor, true);
        leftLeg.addChild(leftForeLeg);

        createOuterParts(scaleFactor);

        reconcileWithVanillaModel(humanoidViewOf(original));

        return true;
    }

    @Override
    protected void createOuterParts(float scaleFactor)
    {
        final float outerOffset = 0.01F;

        outerBody = buildBody(scaleFactor + outerOffset);
        outerSkirt = lastSkirt;
        outerHead = buildHead(scaleFactor + outerOffset, true);
        outerBody.addChild(outerHead);

        outerRightArm = buildArm(scaleFactor + outerOffset, false);
        outerRightForeArm = buildForeArm(scaleFactor + outerOffset, false, true);
        outerRightHand = lastOuterHand;
        outerRightArm.addChild(outerRightForeArm);
        outerBody.addChild(outerRightArm);

        outerLeftArm = buildArm(scaleFactor + outerOffset, true);
        outerLeftForeArm = buildForeArm(scaleFactor + outerOffset, true, true);
        outerLeftHand = lastOuterHand;
        outerLeftArm.addChild(outerLeftForeArm);
        outerBody.addChild(outerLeftArm);

        outerRightLeg = buildLeg(scaleFactor + outerOffset, false);
        outerRightForeLeg = buildForeLeg(scaleFactor + outerOffset, false);
        outerRightLeg.addChild(outerRightForeLeg);

        outerLeftLeg = buildLeg(scaleFactor + outerOffset, true);
        outerLeftForeLeg = buildForeLeg(scaleFactor + outerOffset, true);
        outerLeftLeg.addChild(outerLeftForeLeg);
    }

    protected BendsModelPart buildBody(float scaleFactor)
    {
        final BendsModelPart part = new BendsModelPart(16, 20)
                .setTextureSize(64, textureHeight())
                .setPosition(0.0F, 12.0F, 0.0F);

        part.addCube(-4.0F, -12.0F, -3.0F, 8, 12, 6, scaleFactor);

        part.setTextureOffset(ROBE_TEX_U, ROBE_TEX_V);
        part.addCube(-4.0F, -12.0F, -3.0F, ROBE_WIDTH, ROBE_TORSO_HEIGHT, ROBE_DEPTH,
                     scaleFactor + ROBE_INFLATE);

        part.addChild(buildSkirt(scaleFactor));

        return part;
    }

    protected BendsModelPart buildSkirt(float scaleFactor)
    {
        final int skirtTexV = ROBE_TEX_V + ROBE_TORSO_HEIGHT - SKIRT_OVERLAP;

        final BendsModelPart part = new BendsModelPart(ROBE_TEX_U, skirtTexV)
                .setTextureSize(64, textureHeight())
                .setPosition(0.0F, 0.0F, 0.0F);

        part.developBox(-4.0F, -SKIRT_OVERLAP, -3.0F, ROBE_WIDTH, SKIRT_HEIGHT, ROBE_DEPTH,
                        scaleFactor + ROBE_INFLATE - SKIRT_TUCK)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -(ROBE_TORSO_HEIGHT - SKIRT_OVERLAP))
                .create();

        lastSkirt = part;

        return part;
    }

    private static float pitchOf(BendsModelPart part)
    {
        if (part == null)
        {
            return 0.0F;
        }

        final Quaternion rotation = part.rotation.getSmooth();

        return (float) Math.toDegrees(2.0D * Math.atan2(rotation.x, rotation.w));
    }


    protected BendsModelPart buildHead(float scaleFactor, boolean outer)
    {
        final BendsModelPart part = new BendsModelPart(0, 0)
                .setTextureSize(64, textureHeight())
                .setPosition(0.0F, -12.0F, 0.0F);

        part.addCube(-4.0F, -10.0F, -4.0F, 8, 10, 8, scaleFactor);
        part.setTextureOffset(24, 0);
        part.addCube(-1.0F, -3.0F, -6.0F, 2, 4, 2, scaleFactor);
        part.setTextureOffset(32, 0);
        part.addCube(-4.0F, -10.0F, -4.0F, 8, 10, 8, scaleFactor + 0.51F);

        if (outer && HAT_RIM_ENABLED)
        {
            final BendsModelPart hatRim = new BendsModelPart(30, 47)
                    .setTextureSize(64, textureHeight())
                    .setPosition(0.0F, 0.0F, 0.0F);
            hatRim.addCube(-8.0F, -8.0F, -6.0F, 16, 16, 1, scaleFactor);
            hatRim.rotation.orientInstantX(-90.0F);
            part.addChild(hatRim);
        }

        return part;
    }

    protected BendsModelPart buildArm(float scaleFactor, boolean left)
    {
        final BendsModelPart part = new BendsModelPart(44, 22)
                .setTextureSize(64, textureHeight())
                .setPosition(left ? ARM_X : -ARM_X, ARM_Y, ARM_Z)
                .setMirror(left);

        part.developBox(armBoxX(left), -2.0F, -2.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .hideFace(BoxSide.BOTTOM)
                .create();

        return part;
    }

    protected BendsModelPart buildForeArm(float scaleFactor, boolean left, boolean outer)
    {
        final BendsModelPart part = new BendsModelPart(44, 26)
                .setTextureSize(64, textureHeight())
                .setPosition(0.0F, 4.0F, 2.0F)
                .setMirror(left);

        part.developBox(armBoxX(left), 0.0F, -4.0F, 4, FOREARM_LENGTH, 4, scaleFactor)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -4F)
                .create();

        final BendsModelPart hand = new BendsModelPart(handTexU(), handTexV())
                .setTextureSize(64, textureHeight())
                .setPosition(0.0F, 0.0F, 0.0F)
                .setMirror(left);
        hand.developBox(armBoxX(left), FOREARM_LENGTH, -4.0F, 4, HAND_EXPOSED, 4, scaleFactor)
                .offsetTextureQuad(BoxSide.LEFT, -4, 0)
                .offsetTextureQuad(BoxSide.RIGHT, 4, 0)
                .offsetTextureQuad(BoxSide.BACK, -8, 0)
                .offsetTextureQuad(BoxSide.BOTTOM, -4, 0)
                .create();
        part.addChild(hand);

        if (outer)
        {
            lastOuterHand = hand;
        }
        else
        {
            final BendsModelPart trim = new BendsModelPart(0, 0)
                    .setTextureSize(64, textureHeight())
                    .setPosition(0.0F, 0.0F, 0.0F);

            trim.setTextureOffset(TRIM_RED_U, TRIM_RED_V);
            trim.developBox(armBoxX(left), FOREARM_LENGTH - 1, -4.0F, 4, 1, 4, scaleFactor + TRIM_INFLATE)
                    .offsetTextureQuad(BoxSide.LEFT, -4, 0)
                    .offsetTextureQuad(BoxSide.RIGHT, 4, 0)
                    .offsetTextureQuad(BoxSide.BACK, -8, 0)
                    .create();
            trim.setTextureOffset(TRIM_YELLOW_U, TRIM_YELLOW_V);
            trim.developBox(armBoxX(left), FOREARM_LENGTH - 2, -4.0F, 4, 1, 4, scaleFactor + TRIM_INFLATE)
                    .offsetTextureQuad(BoxSide.LEFT, -4, 0)
                    .offsetTextureQuad(BoxSide.RIGHT, 4, 0)
                    .offsetTextureQuad(BoxSide.BACK, -8, 0)
                    .create();

            trim.setVisible(false);
            part.addChild(trim);
            lastWristTrim = trim;
        }

        return part;
    }

    protected BendsModelPart buildLeg(float scaleFactor, boolean left)
    {
        final BendsModelPart part = new BendsModelPart(0, 22)
                .setTextureSize(64, textureHeight())
                .setPosition(0.0F, 12.0F, 0.0F)
                .setMirror(left);

        part.developBox(left ? 0.1F : -4.1F, 0.0F, -2.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .hideFace(BoxSide.BOTTOM)
                .create();

        return part;
    }

    protected BendsModelPart buildForeLeg(float scaleFactor, boolean left)
    {
        final BendsModelPart part = new BendsModelPart(0, 28)
                .setTextureSize(64, textureHeight())
                .setPosition(0.0F, 6.0F, -2.0F)
                .setMirror(left);

        part.developBox(left ? 0.1F : -4.1F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .hideFace(BoxSide.TOP)
                .create();

        return part;
    }

    @Override
    protected void reconcileWithVanillaModel(HumanoidModel<?> original)
    {
        super.reconcileWithVanillaModel(original);
        attachedParts.clear();
    }

    @Override
    public void syncUpWithData(VillagerData<E> data)
    {
        super.syncUpWithData(data);

        final float fold = Mth.clamp((pitchOf(leftLeg) + pitchOf(rightLeg)) * 0.5F * SKIRT_FOLLOW,
                -SKIRT_MAX_FOLD, SKIRT_MAX_LIFT);

        if (skirt != null) skirt.rotation.orientInstantX(fold);
        if (outerSkirt != null) outerSkirt.rotation.orientInstantX(fold);

        final net.minecraft.world.entity.LivingEntity current = data.getEntity();

        final boolean trader = current instanceof net.minecraft.world.entity.npc.WanderingTrader;
        if (leftWristTrim != null) leftWristTrim.setVisible(trader);
        if (rightWristTrim != null) rightWristTrim.setVisible(trader);
    }

    @Override
    protected void syncConcealmentFromVanillaModel()
    {
        super.syncConcealmentFromVanillaModel();

        final HumanoidModel<?> model =
                goblinbob.mobends.core.client.MoBendsRenderContext.getCurrentVanillaModel();

        if (model != null && model.hat != null)
        {
            model.hat.visible = false;
        }
    }

    @Override
    public void renderOuter(com.mojang.blaze3d.vertex.PoseStack poseStack,
                            com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer,
                            int packedLight, int packedOverlay, int color)
    {
        final boolean showHand = !goblinbob.mobends.standard.client.VillagerOverlayContext.isTypePass();

        if (outerLeftHand != null) outerLeftHand.setVisible(showHand);
        if (outerRightHand != null) outerRightHand.setVisible(showHand);

        super.renderOuter(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof VillagerModel);
    }
}
