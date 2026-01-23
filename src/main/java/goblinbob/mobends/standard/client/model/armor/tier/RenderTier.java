package goblinbob.mobends.standard.client.model.armor.tier;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Defines the two rendering tiers for armor.
 * Each tier represents a different approach to applying Mo'Bends transforms to armor.
 */
@OnlyIn(Dist.CLIENT)
public enum RenderTier
{
    /**
     * Tier 1: Standard HumanoidModel armor
     * For vanilla and standard modded armor using HumanoidModel.
     * Uses direct ModelPart manipulation with joint slicing at elbows/knees.
     * Handles the standard 2-layer armor model structure.
     */
    TIER_1_STANDARD("Standard HumanoidModel"),

    /**
     * Tier 2: Custom Model armor
     * For modded armor using custom 3D models (not standard HumanoidModel structure).
     * Uses spatial analysis to classify body parts and apply transforms.
     * Handles exotic armor with custom geometry.
     */
    TIER_2_CUSTOM_MODEL("Custom Model");

    private final String displayName;

    RenderTier(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Returns true if this is standard HumanoidModel armor.
     */
    public boolean isStandardModel()
    {
        return this == TIER_1_STANDARD;
    }

    /**
     * Returns true if this is custom model armor.
     */
    public boolean isCustomModel()
    {
        return this == TIER_2_CUSTOM_MODEL;
    }

    @Override
    public String toString()
    {
        return displayName;
    }
}
