package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.VertexConsumer;

import javax.annotation.Nullable;

public class ArmorCaptureContext
{
    private static final ThreadLocal<VertexConsumer> ACTIVE = new ThreadLocal<>();

    private static final ThreadLocal<CapturingVertexConsumer> DISCARD =
            ThreadLocal.withInitial(CapturingVertexConsumer::new);

    public static boolean isEmissiveType(Object renderType)
    {
        if (renderType == null)
        {
            return false;
        }

        final String name = renderType.toString();
        return name.startsWith("RenderType[eyes") || name.startsWith("RenderType[emissive");
    }

    public static VertexConsumer discard()
    {
        final CapturingVertexConsumer sink = DISCARD.get();
        sink.clear();
        return sink;
    }

    private static final ThreadLocal<java.util.LinkedHashSet<net.minecraft.client.renderer.RenderType>> EMISSIVE =
            ThreadLocal.withInitial(java.util.LinkedHashSet::new);

    public static void recordEmissive(Object renderType)
    {
        if (renderType instanceof net.minecraft.client.renderer.RenderType type)
        {
            EMISSIVE.get().add(type);
        }
    }

    public static void clearEmissive()
    {
        EMISSIVE.get().clear();
    }

    public static java.util.List<net.minecraft.client.renderer.RenderType> drainEmissive()
    {
        final java.util.LinkedHashSet<net.minecraft.client.renderer.RenderType> set = EMISSIVE.get();
        if (set.isEmpty())
        {
            return java.util.Collections.emptyList();
        }
        final java.util.List<net.minecraft.client.renderer.RenderType> copy = new java.util.ArrayList<>(set);
        set.clear();
        return copy;
    }

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
