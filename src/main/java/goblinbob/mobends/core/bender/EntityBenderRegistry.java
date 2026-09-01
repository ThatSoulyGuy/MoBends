package goblinbob.mobends.core.bender;

import goblinbob.mobends.api.animation.MoBendsAnimationControl;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

public class EntityBenderRegistry
{

    public static final EntityBenderRegistry instance = new EntityBenderRegistry();

    private final Map<Class<? extends LivingEntity>, EntityBender<?>> entityClassToBenderMap = new HashMap<>();

    private final Map<Class<?>, EntityBender<?>> resolvedBenderCache = new HashMap<>();

    public void registerBender(EntityBender<?> entityBender)
    {
        entityClassToBenderMap.put(entityBender.entityClass, entityBender);

        clearCache();
    }

    public void applyConfiguration(CoreClientConfig config)
    {
        for (EntityBender<?> entityBender : entityClassToBenderMap.values())
        {
            entityBender.setAnimate(config.isEntityAnimated(entityBender.getKey()));
        }
    }

    public void setAnimateForKey(String key, boolean animate)
    {
        for (EntityBender<?> entityBender : entityClassToBenderMap.values())
        {
            if (entityBender.getKey().equals(key))
            {
                entityBender.setAnimate(animate);
            }
        }
    }

    public Collection<EntityBender<?>> getRegistered()
    {
        return entityClassToBenderMap.values();
    }

    public Collection<EntityBender<?>> getRegistered(Filter filter)
    {
        final Map<String, EntityBender<?>> uniqueByKey = new LinkedHashMap<>();
        for (EntityBender<?> entityBender : entityClassToBenderMap.values())
        {
            uniqueByKey.putIfAbsent(entityBender.getKey(), entityBender);
        }

        List<EntityBender<?>> benderList = new ArrayList<>(uniqueByKey.values());

        if (filter.query != null)
        {
            benderList.removeIf(bender -> !bender.getUnlocalizedName().toLowerCase().contains(filter.query.toLowerCase()));
        }

        benderList.sort(Comparator.comparing(EntityBender::getKey));

        return benderList;
    }

    public <E extends LivingEntity> EntityBender<E> getForEntityClass(Class<E> c)
    {
        return (EntityBender<E>) entityClassToBenderMap.get(c);
    }

    public boolean hasBenderForClass(Class<?> c)
    {
        return entityClassToBenderMap.containsKey(c);
    }

    @SuppressWarnings("unchecked")
    public <E extends LivingEntity> EntityBender<E> getForEntity(E entity)
    {
        if (MoBendsAnimationControl.isExcluded(entity))
        {
            return null;
        }

        final Class<?> entityClass = entity.getClass();

        if (resolvedBenderCache.containsKey(entityClass))
        {
            return (EntityBender<E>) resolvedBenderCache.get(entityClass);
        }

        EntityBender<?> resolved = resolveBenderFor(entity);
        resolvedBenderCache.put(entityClass, resolved);
        return (EntityBender<E>) resolved;
    }

    private EntityBender<?> resolveBenderFor(LivingEntity entity)
    {
        Class<?> entityClass = entity.getClass();

        for (EntityBender<?> entityBender : entityClassToBenderMap.values())
            if (entityBender.entityClass.equals(entityClass))
                return entityBender;

        for (EntityBender<?> entityBender : entityClassToBenderMap.values())
            if (entityBender.entityClass.isInstance(entity))
                return entityBender;

        return null;
    }

    public void clearCache()
    {
        resolvedBenderCache.clear();
    }

    public void refreshMutators()
    {
        clearCache();

        for (EntityBender<?> entityBender : entityClassToBenderMap.values())
            entityBender.refreshMutation();
    }

    public static class Filter
    {
        public boolean ascending = false;
        public SortingKey sortingKey = SortingKey.NAME;
        public String query = null;

        public enum SortingKey
        {
            NAME,
        }
    }

}
