package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import goblinbob.mobends.api.addon.AddonAnimationRegistry;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.QuaternionUtils;
import goblinbob.mobends.standard.data.SpiderData;
import goblinbob.mobends.standard.mutators.SpiderOverhaulMutator;
import goblinbob.mobends.standard.previewer.SpiderPreviewer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

public final class SpiderOverhaulCompat
{
    private static final String MOD_ID = "spider_overhaul";
    private static final String MODEL_PACKAGE = "dev.chybx.spideroverhaul.entity.";
    private static final String WRAPPER_PACKAGE = "dev.chybx.spideroverhaul.client.render.";

    private static final String[] SPECIES = {
            "birch", "cavern", "desert", "ice", "jungle", "mushroom", "savanna", "sculk", "swamp", "taiga"
    };

    private static final String[] PARTS = {
            "head", "body", "neck", "leg1", "leg2", "leg3", "leg4", "leg5", "leg6", "leg7", "leg8",
            "foreLeg1", "foreLeg2", "foreLeg3", "foreLeg4", "foreLeg5", "foreLeg6", "foreLeg7", "foreLeg8"
    };

    private static final float VANILLA_SEGMENT = 12.0F;
    private static final float VANILLA_HIP_DROP = 7.0F;
    private static final float GROUND_Y = 24.0F;

    private static final Map<Object, Rig> RIGS = new WeakHashMap<>();
    private static final float[] scratch = new float[3];

    private SpiderOverhaulCompat()
    {
    }

    public static void register(AddonAnimationRegistry registry, String[] animations)
    {
        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        for (String species : SPECIES)
        {
            final String name = species.substring(0, 1).toUpperCase(java.util.Locale.ROOT) + species.substring(1);

            try
            {
                @SuppressWarnings("unchecked")
                final Class<Spider> entityClass = (Class<Spider>) Class.forName(MODEL_PACKAGE + name + "SpiderEntity")
                        .asSubclass(Spider.class);
                registry.registerNewEntity(MOD_ID + ":" + species + "_spider",
                        "entity." + MOD_ID + "." + species + "_spider", entityClass,
                        SpiderData::new, SpiderOverhaulMutator::new,
                        new MutatedRenderer<>(), new SpiderPreviewer(), animations, PARTS);
            }
            catch (Throwable ignored)
            {
            }
        }
    }

    public static boolean isModelWrapper(Object model)
    {
        final String name = model.getClass().getName();
        return name.startsWith(WRAPPER_PACKAGE) && name.endsWith("SpiderModelWrapper");
    }

    public static void poseModel(Object model)
    {
        if (!(model instanceof HierarchicalModel<?> hierarchical) || !model.getClass().getName().startsWith(MODEL_PACKAGE))
        {
            return;
        }

        final LivingEntity entity = MoBendsRenderContext.getCurrentEntity();
        if (entity == null || !BenderHelper.isEntityAnimated(entity)
                || !((Object) EntityDatabase.instance.get(entity) instanceof SpiderData data) || data.limbs == null)
        {
            return;
        }

        final Rig rig = RIGS.computeIfAbsent(model, key -> Rig.of(hierarchical.root()));
        if (rig == null)
        {
            return;
        }

        rig.resetGroups();

        for (int i = 0; i < 8 && i < data.limbs.length; ++i)
        {
            final Leg leg = rig.legs[i];
            if (leg != null)
            {
                leg.pose(data.limbs[i]);
            }
        }
    }

    private static final class Rig
    {
        private final ModelPart[] groups;
        private final Leg[] legs = new Leg[8];

        private Rig(ModelPart... groups)
        {
            this.groups = groups;
        }

        @Nullable
        static Rig of(ModelPart root)
        {
            final ModelPart spider = child(root, "spider");
            final ModelPart left = child(spider, "limbsL", "limbs_l");
            final ModelPart right = child(spider, "limbsR", "limbs_r");
            if (spider == null || left == null || right == null)
            {
                return null;
            }

            final Rig rig = new Rig(spider, left, right);
            for (int i = 0; i < 8; ++i)
            {
                final boolean odd = i % 2 == 1;
                final int number = 4 - i / 2;
                final ModelPart group = odd ? left : right;
                final ModelPart upper = odd ? child(group, "L" + number, "l" + number) : child(group, "R" + number, "r" + number);
                if (upper != null)
                {
                    rig.legs[i] = Leg.of(spider, group, upper, odd);
                }
            }
            return rig;
        }

        void resetGroups()
        {
            for (ModelPart group : groups)
            {
                group.resetPose();
            }
        }
    }

    private static final class Leg
    {
        private final ModelPart upper;
        @Nullable
        private final ModelPart lower;
        private final boolean left;
        private final float upperLength;
        private final float lowerLength;
        private final float drop;

