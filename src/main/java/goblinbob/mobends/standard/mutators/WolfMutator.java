package goblinbob.mobends.standard.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.mutators.Mutator;
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
    public BendsModelPart leftEar;
    public BendsModelPart rightEar;
    public BendsModelPart foreLeg1;
    public BendsModelPart foreLeg2;
    public BendsModelPart foreLeg3;
    public BendsModelPart foreLeg4;

    // Store the current data for use in renderMutated
    private WolfData currentData;

    public WolfMutator(IEntityDataFactory<Wolf> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public void storeVanillaModel(WolfModel<Wolf> model)
    {
        // In 1.20.1, we use composition pattern - no need to store vanilla parts
    }

    @Override
    public void applyVanillaModel(WolfModel<Wolf> model)
    {
        // In 1.20.1, demutation handled differently
    }

    @Override
    public void swapLayer(LivingEntityRenderer<Wolf, WolfModel<Wolf>> renderer, int index, boolean isModelVanilla)
    {
        // No custom layers for wolf yet
    }

    @Override
    public void deswapLayer(LivingEntityRenderer<Wolf, WolfModel<Wolf>> renderer, int index)
    {
        // No custom layers for wolf yet
    }

    @Override
    public boolean createParts(WolfModel<Wolf> original, float scaleFactor)
    {
        // Body - main part that other parts are relative to
        // Wolf body position: (0, 14, 2), box: (-3, -2, -3, 6, 9, 6)
        wolfBody = new BendsModelPart(18, 14)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 14.0F, 2.0F);
        wolfBody.addCube(-3.0F, -2.0F, -3.0F, 6, 9, 6, scaleFactor);

        // Head - attached to body, position relative to body
        // Wolf head position: (-1, 13.5, -7)
        wolfHeadMain = new BendsModelPart(0, 0)
                .setTextureSize(64, 32)
                .setPosition(-1.0F, 13.5F, -7.0F);
        wolfHeadMain.addCube(-2.0F, -3.0F, -2.0F, 6, 6, 4, scaleFactor);

        // Mane (upper body/neck)
        // Wolf mane position: (-1, 14, -3)
        wolfMane = new BendsModelPart(21, 0)
                .setTextureSize(64, 32)
                .setPosition(-1.0F, 14.0F, -3.0F);
        wolfMane.addCube(-3.0F, -3.0F, -3.0F, 8, 6, 7, scaleFactor);

        // Back legs (leg1 and leg2)
        // Wolf leg1 position: (-2.5, 16, 7)
        wolfLeg1 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(-2.5F, 16.0F, 7.0F);
        wolfLeg1.addCube(0.0F, 0.0F, -1.0F, 2, 8, 2, scaleFactor);

        // Wolf leg2 position: (0.5, 16, 7)
        wolfLeg2 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(0.5F, 16.0F, 7.0F);
        wolfLeg2.addCube(0.0F, 0.0F, -1.0F, 2, 8, 2, scaleFactor);

        // Front legs (leg3 and leg4)
        // Wolf leg3 position: (-2.5, 16, -4)
        wolfLeg3 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(-2.5F, 16.0F, -4.0F);
        wolfLeg3.addCube(0.0F, 0.0F, -1.0F, 2, 8, 2, scaleFactor);

        // Wolf leg4 position: (0.5, 16, -4)
        wolfLeg4 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(0.5F, 16.0F, -4.0F);
        wolfLeg4.addCube(0.0F, 0.0F, -1.0F, 2, 8, 2, scaleFactor);

        // Tail
        // Wolf tail position: (-1, 12, 8)
        wolfTail = new BendsModelPart(9, 18)
                .setTextureSize(64, 32)
                .setPosition(-1.0F, 12.0F, 8.0F);
        wolfTail.addCube(0.0F, 0.0F, -1.0F, 2, 8, 2, scaleFactor);

        // Nose
        nose = new BendsModelPart(0, 10)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 0.0F, 0.0F);
        nose.addCube(-1.5F, -3.0F, -5.0F, 3, 3, 4, scaleFactor);
        wolfHeadMain.addChild(nose);

        // Mouth (lower jaw)
        mouth = new BendsModelPart(0, 10)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 0.0F, 0.0F);
        mouth.addCube(-1.5F, 0.0F, -5.0F, 3, 1, 4, scaleFactor);
        wolfHeadMain.addChild(mouth);

        // Left ear
        leftEar = new BendsModelPart(16, 14)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 0.0F, 0.0F);
        leftEar.addCube(-3.0F, -5.0F, 0.0F, 2, 2, 1, scaleFactor);
        wolfHeadMain.addChild(leftEar);

        // Right ear
        rightEar = new BendsModelPart(16, 14)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 0.0F, 0.0F);
        rightEar.addCube(1.0F, -5.0F, 0.0F, 2, 2, 1, scaleFactor);
        wolfHeadMain.addChild(rightEar);

        // Fore legs (lower leg segments)
        foreLeg1 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 4.0F, 0.0F);
        foreLeg1.addCube(0.0F, 0.0F, -1.0F, 2, 4, 2, scaleFactor);
        wolfLeg1.addChild(foreLeg1);

        foreLeg2 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 4.0F, 0.0F);
        foreLeg2.addCube(0.0F, 0.0F, -1.0F, 2, 4, 2, scaleFactor);
        wolfLeg2.addChild(foreLeg2);

        foreLeg3 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 4.0F, 0.0F);
        foreLeg3.addCube(0.0F, 0.0F, -1.0F, 2, 4, 2, scaleFactor);
        wolfLeg3.addChild(foreLeg3);

        foreLeg4 = new BendsModelPart(0, 18)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 4.0F, 0.0F);
        foreLeg4.addCube(0.0F, 0.0F, -1.0F, 2, 4, 2, scaleFactor);
        wolfLeg4.addChild(foreLeg4);

        return true;
    }

    @Override
    public void syncUpWithData(WolfData data)
    {
        // Store current data for use in renderMutated
        this.currentData = data;

        if (wolfHeadMain != null) wolfHeadMain.syncUp(data.head);
        if (wolfBody != null) wolfBody.syncUp(data.body);
        if (wolfLeg1 != null) wolfLeg1.syncUp(data.leg1);
        if (wolfLeg2 != null) wolfLeg2.syncUp(data.leg2);
        if (wolfLeg3 != null) wolfLeg3.syncUp(data.leg3);
        if (wolfLeg4 != null) wolfLeg4.syncUp(data.leg4);
        if (wolfTail != null) wolfTail.syncUp(data.tail);
        if (wolfMane != null) wolfMane.syncUp(data.mane);

        if (nose != null) nose.syncUp(data.nose);
        if (mouth != null) mouth.syncUp(data.mouth);
        if (leftEar != null) leftEar.syncUp(data.leftEar);
        if (rightEar != null) rightEar.syncUp(data.rightEar);
        if (foreLeg1 != null) foreLeg1.syncUp(data.foreLeg1);
        if (foreLeg2 != null) foreLeg2.syncUp(data.foreLeg2);
        if (foreLeg3 != null) foreLeg3.syncUp(data.foreLeg3);
        if (foreLeg4 != null) foreLeg4.syncUp(data.foreLeg4);
    }

    @Override
    public boolean isModelVanilla(WolfModel<Wolf> model)
    {
        // Check if we've already created custom parts
        return this.wolfHeadMain == null;
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof WolfModel);
    }

    @Override
    public boolean shouldRenderCustom()
    {
        return this.wolfHeadMain != null;
    }

    @Override
    public void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                              int packedLight, int packedOverlay,
                              float red, float green, float blue, float alpha)
    {
        if (currentData == null)
        {
            // Fallback: just render without transforms
            renderParts(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            return;
        }

        poseStack.pushPose();

        // Render all wolf parts
        renderParts(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);

        poseStack.popPose();
    }

    private void renderParts(PoseStack poseStack, VertexConsumer vertexConsumer,
                             int packedLight, int packedOverlay,
                             float red, float green, float blue, float alpha)
    {
        // Render body first as it's the main part
        if (wolfBody != null)
        {
            wolfBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        // Render head (with nose, mouth, ears as children)
        if (wolfHeadMain != null)
        {
            wolfHeadMain.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        // Render mane
        if (wolfMane != null)
        {
            wolfMane.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        // Render legs (with forelegs as children)
        if (wolfLeg1 != null)
        {
            wolfLeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
        if (wolfLeg2 != null)
        {
            wolfLeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
        if (wolfLeg3 != null)
        {
            wolfLeg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
        if (wolfLeg4 != null)
        {
            wolfLeg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        // Render tail
        if (wolfTail != null)
        {
            wolfTail.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

}
