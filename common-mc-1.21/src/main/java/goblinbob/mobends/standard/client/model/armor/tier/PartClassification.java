package goblinbob.mobends.standard.client.model.armor.tier;

import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PartClassification
{
    private final BoneRegion boneRegion;

    private final float confidence;

    private final ModelPart modelPart;

    private final Map<String, PartClassification> childClassifications;

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

    public static PartClassification of(BoneRegion boneRegion, float confidence, ModelPart modelPart, @Nullable String partName)
    {
        return new PartClassification(boneRegion, confidence, modelPart, partName, Collections.emptyMap());
    }

    public static PartClassification of(BoneRegion boneRegion, float confidence, ModelPart modelPart, @Nullable String partName, Map<String, PartClassification> children)
    {
        return new PartClassification(boneRegion, confidence, modelPart, partName, children);
    }

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

    public boolean isHighConfidence()
    {
        return confidence >= 0.8f;
    }

    public boolean isModerateConfidence()
    {
        return confidence >= 0.5f;
    }

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

    public boolean isUpperLimbSegment()
    {
        return switch (boneRegion)
        {
            case LEFT_ARM_UPPER, RIGHT_ARM_UPPER,
                 LEFT_LEG_UPPER, RIGHT_LEG_UPPER -> true;
            default -> false;
        };
    }

    public boolean isLowerLimbSegment()
    {
        return switch (boneRegion)
        {
            case LEFT_ARM_LOWER, RIGHT_ARM_LOWER,
                 LEFT_LEG_LOWER, RIGHT_LEG_LOWER -> true;
            default -> false;
        };
    }

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
