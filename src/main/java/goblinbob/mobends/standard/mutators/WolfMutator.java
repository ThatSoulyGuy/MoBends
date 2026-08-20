package goblinbob.mobends.standard.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.client.model.FaceRotation;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerWolfMisc;
import goblinbob.mobends.standard.data.WolfData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.animal.Wolf;

public class WolfMutator extends Mutator<WolfData, Wolf, WolfModel<Wolf>>
{

    public BendsModelPart wolfHeadMain;
    public BendsModelPart wolfBody;
    public BendsModelPart wolfLeg1;
    public BendsModelPart wolfLeg2;
    public BendsModelPart wolfLeg3;
    public BendsModelPart wolfLeg4;
    public BendsModelPart wolfTail;
    public BendsModelPart wolfMane;

    public BendsModelPart nose;
    public BendsModelPart mouth;
    public BendsModelPart tongue;
    public BendsModelPart leftEar;
    public BendsModelPart rightEar;
    public BendsModelPart foreLeg1;
    public BendsModelPart foreLeg2;
    public BendsModelPart foreLeg3;
    public BendsModelPart foreLeg4;

    protected float babyHeadScale = 1.0F;

    public WolfMutator(IEntityDataFactory<Wolf> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public void storeVanillaModel(WolfModel<Wolf> model)
    {
    }

    @Override
    public void applyVanillaModel(WolfModel<Wolf> model)
    {
    }

    @Override
    public void swapLayer(LivingEntityRenderer<Wolf, WolfModel<Wolf>> renderer, int index, boolean isModelVanilla)
    {
    }

    @Override
    public void deswapLayer(LivingEntityRenderer<Wolf, WolfModel<Wolf>> renderer, int index)
    {
    }

    @Override
    public boolean createParts(WolfModel<Wolf> original, float scaleFactor)
    {
        wolfBody = new BendsModelPart(18, 14)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 13.0F, 8.0F);
        wolfBody.developBox(-3.0F, -3.0F, -8.0F, 6, 6, 9, scaleFactor)
                .offsetTextureQuad(BoxSide.TOP, 9.0F, 6.0F)
                .rotateTextureQuad(BoxSide.TOP, FaceRotation.HALF_TURN)
                .offsetTextureQuad(BoxSide.BACK, -12F, -9F)
                .rotateTextureQuad(BoxSide.BOTTOM, FaceRotation.HALF_TURN)
                .offsetTextureQuad(BoxSide.BOTTOM, -8F, 6F)
                .rotateTextureQuad(BoxSide.LEFT, FaceRotation.CLOCKWISE)
                .offsetTextureQuad(BoxSide.LEFT, -3F, -3F)
                .rotateTextureQuad(BoxSide.RIGHT, FaceRotation.COUNTER_CLOCKWISE)
                .offsetTextureQuad(BoxSide.RIGHT, 0F, -3F)
                .create();

        wolfHeadMain = new BendsModelPart(0, 0)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 0F, -7.0F);
        wolfHeadMain.addCube(-3.0F, -3.0F, -4.0F, 6, 6, 4, scaleFactor);
        wolfBody.addChild(wolfHeadMain);

