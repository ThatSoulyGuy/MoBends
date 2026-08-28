package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.VillagerData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WitchModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class WitchMutator<E extends LivingEntity> extends VillagerMutator<E>
{
    private static final float NOSE_PIVOT_Y = -2.0F;

    private static final float NOSE_WIGGLE_SPEED = 0.01F;
    private static final float NOSE_PITCH_AMOUNT = 4.5F;
    private static final float NOSE_ROLL_AMOUNT = 2.5F;

    private static final float DRINKING_NOSE_PITCH = -0.9F * 180.0F / (float) Math.PI;
    private static final float DRINKING_NOSE_Y = 1.0F;
    private static final float DRINKING_NOSE_Z = -1.5F;

    private BendsModelPart nose, outerNose;

    public WitchMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public void syncUpWithData(VillagerData<E> data)
    {
        super.syncUpWithData(data);

        final LivingEntity entity = data.getEntity();
        if (entity == null)
        {
            return;
        }

        final boolean holdingItem = !entity.getMainHandItem().isEmpty();

        final float wiggle = entity.tickCount * NOSE_WIGGLE_SPEED * (entity.getId() % 10);

        final float pitch = holdingItem
                ? DRINKING_NOSE_PITCH
                : Mth.sin(wiggle) * NOSE_PITCH_AMOUNT;
        final float roll = Mth.cos(wiggle) * NOSE_ROLL_AMOUNT;

        poseNose(nose, holdingItem, pitch, roll);
        poseNose(outerNose, holdingItem, pitch, roll);
    }

    private static void poseNose(BendsModelPart part, boolean holdingItem, float pitch, float roll)
    {
        if (part == null)
        {
            return;
        }

        if (holdingItem)
        {
            part.setPosition(0.0F, DRINKING_NOSE_Y, DRINKING_NOSE_Z);
        }
        else
        {
            part.setPosition(0.0F, NOSE_PIVOT_Y, 0.0F);
        }

        part.rotation.orientInstantX(pitch).rotateInstantZ(roll);
    }

    @Override
    protected int textureHeight()
    {
        return 128;
    }

    @Override
    protected int handTexU()
    {
        return 14;
    }

    @Override
    protected int handTexV()
    {
        return 8;
    }

    @Override
    protected BendsModelPart buildHead(float scaleFactor, boolean outer)
    {
        final BendsModelPart part = new BendsModelPart(0, 0)
                .setTextureSize(64, textureHeight())
                .setPosition(0.0F, -12.0F, 0.0F);

        part.addCube(-4.0F, -10.0F, -4.0F, 8, 10, 8, scaleFactor);

        final BendsModelPart nosePart = buildNose(scaleFactor);
        part.addChild(nosePart);

        if (outer)
        {
            outerNose = nosePart;
        }
        else
        {
            nose = nosePart;
        }

        part.addChild(buildHat(scaleFactor));

        return part;
    }

    private BendsModelPart buildNose(float scaleFactor)
    {
        final BendsModelPart part = new BendsModelPart(24, 0)
                .setTextureSize(64, textureHeight())
                .setPosition(0.0F, NOSE_PIVOT_Y, 0.0F);

        part.addCube(-1.0F, -1.0F, -6.0F, 2, 4, 2, scaleFactor);

        part.setTextureOffset(0, 0);
        part.addCube(0.0F, 1.0F, -6.75F, 1, 1, 1, scaleFactor - 0.25F);

        return part;
    }

    private BendsModelPart buildHat(float scaleFactor)
    {
        final BendsModelPart hat = new BendsModelPart(0, 64)
                .setTextureSize(64, textureHeight())
                .setPosition(-5.0F, -10.03125F, -5.0F);
        hat.addCube(0.0F, 0.0F, 0.0F, 10, 2, 10, scaleFactor);

        final BendsModelPart hat2 = new BendsModelPart(0, 76)
                .setTextureSize(64, textureHeight())
                .setPosition(1.75F, -4.0F, 2.0F);
        hat2.addCube(0.0F, 0.0F, 0.0F, 7, 4, 7, scaleFactor);
        hat2.rotation.orientInstantX(-3.0F).rotateInstantZ(1.5F);
        hat.addChild(hat2);

        final BendsModelPart hat3 = new BendsModelPart(0, 87)
                .setTextureSize(64, textureHeight())
                .setPosition(1.75F, -4.0F, 2.0F);
        hat3.addCube(0.0F, 0.0F, 0.0F, 4, 4, 4, scaleFactor);
        hat3.rotation.orientInstantX(-6.0F).rotateInstantZ(3.0F);
        hat2.addChild(hat3);

        final BendsModelPart hat4 = new BendsModelPart(0, 95)
                .setTextureSize(64, textureHeight())
                .setPosition(1.75F, -2.0F, 2.0F);
        hat4.addCube(0.0F, 0.0F, 0.0F, 1, 2, 1, scaleFactor + 0.25F);
        hat4.rotation.orientInstantX(-12.0F).rotateInstantZ(6.0F);
        hat3.addChild(hat4);

        return hat;
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof WitchModel);
    }
}
