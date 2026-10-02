package goblinbob.mobends.standard.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.compat.SpiderOverhaulCompat;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.data.SpiderData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.monster.Spider;

public class SpiderOverhaulMutator extends Mutator<SpiderData, Spider, EntityModel<Spider>>
{
    public SpiderOverhaulMutator(IEntityDataFactory<Spider> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public boolean createParts(EntityModel<Spider> original, float scaleFactor)
    {
        return true;
    }

    @Override
    public void syncUpWithData(SpiderData data)
    {
    }

    @Override
    public boolean isModelVanilla(EntityModel<Spider> model)
    {
        return false;
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !SpiderOverhaulCompat.isModelWrapper(model);
    }

    @Override
    public void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                              int packedLight, int packedOverlay, int color)
    {
    }

    @Override
    public boolean shouldRenderCustom()
    {
        return false;
    }
}
