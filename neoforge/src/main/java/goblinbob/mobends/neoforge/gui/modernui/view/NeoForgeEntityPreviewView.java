package goblinbob.mobends.neoforge.gui.modernui.view;

import goblinbob.mobends.core.client.gui.modernui.EntityPreviewRenderer;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.view.View;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Custom Modern UI View that renders Minecraft entity previews.
 * Bridges Modern UI's rendering system with Minecraft's entity rendering.
 */
public class NeoForgeEntityPreviewView extends View
{
    private final EntityPreviewRenderer renderer;
    private final Paint backgroundPaint;
    private int backgroundColor = 0xFF1A1A1A;

    public NeoForgeEntityPreviewView(icyllis.modernui.core.Context context, EntityPreviewRenderer renderer)
    {
        super(context);
        this.renderer = renderer;
        this.backgroundPaint = new Paint();
        this.backgroundPaint.setColor(backgroundColor);
    }

    public void setBackgroundColor(int color)
    {
        this.backgroundColor = color;
        this.backgroundPaint.setColor(color);
        invalidate();
    }

    public EntityPreviewRenderer getRenderer()
    {
        return renderer;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas)
    {
        super.onDraw(canvas);

        // Draw background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        // The actual entity rendering needs to happen in the Minecraft render cycle
        // We mark this view for Minecraft rendering by scheduling a post-draw callback
        if (renderer.hasEntity())
        {
            // Get absolute screen position
            int[] location = new int[2];
            getLocationOnScreen(location);

            // Store render info for later
            pendingRenderX = location[0];
            pendingRenderY = location[1];
            pendingRenderWidth = getWidth();
            pendingRenderHeight = getHeight();
            hasPendingRender = true;
        }
    }

    // Pending render state - used to communicate with Minecraft render pass
    private static int pendingRenderX;
    private static int pendingRenderY;
    private static int pendingRenderWidth;
    private static int pendingRenderHeight;
    private static boolean hasPendingRender = false;
    private static EntityPreviewRenderer pendingRenderer = null;

    /**
     * Called from the Minecraft render pass to actually render the entity.
     * This should be called after Modern UI renders but before the frame ends.
     */
    public static void renderPendingEntity(GuiGraphics guiGraphics, float partialTicks)
    {
        if (hasPendingRender && pendingRenderer != null)
        {
            pendingRenderer.render(guiGraphics, pendingRenderX, pendingRenderY,
                    pendingRenderWidth, pendingRenderHeight, partialTicks);
            hasPendingRender = false;
        }
    }

    /**
     * Schedules this view's entity for rendering in the next Minecraft render pass.
     */
    public void scheduleEntityRender()
    {
        if (renderer.hasEntity())
        {
            int[] location = new int[2];
            getLocationOnScreen(location);

            pendingRenderX = location[0];
            pendingRenderY = location[1];
            pendingRenderWidth = getWidth();
            pendingRenderHeight = getHeight();
            pendingRenderer = renderer;
            hasPendingRender = true;
        }
    }

    /**
     * Updates the animation state. Should be called each frame.
     */
    public void update()
    {
        renderer.update();
        // Request redraw
        invalidate();
        // Schedule entity render for next frame
        scheduleEntityRender();
    }

    /**
     * Checks if there's a pending entity render.
     */
    public static boolean hasPendingEntityRender()
    {
        return hasPendingRender;
    }

    /**
     * Clears pending render state.
     */
    public static void clearPendingRender()
    {
        hasPendingRender = false;
        pendingRenderer = null;
    }
}
