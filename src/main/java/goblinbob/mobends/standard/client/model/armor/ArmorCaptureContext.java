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
 */
public class ArmorCaptureContext
{
    private static final ThreadLocal<VertexConsumer> ACTIVE = new ThreadLocal<>();

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
