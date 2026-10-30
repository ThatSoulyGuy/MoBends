package goblinbob.mobends.compat;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import traben.entity_model_features.EMFAnimationApi;
import traben.entity_model_features.models.animation.state.EMFState;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static goblinbob.mobends.compat.TouchNMotionCompat.BODY;
import static goblinbob.mobends.compat.TouchNMotionCompat.HEAD;
import static goblinbob.mobends.compat.TouchNMotionCompat.LEFT_ARM;
import static goblinbob.mobends.compat.TouchNMotionCompat.PART_COUNT;
import static goblinbob.mobends.compat.TouchNMotionCompat.RIGHT_ARM;

final class TouchNMotionHooks
{
    private static final String TOUCH_N_MOTION_HOOK = "strm.touchnmotion.TouchNMotionHook";
    private static final String[] PART_NAMES = {"head", "body", "left_arm", "right_arm", "left_leg", "right_leg"};
    private static final float WAIST_Y = 12.0F;

    private static final float[] pose = new float[PART_COUNT * 6];
    private static final boolean[] posed = new boolean[PART_COUNT];
    private static final Quaternionf beforeRotation = new Quaternionf();
    private static final Quaternionf afterRotation = new Quaternionf();
    private static final Quaternionf[] bodyRotations = {new Quaternionf(), new Quaternionf()};
    private static final Vector3f[] bodyPivots = {new Vector3f(), new Vector3f()};
    private static final Vector3f before = new Vector3f();
    private static final Vector3f after = new Vector3f();

    private static Player pendingPlayer;
    private static boolean armed;
    private static boolean changed;

    private static final String INERTIA_KEY = "motion.inertia";
    private static final float HELD_WEIGHT = 0.05F;

    private static Method weightMethod;
    private static Method quietsSwingMethod;
    private static Object leftArmEffector;
    private static Object rightArmEffector;
    private static Method configGet;
    private static Field configBooleansField;

    private TouchNMotionHooks()
    {
    }

    static boolean install()
    {
        try
        {
            final Class<?> effector = Class.forName("strm.touchnmotion.interaction.Effector");
            final Class<?> runtime = Class.forName("strm.touchnmotion.interaction.InteractionRuntime");
            weightMethod = runtime.getMethod("weight", UUID.class, effector);
            quietsSwingMethod = runtime.getMethod("quietsSwing", UUID.class);
            leftArmEffector = effector.getField("LEFT_ARM").get(null);
            rightArmEffector = effector.getField("RIGHT_ARM").get(null);
        }
        catch (Throwable e)
        {
            weightMethod = null;
            quietsSwingMethod = null;
        }

        try
        {
            final Class<?> config = Class.forName("strm.emfcompat.core.EMFCompatConfig");
            configGet = config.getMethod("get");
            configBooleansField = config.getField("booleans");
        }
        catch (Throwable e)
        {
            configGet = null;
        }

        final List<EMFAnimationApi.EMFAnimationHook> hooks = EMFState.animationHooks;
        for (int i = 0; i < hooks.size(); i++)
        {
            if (hooks.get(i).getClass().getName().equals(TOUCH_N_MOTION_HOOK))
            {
                hooks.add(i + 1, new After());
                hooks.add(i, new Before());
                return true;
            }
        }

        return false;
    }

