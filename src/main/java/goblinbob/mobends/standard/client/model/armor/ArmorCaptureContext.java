package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.VertexConsumer;

import javax.annotation.Nullable;

public class ArmorCaptureContext
{
    private static VertexConsumer active;

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

    public static void noteRenderType(Object renderType)
    {
        if (active instanceof CapturingVertexConsumer capturing)
        {
            capturing.setCurrentRenderType(renderType instanceof net.minecraft.client.renderer.RenderType type
                    ? type
                    : null);
        }
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

    @Nullable
    public static VertexConsumer begin(VertexConsumer consumer)
    {
        VertexConsumer previous = active;
        active = consumer;
        return previous;
    }

    public static void end(@Nullable VertexConsumer previous)
    {
        active = previous;
    }

    @Nullable
    public static VertexConsumer active()
    {
        return active;
    }

    public static boolean isActive()
    {
        return active != null;
    }
}
