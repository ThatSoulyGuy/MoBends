package goblinbob.mobends.standard.client.model.armor.tier;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Defines the three rendering tiers for armor.
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
     * Tier 2: Model Interception (~10% of armor)
     * For modded armor using ModelPart but not extending HumanoidModel.
     * Uses spatial analysis to classify body parts and apply transforms.
     * Good balance of compatibility and performance.
     */
    TIER_2_MODEL_INTERCEPTION(2, "Model Interception"),

    /**
     * Tier 3: Vertex Capture Fallback (~5% of armor)
     * For exotic renderers with completely custom geometry.
     * Captures vertices during render, inverse-transforms to rest pose,
     * then re-renders with bone transforms.
     * Most compatible but least efficient.
     */
    TIER_3_VERTEX_CAPTURE(3, "Vertex Capture");

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
     * Tier 1 and 2 use pre-analyzed model structure, Tier 3 captures and slices vertices.
     */
    public boolean requiresGeometrySlicing()
    {
        return this != TIER_1_TRANSFORM_INJECTION;
    }

    @Override
    public String toString()
    {
        return "Tier " + tierNumber + " (" + displayName + ")";
    }
}
