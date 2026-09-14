package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

public final class LegendsModCompat
{
    public static final int HEAD = 1;
    public static final int BODY = 1 << 1;
    public static final int RIGHT_ARM = 1 << 2;
    public static final int LEFT_ARM = 1 << 3;
    public static final int RIGHT_LEG = 1 << 4;
    public static final int LEFT_LEG = 1 << 5;
    public static final int ALL_PARTS = HEAD | BODY | RIGHT_ARM | LEFT_ARM | RIGHT_LEG | LEFT_LEG;

    private static final int FULL_BODY_PARTS = BODY | RIGHT_LEG | LEFT_LEG;
    private static final int UPPER_BODY_PARTS = HEAD | RIGHT_ARM | LEFT_ARM;

    private static final String MOD_ID = "legendsmod";
    private static final String ARMOR_ITEM_CLASS = "com.tihyo.legends.armors.LegendsArmorItem";
    private static final String SUIT_CLASS = "com.tihyo.legends.armors.LegendsSuit";

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private static final long SAMPLE_WINDOW_NANOS = 150_000_000L;
    private static final float ROTATION_EPSILON = 1.0e-4F;
    private static final float POSITION_EPSILON = 1.0e-3F;
    private static final int PART_COUNT = 6;
    private static final int VALUES_PER_PART = 6;
    private static final int MAX_SUPPRESSION_DEPTH = 8;

    private static boolean initialized = false;
    private static boolean isLoaded = false;
    private static Class<?> armorItemClass;
    private static Method getSuit;
    private static Method shouldRenderCustomModel;

    private static LivingEntity setupEntity;
    private static HumanoidModel<?> setupModel;
    private static final float[] setupSnapshot = new float[PART_COUNT * VALUES_PER_PART];

    private static int suppressionDepth;
    private static final int[] suppressionMasks = new int[MAX_SUPPRESSION_DEPTH];
    private static final float[][] suppressionSnapshots = new float[MAX_SUPPRESSION_DEPTH][PART_COUNT * VALUES_PER_PART];

    private static final Map<LivingEntity, Sample> samples = new WeakHashMap<>();
    private static final Map<LivingEntity, Long> fullBodyPoses = new WeakHashMap<>();
    private static final Map<LivingEntity, Boolean> externalPoseFrames = new WeakHashMap<>();

