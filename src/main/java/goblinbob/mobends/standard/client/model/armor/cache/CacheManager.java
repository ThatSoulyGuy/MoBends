package goblinbob.mobends.standard.client.model.armor.cache;

import java.util.ArrayList;
import java.util.List;

public class CacheManager
{
    private static CacheManager instance;

    private final ArmorStructureCache structureCache;

    private long totalRenderCalls = 0;
    private long cacheAssistedRenders = 0;

    public CacheManager()
    {
        this.structureCache = new ArmorStructureCache();
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

    public ArmorStructureCache getStructureCache()
    {
        return structureCache;
    }

    public void clearAll()
    {
        structureCache.clear();
        totalRenderCalls = 0;
        cacheAssistedRenders = 0;
    }

    public void invalidateForModel(Class<?> modelClass)
    {
        structureCache.remove(modelClass);
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

    public int getTotalCachedEntries()
    {
        return structureCache.size();
    }

    public float getCombinedHitRate()
    {
        long totalHits = structureCache.getHits();
        long totalMisses = structureCache.getMisses();
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
    }
}
