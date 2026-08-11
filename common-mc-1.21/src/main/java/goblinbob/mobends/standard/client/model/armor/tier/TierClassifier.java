package goblinbob.mobends.standard.client.model.armor.tier;

import goblinbob.mobends.standard.client.model.armor.cache.ArmorStructureCache;
import goblinbob.mobends.standard.client.model.armor.cache.CacheManager;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class TierClassifier
{
    private static TierClassifier instance;

    private final Set<Class<?>> knownTier1Classes = new HashSet<>();

    private final Set<Class<?>> knownTier2Classes = new HashSet<>();

    private RenderTier forcedTier = null;

    public TierClassifier()
    {
        knownTier1Classes.add(HumanoidModel.class);
    }

    public static TierClassifier getInstance()
    {
        if (instance == null)
        {
            instance = new TierClassifier();
        }
        return instance;
    }

    public RenderTier classify(Model model)
    {
        if (model == null)
        {
            return RenderTier.TIER_2_MODEL_INTERCEPTION;
        }

        if (forcedTier != null)
        {
            return forcedTier;
        }

        Class<?> modelClass = model.getClass();

        ArmorStructureCache structureCache = CacheManager.getInstance().getStructureCache();
        ArmorStructureCache.StructureEntry cached = structureCache.get(modelClass);
        if (cached != null)
        {
            return cached.getRecommendedTier();
        }

        RenderTier tier = classifyUncached(model, modelClass);

        cacheClassification(modelClass, tier);

        return tier;
    }

    public RenderTier classifyClass(Class<?> modelClass)
    {
        ArmorStructureCache structureCache = CacheManager.getInstance().getStructureCache();
        ArmorStructureCache.StructureEntry cached = structureCache.get(modelClass);
        if (cached != null)
        {
            return cached.getRecommendedTier();
        }

        if (knownTier1Classes.contains(modelClass))
        {
            return RenderTier.TIER_1_TRANSFORM_INJECTION;
        }
        if (knownTier2Classes.contains(modelClass))
        {
            return RenderTier.TIER_2_MODEL_INTERCEPTION;
        }

        if (HumanoidModel.class.isAssignableFrom(modelClass))
        {
            knownTier1Classes.add(modelClass);
            return RenderTier.TIER_1_TRANSFORM_INJECTION;
        }

        return RenderTier.TIER_2_MODEL_INTERCEPTION;
    }

    private RenderTier classifyUncached(Model model, Class<?> modelClass)
    {
        if (model instanceof HumanoidModel<?>)
        {
            if (hasHumanoidStructure((HumanoidModel<?>) model))
            {
                knownTier1Classes.add(modelClass);
                return RenderTier.TIER_1_TRANSFORM_INJECTION;
            }
        }

        knownTier2Classes.add(modelClass);
        return RenderTier.TIER_2_MODEL_INTERCEPTION;
    }

    private boolean hasHumanoidStructure(HumanoidModel<?> model)
    {
        try
        {
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
            return false;
        }
    }

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

    public void setForcedTier(@Nullable RenderTier tier)
    {
        this.forcedTier = tier;
    }

    @Nullable
    public RenderTier getForcedTier()
    {
        return forcedTier;
    }

    public void registerTier1Class(Class<?> modelClass)
    {
        knownTier1Classes.add(modelClass);
        knownTier2Classes.remove(modelClass);
    }

    public void registerTier2Class(Class<?> modelClass)
    {
        knownTier2Classes.add(modelClass);
        knownTier1Classes.remove(modelClass);
    }

    public boolean isKnownTier(Class<?> modelClass, RenderTier tier)
    {
        return switch (tier)
        {
            case TIER_1_TRANSFORM_INJECTION -> knownTier1Classes.contains(modelClass);
            case TIER_2_MODEL_INTERCEPTION -> knownTier2Classes.contains(modelClass);
        };
    }

    public void clearClassifications()
    {
        knownTier1Classes.clear();
        knownTier2Classes.clear();

        knownTier1Classes.add(HumanoidModel.class);

        CacheManager.getInstance().getStructureCache().clear();
    }

    public String getStats()
    {
        return String.format("TierClassifier: %d Tier1, %d Tier2 known classes",
            knownTier1Classes.size(), knownTier2Classes.size());
    }
}
