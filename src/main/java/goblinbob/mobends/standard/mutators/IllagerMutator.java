package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.IllagerData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.AbstractIllager;

import java.util.IdentityHashMap;
import java.util.Map;

public class IllagerMutator<E extends AbstractIllager>
        extends BipedMutator<IllagerData<E>, E, IllagerModel<E>>
{
    private final Map<IllagerModel<?>, HumanoidModel<?>> views = new IdentityHashMap<>();

    public IllagerMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public HumanoidModel<?> humanoidViewOf(EntityModel<?> model)
    {
        if (!(model instanceof IllagerModel<?> illagerModel))
        {
            return null;
        }

        return views.computeIfAbsent(illagerModel, IllagerMutator::buildView);
    }

    private static HumanoidModel<?> buildView(IllagerModel<?> model)
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
        return !(model instanceof IllagerModel);
    }
}
