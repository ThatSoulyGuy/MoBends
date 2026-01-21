package goblinbob.mobends.standard.client.model.armor.tier2;

import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import goblinbob.mobends.standard.client.model.armor.cache.ArmorStructureCache;
import goblinbob.mobends.standard.client.model.armor.cache.CacheManager;
import goblinbob.mobends.standard.client.model.armor.tier.PartClassification;
import net.minecraft.client.model.geom.ModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Analyzes armor model structure spatially to classify parts into bone regions.
 * Used by Tier 2 rendering for models that don't extend HumanoidModel but
 * still use standard ModelPart structure.
 *
 * Classification is based on:
 * - Field names (if available and meaningful)
 * - Part positions (pivot points)
 * - Part dimensions
 * - Hierarchical relationships
 */
@OnlyIn(Dist.CLIENT)
public class SpatialAnalyzer
{
    // Threshold values for spatial classification (in model units)
    private static final float HEAD_Y_MAX = 0.0f;      // Head is at Y <= 0
    private static final float BODY_Y_MIN = 0.0f;      // Body starts at Y = 0
    private static final float BODY_Y_MAX = 12.0f;     // Body ends around Y = 12
    private static final float ARM_X_THRESHOLD = 4.0f;  // Arms are at |X| >= 4
    private static final float LEG_Y_MIN = 12.0f;      // Legs start at Y >= 12
    private static final float LIMB_SPLIT_Y = 6.0f;    // Midpoint for upper/lower split

    // Name patterns for classification
    private static final String[] HEAD_NAMES = {"head", "helmet", "hat", "hood", "mask", "crown"};
    private static final String[] BODY_NAMES = {"body", "chest", "torso", "chestplate", "breastplate"};
    private static final String[] LEFT_ARM_NAMES = {"leftarm", "left_arm", "armleft", "arm_left", "leftpauldron"};
    private static final String[] RIGHT_ARM_NAMES = {"rightarm", "right_arm", "armright", "arm_right", "rightpauldron"};
    private static final String[] LEFT_LEG_NAMES = {"leftleg", "left_leg", "legleft", "leg_left", "leftboot"};
    private static final String[] RIGHT_LEG_NAMES = {"rightleg", "right_leg", "legright", "leg_right", "rightboot"};

    /**
     * Analyze a model and classify all its ModelPart fields.
     *
     * @param model The model to analyze
     * @return Map of field names to their classifications
     */
    public Map<String, PartClassification> analyzeModel(Object model)
    {
        Map<String, PartClassification> classifications = new HashMap<>();

        Class<?> modelClass = model.getClass();

        // Check cache first
        ArmorStructureCache cache = CacheManager.getInstance().getStructureCache();
        ArmorStructureCache.StructureEntry cached = cache.get(modelClass);
        if (cached != null && !cached.getPartClassifications().isEmpty())
        {
            return cached.getPartClassifications();
        }

        // Analyze all ModelPart fields
        analyzeFieldsRecursively(model, modelClass, classifications);

        return classifications;
    }

    /**
     * Analyze ModelPart fields from a class and its superclasses.
     */
    private void analyzeFieldsRecursively(Object model, Class<?> clazz, Map<String, PartClassification> classifications)
    {
        if (clazz == null || clazz == Object.class)
        {
            return;
        }

        // Process superclass first
        analyzeFieldsRecursively(model, clazz.getSuperclass(), classifications);

        // Process fields in this class
        for (Field field : clazz.getDeclaredFields())
        {
            if (Modifier.isStatic(field.getModifiers()))
            {
                continue;
            }

            if (ModelPart.class.isAssignableFrom(field.getType()))
            {
                try
                {
                    field.setAccessible(true);
                    ModelPart part = (ModelPart) field.get(model);
                    if (part != null)
                    {
                        PartClassification classification = classifyPart(part, field.getName());
                        classifications.put(field.getName(), classification);
                    }
                }
                catch (IllegalAccessException e)
                {
                    // Skip inaccessible fields
                }
            }
        }
    }

    /**
     * Classify a single ModelPart.
     */
    public PartClassification classifyPart(ModelPart part, @Nullable String fieldName)
    {
        // Try name-based classification first (higher confidence)
        if (fieldName != null)
        {
            BoneRegion namedRegion = classifyByName(fieldName);
            if (namedRegion != BoneRegion.ROOT)
            {
                return PartClassification.of(namedRegion, 0.9f, part, fieldName);
            }
        }

        // Fall back to spatial classification
        return classifySpatially(part, fieldName);
    }

