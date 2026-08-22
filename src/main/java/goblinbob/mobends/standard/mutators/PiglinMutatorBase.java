package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.world.entity.Mob;

public abstract class PiglinMutatorBase<D extends BipedEntityData<E>, E extends Mob>
        extends BipedMutator<D, E, PiglinModel<E>>
{
    private static final float EAR_TILT = (float) (Math.PI / 6);
    private static final float NECK_FILL = 0.35F;
    private static final float WEAR_OFFSET = 0.25F;

    protected BendsModelPart leftEar;
    protected BendsModelPart rightEar;

    protected BendsModelPart bodywear;
    protected BendsModelPart leftArmwear;
    protected BendsModelPart rightArmwear;
    protected BendsModelPart leftForeArmwear;
    protected BendsModelPart rightForeArmwear;
    protected BendsModelPart leftLegwear;
    protected BendsModelPart rightLegwear;
    protected BendsModelPart leftForeLegwear;
    protected BendsModelPart rightForeLegwear;

    private boolean rightEarShown = true;

    public PiglinMutatorBase(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public boolean createParts(PiglinModel<E> original, float scaleFactor)
    {
        this.rightEarShown = original == null || original.rightEar.visible;

        if (!super.createParts(original, scaleFactor))
        {
            return false;
        }

        createWearParts(scaleFactor);

        return true;
    }

    @Override
    protected void createHeadParts(float scaleFactor)
    {
        head = new BendsModelPart(0, 0)
                .setTextureSize(64, 64)
                .setPosition(0.0F, -12.0F, 0.0F);
        head.addCube(-5.0F, -8.0F, -4.0F, 10, 8, 8, scaleFactor);
        head.setTextureOffset(31, 1);
        head.addCube(-2.0F, -4.0F, -5.0F, 4, 4, 1, scaleFactor);
        head.setTextureOffset(2, 4);
        head.addCube(2.0F, -2.0F, -5.0F, 1, 2, 1, scaleFactor);
        head.setTextureOffset(2, 0);
        head.addCube(-3.0F, -2.0F, -5.0F, 1, 2, 1, scaleFactor);
        head.setTextureOffset(16, 16);
        head.addCube(-4.0F, -1.0F, -2.0F, 8, 1, 4, scaleFactor + NECK_FILL);
        body.addChild(head);

        leftEar = new BendsModelPart(51, 6)
                .setTextureSize(64, 64)
                .setPosition(4.5F, -6.0F, 0.0F);
        leftEar.addCube(0.0F, 0.0F, -2.0F, 1, 5, 4, scaleFactor);
        leftEar.rotation.orientInstantZ(-EAR_TILT);
        head.addChild(leftEar);

        if (rightEarShown)
        {
            rightEar = new BendsModelPart(39, 6)
                    .setTextureSize(64, 64)
                    .setPosition(-4.5F, -6.0F, 0.0F);
            rightEar.addCube(-1.0F, 0.0F, -2.0F, 1, 5, 4, scaleFactor);
            rightEar.rotation.orientInstantZ(EAR_TILT);
            head.addChild(rightEar);
        }
        else
        {
            rightEar = null;
        }

        headwear = new BendsModelPart(32, 0)
                .setTextureSize(64, 64);
        head.addChild(headwear);
    }

    @Override
    protected void createOuterParts(float scaleFactor)
    {
    }

    protected void createWearParts(float scaleFactor)
    {
        final float limbWearHeight = (6F + 2 * scaleFactor + 0.5F) - 0.25F;
        final int armWidth = 4;

        bodywear = new BendsModelPart(16, 32)
                .setTextureSize(64, 64);
        bodywear.addCube(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor + WEAR_OFFSET);
        body.addChild(bodywear);

        leftArmwear = new BendsModelPart(48, 48)
                .setTextureSize(64, 64)
                .setMirror(true);
        leftArmwear.developBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + WEAR_OFFSET)
                .setHeight(limbWearHeight)
                .inflate(0.0025F, 0F, 0.0025F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        leftArm.addChild(leftArmwear);

        rightArmwear = new BendsModelPart(40, 32)
                .setTextureSize(64, 64);
        rightArmwear.developBox(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + WEAR_OFFSET)
                .setHeight(limbWearHeight)
                .inflate(0.0025F, 0F, 0.0025F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        rightArm.addChild(rightArmwear);

        leftForeArmwear = new BendsModelPart(48, 48 + 6)
                .setTextureSize(64, 64)
                .setMirror(true);
        leftForeArmwear.developBox(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor + WEAR_OFFSET)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        leftForeArm.addChild(leftForeArmwear);

        rightForeArmwear = new BendsModelPart(40, 32 + 6)
                .setTextureSize(64, 64);
        rightForeArmwear.developBox(-armWidth + 1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor + WEAR_OFFSET)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        rightForeArm.addChild(rightForeArmwear);

        leftLegwear = new BendsModelPart(0, 48)
                .setTextureSize(64, 64)
                .setMirror(true);
        leftLegwear.developBox(-0.1F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + WEAR_OFFSET)
                .setHeight(limbWearHeight)
                .hideFace(BoxSide.BOTTOM)
                .create();
        leftLeg.addChild(leftLegwear);

        rightLegwear = new BendsModelPart(0, 32)
                .setTextureSize(64, 64);
        rightLegwear.developBox(-3.9F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + WEAR_OFFSET)
                .setHeight(limbWearHeight)
                .hideFace(BoxSide.BOTTOM)
                .create();
        rightLeg.addChild(rightLegwear);

        leftForeLegwear = new BendsModelPart(0, 48 + 6)
                .setTextureSize(64, 64)
                .setMirror(true);
        leftForeLegwear.developBox(-0.1F, 0.0F, 0.0F, 4, 6, 4, scaleFactor + WEAR_OFFSET)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        leftForeLeg.addChild(leftForeLegwear);

        rightForeLegwear = new BendsModelPart(0, 32 + 6)
                .setTextureSize(64, 64);
        rightForeLegwear.developBox(-3.9F, 0.0F, 0.0F, 4, 6, 4, scaleFactor + WEAR_OFFSET)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        rightForeLeg.addChild(rightForeLegwear);
    }

    @Override
    public void storeVanillaModel(PiglinModel<E> model)
    {
        this.vanillaModel = model;

        super.storeVanillaModel(model);
    }

    @Override
    protected boolean usesAdaptiveGeometry()
    {
        return false;
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof PiglinModel);
    }
}
