package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

public final class LeaningOrNotCompat
{
    private static final Map<BipedEntityData<?>, Lean> APPLIED = new WeakHashMap<>();

    private static boolean initialized = false;
    private static Method leanAngleMethod;
    private static Method bodyDropMethod;
    private static Object legFollowAngle;
    private static Method configGetMethod;

    private LeaningOrNotCompat()
    {
    }

    private record Lean(ModelPartTransform leg, float bodyRoll, float legRoll, float drop)
    {
    }

    private static void init()
    {
        initialized = true;

        if (!Platform.isModLoaded("leaningornot"))
        {
            return;
        }

        try
        {
            final Class<?> leaningData = Class.forName("com.raiiiden.leaningornot.client.ClientLeaningData");
            leanAngleMethod = leaningData.getMethod("getRenderLeanAngle", AbstractClientPlayer.class);
            bodyDropMethod = leaningData.getMethod("getUpperBodyDropPixels", AbstractClientPlayer.class, float.class);

            final Object common = Class.forName("com.raiiiden.leaningornot.config.LeaningOrNotConfig")
                    .getField("COMMON").get(null);
            legFollowAngle = common.getClass().getField("thirdPersonLegFollowAngle").get(common);
            configGetMethod = legFollowAngle.getClass().getMethod("get");
        }
        catch (Throwable e)
        {
            leanAngleMethod = null;
        }
    }

    public static void removeLean(BipedEntityData<?> data)
    {
        final Lean lean = APPLIED.isEmpty() ? null : APPLIED.remove(data);
        if (lean != null)
        {
            apply(data, lean, -1.0F);
        }
    }

    public static void applyLean(BipedEntityData<?> data)
    {
        if (!initialized)
        {
            init();
        }

        if (leanAngleMethod == null || !(data.getEntity() instanceof AbstractClientPlayer player))
        {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();
        if (player == mc.player && mc.options.getCameraType().isFirstPerson())
        {
            return;
        }

        try
        {
            final float angle = (Float) leanAngleMethod.invoke(null, player);
            if (Math.abs(angle) < 0.001F)
            {
                return;
            }

            final float legFollow = ((Number) configGetMethod.invoke(legFollowAngle)).floatValue();
            final Lean lean = new Lean(angle > 0.0F ? data.rightLeg : data.leftLeg,
                    -30.0F * Mth.clamp(angle / 30.0F, -2.0F, 2.0F),
                    20.0F * Mth.clamp(angle, -legFollow, legFollow) / 30.0F,
                    (Float) bodyDropMethod.invoke(null, player, angle));

            apply(data, lean, 1.0F);
            APPLIED.put(data, lean);
        }
        catch (Throwable e)
        {
            leanAngleMethod = null;
        }
    }

    private static void apply(BipedEntityData<?> data, Lean lean, float direction)
    {
        data.body.rotation.rotateAll(lean.bodyRoll() * direction, 0.0F, 0.0F, 1.0F);
        lean.leg().rotation.rotateAll(lean.legRoll() * direction, 0.0F, 0.0F, 1.0F);
        data.body.offset.set(data.body.offset.x, data.body.offset.y + lean.drop() * direction, data.body.offset.z);
    }
}
