package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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

    private static Field transXField;
    private static Field transYField;
    private static Field transZField;

    private static Class<?> partsLayerClass;

    private static int captureDepth = 0;
    private static boolean captureRestoreValue = false;

    private static final float SHOULDER_DROP = 2.0F;

    private static final float[] FOREARM_ANCHOR = {0.0F, -4.0F, -2.0F};
    private static final float[] FORELEG_ANCHOR = {0.0F, -6.0F, 2.0F};

    private static final float[] scratchScale = new float[3];
    private static final float[] bodyScale = new float[3];
    private static final float[] scratchTranslation = new float[3];

    private static final PoseStack limbStack = new PoseStack();
    private static final Vector3f limbPivot = new Vector3f();
    private static final Quaternionf limbRotation = new Quaternionf();
    private static final Quaternion limbOrientation = new Quaternion();
    private static final float[][] savedLimbs = new float[4][6];
    private static boolean limbsPosed = false;

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
        transXField = configClass.getField("transX");
        transYField = configClass.getField("transY");
        transZField = configClass.getField("transZ");

        partsLayerClass = Class.forName("noppes.mpm.client.layer.LayerParts");
    }

    public static boolean isPartsLayer(Object layer)
    {
        return layer != null && isModLoaded() && partsLayerClass != null && partsLayerClass.isInstance(layer);
    }

    public static void beginPartsRender(LivingEntity entity, BipedMutator<?, ?, ?> mutator, PlayerModel<?> model)
    {
        if (limbsPosed || model == null || mutator == null || !isModLoaded() || !(entity instanceof Player player))
        {
            return;
        }

        final Object modelData = modelDataOf(player);
        if (modelData == null)
        {
            return;
        }

        saveLimb(model.rightArm, savedLimbs[0]);
        saveLimb(model.leftArm, savedLimbs[1]);
        saveLimb(model.rightLeg, savedLimbs[2]);
        saveLimb(model.leftLeg, savedLimbs[3]);
        limbsPosed = true;

        poseAsLowerLimb(model.rightArm, mutator.getRightForeArm(), FOREARM_ANCHOR, modelData, rightArmField, true);
        poseAsLowerLimb(model.leftArm, mutator.getLeftForeArm(), FOREARM_ANCHOR, modelData, leftArmField, true);
        poseAsLowerLimb(model.rightLeg, mutator.getRightForeLeg(), FORELEG_ANCHOR, modelData, rightLegField, false);
        poseAsLowerLimb(model.leftLeg, mutator.getLeftForeLeg(), FORELEG_ANCHOR, modelData, leftLegField, false);
    }

    public static void endPartsRender(PlayerModel<?> model)
    {
        if (!limbsPosed)
        {
            return;
        }
        limbsPosed = false;

        if (model == null)
        {
            return;
        }

        restoreLimb(model.rightArm, savedLimbs[0]);
        restoreLimb(model.leftArm, savedLimbs[1]);
        restoreLimb(model.rightLeg, savedLimbs[2]);
        restoreLimb(model.leftLeg, savedLimbs[3]);
    }

    private static void poseAsLowerLimb(ModelPart part, BendsModelPart bone, float[] anchor, Object modelData,
                                        Field configField, boolean arm)
    {
        if (part == null || bone == null)
        {
            return;
        }

        limbStack.pushPose();
        bone.applyCharacterTransformPoseStack(limbStack);
        final Matrix4f pose = limbStack.last().pose();
        limbPivot.set(anchor[0] / 16.0F, anchor[1] / 16.0F, anchor[2] / 16.0F);
        pose.transformPosition(limbPivot);
        pose.getNormalizedRotation(limbRotation);
        limbStack.popPose();

        if (!readTranslation(modelData, configField, scratchTranslation))
        {
            scratchTranslation[0] = 0.0F;
            scratchTranslation[1] = 0.0F;
            scratchTranslation[2] = 0.0F;
        }
        if (!readConfig(modelData, configField, scratchScale))
        {
            scratchScale[0] = 1.0F;
            scratchScale[1] = 1.0F;
            scratchScale[2] = 1.0F;
        }

        final float lift = arm
                ? scratchTranslation[1] + (1.0F - scratchScale[1]) * 0.125F
                : scratchTranslation[1] * 2.0F;

        part.x = divide(limbPivot.x() * 16.0F, scratchScale[0]);
        part.y = divide((limbPivot.y() - lift) * 16.0F, scratchScale[1]);
        part.z = divide(limbPivot.z() * 16.0F, scratchScale[2]);

        limbOrientation.set(limbRotation.x(), limbRotation.y(), limbRotation.z(), limbRotation.w());
        final float[] euler = BipedMutator.eulerAnglesOf(limbOrientation);
        part.xRot = euler[0];
        part.yRot = euler[1];
        part.zRot = euler[2];
    }

    private static void saveLimb(ModelPart part, float[] dest)
    {
        if (part == null)
        {
            return;
        }

        dest[0] = part.x;
        dest[1] = part.y;
        dest[2] = part.z;
        dest[3] = part.xRot;
        dest[4] = part.yRot;
        dest[5] = part.zRot;
    }

    private static void restoreLimb(ModelPart part, float[] src)
    {
        if (part == null)
        {
            return;
        }

        part.x = src[0];
        part.y = src[1];
        part.z = src[2];
        part.xRot = src[3];
        part.yRot = src[4];
        part.zRot = src[5];
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

    public static void removeRenderTranslation(LivingEntity entity, HumanoidModel<?> model)
    {
        offsetSyncedPivots(entity, model, -16.0F);
    }

    public static void restoreRenderTranslation(LivingEntity entity, HumanoidModel<?> model)
    {
        offsetSyncedPivots(entity, model, 16.0F);
    }

    private static void offsetSyncedPivots(LivingEntity entity, HumanoidModel<?> model, float scale)
    {
        if (!(model instanceof PlayerModel<?> playerModel) || !isModLoaded()
                || !(entity instanceof Player player) || !isTranslatingParts())
        {
            return;
        }

        final Object modelData = modelDataOf(player);
        if (modelData == null)
        {
            return;
        }

        offsetPart(model.head, modelData, headField, scale);
        offsetPart(model.hat, modelData, headField, scale);
        offsetPart(model.body, modelData, bodyField, scale);
        offsetPart(model.leftArm, modelData, leftArmField, scale);
        offsetPart(model.rightArm, modelData, rightArmField, scale);
        offsetPart(model.leftLeg, modelData, leftLegField, scale);
        offsetPart(model.rightLeg, modelData, rightLegField, scale);

        playerModel.jacket.copyFrom(model.body);
        playerModel.leftSleeve.copyFrom(model.leftArm);
        playerModel.rightSleeve.copyFrom(model.rightArm);
        playerModel.leftPants.copyFrom(model.leftLeg);
        playerModel.rightPants.copyFrom(model.rightLeg);
    }

    private static boolean isTranslatingParts()
    {
        try
        {
            return !compatibilityField.getBoolean(null);
        }
        catch (Exception e)
        {
            isLoaded = false;
            return false;
        }
    }

    private static void offsetPart(ModelPart part, Object modelData, Field configField, float scale)
    {
        if (part == null || !readTranslation(modelData, configField, scratchTranslation))
        {
            return;
        }

        part.x += scratchTranslation[0] * scale;
        part.y += scratchTranslation[1] * scale;
        part.z += scratchTranslation[2] * scale;
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

    private static boolean readTranslation(Object data, Field configField, float[] translation)
    {
        try
        {
            final Object config = configField.get(data);
            if (config == null)
            {
                return false;
            }

            translation[0] = transXField.getFloat(config);
            translation[1] = transYField.getFloat(config);
            translation[2] = transZField.getFloat(config);
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

        bone.globalOffset.set(0.0F, divide(pivotGap * (scale[1] - parentScale[1]), parentScale[1]), 0.0F);
    }

    private static float divide(float value, float divisor)
    {
        return divisor == 0.0F ? value : value / divisor;
    }
}
