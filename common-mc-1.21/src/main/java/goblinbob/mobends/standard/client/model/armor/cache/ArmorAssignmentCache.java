package goblinbob.mobends.standard.client.model.armor.cache;

import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import net.minecraft.world.entity.EquipmentSlot;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ArmorAssignmentCache
{
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

        @Nullable
        public List<Integer> getVerticesForBone(BoneRegion bone)
        {
            return vertexIndicesByBone.get(bone);
        }

        public BoneRegion getBoneForVertex(int vertexIndex)
        {
            if (vertexIndex < 0 || vertexIndex >= vertexBones.length)
            {
                return BoneRegion.ROOT;
            }
            return vertexBones[vertexIndex];
        }

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

        public java.util.Set<BoneRegion> getUsedBones()
        {
            return vertexIndicesByBone.keySet();
        }
    }

    private final ConcurrentHashMap<CacheKey, AssignmentEntry> cache = new ConcurrentHashMap<>();

    private long hits = 0;
    private long misses = 0;

    @Nullable
    public AssignmentEntry get(Class<?> modelClass, EquipmentSlot slot)
    {
        return get(new CacheKey(modelClass, slot));
    }

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

    public AssignmentEntry put(Class<?> modelClass, EquipmentSlot slot,
                               Map<BoneRegion, List<Integer>> vertexIndicesByBone,
                               BoneRegion[] vertexBones)
    {
        CacheKey key = new CacheKey(modelClass, slot);
        AssignmentEntry entry = new AssignmentEntry(key, vertexIndicesByBone, vertexBones);
        cache.put(key, entry);
        return entry;
    }

    public boolean contains(Class<?> modelClass, EquipmentSlot slot)
    {
        return cache.containsKey(new CacheKey(modelClass, slot));
    }

    public void remove(Class<?> modelClass, EquipmentSlot slot)
    {
        cache.remove(new CacheKey(modelClass, slot));
    }

    public void removeForModel(Class<?> modelClass)
    {
        cache.keySet().removeIf(key -> key.getModelClass().equals(modelClass));
    }

    public void clear()
    {
        cache.clear();
        hits = 0;
        misses = 0;
    }

    public int size()
    {
        return cache.size();
    }

    public long getHits()
    {
        return hits;
    }

    public long getMisses()
    {
        return misses;
    }

    public float getHitRate()
    {
        long total = hits + misses;
        return total > 0 ? (float) hits / total : 0.0f;
    }

    public String getStats()
    {
        return String.format("ArmorAssignmentCache: %d entries, %d hits, %d misses (%.1f%% hit rate)",
            size(), hits, misses, getHitRate() * 100);
    }
}
