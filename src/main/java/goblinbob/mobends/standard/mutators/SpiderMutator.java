package goblinbob.mobends.standard.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.data.SpiderData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.monster.Spider;

public class SpiderMutator extends Mutator<SpiderData, Spider, SpiderModel<Spider>>
{

    public BendsModelPart spiderHead;
    public BendsModelPart spiderNeck;
    public BendsModelPart spiderBody;
    public BendsModelPart[] spiderUpperLimbs;
    public BendsModelPart[] spiderLowerLimbs;

    private SpiderData currentData;

    public SpiderMutator(IEntityDataFactory<Spider> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public void storeVanillaModel(SpiderModel<Spider> model)
    {
    }

    @Override
    public void applyVanillaModel(SpiderModel<Spider> model)
    {
    }

    @Override
    public void swapLayer(LivingEntityRenderer<Spider, SpiderModel<Spider>> renderer, int index, boolean isModelVanilla)
    {
    }

    @Override
    public void deswapLayer(LivingEntityRenderer<Spider, SpiderModel<Spider>> renderer, int index)
    {
    }

    @Override
    public boolean createParts(SpiderModel<Spider> original, float scaleFactor)
    {
        float legLength = 12F;
        float foreLegLength = 12F;

        spiderHead = new BendsModelPart(32, 4)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 15.0F, -3.0F);
        spiderHead.addCube(-4.0F, -4.0F, -8.0F, 8, 8, 8, scaleFactor);

        spiderNeck = new BendsModelPart(0, 0)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 15.0F, 0.0F);
        spiderNeck.addCube(-3.0F, -3.0F, -3.0F, 6, 6, 6, scaleFactor);

        spiderBody = new BendsModelPart(0, 12)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 15.0F, 9.0F);
        spiderBody.addCube(-5.0F, -4.0F, -6.0F, 10, 8, 12, scaleFactor);

        spiderUpperLimbs = new BendsModelPart[8];
        spiderLowerLimbs = new BendsModelPart[8];

        for (int i = 0; i < 8; ++i)
        {
            boolean odd = i % 2 == 1;
            int z = 2 - (i / 2);

            spiderUpperLimbs[i] = new BendsModelPart(odd ? 18 : 26, 0)
                    .setTextureSize(64, 32)
                    .setPosition(odd ? 4F : -4F, 15F, z);
            spiderUpperLimbs[i].developBox(odd ? -1F : (-legLength + 1F), -1.0F, -1.0F, 8, 2, 2, 0.0F)
                    .setWidth(legLength)
                    .create();

            spiderLowerLimbs[i] = new BendsModelPart(odd ? 26 : 18, 0)
                    .setTextureSize(64, 32)
                    .setPosition(odd ? foreLegLength : -foreLegLength, 0F, 0F);
            spiderLowerLimbs[i].developBox(odd ? 0F : -foreLegLength, 0F, -1F, 8, 2, 2, 0F)
                    .offset(0F, 0F, 0.005F)
                    .resize(foreLegLength, 1.99F, 1.99F)
                    .create();
            spiderUpperLimbs[i].addChild(spiderLowerLimbs[i]);
        }

        return true;
    }

    @Override
    public void syncUpWithData(SpiderData data)
    {
        this.currentData = data;

        if (spiderHead != null) spiderHead.syncUp(data.spiderHead);
        if (spiderNeck != null) spiderNeck.syncUp(data.spiderNeck);
        if (spiderBody != null) spiderBody.syncUp(data.spiderBody);

        for (int i = 0; i < 8; ++i)
        {
            if (spiderUpperLimbs[i] != null)
                spiderUpperLimbs[i].syncUp(data.limbs[i].upperPart);
            if (spiderLowerLimbs[i] != null)
                spiderLowerLimbs[i].syncUp(data.limbs[i].lowerPart);
        }
    }

    @Override
    public boolean isModelVanilla(SpiderModel<Spider> model)
    {
        return this.spiderHead == null;
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof SpiderModel);
    }

    @Override
    public boolean shouldRenderCustom()
    {
        return this.spiderHead != null;
    }

    @Override
    public void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                              int packedLight, int packedOverlay, int color)
    {
        renderParts(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void renderParts(PoseStack poseStack, VertexConsumer vertexConsumer,
                             int packedLight, int packedOverlay, int color)
    {
        if (spiderHead != null)
        {
            spiderHead.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
        if (spiderNeck != null)
        {
            spiderNeck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
        if (spiderBody != null)
        {
            spiderBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }

        for (int i = 0; i < 8; ++i)
        {
            if (spiderUpperLimbs[i] != null)
            {
                spiderUpperLimbs[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            }
        }
    }

}
