package goblinbob.mobends.core.data;

import goblinbob.mobends.core.bender.PreviewHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class EntityDatabase
{

    public static EntityDatabase instance = new EntityDatabase();

    private static final int DETACHED_RETENTION_TICKS = 200;

    protected final Map<LivingEntity, LivingEntityData<?>> entryMap = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends LivingEntityData<E>, E extends LivingEntity> T get(E entity)
    {
        if (entity == null)
            return null;

        return (T) this.entryMap.get(entity);
    }

    @SuppressWarnings("unchecked")
    public <T extends LivingEntityData<E>, E extends LivingEntity> T getOrMake(IEntityDataFactory<E> dataCreationFunction, E entity)
    {
        T data = (T) this.entryMap.get(entity);

        if (data == null)
        {
            data = (T) dataCreationFunction.createEntityData(entity);
            this.entryMap.put(entity, data);
        }

        data.markSeen();
        data.markAnimated();

        return data;
    }

    public void add(Entity entity, LivingEntityData<?> data)
    {
        if (entity instanceof LivingEntity livingEntity)
        {
            this.entryMap.put(livingEntity, data);
        }
    }

    public void remove(Entity entity)
    {
        if (entity == null)
            return;

        LivingEntityData<?> data = this.entryMap.remove(entity);
        if (data == null)
            return;
    }

    public void updateClient()
    {
        final Level level = Minecraft.getInstance().level;
        if (level == null) return;

        Iterator<Entry<LivingEntity, LivingEntityData<?>>> it = entryMap.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<LivingEntity, LivingEntityData<?>> entry = it.next();

            LivingEntity entity = entry.getKey();
            LivingEntityData<?> entityData = entry.getValue();

            if (level.getEntity(entity.getId()) == entity
                    || goblinbob.mobends.compat.ModCompatManager.isAttachedProxyEntity(entity))
            {
                entityData.setDetached(false);
                entityData.markSeen();
                entityData.updateClient();
                continue;
            }

            if (PreviewHelper.isPreviewEntity(entity))
            {
                entityData.setDetached(true);
                entityData.markSeen();
                entityData.updateClient();
                continue;
            }

            if (entity.isRemoved() || entityData.trackUnseen() > DETACHED_RETENTION_TICKS)
            {
                it.remove();
                continue;
            }

            entityData.setDetached(true);
            entityData.updateClient();
        }
    }

    public void updateRender(float partialTicks)
    {
        for (EntityData<?> entityData : this.entryMap.values())
        {
            entityData.update(partialTicks);
        }
    }

    public void refresh()
    {
        this.entryMap.clear();
    }

    public void onTicksRestart()
    {
        entryMap.values().forEach(data -> data.onTicksRestart());
    }

}
