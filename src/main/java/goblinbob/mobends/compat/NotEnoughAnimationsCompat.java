package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.standard.mutators.BipedMutator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

public class NotEnoughAnimationsCompat
{
    private static final String MOD_ID = "notenoughanimations";
    private static final String HANDS_PACKAGE = "dev.tr7zw.notenoughanimations.animations.hands.";

    private static final float RAD_TO_DEG = (float) (180.0 / Math.PI);

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Field instanceField;
    private static Field animationProviderField;
    private static Field enabledAnimationsField;
    private static Field heldItemHandlerField;

    private static Method onRenderItemMethod;
    private static Constructor<?> callbackInfoConstructor;
    private static Method isCancelledMethod;

    private static Method isValidMethod;
    private static Method getPriorityMethod;
    private static Method getBodyPartsMethod;

    private static Object leftArmPart;
    private static Object rightArmPart;

    private static final SmoothOrientation scratchOrientation = new SmoothOrientation();
    private static final Quaternion scratchDesired = new Quaternion();
    private static final Quaternion scratchParentInverse = new Quaternion();
    private static final Quaternion scratchLocal = new Quaternion();

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
        Class<?> loaderClass = Class.forName("dev.tr7zw.notenoughanimations.NEAnimationsLoader");
        Class<?> providerClass = Class.forName("dev.tr7zw.notenoughanimations.logic.AnimationProvider");
        Class<?> animationClass = Class.forName("dev.tr7zw.notenoughanimations.api.BasicAnimation");
        Class<?> playerDataClass = Class.forName("dev.tr7zw.notenoughanimations.access.PlayerData");
        Class<?> bodyPartClass = Class.forName("dev.tr7zw.notenoughanimations.versionless.animations.BodyPart");

        instanceField = loaderClass.getField("INSTANCE");
        animationProviderField = loaderClass.getField("animationProvider");

        enabledAnimationsField = providerClass.getDeclaredField("enabledBasicAnimations");
        enabledAnimationsField.setAccessible(true);

        isValidMethod = animationClass.getMethod("isValid", AbstractClientPlayer.class, playerDataClass);
        getPriorityMethod = animationClass.getMethod("getPriority", AbstractClientPlayer.class, playerDataClass);
        getBodyPartsMethod = animationClass.getMethod("getBodyParts", AbstractClientPlayer.class, playerDataClass);

        leftArmPart = Enum.valueOf((Class<Enum>) bodyPartClass, "LEFT_ARM");
        rightArmPart = Enum.valueOf((Class<Enum>) bodyPartClass, "RIGHT_ARM");

