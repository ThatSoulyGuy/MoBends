package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.IllagerData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.AbstractIllager;

import java.util.IdentityHashMap;
import java.util.Map;

public class IllagerMutator<E extends AbstractIllager>
        extends BipedMutator<IllagerData<E>, E, HierarchicalModel<E>>
{
    private final Map<HierarchicalModel<?>, HumanoidModel<?>> views = new IdentityHashMap<>();

    public IllagerMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public HumanoidModel<?> humanoidViewOf(EntityModel<?> model)
    {
        if (!(model instanceof HierarchicalModel<?> hierarchicalModel) || !hasHumanoidParts(hierarchicalModel))
        {
            return null;
        }

        return views.computeIfAbsent(hierarchicalModel, IllagerMutator::buildView);
    }

    private static boolean hasHumanoidParts(HierarchicalModel<?> model)
    {
        final ModelPart root = model.root();
        return root.hasChild("head") && root.hasChild("body") && root.hasChild("right_arm")
                && root.hasChild("left_arm") && root.hasChild("right_leg") && root.hasChild("left_leg");
    }

    private static HumanoidModel<?> buildView(HierarchicalModel<?> model)
    {
        final ModelPart root = model.root();

        return humanoidView(root.getChild("head"), root.getChild("body"),
                root.getChild("right_arm"), root.getChild("left_arm"),
                root.getChild("right_leg"), root.getChild("left_leg"));
    }

    private boolean hatVisible = false;

    @Override
    protected void reconcileWithVanillaModel(HumanoidModel<?> original)
    {
        super.reconcileWithVanillaModel(original);

        this.hatVisible = original != null && original.hat.visible;
    }

    @Override
    protected void syncConcealmentFromVanillaModel()
    {
        final HumanoidModel<?> model = MoBendsRenderContext.getCurrentVanillaModel();
        if (model != null)
        {
            model.leftArm.visible = true;
            model.rightArm.visible = true;
            model.hat.visible = this.hatVisible;
        }

        super.syncConcealmentFromVanillaModel();

        if (model != null)
        {
            model.hat.visible = false;
        }
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof HierarchicalModel<?> hierarchicalModel) || !hasHumanoidParts(hierarchicalModel);
    }
}
