package goblinbob.mobends.standard.client.model.armor.tier;

import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the result of spatial analysis for a model part.
 * Used by Tier 2 rendering to map arbitrary ModelParts to bone regions.
 * Immutable after construction.
 */
public class PartClassification
{
    /**
     * The bone region this part was classified as.
     */
    private final BoneRegion boneRegion;

    /**
     * Confidence score of the classification (0.0 to 1.0).
     * Higher values indicate more certainty in the classification.
     */
    private final float confidence;

    /**
     * The original ModelPart that was classified.
     */
    private final ModelPart modelPart;

    /**
     * Classifications for child parts, keyed by child name.
     */
    private final Map<String, PartClassification> childClassifications;

    /**
     * The name this part was found under (may be null if root).
     */
    @Nullable
    private final String partName;

    private PartClassification(BoneRegion boneRegion,
                               float confidence,
                               ModelPart modelPart,
                               @Nullable String partName,
                               Map<String, PartClassification> childClassifications)
    {
        this.boneRegion = boneRegion;
        this.confidence = Math.max(0.0f, Math.min(1.0f, confidence));
        this.modelPart = modelPart;
        this.partName = partName;
        this.childClassifications = Collections.unmodifiableMap(new HashMap<>(childClassifications));
    }

    /**
     * Create a classification with no children.
     */
    public static PartClassification of(BoneRegion boneRegion, float confidence, ModelPart modelPart, @Nullable String partName)
    {
        return new PartClassification(boneRegion, confidence, modelPart, partName, Collections.emptyMap());
    }

    /**
     * Create a classification with child classifications.
     */
    public static PartClassification of(BoneRegion boneRegion, float confidence, ModelPart modelPart, @Nullable String partName, Map<String, PartClassification> children)
    {
        return new PartClassification(boneRegion, confidence, modelPart, partName, children);
    }

    /**
     * Create an unclassified/unknown part (falls back to ROOT).
     */
    public static PartClassification unknown(ModelPart modelPart, @Nullable String partName)
    {
        return new PartClassification(BoneRegion.ROOT, 0.0f, modelPart, partName, Collections.emptyMap());
    }

    public BoneRegion getBoneRegion()
    {
        return boneRegion;
    }

    public float getConfidence()
    {
        return confidence;
    }

    public ModelPart getModelPart()
    {
        return modelPart;
    }

    @Nullable
    public String getPartName()
    {
        return partName;
    }

    public Map<String, PartClassification> getChildClassifications()
    {
        return childClassifications;
    }

    /**
     * Returns true if this classification has high confidence (>= 0.8).
     */
    public boolean isHighConfidence()
    {
        return confidence >= 0.8f;
    }

    /**
     * Returns true if this classification has moderate or better confidence (>= 0.5).
     */
    public boolean isModerateConfidence()
    {
        return confidence >= 0.5f;
    }

    /**
     * Returns true if this is a limb bone (arm or leg, upper or lower).
     */
    public boolean isLimbBone()
    {
        return switch (boneRegion)
        {
            case LEFT_ARM_UPPER, LEFT_ARM_LOWER,
                 RIGHT_ARM_UPPER, RIGHT_ARM_LOWER,
                 LEFT_LEG_UPPER, LEFT_LEG_LOWER,
                 RIGHT_LEG_UPPER, RIGHT_LEG_LOWER -> true;
            default -> false;
        };
    }

    /**
     * Returns true if this is an upper limb segment (upper arm or upper leg).
     */
    public boolean isUpperLimbSegment()
    {
        return switch (boneRegion)
        {
            case LEFT_ARM_UPPER, RIGHT_ARM_UPPER,
                 LEFT_LEG_UPPER, RIGHT_LEG_UPPER -> true;
            default -> false;
        };
    }

    /**
     * Returns true if this is a lower limb segment (forearm or lower leg).
     */
    public boolean isLowerLimbSegment()
    {
        return switch (boneRegion)
        {
            case LEFT_ARM_LOWER, RIGHT_ARM_LOWER,
                 LEFT_LEG_LOWER, RIGHT_LEG_LOWER -> true;
            default -> false;
        };
    }

    /**
     * Get the child classification for a specific child name.
     * @return The child classification, or null if not found
     */
    @Nullable
    public PartClassification getChildClassification(String childName)
    {
        return childClassifications.get(childName);
    }

    @Override
    public String toString()
    {
        return String.format("PartClassification{region=%s, confidence=%.2f, part=%s, children=%d}",
            boneRegion, confidence, partName, childClassifications.size());
    }
}