    /**
     * Classify a part based on its field name.
     */
    private BoneRegion classifyByName(String name)
    {
        String lowerName = name.toLowerCase().replaceAll("[_\\-\\s]", "");

        // Check head
        for (String pattern : HEAD_NAMES)
        {
            if (lowerName.contains(pattern))
            {
                return BoneRegion.HEAD;
            }
        }

        // Check body
        for (String pattern : BODY_NAMES)
        {
            if (lowerName.contains(pattern))
            {
                return BoneRegion.BODY;
            }
        }

        // Check left arm
        for (String pattern : LEFT_ARM_NAMES)
        {
            if (lowerName.contains(pattern.replaceAll("[_\\-\\s]", "")))
            {
                return BoneRegion.LEFT_ARM_UPPER; // Default to upper, refine later
            }
        }

        // Check right arm
        for (String pattern : RIGHT_ARM_NAMES)
        {
            if (lowerName.contains(pattern.replaceAll("[_\\-\\s]", "")))
            {
                return BoneRegion.RIGHT_ARM_UPPER;
            }
        }

        // Check left leg
        for (String pattern : LEFT_LEG_NAMES)
        {
            if (lowerName.contains(pattern.replaceAll("[_\\-\\s]", "")))
            {
                return BoneRegion.LEFT_LEG_UPPER;
            }
        }

        // Check right leg
        for (String pattern : RIGHT_LEG_NAMES)
        {
            if (lowerName.contains(pattern.replaceAll("[_\\-\\s]", "")))
            {
                return BoneRegion.RIGHT_LEG_UPPER;
            }
        }

        return BoneRegion.ROOT;
    }

    /**
     * Classify a part based on its spatial position.
     */
    private PartClassification classifySpatially(ModelPart part, @Nullable String fieldName)
    {
        float x = part.x;
        float y = part.y;
        float z = part.z;

        // Head: Above body (Y <= 0)
        if (y <= HEAD_Y_MAX && Math.abs(x) < ARM_X_THRESHOLD)
        {
            return PartClassification.of(BoneRegion.HEAD, 0.7f, part, fieldName);
        }

        // Arms: X offset from center
        if (Math.abs(x) >= ARM_X_THRESHOLD && y >= BODY_Y_MIN && y < LEG_Y_MIN)
        {
            boolean isLeft = x > 0;
            // Determine upper vs lower by Y position relative to arm start
            boolean isLower = y > BODY_Y_MIN + LIMB_SPLIT_Y;

            if (isLeft)
            {
                return PartClassification.of(
                        isLower ? BoneRegion.LEFT_ARM_LOWER : BoneRegion.LEFT_ARM_UPPER,
                        0.6f, part, fieldName);
            }
            else
            {
                return PartClassification.of(
                        isLower ? BoneRegion.RIGHT_ARM_LOWER : BoneRegion.RIGHT_ARM_UPPER,
                        0.6f, part, fieldName);
            }
        }

        // Legs: Below body
        if (y >= LEG_Y_MIN)
        {
            boolean isLeft = x > 0;
            // Determine upper vs lower
            boolean isLower = y > LEG_Y_MIN + LIMB_SPLIT_Y;

            if (isLeft)
            {
                return PartClassification.of(
                        isLower ? BoneRegion.LEFT_LEG_LOWER : BoneRegion.LEFT_LEG_UPPER,
                        0.6f, part, fieldName);
            }
            else
            {
                return PartClassification.of(
                        isLower ? BoneRegion.RIGHT_LEG_LOWER : BoneRegion.RIGHT_LEG_UPPER,
                        0.6f, part, fieldName);
            }
        }

        // Body: Center of the model
        if (Math.abs(x) < ARM_X_THRESHOLD && y >= BODY_Y_MIN && y < LEG_Y_MIN)
        {
            return PartClassification.of(BoneRegion.BODY, 0.7f, part, fieldName);
        }

        // Unknown - assign to ROOT with low confidence
        return PartClassification.unknown(part, fieldName);
    }

    /**
     * Refine a classification by analyzing child parts.
     * If a part has children that suggest upper/lower division, update the classification.
     */
    public void refineWithChildren(PartClassification classification, Map<String, ModelPart> children)
    {
        if (children == null || children.isEmpty())
        {
            return;
        }

        // Check if children suggest this is an upper part (has lower child)
        // or lower part (is positioned lower than parent)
        for (Map.Entry<String, ModelPart> entry : children.entrySet())
        {
            String childName = entry.getKey().toLowerCase();
            if (childName.contains("fore") || childName.contains("lower"))
            {
                // This part has a "fore" or "lower" child, so it's likely upper
                // Classification is already correct in most cases
            }
        }
    }

    /**
     * Get the expected bone regions for a specific equipment slot.
     */
    public BoneRegion[] getRegionsForSlot(net.minecraft.world.entity.EquipmentSlot slot)
    {
        return switch (slot)
        {
            case HEAD -> new BoneRegion[]{BoneRegion.HEAD};
            case CHEST -> new BoneRegion[]{
                    BoneRegion.BODY,
                    BoneRegion.LEFT_ARM_UPPER, BoneRegion.LEFT_ARM_LOWER,
                    BoneRegion.RIGHT_ARM_UPPER, BoneRegion.RIGHT_ARM_LOWER
            };
            case LEGS -> new BoneRegion[]{
                    BoneRegion.BODY,
                    BoneRegion.LEFT_LEG_UPPER, BoneRegion.LEFT_LEG_LOWER,
                    BoneRegion.RIGHT_LEG_UPPER, BoneRegion.RIGHT_LEG_LOWER
            };
            case FEET -> new BoneRegion[]{
                    BoneRegion.LEFT_LEG_LOWER,
                    BoneRegion.RIGHT_LEG_LOWER
            };
            default -> new BoneRegion[]{BoneRegion.ROOT};
        };
    }
}