    private LegendsModCompat()
    {
    }

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);
        if (!isLoaded)
        {
            return;
        }

        try
        {
            armorItemClass = Class.forName(ARMOR_ITEM_CLASS);

            Class<?> suitClass = Class.forName(SUIT_CLASS);
            getSuit = suitClass.getMethod("getSuit", LivingEntity.class);
            shouldRenderCustomModel = suitClass.getMethod("shouldRenderCustomModel", LivingEntity.class);
        }
        catch (Throwable t)
        {
            armorItemClass = null;
            getSuit = null;
            shouldRenderCustomModel = null;
        }
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    public static boolean isWearingSuit(LivingEntity entity)
    {
        if (!isModLoaded() || armorItemClass == null || entity == null)
        {
            return false;
        }

        for (EquipmentSlot slot : ARMOR_SLOTS)
        {
            final ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty() && armorItemClass.isInstance(stack.getItem()))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean usesCustomModel(LivingEntity entity)
    {
        if (getSuit == null || shouldRenderCustomModel == null || !isWearingSuit(entity))
        {
            return false;
        }

        try
        {
            final Object suit = getSuit.invoke(null, entity);
            return suit != null && Boolean.TRUE.equals(shouldRenderCustomModel.invoke(suit, entity));
        }
        catch (Throwable t)
        {
            return false;
        }
    }

    public static void beginSetupAnimation(LivingEntity entity, Object model)
    {
        suppressionDepth = 0;
        setupEntity = null;
        setupModel = null;

        if (!isModLoaded() || entity == null || !(model instanceof HumanoidModel<?> humanoidModel))
        {
            return;
        }

        if (!BenderHelper.isEntityAnimated(entity) || ModCompatManager.shouldDeferAnimation(entity))
        {
            return;
        }

        setupEntity = entity;
        setupModel = humanoidModel;
        capture(humanoidModel, setupSnapshot);
    }

    public static void endSetupAnimation(LivingEntity entity, Object model)
    {
        final HumanoidModel<?> humanoidModel = setupModel;
        final LivingEntity posedEntity = setupEntity;

        setupEntity = null;
        setupModel = null;
        suppressionDepth = 0;

        if (humanoidModel == null || entity == null || posedEntity != entity || model != humanoidModel)
        {
            return;
        }

        final int mask = changedParts(humanoidModel, setupSnapshot);
        final long now = System.nanoTime();

        Sample sample = samples.get(entity);
        if (sample == null)
        {
            sample = new Sample();
            samples.put(entity, sample);
        }
        sample.mask = mask;
        sample.time = now;
        sample.consumed = false;

        if ((mask & FULL_BODY_PARTS) != 0)
        {
            fullBodyPoses.put(entity, now);
        }
        else
        {
            fullBodyPoses.remove(entity);
        }
    }

    public static void beginSuppressedWrites(int partMask)
    {
        if (suppressionDepth < MAX_SUPPRESSION_DEPTH)
        {
            final HumanoidModel<?> model = setupModel;
            final int mask = model != null ? partMask : 0;

            suppressionMasks[suppressionDepth] = mask;
            if (mask != 0)
            {
                capture(model, suppressionSnapshots[suppressionDepth]);
            }
        }

        suppressionDepth++;
    }

    public static void endSuppressedWrites()
    {
        if (suppressionDepth <= 0)
        {
            suppressionDepth = 0;
            return;
        }

        suppressionDepth--;

        if (suppressionDepth >= MAX_SUPPRESSION_DEPTH)
        {
            return;
        }

        final int mask = suppressionMasks[suppressionDepth];
        final HumanoidModel<?> model = setupModel;

        if (mask != 0 && model != null)
        {
            restore(model, suppressionSnapshots[suppressionDepth], mask);
        }
    }

    public static boolean isPosingModel(LivingEntity entity)
    {
        if (!isLoaded || entity == null)
        {
            return false;
        }

        final Long posedAt = fullBodyPoses.get(entity);
        final boolean posing = posedAt != null && System.nanoTime() - posedAt <= SAMPLE_WINDOW_NANOS;

        if (posing)
        {
            externalPoseFrames.put(entity, Boolean.TRUE);
        }

        return posing;
    }

    public static void applyPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (!isLoaded || entity == null || mutator == null || !(vanillaModel instanceof PlayerModel<?>))
        {
            return;
        }

        final boolean externalPose = externalPoseFrames.remove(entity) != null;

        int mask = 0;
        final Sample sample = samples.get(entity);
        if (sample != null && !sample.consumed && System.nanoTime() - sample.time <= SAMPLE_WINDOW_NANOS)
        {
            mask = sample.mask;
            sample.consumed = true;
        }

        if (externalPose || (mask & FULL_BODY_PARTS) != 0)
        {
            mutator.adoptPoseFromVanillaModel(vanillaModel, null, null);
            return;
        }

        if ((mask & UPPER_BODY_PARTS) == 0)
        {
            return;
        }

        mutator.adoptUpperBodyFromVanillaModel(vanillaModel,
                (mask & HEAD) != 0,
                (mask & LEFT_ARM) != 0,
                (mask & RIGHT_ARM) != 0);
    }

    private static ModelPart[] partsOf(HumanoidModel<?> model)
    {
        return new ModelPart[]{
                model.head, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg
        };
    }

    private static void capture(HumanoidModel<?> model, float[] target)
    {
        final ModelPart[] parts = partsOf(model);

        for (int i = 0; i < parts.length; ++i)
        {
            final ModelPart part = parts[i];
            final int offset = i * VALUES_PER_PART;

            if (part == null)
            {
                for (int j = 0; j < VALUES_PER_PART; ++j)
                {
                    target[offset + j] = 0.0F;
                }
                continue;
            }

            target[offset] = part.x;
            target[offset + 1] = part.y;
            target[offset + 2] = part.z;
            target[offset + 3] = part.xRot;
            target[offset + 4] = part.yRot;
            target[offset + 5] = part.zRot;
        }
    }

    private static void restore(HumanoidModel<?> model, float[] source, int mask)
    {
        final ModelPart[] parts = partsOf(model);

        for (int i = 0; i < parts.length; ++i)
        {
            final ModelPart part = parts[i];
            if (part == null || (mask & (1 << i)) == 0)
            {
                continue;
            }

            final int offset = i * VALUES_PER_PART;

            part.x = source[offset];
            part.y = source[offset + 1];
            part.z = source[offset + 2];
            part.xRot = source[offset + 3];
            part.yRot = source[offset + 4];
            part.zRot = source[offset + 5];
        }
    }

    private static int changedParts(HumanoidModel<?> model, float[] reference)
    {
        final ModelPart[] parts = partsOf(model);
        int mask = 0;

        for (int i = 0; i < parts.length; ++i)
        {
            final ModelPart part = parts[i];
            if (part == null)
            {
                continue;
            }

            final int offset = i * VALUES_PER_PART;

            if (Math.abs(part.xRot - reference[offset + 3]) > ROTATION_EPSILON
                    || Math.abs(part.yRot - reference[offset + 4]) > ROTATION_EPSILON
                    || Math.abs(part.zRot - reference[offset + 5]) > ROTATION_EPSILON
                    || Math.abs(part.x - reference[offset]) > POSITION_EPSILON
                    || Math.abs(part.y - reference[offset + 1]) > POSITION_EPSILON
                    || Math.abs(part.z - reference[offset + 2]) > POSITION_EPSILON)
            {
                mask |= 1 << i;
            }
        }

        return mask;
    }

    private static final class Sample
    {
        private int mask;
        private long time;
        private boolean consumed;
    }
}
