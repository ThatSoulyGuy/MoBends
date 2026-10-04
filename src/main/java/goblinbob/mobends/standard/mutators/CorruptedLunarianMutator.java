package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.ZombieLikeData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class CorruptedLunarianMutator extends BipedMutator<ZombieLikeData, Mob, EntityModel<Mob>>
{
    public static final String MODEL_CLASS = "earth.terrarium.adastra.client.models.entities.mobs.CorruptedLunarianModel";

    private static final int TEXTURE_SIZE = 128;
    private static final int SKIRT_OVERLAP = 3;

    private final Map<EntityModel<?>, HumanoidModel<?>> views = new IdentityHashMap<>();

    public CorruptedLunarianMutator(IEntityDataFactory<Mob> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return model == null || !MODEL_CLASS.equals(model.getClass().getName());
    }

    @Override
    public HumanoidModel<?> humanoidViewOf(EntityModel<?> model)
    {
        if (shouldModelBeSkipped(model))
        {
            return null;
        }

        return views.computeIfAbsent(model, CorruptedLunarianMutator::buildView);
    }

    @Nullable
    private static HumanoidModel<?> buildView(EntityModel<?> model)
    {
        final ModelPart head = field(model, "head");
        final ModelPart body = field(model, "body");
        final ModelPart leftLeg = field(model, "leg0");
        final ModelPart rightLeg = field(model, "leg1");

        if (head == null || body == null || leftLeg == null || rightLeg == null)
        {
            return null;
        }

        final Map<String, ModelPart> parts = new HashMap<>();
        parts.put("head", head);
        parts.put("hat", detachedPart());
        parts.put("body", body);
        parts.put("right_arm", detachedPart());
        parts.put("left_arm", detachedPart());
        parts.put("right_leg", rightLeg);
        parts.put("left_leg", leftLeg);

        return new HumanoidModel<LivingEntity>(new ModelPart(Collections.emptyList(), parts));
    }

    @Nullable
    private static ModelPart extraArmsOf(EntityModel<?> model)
    {
        final Map<String, ModelPart> arms = new HashMap<>();

        for (int i = 1; i <= 4; ++i)
        {
            final ModelPart arm = field(model, "monsterarm" + i);
            if (arm != null)
            {
                arms.put("backarm" + i, arm);
            }
        }

        if (arms.isEmpty())
        {
            return null;
        }

        final ModelPart extraArms = new ModelPart(Collections.emptyList(), arms);
        extraArms.z = 3.0F;
        return extraArms;
    }

    @Nullable
    private static ModelPart field(Object model, String name)
    {
        try
        {
            final java.lang.reflect.Field field = model.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(model) instanceof ModelPart part ? part : null;
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    private static ModelPart detachedPart()
    {
        return new ModelPart(Collections.emptyList(), Collections.emptyMap());
    }

    private static BendsModelPart part(int u, int v)
    {
        return new BendsModelPart(u, v).setTextureSize(TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    public boolean createParts(EntityModel<Mob> original, float scaleFactor)
    {
        body = part(100, 0).setPosition(0.0F, 12.0F, 0.0F).setMirror(true);
        body.addCube(-4.0F, -12.0F, -3.0F, 8, 12, 6, scaleFactor);
        body.setTextureOffset(0, 36);
        body.addCube(-4.0F, -12.0F, -3.0F, 8, 12, 6, scaleFactor + 0.5F);

        skirt = part(0, 36 + 12 - SKIRT_OVERLAP).setMirror(true);
        skirt.developBox(-4.0F, -SKIRT_OVERLAP, -3.0F, 8, 19 - 12 + SKIRT_OVERLAP, 6, scaleFactor + 0.45F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -(12 - SKIRT_OVERLAP))
                .create();
        body.addChild(skirt);

        head = part(0, 19).setPosition(0.0F, -12.0F, 0.0F).setMirror(true);
        head.addCube(-4.0F, -9.0F, -4.0F, 8, 9, 8, scaleFactor);
        head.setTextureOffset(0, 0);
        head.addCube(-4.5F, -18.0F, -4.5F, 9, 10, 9, scaleFactor);
        head.setTextureOffset(0, 20);
        head.addCube(-1.0F, -3.0F, -6.0F, 2, 4, 2, scaleFactor);
        body.addChild(head);

        leftArm = part(30, 61).setPosition(5.0F, -10.0F, 0.0F).setMirror(true);
        leftArm.developBox(-1.0F, -2.0F, -2.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        body.addChild(leftArm);

        rightArm = part(30, 61).setPosition(-5.0F, -10.0F, 0.0F);
        rightArm.developBox(-3.0F, -2.0F, -2.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        body.addChild(rightArm);

        leftForeArm = part(30, 67).setPosition(0.0F, 4.0F, 2.0F).setMirror(true);
        leftForeArm.developBox(-1.0F, 0.0F, -4.0F, 4, 6, 4, scaleFactor)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        leftArm.addChild(leftForeArm);

        rightForeArm = part(30, 67).setPosition(0.0F, 4.0F, 2.0F);
        rightForeArm.developBox(-3.0F, 0.0F, -4.0F, 4, 6, 4, scaleFactor)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        rightArm.addChild(rightForeArm);

        rightLeg = part(0, 81).setPosition(0.0F, 12.0F, 0.0F);
        rightLeg.addCube(-3.9F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);

        leftLeg = part(0, 81).setPosition(0.0F, 12.0F, 0.0F).setMirror(true);
        leftLeg.addCube(-0.1F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);

        rightForeLeg = part(0, 87).setPosition(0.0F, 6.0F, -2.0F);
        rightForeLeg.developBox(-3.9F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        rightLeg.addChild(rightForeLeg);

        leftForeLeg = part(0, 87).setPosition(0.0F, 6.0F, -2.0F).setMirror(true);
        leftForeLeg.developBox(-0.1F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        leftLeg.addChild(leftForeLeg);

        reconcileWithVanillaModel(humanoidViewOf(original));

        final ModelPart extraArms = original == null ? null : extraArmsOf(original);
        if (extraArms != null)
        {
            attach(extraArms, extraArms, body, absoluteOf(null, body));
        }

        return true;
    }

    @Override
    protected void createOuterParts(float scaleFactor)
    {
    }

    @Override
    public void syncUpWithData(ZombieLikeData data)
    {
        super.syncUpWithData(data);

        if (skirt != null)
        {
            skirt.rotation.orientInstantX(skirtFold(data, 1.0F, 90.0F, 20.0F));
        }
    }
}
