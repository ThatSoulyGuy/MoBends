package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.platform.Platform;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.standard.client.model.armor.CapturedVertex;
import goblinbob.mobends.standard.client.model.armor.CapturingVertexConsumer;
import goblinbob.mobends.standard.client.model.armor.LimbBending;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ChromewareCompat
{
    private static final String MOD_ID = "chromeware";
    public static final String PACKAGE = "com.chromeware.";
    private static final String LAYER_PREFIX = "com.chromeware.client.";
    private static final String LAYER_SUFFIX = "Renderer$Layer";
    private static final String[] SPLIT_LAYERS = {"CyberLimbRenderer$Layer", "GorillaArmRenderer$Layer"};

    private static boolean initialized = false;
    private static Method ruleForMethod;
    private static Method hiddenMethod;
    private static Method mantisCutoutMethod;
    private static Method launcherCutoutMethod;
    private static Method lockedMethod;
    private static Method bladeRenderingMethod;
    private static Method headFollowMethod;
    private static Method launcherRenderingMethod;

    private static final Map<RenderType, LimbCapture> CAPTURES = new LinkedHashMap<>();
    private static final MultiBufferSource CAPTURE_SOURCE = type -> CAPTURES.computeIfAbsent(type, key -> new LimbCapture());
    private static final List<CapturedVertex> SEGMENT = new ArrayList<>();

    private static HumanoidModel<?> capturing;
    private static ModelPart currentLimb;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        try
        {
            final Class<?> ruleClass = Class.forName("com.chromeware.client.body.LimbRenderContext$Rule");
            hiddenMethod = ruleClass.getMethod("hidden");
            mantisCutoutMethod = ruleClass.getMethod("mantisCutout");
            launcherCutoutMethod = ruleClass.getMethod("launcherCutout");
            ruleForMethod = Class.forName("com.chromeware.client.body.LimbRenderContext")
                    .getMethod("ruleFor", ModelPart.class);
        }
        catch (Throwable e)
        {
            ruleForMethod = null;
            return;
        }

        try
        {
            lockedMethod = Class.forName("com.chromeware.cyberware.HardwareHands")
                    .getMethod("locked", Player.class, InteractionHand.class);
        }
        catch (Throwable e)
        {
            lockedMethod = null;
        }

        try
        {
            final Class<?> mantis = Class.forName("com.chromeware.client.mantis.MantisAnimation");
            bladeRenderingMethod = mantis.getMethod("rendering", AbstractClientPlayer.class, HumanoidArm.class);
            headFollowMethod = mantis.getMethod("headFollowWeight", AbstractClientPlayer.class, float.class);
        }
        catch (Throwable e)
        {
            headFollowMethod = null;
        }

        try
        {
            launcherRenderingMethod = Class.forName("com.chromeware.client.launcher.LauncherAnimation")
                    .getMethod("rendering", AbstractClientPlayer.class, HumanoidArm.class);
        }
        catch (Throwable e)
        {
            launcherRenderingMethod = null;
        }
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return ruleForMethod != null;
    }

    public static boolean isLimbLayer(Object layer)
    {
        if (!isModLoaded())
        {
            return false;
        }
        final String name = layer.getClass().getName();
        return name.startsWith(LAYER_PREFIX) && name.endsWith(LAYER_SUFFIX);
    }

    public static boolean splitsLimbs(Object layer)
    {
        final String name = layer.getClass().getName();
        for (String split : SPLIT_LAYERS)
        {
            if (name.endsWith(split))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isHandLocked(LivingEntity entity, HumanoidArm arm)
    {
        if (lockedMethod == null || !(entity instanceof Player player))
        {
            return false;
        }

        try
        {
            final InteractionHand hand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            return (boolean) lockedMethod.invoke(null, player, hand);
        }
        catch (Throwable e)
        {
            lockedMethod = null;
            return false;
        }
    }

    public static void followHead(BipedEntityData<?> data)
    {
        if (!(data.getEntity() instanceof AbstractClientPlayer player))
        {
            return;
        }

        final float yaw = data.getHeadYaw() * Mth.DEG_TO_RAD;
        final float pitch = data.getHeadPitch() * Mth.DEG_TO_RAD;

        if (headFollowMethod != null)
        {
            try
            {
                final boolean right = (boolean) bladeRenderingMethod.invoke(null, player, HumanoidArm.RIGHT);
                final boolean left = (boolean) bladeRenderingMethod.invoke(null, player, HumanoidArm.LEFT);
                if (right || left)
                {
                    final float weight = (float) headFollowMethod.invoke(null, player,
                            goblinbob.mobends.core.client.event.DataUpdateHandler.partialTicks);
                    if (right)
                    {
                        aim(data.rightArm, yaw, pitch, weight);
                    }
                    if (left)
                    {
                        aim(data.leftArm, yaw, pitch, weight);
                    }
                }
            }
            catch (Throwable e)
            {
                headFollowMethod = null;
            }
        }

        if (launcherRenderingMethod != null)
        {
            try
            {
                if ((boolean) launcherRenderingMethod.invoke(null, player, HumanoidArm.RIGHT))
                {
                    aim(data.rightArm, yaw, pitch, raise(data.rightArm));
                }
                if ((boolean) launcherRenderingMethod.invoke(null, player, HumanoidArm.LEFT))
                {
                    aim(data.leftArm, yaw, pitch, raise(data.leftArm));
                }
            }
            catch (Throwable e)
            {
                launcherRenderingMethod = null;
            }
        }
    }

    private static float raise(ModelPartTransform arm)
    {
        final Quaternion current = arm.rotation.getSmooth();
        final float xRot = new Quaternionf(current.x, current.y, current.z, current.w)
                .getEulerAnglesZYX(new Vector3f()).x;
        return Mth.clamp(-xRot / Mth.HALF_PI, 0.0F, 1.0F);
    }

    private static void aim(ModelPartTransform arm, float yaw, float pitch, float weight)
    {
        final Quaternion current = arm.rotation.getSmooth();
        final Quaternionf turned = new Quaternionf().rotationY(yaw * weight).rotateX(pitch * weight)
                .mul(new Quaternionf(current.x, current.y, current.z, current.w));
        arm.rotation.setSmooth(turned.x, turned.y, turned.z, turned.w);
    }

    public static void conceal(ModelPart modelPart, BendsModelPart part, BendsModelPart forePart)
    {
        if (modelPart == null || !isModLoaded())
        {
            return;
        }

        try
        {
            final Object rule = ruleForMethod.invoke(null, modelPart);
            if (rule == null)
            {
                return;
            }

            final boolean hidden = (boolean) hiddenMethod.invoke(rule);
            if (hidden && part != null)
            {
                part.concealed = true;
            }

            if (forePart != null && (hidden || (boolean) mantisCutoutMethod.invoke(rule)
                    || (boolean) launcherCutoutMethod.invoke(rule)))
            {
                forePart.concealed = true;
            }
        }
        catch (Throwable e)
        {
            ruleForMethod = null;
        }
    }

    public static void track(Object part)
    {
        final HumanoidModel<?> model = capturing;
        if (model == null)
        {
            return;
        }

        if (part == model.leftArm || part == model.rightArm || part == model.leftLeg || part == model.rightLeg)
        {
            currentLimb = (ModelPart) part;
        }
        else if (part == model.body || part == model.head)
        {
            currentLimb = null;
        }
    }

    public static void renderBent(BipedMutator<?, ?, ?> mutator, HumanoidModel<?> model, PoseStack poseStack,
                                  MultiBufferSource buffers, boolean split, Consumer<MultiBufferSource> draw)
    {
        final Matrix4f base = new Matrix4f(poseStack.last().pose());

        for (LimbCapture capture : CAPTURES.values())
        {
            capture.reset();
        }

        capturing = model;
        currentLimb = null;
        try
        {
            draw.accept(CAPTURE_SOURCE);
        }
        finally
        {
            capturing = null;
            currentLimb = null;
        }

        for (Map.Entry<RenderType, LimbCapture> entry : CAPTURES.entrySet())
        {
            final LimbCapture capture = entry.getValue();
            final List<CapturedVertex> vertices = capture.getVertices();
            if (vertices.isEmpty())
            {
                continue;
            }

            final VertexConsumer out = buffers.getBuffer(entry.getKey());
            emitSegment(capture, null, null, null, base, split, out);
            emitSegment(capture, model.leftArm, mutator.getLeftArm(), mutator.getLeftForeArm(), base, split, out);
            emitSegment(capture, model.rightArm, mutator.getRightArm(), mutator.getRightForeArm(), base, split, out);
            emitSegment(capture, model.leftLeg, mutator.getLeftLeg(), mutator.getLeftForeLeg(), base, split, out);
            emitSegment(capture, model.rightLeg, mutator.getRightLeg(), mutator.getRightForeLeg(), base, split, out);
        }
    }

    private static void emitSegment(LimbCapture capture, ModelPart limb, BendsModelPart upper, BendsModelPart fore,
                                    Matrix4f base, boolean split, VertexConsumer out)
    {
        final List<CapturedVertex> vertices = capture.getVertices();
        SEGMENT.clear();
        for (int i = 0; i < vertices.size(); ++i)
        {
            if (capture.limbs.get(i) == limb)
            {
                SEGMENT.add(vertices.get(i));
            }
        }
        if (SEGMENT.isEmpty())
        {
            return;
        }

        if (limb == null || upper == null || fore == null)
        {
            LimbBending.emitRigid(SEGMENT, out);
            return;
        }

        final PoseStack limbStack = new PoseStack();
        limbStack.last().pose().set(base);
        limb.translateAndRotate(limbStack);
        final Matrix4f limbPose = new Matrix4f(limbStack.last().pose());

        final PoseStack upperStack = new PoseStack();
        upper.applyCharacterTransformPoseStack(upperStack);
        final PoseStack foreStack = new PoseStack();
        fore.applyCharacterTransformPoseStack(foreStack);
        final Matrix4f joint = new Matrix4f(upperStack.last().pose()).invert().mul(foreStack.last().pose());

        final Matrix4f forePose = new Matrix4f(limbPose).mul(joint);
        if (split)
        {
            LimbBending.emitBent(SEGMENT, limbPose, forePose, out);
        }
        else
        {
            LimbBending.emitAttached(SEGMENT, limbPose, forePose, out);
        }
    }

    private static final class LimbCapture extends CapturingVertexConsumer
    {
        private final List<ModelPart> limbs = new ArrayList<>();

        private void reset()
        {
            clear();
            limbs.clear();
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z)
        {
            super.addVertex(x, y, z);
            limbs.add(currentLimb);
            return this;
        }
    }
}
