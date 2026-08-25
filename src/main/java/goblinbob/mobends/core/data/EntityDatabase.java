package goblinbob.mobends.core.data;

import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.bender.PreviewHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class EntityDatabase
{

    public static EntityDatabase instance = new EntityDatabase();

    protected final Map<Integer, LivingEntityData<?>> entryMap = new HashMap<>();

    private LivingEntityData<?> get(Integer identifier)
    {
        return entryMap.get(identifier);
    }

    public <T extends LivingEntityData<E>, E extends LivingEntity> T get(E entity)
    {
        return (T) this.get(entity.getId());
    }

    public <T extends LivingEntityData<E>, E extends LivingEntity> T getOrMake(IEntityDataFactory<E> dataCreationFunction, E entity)
    {
        final int entityId = entity.getId();

        @SuppressWarnings("unchecked")
        T data = (T) this.get(entityId);

        if (data == null)
        {
            data = (T) dataCreationFunction.createEntityData(entity);
            this.add(entityId, data);
        }
        return data;
    }

    private void add(int identifier, LivingEntityData<?> data)
    {
        this.entryMap.put(identifier, data);
    }

    public void add(Entity entity, LivingEntityData<?> data)
    {
        this.add(entity.getId(), data);
    }

    public void remove(Entity entity)
    {
        if (entity == null)
            return;

        LivingEntityData<?> data = this.entryMap.get(entity.getId());
        if (data == null || data.getEntity() != entity)
            return;

        this.entryMap.remove(entity.getId());
    }

    public void updateClient()
    {
        if (Minecraft.getInstance().level == null) return;

        Iterator<Entry<Integer, LivingEntityData<?>>> it = entryMap.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<Integer, LivingEntityData<?>> entry = it.next();

            LivingEntityData<?> entityData = entry.getValue();
            LivingEntity entityInData = entityData.getEntity();
            Entity entity = Minecraft.getInstance().level.getEntity(entry.getKey());
            if (!PreviewHelper.isPreviewEntity(entityInData) && (entity == null || entityInData != entity))
            {

                it.remove();
            }
            else
            {
                entityData.updateClient();
            }
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
