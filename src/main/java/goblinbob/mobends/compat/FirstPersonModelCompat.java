package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class FirstPersonModelCompat
{
    private static final String MOD_ID = "firstperson";

    private static final String API_CLASS = "dev.tr7zw.firstperson.api.FirstPersonAPI";
    private static final String OFFSET_HANDLER_CLASS = "dev.tr7zw.firstperson.api.PlayerOffsetHandler";

    private static final float MODEL_SCALE = 0.9375F / 16.0F;
    private static final float ENTITY_SCALE = 1.0F / 16.0F;
    private static final float DEGREES_TO_RADIANS = (float) Math.PI / 180.0F;
    private static final double MAX_COMPENSATION = 1.5D;
    private static final float MAX_HEAD_DISPLACEMENT = 48.0F;

    private static boolean initialized = false;
    private static boolean isLoaded = false;
    private static boolean offsetHandlerRegistered = false;

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
            offsetHandlerRegistered = true;
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

    public static boolean isEnabled()
    {
        if (!isModLoaded() || isEnabledMethod == null) return false;

        try
        {
            return Boolean.TRUE.equals(isEnabledMethod.invoke(null));
        }
        catch (Throwable e)
        {
            return false;
        }
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

        Object rawData = EntityDatabase.instance.get(entity);
        if (!(rawData instanceof BipedEntityData<?>))
        {
            return current;
        }

        BipedEntityData<?> data = (BipedEntityData<?>) rawData;
        ModelPartTransform body = data.body;
        ModelPartTransform head = data.head;
        if (body == null || head == null)
        {
            return current;
        }

        Quaternion bodyRotation = body.rotation.getSmooth();

        float bodyPivotX = body.globalOffset.x + (body.position.x + body.offset.x) * body.offsetScale;
        float bodyPivotZ = body.globalOffset.z + (body.position.z + body.offset.z) * body.offsetScale;

        float[] neck = rotateVectorByQuaternion(bodyRotation,
                (head.position.x + head.offset.x) * head.offsetScale,
                (head.position.y + head.offset.y) * head.offsetScale,
                (head.position.z + head.offset.z) * head.offsetScale);

        float headX = bodyPivotX + neck[0];
        float headZ = bodyPivotZ + neck[2];

        if (Math.abs(headX) > MAX_HEAD_DISPLACEMENT || Math.abs(headZ) > MAX_HEAD_DISPLACEMENT)
        {
            return current;
        }

        float entityX = data.globalOffset.getX() + data.localOffset.getX();
        float entityZ = data.globalOffset.getZ() + data.localOffset.getZ();

        float yaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot) * DEGREES_TO_RADIANS;
        float cos = Mth.cos(yaw);
        float sin = Mth.sin(yaw);

        double offsetX = -MODEL_SCALE * (headX * cos + headZ * sin)
                - ENTITY_SCALE * (entityX * cos - entityZ * sin);
        double offsetZ = MODEL_SCALE * (-headX * sin + headZ * cos)
                - ENTITY_SCALE * (entityX * sin + entityZ * cos);

        double length = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
        if (length > MAX_COMPENSATION)
        {
            double factor = MAX_COMPENSATION / length;
            offsetX *= factor;
            offsetZ *= factor;
        }

        return current.add(offsetX, 0.0D, offsetZ);
    }

    private static float[] rotateVectorByQuaternion(Quaternion q, float x, float y, float z)
    {
        float tx = 2.0F * (q.y * z - q.z * y);
        float ty = 2.0F * (q.z * x - q.x * z);
        float tz = 2.0F * (q.x * y - q.y * x);
        return new float[]{
                x + q.w * tx + (q.y * tz - q.z * ty),
                y + q.w * ty + (q.z * tx - q.x * tz),
                z + q.w * tz + (q.x * ty - q.y * tx)
        };
    }
}
