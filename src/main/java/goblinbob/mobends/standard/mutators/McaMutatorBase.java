package goblinbob.mobends.standard.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.compat.McaCompat;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.client.model.BendsMesh;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.client.model.adaptive.AdaptiveHumanoidGeometry;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

public abstract class McaMutatorBase<D extends BipedEntityData<E>, E extends LivingEntity>
        extends BipedMutator<D, E, HumanoidModel<E>>
{
    private HumanoidModel<?> builtFromModel;

    public McaMutatorBase(IEntityDataFactory<E> dataFactory)
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
            McaCompat.alignWearParts(model);
            createParts(model, 0.0F);
        }

        super.updateModel(entity, renderer, partialTicks);
    }

    @Override
    public boolean createParts(HumanoidModel<E> original, float scaleFactor)
    {
        builtFromModel = original;

        McaCompat.alignWearParts(original);

        if (tryCreateAdaptiveParts(original))
        {
            return true;
        }

        return super.createParts(original, scaleFactor);
    }

    @Override
    protected AdaptiveHumanoidGeometry.WearParts adaptiveWearParts(HumanoidModel<E> original)
    {
        return McaCompat.wearPartsOf(original);
    }

    @Override
    protected AdaptiveHumanoidGeometry.WearParts overlayWearParts(HumanoidModel<?> model)
    {
        McaCompat.alignWearParts(model);
        return McaCompat.wearPartsOf(model);
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
    public boolean shouldRenderCustom()
    {
        return super.shouldRenderCustom()
                && !McaCompat.isUnanimatedAge(MoBendsRenderContext.getCurrentEntity());
    }

    @Override
    public void syncPosesToVanillaModel(HumanoidModel<?> model)
    {
        if (McaCompat.isUnanimatedAge(MoBendsRenderContext.getCurrentEntity()))
        {
            return;
        }

        super.syncPosesToVanillaModel(model);
    }

    @Override
    public void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                              int packedLight, int packedOverlay, int color)
    {
        super.renderMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);

        renderBreasts(MoBendsRenderContext.getCurrentVanillaModel(), poseStack, vertexConsumer,
                packedLight, packedOverlay, color);
    }

    @Override
    protected void drawOverlayExtras(HumanoidModel<?> model, PoseStack poseStack, VertexConsumer vertexConsumer,
                                     int packedLight, int packedOverlay, int color)
    {
        renderBreasts(model, poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void renderBreasts(HumanoidModel<?> model, PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay, int color)
    {
        if (model == null || body == null || !body.isShowing())
        {
            return;
        }

        poseStack.pushPose();
        body.applyCharacterTransformPoseStack(poseStack);
        poseStack.translate(-body.position.x / 16.0F, -body.position.y / 16.0F, -body.position.z / 16.0F);

        McaCompat.renderBreasts(model, poseStack, vertexConsumer, packedLight, packedOverlay, color);

        poseStack.popPose();
    }
}
