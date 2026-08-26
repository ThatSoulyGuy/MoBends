package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.BendsMesh;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.client.model.adaptive.AdaptiveHumanoidGeometry;
import goblinbob.mobends.standard.data.HumanoidMobData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

public class HumanoidMobMutator<E extends LivingEntity>
        extends BipedMutator<HumanoidMobData<E>, E, HumanoidModel<E>>
{
    private HumanoidModel<?> builtFromModel;

    public HumanoidMobMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public void updateModel(E entity, LivingEntityRenderer<E, HumanoidModel<E>> renderer, float partialTicks)
    {
        final HumanoidModel<E> model = renderer.getModel();

        if (model != null && model != builtFromModel && body != null && !shouldModelBeSkipped(model))
        {
            restoreVanillaPivots(model);
            alignWearParts(model);
            createParts(model, 0.0F);
        }

        super.updateModel(entity, renderer, partialTicks);
    }

    private static void alignWearParts(HumanoidModel<?> model)
    {
        if (!(model instanceof PlayerModel<?> playerModel))
        {
            return;
        }

        playerModel.jacket.copyFrom(model.body);
        playerModel.leftSleeve.copyFrom(model.leftArm);
        playerModel.rightSleeve.copyFrom(model.rightArm);
        playerModel.leftPants.copyFrom(model.leftLeg);
        playerModel.rightPants.copyFrom(model.rightLeg);
    }

    @Override
    protected AdaptiveHumanoidGeometry.WearParts adaptiveWearParts(HumanoidModel<E> original)
    {
        if (!(original instanceof PlayerModel<?> playerModel))
        {
            return null;
        }

        return new AdaptiveHumanoidGeometry.WearParts(playerModel.jacket,
                playerModel.leftSleeve, playerModel.rightSleeve,
                playerModel.leftPants, playerModel.rightPants);
    }

    @Override
    protected AdaptiveHumanoidGeometry.CaptureMode adaptiveLimbCaptureMode()
    {
        return AdaptiveHumanoidGeometry.CaptureMode.SUBTREE;
    }

    @Override
    protected void createAdaptiveWearParts(AdaptiveHumanoidGeometry geometry)
    {
        attachWear(body, geometry.bodyWearMesh);
        attachWear(leftArm, geometry.leftArmWearMesh);
        attachWear(rightArm, geometry.rightArmWearMesh);
        attachWear(leftForeArm, geometry.leftForeArmWearMesh);
        attachWear(rightForeArm, geometry.rightForeArmWearMesh);
        attachWear(leftLeg, geometry.leftLegWearMesh);
        attachWear(rightLeg, geometry.rightLegWearMesh);
        attachWear(leftForeLeg, geometry.leftForeLegWearMesh);
        attachWear(rightForeLeg, geometry.rightForeLegWearMesh);
    }

    private static void attachWear(BendsModelPart parent, BendsMesh mesh)
    {
        if (parent == null || mesh == null)
        {
            return;
        }

        parent.addChild(new BendsModelPart().addMesh(mesh));
    }

    @Override
    public boolean createParts(HumanoidModel<E> original, float scaleFactor)
    {
        builtFromModel = original;

        if (tryCreateAdaptiveParts(original))
        {
            return true;
        }

        return super.createParts(original, scaleFactor);
    }
}
