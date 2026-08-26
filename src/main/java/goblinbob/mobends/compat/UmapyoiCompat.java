package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Field;

public class UmapyoiCompat
{
    private static final String MOD_ID = "umapyoi";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Field bodyField;
    private static Field tailField;
    private static Field rightArmDownField;
    private static Field leftArmDownField;
    private static Field rightLegDownField;
    private static Field leftLegDownField;

    private static Field partX;
    private static Field partY;
    private static Field partZ;
    private static Field partXRot;
    private static Field partYRot;
    private static Field partZRot;

    private static final java.util.Map<Object, float[]> tailRestPoses = new java.util.WeakHashMap<>();

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
            Class<?> modelClass = Class.forName("net.tracen.umapyoi.client.model.UmaPlayerModel");
            bodyField = modelClass.getField("body");
            tailField = modelClass.getField("tail");
            rightArmDownField = modelClass.getField("rightArmDown");
            leftArmDownField = modelClass.getField("leftArmDown");
            rightLegDownField = modelClass.getField("rightLegDown");
            leftLegDownField = modelClass.getField("leftLegDown");

            Class<?> partClass = Class.forName("cn.mcmod_mmf.mmlib.client.model.bedrock.BedrockPart");
            partX = partClass.getField("x");
            partY = partClass.getField("y");
            partZ = partClass.getField("z");
            partXRot = partClass.getField("xRot");
            partYRot = partClass.getField("yRot");
            partZRot = partClass.getField("zRot");
        }
        catch (Exception e)
        {
            isLoaded = false;
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

    public static void applyPose(Object umaModel, LivingEntity entity)
    {
        if (umaModel == null || entity == null || !isModLoaded())
        {
            return;
        }

        if (MoBendsRenderContext.getCurrentEntity() != entity)
        {
            return;
        }

        final BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        if (mutator == null || !mutator.shouldRenderCustom())
        {
            return;
        }

        final HumanoidModel<?> vanillaModel = MoBendsRenderContext.getCurrentVanillaModel();
        if (vanillaModel == null)
        {
            return;
        }

        try
        {
            applyJoint(umaModel, rightArmDownField, mutator, mutator.getRightForeArm());
            applyJoint(umaModel, leftArmDownField, mutator, mutator.getLeftForeArm());
            applyJoint(umaModel, rightLegDownField, mutator, mutator.getRightForeLeg());
            applyJoint(umaModel, leftLegDownField, mutator, mutator.getLeftForeLeg());

            applyTail(umaModel, mutator, vanillaModel);
        }
        catch (Exception e)
        {
            isLoaded = false;
        }
    }

    private static void applyJoint(Object umaModel, Field field, BipedMutator<?, ?, ?> mutator,
                                   BendsModelPart bone) throws Exception
    {
        if (field == null || bone == null)
        {
            return;
        }

        final Object part = field.get(umaModel);
        if (part == null)
        {
            return;
        }

        final float[] euler = mutator.getPartEulerAngles(bone);
        partXRot.setFloat(part, euler[0]);
        partYRot.setFloat(part, euler[1]);
        partZRot.setFloat(part, euler[2]);
    }

    public static void captureRestPose(Object umaModel)
    {
        if (umaModel == null || !isModLoaded())
        {
            return;
        }

        try
        {
            final Object tail = tailField.get(umaModel);
            if (tail == null || tailRestPoses.containsKey(tail))
            {
                return;
            }

            tailRestPoses.put(tail, new float[]{
                    partX.getFloat(tail), partY.getFloat(tail), partZ.getFloat(tail)});
        }
        catch (Exception e)
        {
            isLoaded = false;
        }
    }

    private static void applyTail(Object umaModel, BipedMutator<?, ?, ?> mutator,
                                  HumanoidModel<?> vanillaModel) throws Exception
    {
        final Object tail = tailField.get(umaModel);
        if (tail == null)
        {
            return;
        }

        final BendsModelPart bodyBone = mutator.getBody();
        if (bodyBone == null)
        {
            return;
        }

        final float[] tailRest = tailRestPoses.get(tail);
        if (tailRest == null)
        {
            return;
        }

        final Quaternion bodyRotation = bodyBone.rotation.getSmooth();
        final Quaternionf rotation = new Quaternionf(
                bodyRotation.x, bodyRotation.y, bodyRotation.z, bodyRotation.w);

        final Vector3f offset = new Vector3f(tailRest[0], tailRest[1], tailRest[2]);
        rotation.transform(offset);

        partX.setFloat(tail, vanillaModel.body.x + offset.x);
        partY.setFloat(tail, vanillaModel.body.y + offset.y);
        partZ.setFloat(tail, vanillaModel.body.z + offset.z);

        final Quaternion own = new Quaternion();
        own.setIdentity();
        own.rotate(1.0F, 0.0F, 0.0F, partXRot.getFloat(tail));
        own.rotate(0.0F, 1.0F, 0.0F, partYRot.getFloat(tail));
        own.rotate(0.0F, 0.0F, 1.0F, partZRot.getFloat(tail));

        final Quaternion combined = Quaternion.mul(bodyRotation, own, new Quaternion());
        final float[] euler = BipedMutator.eulerAnglesOf(combined);

        partXRot.setFloat(tail, euler[0]);
        partYRot.setFloat(tail, euler[1]);
        partZRot.setFloat(tail, euler[2]);
    }
}
