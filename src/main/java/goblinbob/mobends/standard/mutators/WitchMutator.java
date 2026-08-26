package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WitchModel;
import net.minecraft.world.entity.LivingEntity;

public class WitchMutator<E extends LivingEntity> extends VillagerMutator<E>
{
    public WitchMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
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

        part.setTextureOffset(24, 0);
        part.addCube(-1.0F, -3.0F, -6.0F, 2, 4, 2, scaleFactor);
        part.setTextureOffset(0, 0);
        part.addCube(0.0F, -1.0F, -6.75F, 1, 1, 1, scaleFactor - 0.25F);

        part.addChild(buildHat(scaleFactor));

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
