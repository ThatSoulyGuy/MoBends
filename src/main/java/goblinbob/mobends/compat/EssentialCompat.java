package goblinbob.mobends.compat;

import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class EssentialCompat
{
    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Class<?> playerExtClass;
    private static Method getCosmeticsStateMethod;
    private static Method isPoseModifiedMethod;
    private static Method getCosmeticsMethod;
    private static Object emoteSlot;

    private static Class<?> modelExtClass;
    private static Method getResetPoseMethod;
    private static Method getPoseLeftLegMethod;
    private static Method getPoseRightLegMethod;
    private static Method getPivotXMethod;
    private static Method getPivotYMethod;
    private static Method getPivotZMethod;

    private static final float[] leftLegRestPivot = new float[3];
    private static final float[] rightLegRestPivot = new float[3];

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        try
        {
            initReflection();
            isLoaded = true;
        }
        catch (Throwable e)
        {
            isLoaded = false;
        }
    }

    private static void initReflection() throws Exception
    {
        playerExtClass = Class.forName("gg.essential.mixins.impl.client.entity.AbstractClientPlayerExt");

        Class<?> cosmeticsStateClass = Class.forName("gg.essential.cosmetics.CosmeticsState");
        Class<?> cosmeticSlotClass = Class.forName("gg.essential.mod.cosmetics.CosmeticSlot");

        getCosmeticsStateMethod = playerExtClass.getMethod("getCosmeticsState");
        isPoseModifiedMethod = playerExtClass.getMethod("isPoseModified");
        getCosmeticsMethod = cosmeticsStateClass.getMethod("getCosmetics");

        Field emoteField = cosmeticSlotClass.getField("EMOTE");
        emoteSlot = emoteField.get(null);

        if (emoteSlot == null)
        {
            throw new IllegalStateException("CosmeticSlot.EMOTE is null");
        }

        Class<?> playerPoseClass = Class.forName("gg.essential.model.backend.PlayerPose");
        Class<?> posePartClass = Class.forName("gg.essential.model.backend.PlayerPose$Part");

        modelExtClass = Class.forName("gg.essential.mixins.impl.client.model.ModelBipedExt");
        getResetPoseMethod = modelExtClass.getMethod("getResetPose");
        getPoseLeftLegMethod = playerPoseClass.getMethod("getLeftLeg");
        getPoseRightLegMethod = playerPoseClass.getMethod("getRightLeg");
        getPivotXMethod = posePartClass.getMethod("getPivotX");
        getPivotYMethod = posePartClass.getMethod("getPivotY");
        getPivotZMethod = posePartClass.getMethod("getPivotZ");
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    public static boolean isPlayingEmote(LivingEntity entity)
    {
        if (!isModLoaded() || !(entity instanceof AbstractClientPlayer player))
        {
            return false;
        }

        if (!playerExtClass.isInstance(player))
        {
            return false;
        }

        try
        {
            Object state = getCosmeticsStateMethod.invoke(player);
            if (state == null)
            {
                return false;
            }

            Object cosmetics = getCosmeticsMethod.invoke(state);
            return cosmetics instanceof Map<?, ?> map && map.containsKey(emoteSlot);
        }
        catch (Exception e)
        {
            isLoaded = false;
            return false;
        }
    }

    public static void applyEmotePose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (mutator == null || vanillaModel == null || !isPlayingEmote(entity))
        {
            return;
        }

        final boolean restResolved = readRestLegPivots(vanillaModel);

        mutator.adoptPoseFromVanillaModel(vanillaModel,
                restResolved ? leftLegRestPivot : null,
                restResolved ? rightLegRestPivot : null);
    }

    private static boolean readRestLegPivots(HumanoidModel<?> vanillaModel)
    {
        if (modelExtClass == null || !modelExtClass.isInstance(vanillaModel))
        {
            return false;
        }

        try
        {
            Object resetPose = getResetPoseMethod.invoke(vanillaModel);
            if (resetPose == null)
            {
                return false;
            }

            readPivot(getPoseLeftLegMethod.invoke(resetPose), leftLegRestPivot);
            readPivot(getPoseRightLegMethod.invoke(resetPose), rightLegRestPivot);
            return true;
        }
        catch (Exception e)
        {
            isLoaded = false;
            return false;
        }
    }

    private static void readPivot(Object posePart, float[] dest) throws Exception
    {
        dest[0] = (Float) getPivotXMethod.invoke(posePart);
        dest[1] = (Float) getPivotYMethod.invoke(posePart);
        dest[2] = (Float) getPivotZMethod.invoke(posePart);
    }

    public static boolean hasModifiedPose(LivingEntity entity)
    {
        if (!isModLoaded() || !(entity instanceof AbstractClientPlayer player))
        {
            return false;
        }

        if (!playerExtClass.isInstance(player))
        {
            return false;
        }

        try
        {
            return (Boolean) isPoseModifiedMethod.invoke(player);
        }
        catch (Exception e)
        {
            isLoaded = false;
            return false;
        }
    }
}
