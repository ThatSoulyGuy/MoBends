package goblinbob.mobends.standard.client.model.armor.cache;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized manager for all armor rendering caches.
 * Handles cache lifecycle, configuration, and statistics.
 */
@OnlyIn(Dist.CLIENT)
public class CacheManager
{
    private static CacheManager instance;

    private final ArmorStructureCache structureCache;
    private final ArmorAssignmentCache assignmentCache;
    private final ArmorGeometryCache geometryCache;

    // Configuration
    private int maxGeometryCacheEntries = 100;
    private long geometryCacheMaxAge = 60000; // 1 minute

    // Performance tracking
    private long totalRenderCalls = 0;
    private long cacheAssistedRenders = 0;

    public CacheManager()
    {
        this.structureCache = new ArmorStructureCache();
        this.assignmentCache = new ArmorAssignmentCache();
        this.geometryCache = new ArmorGeometryCache();

        applyConfiguration();
    }

    /**
     * Get the singleton instance.
     */
    public static CacheManager getInstance()
    {
        if (instance == null)
        {
            instance = new CacheManager();
        }
        return instance;
    }

    /**
     * Reset the singleton instance (for testing or configuration reload).
     */
    public static void resetInstance()
    {
        if (instance != null)
        {
            instance.clearAll();
        }
        instance = null;
    }

    /**
     * Apply current configuration to caches.
     */
    private void applyConfiguration()
    {
        geometryCache.setMaxEntries(maxGeometryCacheEntries);
        geometryCache.setMaxAge(geometryCacheMaxAge);
    }

    /**
     * Get the structure cache.
     */
    public ArmorStructureCache getStructureCache()
    {
        return structureCache;
    }

    /**
     * Get the assignment cache.
     */
    public ArmorAssignmentCache getAssignmentCache()
    {
        return assignmentCache;
    }

    /**
     * Get the geometry cache.
     */
    public ArmorGeometryCache getGeometryCache()
    {
        return geometryCache;
    }

    /**
     * Clear all caches.
     */
    public void clearAll()
    {
        structureCache.clear();
        assignmentCache.clear();
        geometryCache.clear();
        totalRenderCalls = 0;
        cacheAssistedRenders = 0;
    }

    /**
     * Invalidate all caches for a specific model class.
     */
    public void invalidateForModel(Class<?> modelClass)
    {
        structureCache.remove(modelClass);
        assignmentCache.removeForModel(modelClass);
        geometryCache.invalidateForModel(modelClass);
    }

    /**
     * Called when a render uses cached data.
     */
    public void recordCacheAssistedRender()
    {
        totalRenderCalls++;
        cacheAssistedRenders++;
    }

    /**
     * Called when a render does not use cached data.
     */
    public void recordUncachedRender()
    {
        totalRenderCalls++;
    }

    /**
     * Set maximum geometry cache entries.
     */
    public void setMaxGeometryCacheEntries(int max)
    {
        this.maxGeometryCacheEntries = max;
        geometryCache.setMaxEntries(max);
    }

    /**
     * Set geometry cache maximum age in milliseconds.
     */
    public void setGeometryCacheMaxAge(long maxAge)
    {
        this.geometryCacheMaxAge = maxAge;
        geometryCache.setMaxAge(maxAge);
    }

    /**
     * Get total number of cached entries across all caches.
     */
    public int getTotalCachedEntries()
    {
        return structureCache.size() + assignmentCache.size() + geometryCache.size();
    }

    /**
     * Get combined hit rate across all caches.
     */
    public float getCombinedHitRate()
    {
        long totalHits = structureCache.getHits() + assignmentCache.getHits() + geometryCache.getHits();
        long totalMisses = structureCache.getMisses() + assignmentCache.getMisses() + geometryCache.getMisses();
        long total = totalHits + totalMisses;
        return total > 0 ? (float) totalHits / total : 0.0f;
    }

    /**
     * Get the ratio of renders that used cached data.
     */
    public float getCacheAssistedRenderRatio()
    {
        return totalRenderCalls > 0 ? (float) cacheAssistedRenders / totalRenderCalls : 0.0f;
    }

    /**
     * Get comprehensive debug statistics.
     */
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

    /**
     * Get a summary statistics string.
     */
    public String getSummaryStats()
    {
        return String.format("Armor Caches: %d entries, %.1f%% hit rate, %.1f%% renders cached",
            getTotalCachedEntries(),
            getCombinedHitRate() * 100,
            getCacheAssistedRenderRatio() * 100);
    }

    /**
     * Perform maintenance tasks (eviction, cleanup).
     * Should be called periodically (e.g., every few seconds).
     */
    public void performMaintenance()
    {
        // Geometry cache handles its own eviction on put
        // Could add more maintenance tasks here if needed
    }

    /**
     * Called on resource reload to invalidate caches that might be stale.
     */
    public void onResourceReload()
    {
        // Structure and assignment caches should be invalidated
        // as model classes might have changed
        structureCache.clear();
        assignmentCache.clear();
        geometryCache.clear();
    }
}
