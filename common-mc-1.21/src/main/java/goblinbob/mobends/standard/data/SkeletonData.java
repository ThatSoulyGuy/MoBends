package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.SkeletonController;
import net.minecraft.world.entity.monster.AbstractSkeleton;

public class SkeletonData<E extends AbstractSkeleton> extends BipedEntityData<E>
{

    private final SkeletonController controller = new SkeletonController();

    public SkeletonData(E entity)
    {
        super(entity);
    }

    @Override
    public SkeletonController getController()
    {
        return controller;
    }

    @Override
    public void onTicksRestart()
    {
    }

    @Override
    public void initModelPose()
    {
        super.initModelPose();

        this.rightArm.position.set(-5F, -10F, 0F);
        this.leftArm.position.set(5F, -10f, 0f);
        this.rightLeg.position.set(-2F, 12.0F, 0.0F);
        this.leftLeg.position.set(2F, 12.0F, 0.0F);
        this.rightForeArm.position.set(0F, 4F, 1F);
        this.leftForeArm.position.set(0F, 4F, 1F);
        this.leftForeLeg.position.set(0, 6.0F, -1.0F);
        this.rightForeLeg.position.set(0, 6.0F, -1.0F);
    }

}
