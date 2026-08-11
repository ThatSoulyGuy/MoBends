package goblinbob.mobends.neoforge.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@OnlyIn(Dist.CLIENT)
public class CuriosCompat
{
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends-CuriosCompat");
    private static final String MOD_ID = "curios";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Class<?> curiosApiClass;
    private static Method getCuriosInventoryMethod;

    private static final ThreadLocal<SplitLimbTransforms> currentSplitTransforms =
            ThreadLocal.withInitial(SplitLimbTransforms::new);

    public static class SplitLimbTransforms
    {
        public float leftArmEndXRot, leftArmEndYRot, leftArmEndZRot;
        public float rightArmEndXRot, rightArmEndYRot, rightArmEndZRot;
        public float leftLegEndXRot, leftLegEndYRot, leftLegEndZRot;
        public float rightLegEndXRot, rightLegEndYRot, rightLegEndZRot;
        public boolean valid = false;
    }

    public static void init()
    {
        if (initialized) return;
        initialized = true;

        isLoaded = ModList.get().isLoaded(MOD_ID);

        if (isLoaded)
        {
            LOGGER.info("Curios API detected, initializing compatibility layer");
            try
            {
                initReflection();
                LOGGER.info("Curios compatibility initialized successfully");
            }
            catch (Exception e)
            {
                LOGGER.warn("Failed to initialize Curios compatibility: {}", e.getMessage());
            }
        }
    }

    private static void initReflection() throws Exception
    {
        curiosApiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
        getCuriosInventoryMethod = curiosApiClass.getMethod("getCuriosInventory", LivingEntity.class);

        LOGGER.debug("Curios reflection initialized:");
        LOGGER.debug("  - CuriosApi class: {}", curiosApiClass);
        LOGGER.debug("  - getCuriosInventory method: {}", getCuriosInventoryMethod);
    }

    public static boolean isModLoaded()
    {
        if (!initialized) init();
        return isLoaded;
    }

    public static void syncTransformsForCurios(LivingEntity entity, HumanoidModel<?> model, PoseStack poseStack)
    {
        if (!isModLoaded()) return;

        BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        if (mutator == null) return;

        SplitLimbTransforms transforms = currentSplitTransforms.get();
        transforms.valid = false;

        float[] leftForeArm = mutator.getLeftForeArmEulerAngles();
        transforms.leftArmEndXRot = model.leftArm.xRot + leftForeArm[0];
        transforms.leftArmEndYRot = model.leftArm.yRot + leftForeArm[1];
        transforms.leftArmEndZRot = model.leftArm.zRot + leftForeArm[2];

        float[] rightForeArm = mutator.getRightForeArmEulerAngles();
        transforms.rightArmEndXRot = model.rightArm.xRot + rightForeArm[0];
        transforms.rightArmEndYRot = model.rightArm.yRot + rightForeArm[1];
        transforms.rightArmEndZRot = model.rightArm.zRot + rightForeArm[2];

        float[] leftForeLeg = mutator.getLeftForeLegEulerAngles();
        transforms.leftLegEndXRot = model.leftLeg.xRot + leftForeLeg[0];
        transforms.leftLegEndYRot = model.leftLeg.yRot + leftForeLeg[1];
        transforms.leftLegEndZRot = model.leftLeg.zRot + leftForeLeg[2];

        float[] rightForeLeg = mutator.getRightForeLegEulerAngles();
        transforms.rightLegEndXRot = model.rightLeg.xRot + rightForeLeg[0];
        transforms.rightLegEndYRot = model.rightLeg.yRot + rightForeLeg[1];
        transforms.rightLegEndZRot = model.rightLeg.zRot + rightForeLeg[2];

        transforms.valid = true;
    }

    public static SplitLimbTransforms getCurrentSplitLimbTransforms()
    {
        return currentSplitTransforms.get();
    }

    public static void clearSplitLimbTransforms()
    {
        currentSplitTransforms.remove();
    }

    public static boolean hasCurioItems(LivingEntity entity)
    {
        if (!isModLoaded() || getCuriosInventoryMethod == null) return false;

        try
        {
            Object optional = getCuriosInventoryMethod.invoke(null, entity);
            if (optional != null)
            {
                Method isPresentMethod = optional.getClass().getMethod("isPresent");
                return (Boolean) isPresentMethod.invoke(optional);
            }
        }
        catch (Exception e)
        {
            LOGGER.debug("Error checking curio items: {}", e.getMessage());
        }

        return false;
    }

    public static String getCompatInfo()
    {
        if (!isModLoaded()) return "Curios: Not loaded";
        return "Curios: Loaded, compatibility active";
    }
}