        private Leg(ModelPart upper, @Nullable ModelPart lower, boolean left, float upperLength, float lowerLength, float drop)
        {
            this.upper = upper;
            this.lower = lower;
            this.left = left;
            this.upperLength = upperLength;
            this.lowerLength = lowerLength;
            this.drop = drop;
        }

        static Leg of(ModelPart spider, ModelPart group, ModelPart upper, boolean left)
        {
            final float hipY = spider.getInitialPose().y + group.getInitialPose().y + upper.getInitialPose().y;
            final ModelPart lower = upper.getAllParts().skip(1).findFirst().orElse(null);

            if (lower == null)
            {
                final float[] bounds = ownBounds(upper, left);
                return new Leg(upper, null, left, bounds[0], 0.0F, GROUND_Y - hipY - bounds[1]);
            }

            final PartPose knee = lower.getInitialPose();
            final float[] bounds = ownBounds(lower, left);
            return new Leg(upper, lower, left, Math.abs(knee.x), bounds[0],
                    GROUND_Y - hipY - knee.y - bounds[1]);
        }

        void pose(SpiderData.Limb limb)
        {
            final float side = left ? 1.0F : -1.0F;
            final Quaternion upperRotation = limb.upperPart.rotation.getSmooth();
            final Quaternion lowerRotation = limb.lowerPart.rotation.getSmooth();

            final float[] shin = QuaternionUtils.rotateVector(lowerRotation, side * VANILLA_SEGMENT, 0.0F, 0.0F, scratch);
            final float[] foot = QuaternionUtils.rotateVector(upperRotation,
                    side * VANILLA_SEGMENT + shin[0], shin[1], shin[2], scratch);

            final float scale = (upperLength + lowerLength) / (VANILLA_SEGMENT * 2.0F);
            final Vector3f target = new Vector3f(foot[0] * scale, (foot[1] - VANILLA_HIP_DROP) * scale + drop, foot[2] * scale);

            if (lower == null)
            {
                aim(upper, target.normalize(), left);
                return;
            }

            final float horizontal = (float) Math.sqrt(target.x * target.x + target.z * target.z);
            final float dirX = horizontal > 1.0E-4F ? target.x / horizontal : side;
            final float dirZ = horizontal > 1.0E-4F ? target.z / horizontal : 0.0F;
            final float reach = Math.max(Math.abs(upperLength - lowerLength) + 1.0E-3F,
                    Math.min(target.length(), upperLength + lowerLength - 1.0E-3F));
            final float slope = (float) Math.atan2(target.y, horizontal);
            final float hipBend = (float) Math.acos(clamp((upperLength * upperLength + reach * reach - lowerLength * lowerLength)
                    / (2.0F * upperLength * reach)));
            final float kneeBend = (float) Math.acos(clamp((lowerLength * lowerLength + reach * reach - upperLength * upperLength)
                    / (2.0F * lowerLength * reach)));

            final Vector3f thigh = direction(dirX, dirZ, slope - hipBend);
            final Vector3f calf = direction(dirX, dirZ, slope + kneeBend);

            aim(upper, thigh, left);
            new Quaternionf().rotationZYX(upper.zRot, upper.yRot, upper.xRot).conjugate().transform(calf);
            aim(lower, calf, left);
        }

        private static Vector3f direction(float dirX, float dirZ, float pitch)
        {
            final float cos = (float) Math.cos(pitch);
            return new Vector3f(dirX * cos, (float) Math.sin(pitch), dirZ * cos);
        }

        private static void aim(ModelPart part, Vector3f direction, boolean left)
        {
            final float sign = left ? 1.0F : -1.0F;
            part.xRot = 0.0F;
            part.yRot = (float) Math.asin(clamp(-sign * direction.z));
            part.zRot = (float) Math.atan2(sign * direction.y, sign * direction.x);
        }

        private static float[] ownBounds(ModelPart part, boolean left)
        {
            final float[] bounds = {0.0F, 0.0F};
            part.visit(new PoseStack(), (pose, path, index, cube) -> {
                if (path.isEmpty())
                {
                    bounds[0] = Math.max(bounds[0], left ? cube.maxX : -cube.minX);
                    bounds[1] = Math.max(bounds[1], cube.maxY);
                }
            });
            return bounds;
        }
    }

    private static float clamp(float value)
    {
        return Math.max(-1.0F, Math.min(1.0F, value));
    }

    @Nullable
    private static ModelPart child(@Nullable ModelPart parent, String... names)
    {
        if (parent == null)
        {
            return null;
        }
        for (String name : names)
        {
            if (parent.hasChild(name))
            {
                return parent.getChild(name);
            }
        }
        return null;
    }
}