    static boolean run(Player player, HumanoidModel<?> model)
    {
        final ModelPart[] parts = {model.head, model.body, model.leftArm, model.rightArm, model.leftLeg, model.rightLeg};
        for (int i = 0; i < PART_COUNT; i++)
        {
            posed[i] = parts[i] != null;
            if (posed[i])
            {
                final ModelPart part = parts[i];
                final int p = i * 6;
                pose[p] = part.x;
                pose[p + 1] = part.y;
                pose[p + 2] = part.z;
                pose[p + 3] = part.xRot;
                pose[p + 4] = part.yRot;
                pose[p + 5] = part.zRot;
            }
        }

        final Map<String, Boolean> config = configBooleans();
        final Boolean inertia = config == null ? null : config.put(INERTIA_KEY, false);

        pendingPlayer = player;
        changed = false;
        try
        {
            EmfSupport.advanceAnimation(model.head);
        }
        finally
        {
            pendingPlayer = null;
            armed = false;

            if (config != null)
            {
                if (inertia == null)
                {
                    config.remove(INERTIA_KEY);
                }
                else
                {
                    config.put(INERTIA_KEY, inertia);
                }
            }
        }

        if (changed)
        {
            TouchNMotionCompat.armWeights[0] = armWeight(player, leftArmEffector);
            TouchNMotionCompat.armWeights[1] = armWeight(player, rightArmEffector);
        }

        return changed;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Boolean> configBooleans()
    {
        if (configGet == null)
        {
            return null;
        }

        try
        {
            return (Map<String, Boolean>) configBooleansField.get(configGet.invoke(null));
        }
        catch (Throwable e)
        {
            configGet = null;
            return null;
        }
    }

    static boolean claimsSwing(Player player)
    {
        if (armWeight(player, leftArmEffector) > HELD_WEIGHT || armWeight(player, rightArmEffector) > HELD_WEIGHT)
        {
            return true;
        }

        if (quietsSwingMethod == null)
        {
            return false;
        }

        try
        {
            return (Boolean) quietsSwingMethod.invoke(null, player.getUUID());
        }
        catch (Throwable e)
        {
            quietsSwingMethod = null;
            return false;
        }
    }

    private static float armWeight(Player player, Object effector)
    {
        if (weightMethod == null)
        {
            return 0.0F;
        }

        try
        {
            return Math.min(1.0F, Math.max(0.0F, (Float) weightMethod.invoke(null, player.getUUID(), effector)));
        }
        catch (Throwable e)
        {
            weightMethod = null;
            return 0.0F;
        }
    }

    private static boolean isPending(EMFAnimationApi.EMFAnimationHook.AnimationContext context)
    {
        return pendingPlayer != null && context.activeState() != null && !context.activeState().isFirstPersonHand()
                && context.animatingModelRoot() != null && context.animatingModelRoot().isMainModel
                && context.activeState().emfEntity() == pendingPlayer;
    }

    private static Quaternionf rotationOf(float xRot, float yRot, float zRot, Quaternionf dest)
    {
        return dest.rotationZYX(zRot, yRot, xRot);
    }

    private static final class Before extends EMFAnimationApi.EMFAnimationHook
    {
        @Override
        public void onAnimationEnd(AnimationContext context, boolean wasCancelledByHook)
        {
            if (!isPending(context))
            {
                return;
            }

            final Map<String, ? extends ModelPart> parts = context.animatingModelRoot().getAllVanillaPartsByNameEMF();
            for (int i = 0; i < PART_COUNT; i++)
            {
                final ModelPart part = parts.get(PART_NAMES[i]);
                posed[i] = posed[i] && part != null;
                if (posed[i])
                {
                    final int p = i * 6;
                    part.setPos(pose[p], pose[p + 1], pose[p + 2]);
                    part.setRotation(pose[p + 3], pose[p + 4], pose[p + 5]);
                }
            }

            armed = true;
        }
    }

    private static final class After extends EMFAnimationApi.EMFAnimationHook
    {
        @Override
        public void onAnimationEnd(AnimationContext context, boolean wasCancelledByHook)
        {
            if (!armed || !isPending(context))
            {
                return;
            }
            armed = false;

            final Map<String, ? extends ModelPart> parts = context.animatingModelRoot().getAllVanillaPartsByNameEMF();
            final ModelPart body = posed[BODY] ? parts.get(PART_NAMES[BODY]) : null;
            if (body != null)
            {
                final int p = BODY * 6;
                rotationOf(pose[p + 3], pose[p + 4], pose[p + 5], bodyRotations[0]);
                bodyPivots[0].set(pose[p], pose[p + 1], pose[p + 2]);
                rotationOf(body.xRot, body.yRot, body.zRot, bodyRotations[1]);
                bodyPivots[1].set(body.x, body.y, body.z);
            }

            for (int i = 0; i < PART_COUNT; i++)
            {
                final ModelPart part = posed[i] ? parts.get(PART_NAMES[i]) : null;
                final int p = i * 6;
                if (part == null)
                {
                    TouchNMotionCompat.rotations[i].set(0.0F, 0.0F, 0.0F, 1.0F);
                    TouchNMotionCompat.offsets[i * 3] = 0.0F;
                    TouchNMotionCompat.offsets[i * 3 + 1] = 0.0F;
                    TouchNMotionCompat.offsets[i * 3 + 2] = 0.0F;
                    continue;
                }

                rotationOf(pose[p + 3], pose[p + 4], pose[p + 5], beforeRotation);
                rotationOf(part.xRot, part.yRot, part.zRot, afterRotation);
                afterRotation.mul(beforeRotation.conjugate());
                TouchNMotionCompat.rotations[i].set(afterRotation.x, afterRotation.y, afterRotation.z, afterRotation.w);

                before.set(pose[p], pose[p + 1], pose[p + 2]);
                after.set(part.x, part.y, part.z);

                if (i == BODY)
                {
                    bodyRotations[0].transform(before.set(0.0F, WAIST_Y, 0.0F)).add(pose[p], pose[p + 1], pose[p + 2]);
                    bodyRotations[1].transform(after.set(0.0F, WAIST_Y, 0.0F)).add(part.x, part.y, part.z);
                }
                else if (i == HEAD || i == LEFT_ARM || i == RIGHT_ARM)
                {
                    if (body == null)
                    {
                        before.zero();
                        after.zero();
                    }
                    else
                    {
                        bodyRotations[0].transformInverse(before.sub(bodyPivots[0]));
                        bodyRotations[1].transformInverse(after.sub(bodyPivots[1]));
                    }
                }

                TouchNMotionCompat.offsets[i * 3] = after.x - before.x;
                TouchNMotionCompat.offsets[i * 3 + 1] = after.y - before.y;
                TouchNMotionCompat.offsets[i * 3 + 2] = after.z - before.z;
            }

            changed = true;
        }
    }
}
