package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

import java.lang.reflect.Field;

public class CrawlCompat
{
    private static final String MOD_ID = "crawl";
    private static final String CRAWLING_POSE_NAME = "CRAWLING";

    private static boolean initialized = false;
    private static Pose crawlingPose;
    private static Field replaceAnimationField;

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
            crawlingPose = Pose.valueOf(CRAWLING_POSE_NAME);
        }
        catch (Throwable e)
        {
            crawlingPose = null;
            return;
        }

        try
        {
            replaceAnimationField = Class.forName("ru.fewizz.crawl.client.CrawlClient")
                    .getField("replaceAnimation");
        }
        catch (Throwable e)
        {
            replaceAnimationField = null;
        }
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return crawlingPose != null;
    }

    public static boolean isCrawling(LivingEntity entity)
    {
        if (entity == null || !isModLoaded())
        {
            return false;
        }

        return entity.getPose() == crawlingPose;
    }

    public static boolean isReplacingAnimation()
    {
        if (replaceAnimationField == null)
        {
            return false;
        }

        try
        {
            return replaceAnimationField.getBoolean(null);
        }
        catch (Exception e)
        {
            replaceAnimationField = null;
            return false;
        }
    }

    public static boolean isPosingModel(LivingEntity entity)
    {
        return isCrawling(entity) && isReplacingAnimation();
    }

    public static void applyPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (mutator == null || !(vanillaModel instanceof PlayerModel<?>))
        {
            return;
        }

        if (!isPosingModel(entity))
        {
            return;
        }

        mutator.adoptPoseFromVanillaModel(vanillaModel, null, null);
    }
}
