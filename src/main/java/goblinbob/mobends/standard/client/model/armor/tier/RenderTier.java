package goblinbob.mobends.standard.client.model.armor.tier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Defines the two rendering tiers for armor.
 * Each tier represents a different approach to applying Mo'Bends transforms to armor.
 */
@OnlyIn(Dist.CLIENT)
public enum RenderTier
{
    /**
     * Tier 1: Transform Injection (~85% of armor)
     * For vanilla and standard modded armor using HumanoidModel.
     * Uses a proxy model to intercept copyPropertiesFrom() and inject our transforms.
     * Most efficient approach with the best visual results.
     */
    TIER_1_TRANSFORM_INJECTION(1, "Transform Injection"),

    /**
     * Tier 2: Model Interception (~15% of armor)
     * For modded armor using ModelPart but not extending HumanoidModel.
     * Uses MC API to find parts by standard limb names and applies vertex capture + joint slicing.
     * Fallback for non-HumanoidModel armor.
     */
    TIER_2_MODEL_INTERCEPTION(2, "Model Interception");

    private final int tierNumber;
    private final String displayName;

    RenderTier(int tierNumber, String displayName)
    {
        this.tierNumber = tierNumber;
        this.displayName = displayName;
    }

    public int getTierNumber()
    {
        return tierNumber;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Returns true if this tier should attempt to slice limb geometry at joints.
     * Both tiers slice geometry - Tier 1 for HumanoidModel, Tier 2 for other models.
     */
    public boolean requiresGeometrySlicing()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return "Tier " + tierNumber + " (" + displayName + ")";
    }
}
