package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.compat.CustomNpcsCompat;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.client.model.BendsMesh;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.client.model.adaptive.AdaptiveHumanoidGeometry;
import goblinbob.mobends.standard.data.CustomNpcData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class CustomNpcMutator<E extends LivingEntity>
        extends BipedMutator<CustomNpcData<E>, E, HumanoidModel<E>>
{
    private HumanoidModel<?> builtFromModel;

    private List<RenderLayer<E, HumanoidModel<E>>> liveLayers;

    public CustomNpcMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }

    private HumanoidModel<E> ownModelOf(LivingEntityRenderer<E, HumanoidModel<E>> renderer)
    {
        final HumanoidModel<E> own = CustomNpcsCompat.ownModelOf(renderer);
        return own != null ? own : renderer.getModel();
    }

    @Override
    public void fetchFields(LivingEntityRenderer<E, HumanoidModel<E>> renderer)
    {
        super.fetchFields(renderer);

        this.liveLayers = this.layerRenderers;

        final List<RenderLayer<E, HumanoidModel<E>>> own = CustomNpcsCompat.ownLayersOf(renderer);
        if (own != null)
        {
            this.layerRenderers = own;
        }
    }

    @Override
    public boolean mutate(LivingEntityRenderer<E, HumanoidModel<E>> renderer)
    {
        final HumanoidModel<E> model = ownModelOf(renderer);
        if (model == null || shouldModelBeSkipped(model))
        {
            return false;
        }

        fetchFields(renderer);

        final boolean isModelVanilla = isModelVanilla(model);
        if (isModelVanilla)
        {
            storeVanillaModel(model);
        }

        createParts(model, 0.0F);

        if (layerRenderers != null)
        {
            for (int i = 0; i < layerRenderers.size(); ++i)
            {
                swapLayer(renderer, i, isModelVanilla);
            }

            mirrorSwappedLayers();
        }

        return true;
    }

    @Override
    public void demutate(LivingEntityRenderer<E, HumanoidModel<E>> renderer)
    {
        final HumanoidModel<E> model = ownModelOf(renderer);
        if (model == null || shouldModelBeSkipped(model))
        {
            return;
        }

        applyVanillaModel(model);

        if (layerRenderers == null)
        {
            return;
        }

        final Map<RenderLayer<E, HumanoidModel<E>>, RenderLayer<E, HumanoidModel<E>>> restored = new IdentityHashMap<>();
        for (Map.Entry<Integer, RenderLayer<E, HumanoidModel<E>>> entry : originalLayers.entrySet())
        {
            final int index = entry.getKey();
            if (index >= 0 && index < layerRenderers.size())
            {
                restored.put(layerRenderers.get(index), entry.getValue());
            }
        }

        for (int i = 0; i < layerRenderers.size(); ++i)
        {
            deswapLayer(renderer, i);
        }

        for (Map.Entry<RenderLayer<E, HumanoidModel<E>>, RenderLayer<E, HumanoidModel<E>>> entry : restored.entrySet())
        {
            replaceInLiveLayers(entry.getKey(), entry.getValue());
        }
    }

    private void mirrorSwappedLayers()
    {
        for (Map.Entry<Integer, RenderLayer<E, HumanoidModel<E>>> entry : originalLayers.entrySet())
        {
            final int index = entry.getKey();
            if (index >= 0 && index < layerRenderers.size())
            {
                replaceInLiveLayers(entry.getValue(), layerRenderers.get(index));
            }
        }
    }

    private void replaceInLiveLayers(RenderLayer<E, HumanoidModel<E>> from, RenderLayer<E, HumanoidModel<E>> to)
    {
        if (liveLayers == null || liveLayers == layerRenderers || from == to)
        {
            return;
        }

        for (int i = 0; i < liveLayers.size(); ++i)
        {
            if (liveLayers.get(i) == from)
            {
                liveLayers.set(i, to);
            }
        }
    }

    @Override
    public void updateModel(E entity, LivingEntityRenderer<E, HumanoidModel<E>> renderer, float partialTicks)
    {
        final HumanoidModel<E> model = ownModelOf(renderer);

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

    @Override
    public void syncUpWithData(CustomNpcData<E> data)
    {
        CustomNpcsCompat.applyModelScaling(data.getEntity(), data);

        super.syncUpWithData(data);
    }

    @Override
    protected void onPosesSyncedToVanillaModel(HumanoidModel<?> model)
    {
        CustomNpcsCompat.removeRenderTranslation(MoBendsRenderContext.getCurrentEntity(), model);
    }

    @Override
    protected void beforeAdoptingPoseFromVanillaModel(HumanoidModel<?> model)
    {
        CustomNpcsCompat.restoreRenderTranslation(MoBendsRenderContext.getCurrentEntity(), model);
    }

    @Override
    protected void afterAdoptingPoseFromVanillaModel(HumanoidModel<?> model)
    {
        CustomNpcsCompat.removeRenderTranslation(MoBendsRenderContext.getCurrentEntity(), model);
    }
}
