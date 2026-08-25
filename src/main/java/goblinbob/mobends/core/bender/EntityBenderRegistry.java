package goblinbob.mobends.core.bender;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import org.slf4j.Logger;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

public class EntityBenderRegistry
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final EntityBenderRegistry instance = new EntityBenderRegistry();

    private final Map<Class<? extends LivingEntity>, EntityBender<?>> entityClassToBenderMap = new HashMap<>();

    /**
     * Resolution cache, keyed by entity CLASS.
     *
     * <p>Which bender an entity gets depends only on its class — there is no per-instance input —
     * so caching per entity was both unnecessary and harmful. It kept a strong reference to every
     * entity it saw, and because {@code HashMap.computeIfAbsent} does not store a null result, an
     * entity with no bender was never cached at all: every render call re-ran two linear scans
     * over the whole registry. Between them those two facts meant the cache leaked the entities it
     * did hold and did nothing for the majority that it did not.
     *
     * <p>A null value here means "resolved, and there is no bender" — hence the explicit
     * containsKey/put rather than computeIfAbsent. The map is bounded by the number of distinct
     * living-entity classes, and Class objects are held by their classloader regardless, so
     * nothing here can keep a world alive.
     */
    private final Map<Class<?>, EntityBender<?>> resolvedBenderCache = new HashMap<>();

    public void registerBender(EntityBender<?> entityBender)
    {
        entityClassToBenderMap.put(entityBender.entityClass, entityBender);

        // Registration can happen after entities have already been resolved -- BenderDiscovery
        // adds derived benders mid-session -- and a class cached as "no bender" would otherwise
        // stay that way for good. Registration is rare; a full drop is the cheap, safe answer.
        clearCache();
    }

    public void applyConfiguration(CoreClientConfig config)
    {
        for (EntityBender<?> entityBender : entityClassToBenderMap.values())
        {
            entityBender.setAnimate(config.isEntityAnimated(entityBender.getKey()));
        }
    }

    public Collection<EntityBender<?>> getRegistered()
    {
        return entityClassToBenderMap.values();
    }

    public Collection<EntityBender<?>> getRegistered(Filter filter)
    {
        List<EntityBender<?>> benderList = new ArrayList<>(entityClassToBenderMap.values());

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
        final Class<?> entityClass = entity.getClass();

        // containsKey, not get() != null: a cached null is a real answer ("no bender for this
        // class"), and it is the answer for most entities in a world.
        if (resolvedBenderCache.containsKey(entityClass))
        {
            return (EntityBender<E>) resolvedBenderCache.get(entityClass);
        }

        EntityBender<?> resolved = resolveBenderFor(entity);
        resolvedBenderCache.put(entityClass, resolved);
        return (EntityBender<E>) resolved;
    }

    /** Exact class match wins over an assignable one, so a subclass bender is never shadowed. */
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

    /**
     * Drops every resolution.
     *
     * <p>Needed whenever the set of registered benders changes — {@code BenderDiscovery} adding
     * derived benders, or a mutator refresh — because a class already resolved to "no bender"
     * would otherwise stay that way.
     */
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
