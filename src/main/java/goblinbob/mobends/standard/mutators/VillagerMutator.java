package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.VillagerData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
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
    private BendsModelPart outerLeftHand, outerRightHand;
    private BendsModelPart leftWristTrim, rightWristTrim;

    public VillagerMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
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
        part.setTextureOffset(0, 38);
        part.addCube(-4.0F, -12.0F, -3.0F, 8, 20, 6, scaleFactor + ROBE_INFLATE);

        return part;
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
