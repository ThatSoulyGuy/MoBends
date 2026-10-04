package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class AdaptiveHumanoidMutator<D extends BipedEntityData<E>, E extends LivingEntity>
        extends BipedMutator<D, E, HumanoidModel<E>>
{
    private final Map<HumanoidModel<?>, HumanoidModel<?>> views = new IdentityHashMap<>();

    public AdaptiveHumanoidMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public HumanoidModel<?> humanoidViewOf(EntityModel<?> model)
    {
        if (!(model instanceof HumanoidModel<?> humanoid))
        {
            return null;
        }

        return views.computeIfAbsent(humanoid, AdaptiveHumanoidMutator::sideCorrectedView);
    }

    private static HumanoidModel<?> sideCorrectedView(HumanoidModel<?> model)
    {
        if (model.rightLeg.x <= model.leftLeg.x)
        {
            return model;
        }

        final Map<String, ModelPart> parts = new HashMap<>();
        parts.put("head", model.head);
        parts.put("hat", model.hat);
        parts.put("body", model.body);
        parts.put("right_arm", model.rightArm);
        parts.put("left_arm", model.leftArm);
        parts.put("right_leg", model.leftLeg);
        parts.put("left_leg", model.rightLeg);

        return new HumanoidModel<LivingEntity>(new ModelPart(Collections.emptyList(), parts));
    }
}