        try
        {
            Class<?> handlerClass = Class.forName("dev.tr7zw.notenoughanimations.logic.HeldItemHandler");
            Class<?> callbackClass = Class.forName("org.spongepowered.asm.mixin.injection.callback.CallbackInfo");

            heldItemHandlerField = loaderClass.getField("heldItemHandler");
            onRenderItemMethod = handlerClass.getMethod("onRenderItem", LivingEntity.class, EntityModel.class,
                    ItemStack.class, HumanoidArm.class, PoseStack.class, MultiBufferSource.class,
                    int.class, callbackClass);

            callbackInfoConstructor = callbackClass.getConstructor(String.class, boolean.class);
            isCancelledMethod = callbackClass.getMethod("isCancelled");
        }
        catch (Exception e)
        {
            onRenderItemMethod = null;
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

    private static Set<?> getEnabledAnimations()
    {
        try
        {
            Object loader = instanceField.get(null);
            if (loader == null)
            {
                return null;
            }

            Object provider = animationProviderField.get(loader);
            if (provider == null)
            {
                return null;
            }

            return (Set<?>) enabledAnimationsField.get(provider);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static boolean isHandAnimation(Object animation)
    {
        return animation != null && animation.getClass().getName().startsWith(HANDS_PACKAGE);
    }

    public static void applyArmPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (mutator == null || vanillaModel == null || !isModLoaded())
        {
            return;
        }

        if (!(entity instanceof AbstractClientPlayer player) || player.swinging)
        {
            return;
        }

        final BendsModelPart body = mutator.getBody();
        if (body == null)
        {
            return;
        }

        final Set<?> animations = getEnabledAnimations();
        if (animations == null || animations.isEmpty())
        {
            return;
        }

        Object leftWinner = null;
        Object rightWinner = null;
        int leftPriority = 0;
        int rightPriority = 0;

        try
        {
            for (Object animation : animations)
            {
                Boolean valid = (Boolean) isValidMethod.invoke(animation, player, player);
                if (valid == null || !valid)
                {
                    continue;
                }

                Integer priority = (Integer) getPriorityMethod.invoke(animation, player, player);
                if (priority == null || priority <= 0)
                {
                    continue;
                }

                Object[] parts = (Object[]) getBodyPartsMethod.invoke(animation, player, player);
                if (parts == null)
                {
                    continue;
                }

                for (Object part : parts)
                {
                    if (part == leftArmPart && priority > leftPriority)
                    {
                        leftPriority = priority;
                        leftWinner = animation;
                    }
                    else if (part == rightArmPart && priority > rightPriority)
                    {
                        rightPriority = priority;
                        rightWinner = animation;
                    }
                }
            }
        }
        catch (Exception e)
        {
            return;
        }

        if (!isHandAnimation(leftWinner) && !isHandAnimation(rightWinner))
        {
            return;
        }

        final goblinbob.mobends.standard.data.BipedEntityData<?> data = mutator.getRenderData();

        final Quaternion bodyRotation = body.rotation.getSmooth();
        scratchParentInverse.set(-bodyRotation.x, -bodyRotation.y, -bodyRotation.z, bodyRotation.w);

        if (isHandAnimation(rightWinner))
        {
            adoptArm(vanillaModel.rightArm, mutator.getRightArm(), data == null ? null : data.rightArm,
                    mutator.getRightForeArm(), data == null ? null : data.rightForeArm);
        }

        if (isHandAnimation(leftWinner))
        {
            adoptArm(vanillaModel.leftArm, mutator.getLeftArm(), data == null ? null : data.leftArm,
                    mutator.getLeftForeArm(), data == null ? null : data.leftForeArm);
        }
    }

    public static boolean renderHeldItem(LivingEntity entity, Object model, ItemStack itemStack,
                                         HumanoidArm arm, PoseStack poseStack,
                                         MultiBufferSource bufferSource, int packedLight)
    {
        if (entity == null || model == null || itemStack == null || !isModLoaded()
                || onRenderItemMethod == null)
        {
            return false;
        }

        try
        {
            Object loader = instanceField.get(null);
            if (loader == null)
            {
                return false;
            }

            Object handler = heldItemHandlerField.get(loader);
            if (handler == null)
            {
                return false;
            }

            Object callback = callbackInfoConstructor.newInstance("renderArmWithItem", true);
            onRenderItemMethod.invoke(handler, entity, model, itemStack, arm,
                    poseStack, bufferSource, packedLight, callback);

            return (Boolean) isCancelledMethod.invoke(callback);
        }
        catch (Exception e)
        {
            onRenderItemMethod = null;
            return false;
        }
    }

    private static void adoptArm(ModelPart source, BendsModelPart arm,
                                 goblinbob.mobends.core.client.model.ModelPartTransform dataArm,
                                 BendsModelPart foreArm,
                                 goblinbob.mobends.core.client.model.ModelPartTransform dataForeArm)
    {
        if (source == null || arm == null)
        {
            return;
        }

        scratchOrientation
                .orientInstantX(source.xRot * RAD_TO_DEG)
                .rotateInstantY(source.yRot * RAD_TO_DEG)
                .rotateInstantZ(source.zRot * RAD_TO_DEG);
        scratchDesired.set(scratchOrientation.getSmooth());

        Quaternion.mul(scratchParentInverse, scratchDesired, scratchLocal);
        BipedMutator.applyAdoptedRotation(arm, dataArm, scratchLocal);

        if (foreArm != null || dataForeArm != null)
        {
            BipedMutator.straightenJoint(foreArm, dataForeArm);
        }
    }
}
