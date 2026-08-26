package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.VertexConsumer;

import javax.annotation.Nullable;

/**
 * Redirects vertex output into a capture buffer for the duration of one armor model render.
 *
 * <p>While a capture is active, {@code BufferSourceCaptureMixin} makes every
 * {@code MultiBufferSource.BufferSource.getBuffer} call on this thread return the capture consumer,
 * whatever render type was asked for. That is deliberate — the point is to intercept a modded armor
 * model's vertices rather than let it draw — and the blast radius is bounded by the fact that a
 * capture only spans a synchronous model render inside the armor layer.
 *
 * <p>{@link #begin} returns the previously active consumer and {@link #end} takes it back, so a
 * nested capture restores its parent rather than clearing the slot outright. Callers must pair them
 * in a {@code try/finally}.
 *
 * <p>The emissive helpers are a separate concern that rides along on the same per-thread scope:
 * a capture notes which render types were emissive so the glow pass can replay them afterwards.
 */
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

    /**
     * Starts capturing into {@code consumer}.
     *
     * @return the consumer that was active before, which must be handed back to {@link #end}
     */
    @Nullable
    public static VertexConsumer begin(VertexConsumer consumer)
    {
        VertexConsumer previous = ACTIVE.get();
        ACTIVE.set(consumer);
        return previous;
    }

    /**
     * Stops capturing, restoring whatever was active beforehand.
     *
     * @param previous the value {@link #begin} returned
     */
    public static void end(@Nullable VertexConsumer previous)
    {
        if (previous == null)
        {
            ACTIVE.remove();
        }
        else
        {
            ACTIVE.set(previous);
        }
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
