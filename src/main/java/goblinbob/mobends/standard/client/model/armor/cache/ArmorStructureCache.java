package goblinbob.mobends.standard.client.model.armor.cache;

import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import goblinbob.mobends.standard.client.model.armor.tier.PartClassification;
import goblinbob.mobends.standard.client.model.armor.tier.RenderTier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches analyzed armor model structures.
 * Maps model classes to their structural analysis results.
 * Thread-safe for concurrent access during rendering.
 */
@OnlyIn(Dist.CLIENT)
public class ArmorStructureCache
{
    /**
     * Holds the cached structure analysis for an armor model.
     */
    public static class StructureEntry
    {
        private final Class<?> modelClass;
        private final RenderTier recommendedTier;
        private final Map<String, PartClassification> partClassifications;
        private final Map<String, BoneRegion> partToBoneMap;
        private final boolean isHumanoidModel;
        private final boolean hasStandardStructure;
        private final long creationTime;

        public StructureEntry(Class<?> modelClass,
                             RenderTier recommendedTier,
                             Map<String, PartClassification> partClassifications,
                             Map<String, BoneRegion> partToBoneMap,
                             boolean isHumanoidModel,
                             boolean hasStandardStructure)
        {
            this.modelClass = modelClass;
            this.recommendedTier = recommendedTier;
            this.partClassifications = Map.copyOf(partClassifications);
            this.partToBoneMap = Map.copyOf(partToBoneMap);
            this.isHumanoidModel = isHumanoidModel;
            this.hasStandardStructure = hasStandardStructure;
            this.creationTime = System.currentTimeMillis();
        }

        public Class<?> getModelClass()
        {
            return modelClass;
        }

        public RenderTier getRecommendedTier()
        {
            return recommendedTier;
        }

        public Map<String, PartClassification> getPartClassifications()
        {
            return partClassifications;
        }

        public Map<String, BoneRegion> getPartToBoneMap()
        {
            return partToBoneMap;
        }

        public boolean isHumanoidModel()
        {
            return isHumanoidModel;
        }

        public boolean hasStandardStructure()
        {
            return hasStandardStructure;
        }

        public long getCreationTime()
        {
            return creationTime;
        }

        @Nullable
        public PartClassification getClassification(String partName)
        {
            return partClassifications.get(partName);
        }

        @Nullable
        public BoneRegion getBoneForPart(String partName)
        {
            return partToBoneMap.get(partName);
        }
    }

    // Cache keyed by model class
    private final ConcurrentHashMap<Class<?>, StructureEntry> cache = new ConcurrentHashMap<>();

    // Statistics
    private long hits = 0;
    private long misses = 0;

    /**
     * Get cached structure analysis for a model class.
     *
     * @param modelClass The model class to look up
     * @return Cached entry, or null if not cached
     */
    @Nullable
    public StructureEntry get(Class<?> modelClass)
    {
        StructureEntry entry = cache.get(modelClass);
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
     * Cache structure analysis for a model class.
     *
     * @param modelClass The model class
     * @param entry The structure entry to cache
     */
    public void put(Class<?> modelClass, StructureEntry entry)
    {
        cache.put(modelClass, entry);
    }

    /**
     * Create and cache a structure entry for a HumanoidModel.
     * This is the fast path for Tier 1 models.
     *
     * @param modelClass The HumanoidModel class
     * @return The created structure entry
     */
    public StructureEntry cacheHumanoidModel(Class<? extends HumanoidModel<?>> modelClass)
    {
        Map<String, BoneRegion> partToBone = new HashMap<>();
        partToBone.put("head", BoneRegion.HEAD);
        partToBone.put("hat", BoneRegion.HEAD);
        partToBone.put("body", BoneRegion.BODY);
        partToBone.put("rightArm", BoneRegion.RIGHT_ARM_UPPER);
        partToBone.put("leftArm", BoneRegion.LEFT_ARM_UPPER);
        partToBone.put("rightLeg", BoneRegion.RIGHT_LEG_UPPER);
        partToBone.put("leftLeg", BoneRegion.LEFT_LEG_UPPER);

        StructureEntry entry = new StructureEntry(
            modelClass,
            RenderTier.TIER_1_TRANSFORM_INJECTION,
            Map.of(), // No detailed classifications needed for Tier 1
            partToBone,
            true,
            true
        );

        cache.put(modelClass, entry);
        return entry;
    }

    /**
     * Check if a model class is cached.
     */
    public boolean contains(Class<?> modelClass)
    {
        return cache.containsKey(modelClass);
    }

    /**
     * Remove a model class from the cache.
     */
    public void remove(Class<?> modelClass)
    {
        cache.remove(modelClass);
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
        return String.format("ArmorStructureCache: %d entries, %d hits, %d misses (%.1f%% hit rate)",
            size(), hits, misses, getHitRate() * 100);
    }
}
