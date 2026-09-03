package goblinbob.mobends.core.bender;

import goblinbob.mobends.api.entity.IMobSpawnHelper;
import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.mutators.IMutatorFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class DerivedEntityBender<T extends LivingEntity> extends EntityBender<T>
{
    private final EntityBender<?> parent;
    private final EntityType<?> entityType;

    @Nullable
    private T previewEntity;

    @SuppressWarnings("unchecked")
    public DerivedEntityBender(String modId, String key, String unlocalizedName, Class<T> entityClass,
                               EntityType<?> entityType, EntityBender<?> parent)
    {
        super(modId, key, unlocalizedName, entityClass,
                (MutatedRenderer<T>) parent.getMutatedRenderer());

        this.parent = parent;
        this.entityType = entityType;
    }

    public EntityBender<?> getParent()
    {
        return parent;
    }

    @Override
    public String[] getAlterableParts()
    {
        return parent.getAlterableParts();
    }

    @Override
    public String[] getSupportedAnimations()
    {
        return parent.getSupportedAnimations();
    }

    @Override
    @SuppressWarnings("unchecked")
    public IEntityDataFactory<T> getDataFactory()
    {
        return (IEntityDataFactory<T>) parent.getDataFactory();
    }

    @Override
    @SuppressWarnings("unchecked")
    public IMutatorFactory<T> getMutatorFactory()
    {
        return (IMutatorFactory<T>) parent.getMutatorFactory();
    }

    @Override
    public IPreviewer<?> getPreviewer()
    {
        return parent.getPreviewer();
    }

    @Override
    public LivingEntityData<?> getDataForPreview()
    {
        if (previewEntity == null)
        {
            previewEntity = createPreviewEntity();
        }

        return previewEntity != null
                ? EntityDatabase.instance.getOrMake(getDataFactory(), previewEntity)
                : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T createPreviewEntity()
    {
        Level level = Minecraft.getInstance().level;
        if (level == null) return null;

        try
        {
            Entity entity = entityType.create(level);
            if (!entityClass.isInstance(entity)) return null;

            entity.moveTo(0, 0, 0, 0, 0);
            entity.refreshDimensions();

            IMobSpawnHelper helper = IMobSpawnHelper.Holder.getHelper();
            if (helper != null && entity instanceof Mob mob)
            {
                Object serverLevel = Minecraft.getInstance().getSingleplayerServer() != null
                        ? Minecraft.getInstance().getSingleplayerServer().overworld()
                        : null;
                helper.finalizeSpawn(mob, serverLevel,
                        level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND);
            }

            PreviewHelper.registerPreviewEntity(entity);
            return (T) entity;
        }
        catch (Throwable t)
        {
            return null;
        }
    }
}
