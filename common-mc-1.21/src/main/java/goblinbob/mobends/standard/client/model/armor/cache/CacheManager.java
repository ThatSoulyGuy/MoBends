package goblinbob.mobends.standard.client.model.armor.cache;

import java.util.ArrayList;
import java.util.List;

public class CacheManager
{
    private static CacheManager instance;

    private final ArmorStructureCache structureCache;
    private final ArmorAssignmentCache assignmentCache;
    private final ArmorGeometryCache geometryCache;

    private int maxGeometryCacheEntries = 100;
    private long geometryCacheMaxAge = 60000;

    private long totalRenderCalls = 0;
    private long cacheAssistedRenders = 0;

    public CacheManager()
    {
        this.structureCache = new ArmorStructureCache();
        this.assignmentCache = new ArmorAssignmentCache();
        this.geometryCache = new ArmorGeometryCache();

        applyConfiguration();
    }

    public static CacheManager getInstance()
    {
        if (instance == null)
        {
            instance = new CacheManager();
        }
        return instance;
    }

    public static void resetInstance()
    {
        if (instance != null)
        {
            instance.clearAll();
        }
        instance = null;
    }

    private void applyConfiguration()
    {
        geometryCache.setMaxEntries(maxGeometryCacheEntries);
        geometryCache.setMaxAge(geometryCacheMaxAge);
    }

    public ArmorStructureCache getStructureCache()
    {
        return structureCache;
    }

    public ArmorAssignmentCache getAssignmentCache()
    {
        return assignmentCache;
    }

    public ArmorGeometryCache getGeometryCache()
    {
        return geometryCache;
    }

    public void clearAll()
    {
        structureCache.clear();
        assignmentCache.clear();
        geometryCache.clear();
        totalRenderCalls = 0;
        cacheAssistedRenders = 0;
    }

    public void invalidateForModel(Class<?> modelClass)
    {
        structureCache.remove(modelClass);
        assignmentCache.removeForModel(modelClass);
        geometryCache.invalidateForModel(modelClass);
    }

    public void recordCacheAssistedRender()
    {
        totalRenderCalls++;
        cacheAssistedRenders++;
    }

    public void recordUncachedRender()
    {
        totalRenderCalls++;
    }

    public void setMaxGeometryCacheEntries(int max)
    {
        this.maxGeometryCacheEntries = max;
        geometryCache.setMaxEntries(max);
    }

    public void setGeometryCacheMaxAge(long maxAge)
    {
        this.geometryCacheMaxAge = maxAge;
        geometryCache.setMaxAge(maxAge);
    }

    public int getTotalCachedEntries()
    {
        return structureCache.size() + assignmentCache.size() + geometryCache.size();
    }

    public float getCombinedHitRate()
    {
        long totalHits = structureCache.getHits() + assignmentCache.getHits() + geometryCache.getHits();
        long totalMisses = structureCache.getMisses() + assignmentCache.getMisses() + geometryCache.getMisses();
        long total = totalHits + totalMisses;
        return total > 0 ? (float) totalHits / total : 0.0f;
    }

    public float getCacheAssistedRenderRatio()
    {
        return totalRenderCalls > 0 ? (float) cacheAssistedRenders / totalRenderCalls : 0.0f;
    }

    public List<String> getDetailedStats()
    {
        List<String> stats = new ArrayList<>();
        stats.add("=== Armor Cache Statistics ===");
        stats.add(structureCache.getStats());
        stats.add(assignmentCache.getStats());
        stats.add(geometryCache.getStats());
        stats.add(String.format("Total entries: %d", getTotalCachedEntries()));
        stats.add(String.format("Combined hit rate: %.1f%%", getCombinedHitRate() * 100));
        stats.add(String.format("Cache-assisted renders: %d/%d (%.1f%%)",
            cacheAssistedRenders, totalRenderCalls, getCacheAssistedRenderRatio() * 100));
        return stats;
    }

    public String getSummaryStats()
    {
        return String.format("Armor Caches: %d entries, %.1f%% hit rate, %.1f%% renders cached",
            getTotalCachedEntries(),
            getCombinedHitRate() * 100,
            getCacheAssistedRenderRatio() * 100);
    }

    public void performMaintenance()
    {
    }

    public void onResourceReload()
    {
        structureCache.clear();
        assignmentCache.clear();
        geometryCache.clear();
    }
}
