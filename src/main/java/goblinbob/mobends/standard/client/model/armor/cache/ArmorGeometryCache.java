package goblinbob.mobends.standard.client.model.armor.cache;

import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import goblinbob.mobends.standard.client.model.armor.SliceResult;
import net.minecraft.world.entity.EquipmentSlot;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ArmorGeometryCache
{
    public static final class GeometryKey
    {
        private final Class<?> modelClass;
        private final EquipmentSlot slot;
        private final int modelStateHash;
        private final int hashCode;

        public GeometryKey(Class<?> modelClass, EquipmentSlot slot, int modelStateHash)
        {
            this.modelClass = modelClass;
            this.slot = slot;
            this.modelStateHash = modelStateHash;
            this.hashCode = Objects.hash(modelClass, slot, modelStateHash);
        }

        public Class<?> getModelClass()
        {
            return modelClass;
        }

        public EquipmentSlot getSlot()
        {
            return slot;
        }

        public int getModelStateHash()
        {
            return modelStateHash;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GeometryKey that = (GeometryKey) o;
            return modelStateHash == that.modelStateHash &&
                   modelClass.equals(that.modelClass) &&
                   slot == that.slot;
        }

        @Override
        public int hashCode()
        {
            return hashCode;
        }
    }

    public static class GeometryEntry
    {
        private final GeometryKey key;
        private final Map<BoneRegion, List<SliceResult.SlicedVertex>> verticesByBone;
        private final List<SliceResult> sliceResults;
        private final int totalTriangles;
        private final long creationTime;
        private long lastAccessTime;
        private int accessCount;

        public GeometryEntry(GeometryKey key,
                            Map<BoneRegion, List<SliceResult.SlicedVertex>> verticesByBone,
                            List<SliceResult> sliceResults)
        {
            this.key = key;
            this.verticesByBone = new EnumMap<>(BoneRegion.class);
            this.verticesByBone.putAll(verticesByBone);
            this.sliceResults = List.copyOf(sliceResults);
            this.totalTriangles = countTriangles(sliceResults);
            this.creationTime = System.currentTimeMillis();
            this.lastAccessTime = this.creationTime;
            this.accessCount = 0;
        }

        private int countTriangles(List<SliceResult> results)
        {
            int count = 0;
            for (SliceResult result : results)
            {
                count += countTrianglesInPolygon(result.getUpperVertices().size());
                count += countTrianglesInPolygon(result.getLowerVertices().size());
            }
            return count;
        }

        private int countTrianglesInPolygon(int vertexCount)
        {
            return Math.max(0, vertexCount - 2);
        }

        public GeometryKey getKey()
        {
            return key;
        }

        @Nullable
        public List<SliceResult.SlicedVertex> getVerticesForBone(BoneRegion bone)
        {
            lastAccessTime = System.currentTimeMillis();
            accessCount++;
            return verticesByBone.get(bone);
        }

        public List<SliceResult> getSliceResults()
        {
            lastAccessTime = System.currentTimeMillis();
            accessCount++;
            return sliceResults;
        }

        public int getTotalTriangles()
        {
            return totalTriangles;
        }

        public long getCreationTime()
        {
            return creationTime;
        }

        public long getLastAccessTime()
        {
            return lastAccessTime;
        }

        public int getAccessCount()
        {
            return accessCount;
        }

        public long getAge()
        {
            return System.currentTimeMillis() - creationTime;
        }

        public long getTimeSinceLastAccess()
        {
            return System.currentTimeMillis() - lastAccessTime;
        }
    }

    private final ConcurrentHashMap<GeometryKey, GeometryEntry> cache = new ConcurrentHashMap<>();

    private int maxEntries = 100;
    private long maxAge = 60000;

    private long hits = 0;
    private long misses = 0;

    @Nullable
    public GeometryEntry get(Class<?> modelClass, EquipmentSlot slot, int modelStateHash)
    {
        return get(new GeometryKey(modelClass, slot, modelStateHash));
    }

    @Nullable
    public GeometryEntry get(GeometryKey key)
    {
        GeometryEntry entry = cache.get(key);
        if (entry != null)
        {
            if (entry.getAge() > maxAge)
            {
                cache.remove(key);
                misses++;
                return null;
            }
            hits++;
            return entry;
        }
        misses++;
        return null;
    }

    public GeometryEntry put(Class<?> modelClass, EquipmentSlot slot, int modelStateHash,
                            Map<BoneRegion, List<SliceResult.SlicedVertex>> verticesByBone,
                            List<SliceResult> sliceResults)
    {
        if (cache.size() >= maxEntries)
        {
            evictOldEntries();
        }

        GeometryKey key = new GeometryKey(modelClass, slot, modelStateHash);
        GeometryEntry entry = new GeometryEntry(key, verticesByBone, sliceResults);
        cache.put(key, entry);
        return entry;
    }

    private void evictOldEntries()
    {
        long now = System.currentTimeMillis();

        cache.entrySet().removeIf(entry -> entry.getValue().getAge() > maxAge);

        while (cache.size() >= maxEntries)
        {
            GeometryKey lruKey = null;
            long lruTime = Long.MAX_VALUE;

            for (Map.Entry<GeometryKey, GeometryEntry> entry : cache.entrySet())
            {
                if (entry.getValue().getLastAccessTime() < lruTime)
                {
                    lruTime = entry.getValue().getLastAccessTime();
                    lruKey = entry.getKey();
                }
            }

            if (lruKey != null)
            {
                cache.remove(lruKey);
            }
            else
            {
                break;
            }
        }
    }

    public boolean contains(Class<?> modelClass, EquipmentSlot slot, int modelStateHash)
    {
        GeometryKey key = new GeometryKey(modelClass, slot, modelStateHash);
        GeometryEntry entry = cache.get(key);
        return entry != null && entry.getAge() <= maxAge;
    }

    public void invalidateForModel(Class<?> modelClass)
    {
        cache.keySet().removeIf(key -> key.getModelClass().equals(modelClass));
    }

    public void clear()
    {
        cache.clear();
        hits = 0;
        misses = 0;
    }

    public void setMaxEntries(int maxEntries)
    {
        this.maxEntries = maxEntries;
    }

    public void setMaxAge(long maxAge)
    {
        this.maxAge = maxAge;
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
        return String.format("ArmorGeometryCache: %d/%d entries, %d hits, %d misses (%.1f%% hit rate)",
            size(), maxEntries, hits, misses, getHitRate() * 100);
    }
}
