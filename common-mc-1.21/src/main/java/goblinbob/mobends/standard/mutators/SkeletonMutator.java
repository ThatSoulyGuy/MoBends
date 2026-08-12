package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.SkeletonData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.monster.AbstractSkeleton;

public class SkeletonMutator<E extends AbstractSkeleton> extends BipedMutator<SkeletonData<E>, E, SkeletonModel<E>>
{

    private static final String BOGGED_MUSHROOMS_PART = "mushrooms";

    protected boolean boneLimbs = false;

    private final float clothingDeformation;

    public SkeletonMutator(IEntityDataFactory<E> dataCreationFunction)
    {
        this(dataCreationFunction, 0.0F);
    }

    public SkeletonMutator(IEntityDataFactory<E> dataCreationFunction, float clothingDeformation)
    {
        super(dataCreationFunction);
        this.clothingDeformation = clothingDeformation;
    }

    @Override
    public void fetchFields(LivingEntityRenderer<E, SkeletonModel<E>> renderer)
    {
        super.fetchFields(renderer);

        this.boneLimbs = true;
    }

    @Override
    public void storeVanillaModel(SkeletonModel<E> model)
    {
        super.storeVanillaModel(model);

        this.extraHeadPart = model.head.hasChild(BOGGED_MUSHROOMS_PART)
                ? model.head.getChild(BOGGED_MUSHROOMS_PART)
                : null;
    }

    @Override
    public boolean createParts(SkeletonModel<E> original, float scaleFactor)
    {
        body = new BendsModelPart(16, 16)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 12.0F, 0.0F);
        body.addCube(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor);

        head = new BendsModelPart(0, 0)
                .setTextureSize(64, 32)
                .setPosition(0.0F, -12.0F, 0.0F);
        head.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor);
        body.addChild(head);

        headwear = new BendsModelPart(32, 0)
                .setTextureSize(64, 32);
        headwear.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor + 0.5F);
        head.addChild(headwear);

        rightArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 32)
                .setPosition(-5.0F, -10.0F, 0.0F);
        rightArm.developBox(-1.0F, -2.0F, -1.0F, 2, 6, 2, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        body.addChild(rightArm);

        leftArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 32)
                .setPosition(5.0F, -10.0F, 0.0F)
                .setMirror(true);
        leftArm.developBox(-1.0F, -2.0F, -1.0F, 2, 6, 2, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        body.addChild(leftArm);

        rightForeArm = new BendsModelPart(40, 16 + 6)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 4.0F, 1.0F);
        rightForeArm.developBox(-1.0F, 0.0F, -2.0F, 2, 6, 2, scaleFactor)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        rightArm.addChild(rightForeArm);

        leftForeArm = new BendsModelPart(40, 16 + 6)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 4.0F, 1.0F)
                .setMirror(true);
        leftForeArm.developBox(-1.0F, 0.0F, -2.0F, 2, 6, 2, scaleFactor)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        leftArm.addChild(leftForeArm);

        rightLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 32)
                .setPosition(-2.0F, 12.0F, 0.0F);
        rightLeg.addCube(-1.0F, 0.0F, -1.0F, 2, 6, 2, scaleFactor);

        leftLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 32)
                .setPosition(2.0F, 12.0F, 0.0F)
                .setMirror(true);
        leftLeg.addCube(-1.0F, 0.0F, -1.0F, 2, 6, 2, scaleFactor);

        rightForeLeg = new BendsModelPart(0, 16 + 6)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 6.0F, -2.0F);
        rightForeLeg.developBox(-1.0F, 0.0F, 0.0F, 2, 6, 2, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        rightLeg.addChild(rightForeLeg);

        leftForeLeg = new BendsModelPart(0, 16 + 6)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 6.0F, -2.0F)
                .setMirror(true);
        leftForeLeg.developBox(-1.0F, 0.0F, 0.0F, 2, 6, 2, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        leftLeg.addChild(leftForeLeg);

        createOuterParts(scaleFactor);

        return true;
    }

    @Override
    protected void createOuterParts(float scaleFactor)
    {
        if (clothingDeformation <= 0.0F)
        {
            return;
        }

        final float outerScale = scaleFactor + clothingDeformation;
        final float limbWearHeight = (6F + 2 * outerScale) - 0.25F;
        final int armWidth = 4;
        final float armY = -10F;

        outerBody = new BendsModelPart(16, 16)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 12.0F, 0.0F);
        outerBody.addCube(-4.0F, -12.0F, -2.0F, 8, 12, 4, outerScale);

        outerHead = new BendsModelPart(0, 0)
                .setTextureSize(64, 32)
                .setPosition(0.0F, -12.0F, 0.0F);
        outerHead.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, outerScale);
        outerBody.addChild(outerHead);

        outerLeftArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 32)
                .setPosition(5.0F, armY, 0.0F)
                .setMirror(true);
        outerLeftArm.developBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, outerScale)
                .setHeight(limbWearHeight)
                .inflate(0.0025F, 0F, 0.0025F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        outerBody.addChild(outerLeftArm);

        outerRightArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 32)
                .setPosition(-5.0F, armY, 0.0F);
        outerRightArm.developBox(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, outerScale)
                .setHeight(limbWearHeight)
                .inflate(0.0025F, 0F, 0.0025F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        outerBody.addChild(outerRightArm);

        outerLeftForeArm = new BendsModelPart(40, 22)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 4.0F, 2.0F)
                .setMirror(true);
        outerLeftForeArm.developBox(-1.0F, 0.0F, -3.0F, armWidth, 6, 4, outerScale)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        outerLeftArm.addChild(outerLeftForeArm);

        outerRightForeArm = new BendsModelPart(40, 22)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 4.0F, 2.0F);
        outerRightForeArm.developBox(-armWidth + 1, 0.0F, -3.0F, armWidth, 6, 4, outerScale)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        outerRightArm.addChild(outerRightForeArm);

        outerRightLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 12F, 0F);
        outerRightLeg.developBox(-1.9F, 0.0F, -2.0F, 4, 6, 4, outerScale)
                .setHeight(limbWearHeight)
                .hideFace(BoxSide.BOTTOM)
                .create();

        outerLeftLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 12.0F, 0.0F)
                .setMirror(true);
        outerLeftLeg.developBox(-2.1F, 0.0F, -2.0F, 4, 6, 4, outerScale)
                .setHeight(limbWearHeight)
                .hideFace(BoxSide.BOTTOM)
                .create();

        outerLeftForeLeg = new BendsModelPart(0, 22)
                .setTextureSize(64, 32)
                .setPosition(0, 6.0F, -2.0F)
                .setMirror(true);
        outerLeftForeLeg.developBox(-2.1F, 0.0F, -1.0F, 4, 6, 4, outerScale)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        outerLeftLeg.addChild(outerLeftForeLeg);

        outerRightForeLeg = new BendsModelPart(0, 22)
                .setTextureSize(64, 32)
                .setPosition(0, 6.0F, -2.0F);
        outerRightForeLeg.developBox(-1.9F, 0.0F, -1.0F, 4, 6, 4, outerScale)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        outerRightLeg.addChild(outerRightForeLeg);
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof SkeletonModel);
    }

}
