package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.VertexConsumer;

import javax.annotation.Nullable;

public class ArmorCaptureContext
{
    private static final ThreadLocal<VertexConsumer> ACTIVE = new ThreadLocal<>();

    public static void begin(VertexConsumer consumer)
    {
        ACTIVE.set(consumer);
    }

    public static void end()
    {
        ACTIVE.remove();
    }

    @Nullable
    public static VertexConsumer active()
    {
        return ACTIVE.get();
    }

    public static boolean isActive()
    {
        return ACTIVE.get() != null;
    }
}
