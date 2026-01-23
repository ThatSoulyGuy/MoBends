package goblinbob.mobends.standard.client.model.armor.tier;

import goblinbob.mobends.standard.client.model.armor.cache.ArmorStructureCache;
import goblinbob.mobends.standard.client.model.armor.cache.CacheManager;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Determines which rendering tier to use for a given armor model.
 *
 * Classification logic:
 * - Tier 1: Model extends HumanoidModel (or is a known vanilla-compatible subclass)
 * - Tier 2: All other models (uses MC API to find parts by standard limb names)
 *
 * Results are cached for performance.
 */
@OnlyIn(Dist.CLIENT)
public class TierClassifier
{
    // Singleton instance
    private static TierClassifier instance;

    // Known Tier 1 compatible model classes (added dynamically as discovered)
    private final Set<Class<?>> knownTier1Classes = new HashSet<>();

    // Known Tier 2 compatible model classes
    private final Set<Class<?>> knownTier2Classes = new HashSet<>();

    // Configuration: force a specific tier for all models (for debugging)
    private RenderTier forcedTier = null;

    public TierClassifier()
    {
        // Pre-register known vanilla classes as Tier 1
        knownTier1Classes.add(HumanoidModel.class);
    }

    /**
     * Get the singleton instance.
     */
    public static TierClassifier getInstance()
    {
        if (instance == null)
        {
            instance = new TierClassifier();
        }
        return instance;
    }

    /**
     * Classify an armor model and determine the appropriate rendering tier.
     *
     * @param model The armor model to classify
     * @return The recommended rendering tier
     */
    public RenderTier classify(Model model)
    {
        if (model == null)
        {
            return RenderTier.TIER_2_MODEL_INTERCEPTION;
        }

        // Check for forced tier (debugging)
        if (forcedTier != null)
        {
            return forcedTier;
        }

        Class<?> modelClass = model.getClass();

        // Check cache first
        ArmorStructureCache structureCache = CacheManager.getInstance().getStructureCache();
        ArmorStructureCache.StructureEntry cached = structureCache.get(modelClass);
        if (cached != null)
        {
            return cached.getRecommendedTier();
        }

        // Classify the model
        RenderTier tier = classifyUncached(model, modelClass);

        // Cache the result
        cacheClassification(modelClass, tier);

        return tier;
    }

    /**
     * Classify a model class directly (for pre-classification).
     */
    public RenderTier classifyClass(Class<?> modelClass)
    {
        // Check cache
        ArmorStructureCache structureCache = CacheManager.getInstance().getStructureCache();
        ArmorStructureCache.StructureEntry cached = structureCache.get(modelClass);
        if (cached != null)
        {
            return cached.getRecommendedTier();
        }

        // Check known classes
        if (knownTier1Classes.contains(modelClass))
        {
            return RenderTier.TIER_1_TRANSFORM_INJECTION;
        }
        if (knownTier2Classes.contains(modelClass))
        {
            return RenderTier.TIER_2_MODEL_INTERCEPTION;
        }

        // Check inheritance for HumanoidModel
        if (HumanoidModel.class.isAssignableFrom(modelClass))
        {
            knownTier1Classes.add(modelClass);
            return RenderTier.TIER_1_TRANSFORM_INJECTION;
        }

        // Default to Tier 2 for unknown classes
        return RenderTier.TIER_2_MODEL_INTERCEPTION;
    }

    /**
     * Perform classification without using cache.
     */
    private RenderTier classifyUncached(Model model, Class<?> modelClass)
    {
        // Tier 1: Check if model extends HumanoidModel
        if (model instanceof HumanoidModel<?>)
        {
            // Verify it has the expected structure
            if (hasHumanoidStructure((HumanoidModel<?>) model))
            {
                knownTier1Classes.add(modelClass);
                return RenderTier.TIER_1_TRANSFORM_INJECTION;
            }
            // Falls through to Tier 2
        }

        // Tier 2: Fallback for everything else
        knownTier2Classes.add(modelClass);
        return RenderTier.TIER_2_MODEL_INTERCEPTION;
    }

    /**
     * Check if a HumanoidModel has the expected part structure.
     */
    private boolean hasHumanoidStructure(HumanoidModel<?> model)
    {
        try
        {
            // Check for essential parts
            ModelPart head = model.head;
            ModelPart body = model.body;
            ModelPart rightArm = model.rightArm;
            ModelPart leftArm = model.leftArm;
            ModelPart rightLeg = model.rightLeg;
            ModelPart leftLeg = model.leftLeg;

            return head != null && body != null &&
                   rightArm != null && leftArm != null &&
                   rightLeg != null && leftLeg != null;
        }
        catch (Exception e)
        {
            // If we can't access parts, fall back
            return false;
        }
    }

    /**
     * Cache a classification result.
     */
    private void cacheClassification(Class<?> modelClass, RenderTier tier)
    {
        ArmorStructureCache cache = CacheManager.getInstance().getStructureCache();

        if (tier == RenderTier.TIER_1_TRANSFORM_INJECTION && HumanoidModel.class.isAssignableFrom(modelClass))
        {
            @SuppressWarnings("unchecked")
            Class<? extends HumanoidModel<?>> humanoidClass = (Class<? extends HumanoidModel<?>>) modelClass;
            cache.cacheHumanoidModel(humanoidClass);
        }
        else
        {
            // Create a basic structure entry for non-Tier1 models
            ArmorStructureCache.StructureEntry entry = new ArmorStructureCache.StructureEntry(
                modelClass,
                tier,
                java.util.Map.of(),
                java.util.Map.of(),
                HumanoidModel.class.isAssignableFrom(modelClass),
                tier == RenderTier.TIER_1_TRANSFORM_INJECTION
            );
            cache.put(modelClass, entry);
        }
    }

    /**
     * Force all models to use a specific tier (for debugging).
     * Set to null to disable forcing.
     */
    public void setForcedTier(@Nullable RenderTier tier)
    {
        this.forcedTier = tier;
    }

    /**
     * Get the currently forced tier, or null if not forcing.
     */
    @Nullable
    public RenderTier getForcedTier()
    {
        return forcedTier;
    }

    /**
     * Register a model class as known Tier 1 compatible.
     */
    public void registerTier1Class(Class<?> modelClass)
    {
        knownTier1Classes.add(modelClass);
        knownTier2Classes.remove(modelClass);
    }

    /**
     * Register a model class as known Tier 2 compatible.
     */
    public void registerTier2Class(Class<?> modelClass)
    {
        knownTier2Classes.add(modelClass);
        knownTier1Classes.remove(modelClass);
    }

    /**
     * Check if a model class is known to be compatible with a specific tier.
     */
    public boolean isKnownTier(Class<?> modelClass, RenderTier tier)
    {
        return switch (tier)
        {
            case TIER_1_TRANSFORM_INJECTION -> knownTier1Classes.contains(modelClass);
            case TIER_2_MODEL_INTERCEPTION -> knownTier2Classes.contains(modelClass);
        };
    }

    /**
     * Clear all cached classifications and known class lists.
     */
    public void clearClassifications()
    {
        knownTier1Classes.clear();
        knownTier2Classes.clear();

        // Re-add base known classes
        knownTier1Classes.add(HumanoidModel.class);

        CacheManager.getInstance().getStructureCache().clear();
    }

    /**
     * Get statistics about classified models.
     */
    public String getStats()
    {
        return String.format("TierClassifier: %d Tier1, %d Tier2 known classes",
            knownTier1Classes.size(), knownTier2Classes.size());
    }
}
