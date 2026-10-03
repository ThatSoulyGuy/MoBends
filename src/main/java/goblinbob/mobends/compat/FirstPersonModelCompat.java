package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.architectury.platform.Platform;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.standard.client.model.armor.ArmorPoseHelper;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class FirstPersonModelCompat
{
    private static final String MOD_ID = "firstperson";

    private static final String API_CLASS = "dev.tr7zw.firstperson.api.FirstPersonAPI";
    private static final String OFFSET_HANDLER_CLASS = "dev.tr7zw.firstperson.api.PlayerOffsetHandler";

    private static final float PLAYER_SCALE = 0.9375F;
    private static final float CROUCHING_HEAD_Y = 4.2F / 16.0F;
    private static final float MAX_COMPENSATION = 1.5F;

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Method isEnabledMethod;
    private static Method isRenderingPlayerMethod;

    public static void init()
    {
        if (initialized) return;
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);

        if (!isLoaded)
        {
            return;
        }

        try
        {
            initReflection();
        }
        catch (Throwable e)
        {
            isLoaded = false;
            return;
        }

        try
        {
            registerOffsetHandler();
        }
        catch (Throwable ignored)
        {
        }
    }

    private static void initReflection() throws Exception
    {
        Class<?> apiClass = Class.forName(API_CLASS);
        isEnabledMethod = apiClass.getMethod("isEnabled");
        isRenderingPlayerMethod = apiClass.getMethod("isRenderingPlayer");
    }

    private static void registerOffsetHandler() throws Exception
    {
        Class<?> apiClass = Class.forName(API_CLASS);
        Class<?> handlerClass = Class.forName(OFFSET_HANDLER_CLASS);
        Method registerMethod = apiClass.getMethod("registerPlayerHandler", Object.class);

        Object handler = Proxy.newProxyInstance(
                handlerClass.getClassLoader(),
                new Class<?>[]{handlerClass},
                (proxy, method, args) ->
                {
                    if ("applyOffset".equals(method.getName()) && args != null && args.length == 4)
                    {
                        return applyOffset((AbstractClientPlayer) args[0], (Float) args[1], (Vec3) args[3]);
                    }
                    if ("hashCode".equals(method.getName()))
                    {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName()))
                    {
                        return proxy == (args == null ? null : args[0]);
                    }
                    if ("toString".equals(method.getName()))
                    {
                        return "MoBendsPlayerOffsetHandler";
                    }
                    return null;
                });

        registerMethod.invoke(null, handler);
    }

    public static boolean isModLoaded()
    {
        if (!initialized) init();
        return isLoaded;
    }

    public static boolean isRenderingFirstPersonBody()
    {
        if (!isModLoaded() || isRenderingPlayerMethod == null) return false;

        try
        {
            return Boolean.TRUE.equals(isRenderingPlayerMethod.invoke(null));
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    public static boolean isRenderingFirstPersonBody(Entity entity)
    {
        return isRenderingFirstPersonBody() && entity == Minecraft.getInstance().getCameraEntity();
    }

    public static boolean showsVanillaHands(HumanoidModel<?> model)
    {
        return model != null && !model.leftArm.visible && !model.rightArm.visible;
    }

    @SuppressWarnings("unchecked")
    private static Vec3 applyOffset(AbstractClientPlayer entity, float partialTicks, Vec3 current)
    {
        if (entity == null || current == null)
        {
            return current;
        }

        if (!BenderHelper.isEntityAnimated(entity) || ModCompatManager.shouldDeferAnimation(entity))
        {
            return current;
        }

        final EntityBender<AbstractClientPlayer> bender = EntityBenderRegistry.instance.getForEntity(entity);
        final Object rawData = EntityDatabase.instance.get(entity);
        if (bender == null || !(rawData instanceof BipedEntityData<?> data) || data.body == null || data.head == null)
        {
            return current;
        }

        final float yaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        final boolean crawling = (entity.isVisuallySwimming() && !entity.isInWater()) || CrawlCompat.isCrawling(entity);

        final PoseStack bendsPose = new PoseStack();
        bendsPose.mulPose(Axis.YP.rotationDegrees(-yaw));
        bender.applyLocalTransform((EntityData<AbstractClientPlayer>) rawData, entity, partialTicks, bendsPose);
        bendsPose.mulPose(Axis.YP.rotationDegrees(yaw));
        applyVanillaRotations(bendsPose, entity, yaw, partialTicks, crawling);
        ArmorPoseHelper.applyPartTransform(bendsPose, data.body, true);
        ArmorPoseHelper.applyPartTransform(bendsPose, data.head, true);
        final Vector3f bendsNeck = bendsPose.last().pose().transformPosition(new Vector3f());

        final PoseStack vanillaPose = new PoseStack();
        applyVanillaRotations(vanillaPose, entity, yaw, partialTicks, true);
        final Vector3f vanillaNeck = vanillaPose.last().pose().transformPosition(
                new Vector3f(0.0F, entity.isCrouching() ? CROUCHING_HEAD_Y : 0.0F, 0.0F));

        final Vector3f offset = vanillaNeck.sub(bendsNeck);
        if (!Float.isFinite(offset.lengthSquared()))
        {
            return current;
        }

        if (offset.length() > MAX_COMPENSATION)
        {
            offset.normalize(MAX_COMPENSATION);
        }

        return current.add(offset.x, offset.y, offset.z);
    }

    private static void applyVanillaRotations(PoseStack poseStack, AbstractClientPlayer entity, float yaw,
                                              float partialTicks, boolean swimRotation)
    {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));

        final float swimAmount = swimRotation ? entity.getSwimAmount(partialTicks) : 0.0F;
        if (swimAmount > 0.0F && !entity.isFallFlying())
        {
            final float target = entity.isInWater() ? -90.0F - entity.getXRot() : -90.0F;
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(swimAmount, 0.0F, target)));

            if (entity.isVisuallySwimming())
            {
                poseStack.translate(0.0F, -1.0F, 0.3F);
            }
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.scale(PLAYER_SCALE, PLAYER_SCALE, PLAYER_SCALE);
        poseStack.translate(0.0F, -1.501F, 0.0F);
    }
}
