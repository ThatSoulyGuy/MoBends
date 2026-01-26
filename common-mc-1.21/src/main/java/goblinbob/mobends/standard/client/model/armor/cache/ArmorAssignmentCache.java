package goblinbob.mobends.standard.client.model.armor.cache;

import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import net.minecraft.world.entity.EquipmentSlot;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches vertex-to-bone assignments for armor models.
 * Key: (ModelClass, BodyPart/Slot) -> List of bone assignments per vertex
 */
public class ArmorAssignmentCache
{
    /**
     * Key for cache lookups, combining model class and equipment slot.
     */
    public static final class CacheKey
    {
        private final Class<?> modelClass;
        private final EquipmentSlot slot;
        private final int hashCode;

        public CacheKey(Class<?> modelClass, EquipmentSlot slot)
        {
            this.modelClass = modelClass;
            this.slot = slot;
            this.hashCode = Objects.hash(modelClass, slot);
        }

        public Class<?> getModelClass()
        {
            return modelClass;
        }

        public EquipmentSlot getSlot()
        {
            return slot;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return modelClass.equals(cacheKey.modelClass) && slot == cacheKey.slot;
        }

        @Override
        public int hashCode()
        {
            return hashCode;
        }
    }

    /**
     * Holds cached bone assignments for a specific armor piece.
     */
    public static class AssignmentEntry
    {
        private final CacheKey key;
        private final Map<BoneRegion, List<Integer>> vertexIndicesByBone;
        private final BoneRegion[] vertexBones;
        private final int totalVertices;
        private final long creationTime;

        public AssignmentEntry(CacheKey key,
                              Map<BoneRegion, List<Integer>> vertexIndicesByBone,
                              BoneRegion[] vertexBones)
        {
            this.key = key;
            this.vertexIndicesByBone = new EnumMap<>(BoneRegion.class);
            this.vertexIndicesByBone.putAll(vertexIndicesByBone);
            this.vertexBones = vertexBones.clone();
            this.totalVertices = vertexBones.length;
            this.creationTime = System.currentTimeMillis();
        }

        public CacheKey getKey()
        {
            return key;
        }

        /**
         * Get all vertex indices assigned to a specific bone.
         */
        @Nullable
        public List<Integer> getVerticesForBone(BoneRegion bone)
        {
            return vertexIndicesByBone.get(bone);
        }

        /**
         * Get the bone assignment for a specific vertex index.
         */
        public BoneRegion getBoneForVertex(int vertexIndex)
        {
            if (vertexIndex < 0 || vertexIndex >= vertexBones.length)
            {
                return BoneRegion.ROOT;
            }
            return vertexBones[vertexIndex];
        }

        /**
         * Get all vertex bone assignments.
         */
        public BoneRegion[] getVertexBones()
        {
            return vertexBones;
        }

        public int getTotalVertices()
        {
            return totalVertices;
        }

        public long getCreationTime()
        {
            return creationTime;
        }

        /**
         * Get the set of bone regions that have assigned vertices.
         */
        public java.util.Set<BoneRegion> getUsedBones()
        {
            return vertexIndicesByBone.keySet();
        }
    }

    // Cache storage
    private final ConcurrentHashMap<CacheKey, AssignmentEntry> cache = new ConcurrentHashMap<>();

    // Statistics
    private long hits = 0;
    private long misses = 0;

    /**
     * Get cached bone assignments for a model/slot combination.
     *
     * @param modelClass The armor model class
     * @param slot The equipment slot
     * @return Cached entry, or null if not cached
     */
    @Nullable
    public AssignmentEntry get(Class<?> modelClass, EquipmentSlot slot)
    {
        return get(new CacheKey(modelClass, slot));
    }

    /**
     * Get cached bone assignments by key.
     */
    @Nullable
    public AssignmentEntry get(CacheKey key)
    {
        AssignmentEntry entry = cache.get(key);
        if (entry != null)
        {
            hits++;
        }
        else
        {
            misses++;
        }
        return entry;
    }

    /**
     * Cache bone assignments for a model/slot combination.
     *
     * @param modelClass The armor model class
     * @param slot The equipment slot
     * @param vertexIndicesByBone Map of bone regions to their assigned vertex indices
     * @param vertexBones Array of bone assignments indexed by vertex index
     * @return The created cache entry
     */
    public AssignmentEntry put(Class<?> modelClass, EquipmentSlot slot,
                               Map<BoneRegion, List<Integer>> vertexIndicesByBone,
                               BoneRegion[] vertexBones)
    {
        CacheKey key = new CacheKey(modelClass, slot);
        AssignmentEntry entry = new AssignmentEntry(key, vertexIndicesByBone, vertexBones);
        cache.put(key, entry);
        return entry;
    }

    /**
     * Check if assignments are cached for a model/slot combination.
     */
    public boolean contains(Class<?> modelClass, EquipmentSlot slot)
    {
        return cache.containsKey(new CacheKey(modelClass, slot));
    }

    /**
     * Remove cached assignments for a model/slot combination.
     */
    public void remove(Class<?> modelClass, EquipmentSlot slot)
    {
        cache.remove(new CacheKey(modelClass, slot));
    }

    /**
     * Remove all cached assignments for a model class (all slots).
     */
    public void removeForModel(Class<?> modelClass)
    {
        cache.keySet().removeIf(key -> key.getModelClass().equals(modelClass));
    }

    /**
     * Clear all cached entries.
     */
    public void clear()
    {
        cache.clear();
        hits = 0;
        misses = 0;
    }

    /**
     * Get the number of cached entries.
     */
    public int size()
    {
        return cache.size();
    }

    /**
     * Get cache hit count.
     */
    public long getHits()
    {
        return hits;
    }

    /**
     * Get cache miss count.
     */
    public long getMisses()
    {
        return misses;
    }

    /**
     * Get cache hit rate.
     */
    public float getHitRate()
    {
        long total = hits + misses;
        return total > 0 ? (float) hits / total : 0.0f;
    }

    /**
     * Get debug statistics.
     */
    public String getStats()
    {
        return String.format("ArmorAssignmentCache: %d entries, %d hits, %d misses (%.1f%% hit rate)",
            size(), hits, misses, getHitRate() * 100);
    }
}
