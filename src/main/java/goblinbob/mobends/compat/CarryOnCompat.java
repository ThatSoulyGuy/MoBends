package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CarryOnCompat
{
    private static final String MOD_ID = "carryon";

    private static final float RAD_TO_DEG = (float) (180.0 / Math.PI);
    private static final float ARM_ROLL = 0.05F;
    private static final float MODEL_UNIT = 1.0F / 16.0F;
    private static final float CARRY_SCALE = 0.6F;
    private static final float HAND_Y = 5.0F;
    private static final float HAND_Z = -2.0F;
    private static final float BACK_Y = -6.0F;
    private static final float BACK_Z = 7.0F;

    private static final Map<Integer, Boolean> pendingAnchors = new HashMap<>();
    private static final Map<Integer, Anchor> anchors = new HashMap<>();

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Method getCarryDataMethod;
    private static Method isCarryingMethod;
    private static Method isCarryingTypeMethod;
    private static Object carryTypeBlock;

    private static Method getActiveScriptMethod;
    private static Method scriptRenderMethod;
    private static Method renderLeftArmMethod;
    private static Method renderRightArmMethod;
    private static Method rotationLeftArmMethod;
    private static Method rotationRightArmMethod;
    private static Method getVecMethod;

    private static Method getRenderWidthMethod;

    private static Object clientConfig;
    private static Field renderArmsField;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);

        if (isLoaded)
        {
            try
            {
                initReflection();
            }
            catch (Exception e)
            {
                isLoaded = false;
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void initReflection() throws Exception
    {
        Class<?> managerClass = Class.forName("tschipp.carryon.common.carry.CarryOnDataManager");
        Class<?> dataClass = Class.forName("tschipp.carryon.common.carry.CarryOnData");
        Class<?> typeClass = Class.forName("tschipp.carryon.common.carry.CarryOnData$CarryType");

        getCarryDataMethod = managerClass.getMethod("getCarryData", Player.class);
        isCarryingMethod = dataClass.getMethod("isCarrying");
        isCarryingTypeMethod = dataClass.getMethod("isCarrying", typeClass);
        carryTypeBlock = Enum.valueOf((Class<Enum>) typeClass, "BLOCK");

        try
        {
            Class<?> scriptClass = Class.forName("tschipp.carryon.common.scripting.CarryOnScript");
            Class<?> renderClass = Class.forName("tschipp.carryon.common.scripting.CarryOnScript$ScriptRender");
            Class<?> vecClass = Class.forName("tschipp.carryon.common.scripting.Matchables$OptionalVec3");

            getActiveScriptMethod = dataClass.getMethod("getActiveScript");
            scriptRenderMethod = scriptClass.getMethod("scriptRender");
            renderLeftArmMethod = renderClass.getMethod("renderLeftArm");
            renderRightArmMethod = renderClass.getMethod("renderRightArm");
            rotationLeftArmMethod = renderClass.getMethod("renderRotationLeftArm");
            rotationRightArmMethod = renderClass.getMethod("renderRotationRightArm");
            getVecMethod = vecClass.getMethod("getVec", double.class, double.class, double.class);
        }
        catch (Exception e)
        {
            getActiveScriptMethod = null;
        }

        try
        {
            Class<?> helperClass = Class.forName("tschipp.carryon.client.render.CarryRenderHelper");
            getRenderWidthMethod = helperClass.getMethod("getRenderWidth", Player.class);
        }
        catch (Exception e)
        {
            getRenderWidthMethod = null;
        }

        try
        {
            Class<?> constantsClass = Class.forName("tschipp.carryon.Constants");
            clientConfig = constantsClass.getField("CLIENT_CONFIG").get(null);
            renderArmsField = clientConfig.getClass().getField("renderArms");
        }
        catch (Exception e)
        {
            clientConfig = null;
            renderArmsField = null;
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

    private static Object getCarryData(Player player)
    {
        try
        {
            return getCarryDataMethod.invoke(null, player);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static boolean isCarrying(net.minecraft.world.entity.LivingEntity entity)
    {
        if (!isModLoaded() || !(entity instanceof Player player))
        {
            return false;
        }

        try
        {
            Object carry = getCarryData(player);
            if (carry == null)
            {
                return false;
            }
            Boolean carrying = (Boolean) isCarryingMethod.invoke(carry);
            return carrying != null && carrying;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private static boolean shouldRenderArms()
    {
        if (renderArmsField == null)
        {
            return true;
        }

        try
        {
            return renderArmsField.getBoolean(clientConfig);
        }
        catch (Exception e)
        {
            return true;
        }
    }

    private static float getRenderWidth(Player player)
    {
        if (getRenderWidthMethod == null)
        {
            return 1.0F;
        }

        try
        {
            Float width = (Float) getRenderWidthMethod.invoke(null, player);
            return width == null ? 1.0F : width;
        }
        catch (Exception e)
        {
            return 1.0F;
        }
    }

    private static Object getScriptRender(Object carry)
    {
        if (getActiveScriptMethod == null)
        {
            return null;
        }

        try
        {
            Optional<?> script = (Optional<?>) getActiveScriptMethod.invoke(carry);
            if (script == null || script.isEmpty())
            {
                return null;
            }
            return scriptRenderMethod.invoke(script.get());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static boolean applyToPose(BipedEntityData<?> data)
    {
        if (data == null || !isModLoaded())
        {
            return false;
        }

        if (!(data.getEntity() instanceof Player player))
        {
            return false;
        }

        if (!shouldRenderArms())
        {
            return false;
        }

        final Object carry = getCarryData(player);
        if (carry == null)
        {
            return false;
        }

        try
        {
            Boolean carrying = (Boolean) isCarryingMethod.invoke(carry);
            if (carrying == null || !carrying)
            {
                return false;
            }

            if (player.isVisuallySwimming() || player.isFallFlying())
            {
                pendingAnchors.put(player.getId(), Boolean.TRUE);
                return true;
            }

            Boolean block = (Boolean) isCarryingTypeMethod.invoke(carry, carryTypeBlock);
            final boolean isBlock = block != null && block;

            final boolean sneaking = !player.getAbilities().flying && player.isShiftKeyDown() || player.isCrouching();
            final float pitch = 1.0F + (sneaking ? 0.2F : 0.0F) + (isBlock ? 0.0F : 0.3F);
            final float offset = Math.min((getRenderWidth(player) - 1.0F) / 1.5F, 0.2F);

            final Object render = getScriptRender(carry);
            if (render != null)
            {
                Boolean left = (Boolean) renderLeftArmMethod.invoke(render);
                Boolean right = (Boolean) renderRightArmMethod.invoke(render);

                if (right != null && right)
                {
                    Vec3 rot = (Vec3) getVecMethod.invoke(rotationRightArmMethod.invoke(render),
                            (double) -pitch, (double) offset, (double) -ARM_ROLL);
                    applyArm(data.rightArm, data.rightForeArm, (float) rot.x, (float) rot.y, (float) rot.z);
                }

                if (left != null && left)
                {
                    Vec3 rot = (Vec3) getVecMethod.invoke(rotationLeftArmMethod.invoke(render),
                            (double) -pitch, (double) -offset, (double) ARM_ROLL);
                    applyArm(data.leftArm, data.leftForeArm, (float) rot.x, (float) rot.y, (float) rot.z);
                }
            }
            else
            {
                applyArm(data.rightArm, data.rightForeArm, -pitch, offset, -ARM_ROLL);
                applyArm(data.leftArm, data.leftForeArm, -pitch, -offset, ARM_ROLL);
            }

            pendingAnchors.put(player.getId(), Boolean.FALSE);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static void captureAnchor(net.minecraft.world.entity.LivingEntity entity, Object rawMutator)
    {
        if (!(entity instanceof Player player))
        {
            return;
        }

        final Boolean backMounted = pendingAnchors.remove(player.getId());
        if (backMounted == null || !(rawMutator instanceof BipedMutator<?, ?, ?> mutator))
        {
            return;
        }

        final Matrix4f pose = mutator.getRenderAnchorPose();
        final BendsModelPart body = mutator.getBody();
        if (pose == null || body == null)
        {
            return;
        }

        final Vector3f point = backMounted ? backAnchor(body) : handAnchor(mutator, body);

        final Matrix4f frame = new Matrix4f(pose)
                .translate(point.x * MODEL_UNIT, point.y * MODEL_UNIT, point.z * MODEL_UNIT)
                .rotate(quaternionOf(body))
                .scale(1.0F, -1.0F, -1.0F);

        if (net.minecraft.client.Minecraft.getInstance().options.getCameraType().isMirrored())
        {
            frame.rotateY((float) Math.PI);
        }

        anchors.put(player.getId(), new Anchor(
                frame.getTranslation(new Vector3f()),
                new Matrix4f(frame).normalize3x3().getNormalizedRotation(new Quaternionf())));
    }

    public static void applyAnchor(Player player, PoseStack poseStack)
    {
        if (player == null || poseStack == null)
        {
            return;
        }

        final Anchor anchor = anchors.remove(player.getId());
        if (anchor == null)
        {
            return;
        }

        poseStack.last().pose().translationRotateScale(
                anchor.position.x, anchor.position.y, anchor.position.z,
                anchor.rotation.x, anchor.rotation.y, anchor.rotation.z, anchor.rotation.w,
                CARRY_SCALE);

        poseStack.last().normal().rotation(anchor.rotation);
    }

    private record Anchor(Vector3f position, Quaternionf rotation) {}

    private static Vector3f backAnchor(BendsModelPart body)
    {
        final Vector3f point = pivotOf(body);
        point.add(quaternionOf(body).transform(new Vector3f(0.0F, BACK_Y, BACK_Z)));
        return point;
    }

    private static Vector3f handAnchor(BipedMutator<?, ?, ?> mutator, BendsModelPart body)
    {
        final Vector3f anchor = new Vector3f();

        for (int i = 0; i < 2; ++i)
        {
            final BendsModelPart arm = i == 0 ? mutator.getRightArm() : mutator.getLeftArm();
            final BendsModelPart foreArm = i == 0 ? mutator.getRightForeArm() : mutator.getLeftForeArm();
            if (arm == null || foreArm == null)
            {
                return backAnchor(body);
            }

            final Quaternionf chain = quaternionOf(body);
            final Vector3f point = pivotOf(body);

            advance(point, chain, arm);
            advance(point, chain, foreArm);
            point.add(chain.transform(new Vector3f(0.0F, HAND_Y, HAND_Z)));

            anchor.add(point);
        }

        return anchor.mul(0.5F);
    }

    private static void advance(Vector3f point, Quaternionf chain, BendsModelPart part)
    {
        point.add(chain.transform(pivotOf(part)));
        chain.mul(quaternionOf(part));
    }

    private static Vector3f pivotOf(BendsModelPart part)
    {
        return new Vector3f(part.position.x + part.offset.x,
                part.position.y + part.offset.y,
                part.position.z + part.offset.z);
    }

    private static Quaternionf quaternionOf(BendsModelPart part)
    {
        final Quaternion rotation = part.rotation.getSmooth();
        return new Quaternionf(rotation.x, rotation.y, rotation.z, rotation.w);
    }

    private static void applyArm(ModelPartTransform arm, ModelPartTransform foreArm,
                                 float x, float y, float z)
    {
        if (arm == null)
        {
            return;
        }

        setRotation(arm.rotation, x * RAD_TO_DEG, y * RAD_TO_DEG, z * RAD_TO_DEG);

        if (foreArm != null)
        {
            foreArm.rotation.orientInstantX(0.0F);
        }
    }

    private static void setRotation(SmoothOrientation rotation, float x, float y, float z)
    {
        rotation.orientInstantX(x).rotateInstantY(y).rotateInstantZ(z);
    }
}
