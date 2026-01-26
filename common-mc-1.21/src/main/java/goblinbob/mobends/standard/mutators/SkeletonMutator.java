package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.SkeletonData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.monster.Skeleton;

/**
 * Instantiated once per SkeletonRenderer
 *
 * @author Iwo Plaza
 */
public class SkeletonMutator extends BipedMutator<SkeletonData, Skeleton, SkeletonModel<Skeleton>>
{

    protected boolean boneLimbs = false;

    public SkeletonMutator(IEntityDataFactory<Skeleton> dataCreationFunction)
    {
        super(dataCreationFunction);
    }

    @Override
    public void fetchFields(LivingEntityRenderer<Skeleton, SkeletonModel<Skeleton>> renderer)
    {
        super.fetchFields(renderer);

        // In 1.20.1, skeleton limbs are always bone-style (thin)
        this.boneLimbs = true;
    }

    @Override
    public void storeVanillaModel(SkeletonModel<Skeleton> model)
    {
        super.storeVanillaModel(model);
    }

    @Override
    public boolean createParts(SkeletonModel<Skeleton> original, float scaleFactor)
    {
        // Create custom bendable parts using BendsModelPart
        // Body - root of upper body hierarchy
        body = new BendsModelPart(16, 16)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 12.0F, 0.0F);
        body.addCube(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor);

        // Head - child of body
        head = new BendsModelPart(0, 0)
                .setTextureSize(64, 32)
                .setPosition(0.0F, -12.0F, 0.0F);
        head.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor);
        body.addChild(head);

        // Headwear - child of head
        headwear = new BendsModelPart(32, 0)
                .setTextureSize(64, 32);
        headwear.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor + 0.5F);
        head.addChild(headwear);

        // Skeleton uses thin bone limbs (2 pixels wide)
        // Right Arm - child of body
        rightArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 32)
                .setPosition(-5.0F, -10.0F, 0.0F);
        rightArm.addCube(-1.0F, -2.0F, -1.0F, 2, 6, 2, scaleFactor);
        body.addChild(rightArm);

        // Left Arm - child of body
        leftArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 32)
                .setPosition(5.0F, -10.0F, 0.0F)
                .setMirror(true);
        leftArm.addCube(-1.0F, -2.0F, -1.0F, 2, 6, 2, scaleFactor);
        body.addChild(leftArm);

        // Right Forearm - child of rightArm
        rightForeArm = new BendsModelPart(40, 16 + 6)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 4.0F, 1.0F);
        rightForeArm.addCube(-1.0F, 0.0F, -2.0F, 2, 6, 2, scaleFactor);
        rightArm.addChild(rightForeArm);

        // Left Forearm - child of leftArm
        leftForeArm = new BendsModelPart(40, 16 + 6)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 4.0F, 1.0F)
                .setMirror(true);
        leftForeArm.addCube(-1.0F, 0.0F, -2.0F, 2, 6, 2, scaleFactor);
        leftArm.addChild(leftForeArm);

        // Legs (also thin) - independent roots
        rightLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 32)
                .setPosition(-2.0F, 12.0F, 0.0F);
        rightLeg.addCube(-1.0F, 0.0F, -1.0F, 2, 6, 2, scaleFactor);

        leftLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 32)
                .setPosition(2.0F, 12.0F, 0.0F)
                .setMirror(true);
        leftLeg.addCube(-1.0F, 0.0F, -1.0F, 2, 6, 2, scaleFactor);

        // Right Foreleg - child of rightLeg
        rightForeLeg = new BendsModelPart(0, 16 + 6)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 6.0F, -2.0F);
        rightForeLeg.addCube(-1.0F, 0.0F, 0.0F, 2, 6, 2, scaleFactor);
        rightLeg.addChild(rightForeLeg);

        // Left Foreleg - child of leftLeg
        leftForeLeg = new BendsModelPart(0, 16 + 6)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 6.0F, -2.0F)
                .setMirror(true);
        leftForeLeg.addCube(-1.0F, 0.0F, 0.0F, 2, 6, 2, scaleFactor);
        leftLeg.addChild(leftForeLeg);

        return true;
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof SkeletonModel);
    }

}
