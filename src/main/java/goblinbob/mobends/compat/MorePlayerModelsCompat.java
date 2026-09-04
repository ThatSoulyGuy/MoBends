package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MorePlayerModelsCompat
{
    private static final String MOD_ID = "moreplayermodels";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Field compatibilityField;
    private static Method getModelDataMethod;
    private static Method getLegsYMethod;

    private static Field headField;
    private static Field bodyField;
    private static Field leftArmField;
    private static Field rightArmField;
    private static Field leftLegField;
    private static Field rightLegField;

    private static Field scaleXField;
    private static Field scaleYField;
    private static Field scaleZField;

    private static int captureDepth = 0;
    private static boolean captureRestoreValue = false;

    private static final float SHOULDER_DROP = 2.0F;

    private static final float[] scratchScale = new float[3];
    private static final float[] bodyScale = new float[3];

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
        compatibilityField = Class.forName("noppes.mpm.MorePlayerModels").getField("Compatibility");
        getModelDataMethod = Class.forName("noppes.mpm.ModelData").getMethod("get", Player.class);

        final Class<?> sharedClass = Class.forName("noppes.mpm.ModelDataShared");
        getLegsYMethod = sharedClass.getMethod("getLegsY");
        headField = sharedClass.getField("head");
        bodyField = sharedClass.getField("body");
        leftArmField = sharedClass.getField("arm1");
        rightArmField = sharedClass.getField("arm2");
        leftLegField = sharedClass.getField("leg1");
        rightLegField = sharedClass.getField("leg2");

        final Class<?> configClass = Class.forName("noppes.mpm.ModelPartConfig");
        scaleXField = configClass.getField("scaleX");
        scaleYField = configClass.getField("scaleY");
        scaleZField = configClass.getField("scaleZ");
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    public static void beginNeutralCapture()
    {
        if (!isModLoaded())
        {
            return;
        }

        if (captureDepth++ > 0)
        {
            return;
        }

        try
        {
            captureRestoreValue = compatibilityField.getBoolean(null);
            compatibilityField.setBoolean(null, true);
        }
        catch (Exception e)
        {
            isLoaded = false;
            captureDepth = 0;
        }
    }

    public static void endNeutralCapture()
    {
        if (!isModLoaded() || captureDepth == 0)
        {
            return;
        }

        if (--captureDepth > 0)
        {
            return;
        }

        try
        {
            compatibilityField.setBoolean(null, captureRestoreValue);
        }
        catch (Exception e)
        {
            isLoaded = false;
        }
    }

    public static void applyModelScaling(LivingEntity entity, BipedEntityData<?> data)
    {
        if (data == null || !isModLoaded() || !(entity instanceof Player player))
        {
            return;
        }

        final Object modelData = modelDataOf(player);
        if (modelData == null)
        {
            return;
        }

        if (!readConfig(modelData, bodyField, bodyScale))
        {
            return;
        }

        final float legsDrop = legsDropOf(modelData);

        applyToBone(data.body, bodyScale, null, 0.0F, legsDrop);

        applyPart(data.head, modelData, headField, bodyScale, 0.0F, 0.0F);
        applyPart(data.leftArm, modelData, leftArmField, bodyScale, SHOULDER_DROP, 0.0F);
        applyPart(data.rightArm, modelData, rightArmField, bodyScale, SHOULDER_DROP, 0.0F);
        applyPart(data.leftLeg, modelData, leftLegField, null, 0.0F, legsDrop);
        applyPart(data.rightLeg, modelData, rightLegField, null, 0.0F, legsDrop);
    }

    public static void compensateSyncedPivots(LivingEntity entity, HumanoidModel<?> model)
    {
        if (model == null || !isModLoaded() || !(entity instanceof Player player))
        {
            return;
        }

        final Object modelData = modelDataOf(player);
        if (modelData == null)
        {
            return;
        }

        final float legsDrop = legsDropOf(modelData);
        if (legsDrop == 0.0F)
        {
            return;
        }

        model.head.y -= legsDrop;
        if (model.hat != null)
        {
            model.hat.y -= legsDrop;
        }
        model.body.y -= legsDrop;
        model.leftArm.y -= legsDrop;
        model.rightArm.y -= legsDrop;

        if (model instanceof PlayerModel<?> playerModel)
        {
            playerModel.jacket.copyFrom(model.body);
            playerModel.leftSleeve.copyFrom(model.leftArm);
            playerModel.rightSleeve.copyFrom(model.rightArm);
        }
    }

    private static Object modelDataOf(Player player)
    {
        try
        {
            return getModelDataMethod.invoke(null, player);
        }
        catch (Exception e)
        {
            isLoaded = false;
            return null;
        }
    }

    private static float legsDropOf(Object modelData)
    {
        try
        {
            return ((Number) getLegsYMethod.invoke(modelData)).floatValue() * 16.0F;
        }
        catch (Exception e)
        {
            isLoaded = false;
            return 0.0F;
        }
    }

    private static void applyPart(ModelPartTransform bone, Object modelData, Field configField,
                                  float[] parentScale, float pivotGap, float rootDrop)
    {
        if (bone == null || !readConfig(modelData, configField, scratchScale))
        {
            return;
        }

        applyToBone(bone, scratchScale, parentScale, pivotGap, rootDrop);
    }

    private static boolean readConfig(Object data, Field configField, float[] scale)
    {
        try
        {
            final Object config = configField.get(data);
            if (config == null)
            {
                return false;
            }

            scale[0] = scaleXField.getFloat(config);
            scale[1] = scaleYField.getFloat(config);
            scale[2] = scaleZField.getFloat(config);
            return true;
        }
        catch (Exception e)
        {
            isLoaded = false;
            return false;
        }
    }

    private static void applyToBone(ModelPartTransform bone, float[] scale, float[] parentScale,
                                    float pivotGap, float rootDrop)
    {
        if (bone == null)
        {
            return;
        }

        bone.scale.set(scale[0], scale[1], scale[2]);

        if (parentScale == null)
        {
            bone.preRotationScale.set(1.0F, 1.0F, 1.0F);
            bone.globalOffset.set(0.0F, rootDrop, 0.0F);
            return;
        }

        bone.preRotationScale.set(divide(1.0F, parentScale[0]),
                divide(1.0F, parentScale[1]),
                divide(1.0F, parentScale[2]));

        bone.globalOffset.set(0.0F, divide(-pivotGap * (parentScale[1] - 1.0F), parentScale[1]), 0.0F);
    }

    private static float divide(float value, float divisor)
    {
        return divisor == 0.0F ? value : value / divisor;
    }
}