        wolfMane = new BendsModelPart(21, 0)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 0.0F, -7.0F);
        wolfMane.developBox(-4.0F, -3.5F, -2.0F, 8, 7, 6, scaleFactor)
                .offsetTextureQuad(BoxSide.TOP, 1.0F, 7.0F)
                .rotateTextureQuad(BoxSide.TOP, FaceRotation.HALF_TURN)
                .offsetTextureQuad(BoxSide.BACK, -5F, -6F)
                .offsetTextureQuad(BoxSide.BOTTOM, 8F, 7F)
                .rotateTextureQuad(BoxSide.BOTTOM, FaceRotation.HALF_TURN)
                .rotateTextureQuad(BoxSide.LEFT, FaceRotation.CLOCKWISE)
                .offsetTextureQuad(BoxSide.LEFT, -14F, 1F)
                .rotateTextureQuad(BoxSide.RIGHT, FaceRotation.COUNTER_CLOCKWISE)
                .offsetTextureQuad(BoxSide.RIGHT, 15F, 1F)
                .offsetTextureQuad(BoxSide.FRONT, 1F, -6F)
                .create();
        wolfBody.addChild(wolfMane);

        wolfLeg1 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(-2.5F, 16.0F, 7.0F);
        wolfLeg1.addCube(-1.0F, 0.0F, -1.0F, 2, 4, 2, scaleFactor);
        wolfBody.addChild(wolfLeg1);

        wolfLeg2 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(0.5F, 16.0F, 7.0F);
        wolfLeg2.addCube(-1.0F, 0.0F, -1.0F, 2, 4, 2, scaleFactor);
        wolfBody.addChild(wolfLeg2);

        wolfLeg3 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(-2.5F, 0.0F, -4.0F);
        wolfLeg3.addCube(-1.0F, 0.0F, -1.0F, 2, 4, 2, scaleFactor);
        wolfBody.addChild(wolfLeg3);

        wolfLeg4 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(0.5F, 0.0F, -4.0F);
        wolfLeg4.addCube(-1.0F, 0.0F, -1.0F, 2, 4, 2, scaleFactor);
        wolfBody.addChild(wolfLeg4);

        wolfTail = new BendsModelPart(9, 18)
                .setTextureSize(64, 32)
                .setPosition(-1.0F, 0.0F, 8.0F);
        wolfTail.addCube(-1.0F, 0.0F, -2.0F, 2, 8, 2, scaleFactor);
        wolfBody.addChild(wolfTail);

        nose = new BendsModelPart(0, 10)
                .setTextureSize(64, 32)
                .setPosition(0, 1F, -4F);
        nose.developBox(-1.5F, -1.0F, -4.0F, 3, 2, 4, 0.0F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        wolfHeadMain.addChild(nose);

        mouth = new BendsModelPart(0, 12)
                .setTextureSize(64, 32)
                .setPosition(0, 2F, -4F);
        mouth.developBox(-1.5F, 0.0F, -4.0F, 3, 1, 4, 0.0F)
                .hideFace(BoxSide.TOP)
                .create();
        wolfHeadMain.addChild(mouth);

        tongue = new BendsModelPart(0, 0)
                .setTextureSize(64, 32)
                .setPosition(0, 2F, -3F);
        wolfHeadMain.addChild(tongue);

        leftEar = new BendsModelPart(16, 14)
                .setTextureSize(64, 32)
                .setPosition(0, 1F, -4F);
        leftEar.addCube(-1.0F, -2.0F, -1.0F, 2, 2, 1, 0.0F);
        wolfHeadMain.addChild(leftEar);

        rightEar = new BendsModelPart(16, 14)
                .setTextureSize(64, 32)
                .setPosition(0, 1F, -4F);
        rightEar.addCube(-1.0F, -2.0F, -1.0F, 2, 2, 1, 0.0F);
        wolfHeadMain.addChild(rightEar);

        foreLeg1 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(0.0F, -4.0F, -1.0F);
        foreLeg1.addCube(-1.0F, 0, 0, 2, 4, 2, scaleFactor);
        wolfLeg1.addChild(foreLeg1);

        foreLeg2 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(0.0F, -4.0F, -1.0F);
        foreLeg2.addCube(-1.0F, 0, 0, 2, 4, 2, scaleFactor);
        wolfLeg2.addChild(foreLeg2);

        foreLeg3 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(0.0F, -4.0F, 1.0F);
        foreLeg3.addCube(-1.0F, 0, -2, 2, 4, 2, scaleFactor);
        wolfLeg3.addChild(foreLeg3);

        foreLeg4 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(0.0F, -4.0F, 1.0F);
        foreLeg4.addCube(-1.0F, 0, -2, 2, 4, 2, scaleFactor);
        wolfLeg4.addChild(foreLeg4);

        return true;
    }

    @Override
    public void syncUpWithData(WolfData data)
    {
        wolfHeadMain.syncUp(data.head);
        wolfBody.syncUp(data.body);
        wolfLeg1.syncUp(data.leg1);
        wolfLeg2.syncUp(data.leg2);
        wolfLeg3.syncUp(data.leg3);
        wolfLeg4.syncUp(data.leg4);
        wolfTail.syncUp(data.tail);
        wolfMane.syncUp(data.mane);

        nose.syncUp(data.nose);
        mouth.syncUp(data.mouth);
        tongue.syncUp(data.tongue);
        leftEar.syncUp(data.leftEar);
        rightEar.syncUp(data.rightEar);
        foreLeg1.syncUp(data.foreLeg1);
        foreLeg2.syncUp(data.foreLeg2);
        foreLeg3.syncUp(data.foreLeg3);
        foreLeg4.syncUp(data.foreLeg4);
    }

    @Override
    public boolean isModelVanilla(WolfModel<Wolf> model)
    {
        return this.wolfBody == null;
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof WolfModel);
    }

    @Override
    public boolean shouldRenderCustom()
    {
        return this.wolfBody != null;
    }

    private static final float HEAD_HALF_HEIGHT = 3.0F;

    public void setBabyHeadScale(float scale)
    {
        this.babyHeadScale = scale;
    }

    private void applyBabyHeadScale()
    {
        if (wolfHeadMain != null)
        {
            wolfHeadMain.scale.set(babyHeadScale, babyHeadScale, babyHeadScale);
            wolfHeadMain.globalOffset.set(0.0F, -(babyHeadScale - 1.0F) * HEAD_HALF_HEIGHT, 0.0F);
        }
    }

    @Override
    public void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                              int packedLight, int packedOverlay, int color)
    {
        applyBabyHeadScale();

        if (wolfBody != null)
        {
            wolfBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);

            LayerWolfMisc.render(poseStack, this, MoBendsRenderContext.getCurrentBufferSource(),
                    packedLight, packedOverlay);
        }
    }

}
