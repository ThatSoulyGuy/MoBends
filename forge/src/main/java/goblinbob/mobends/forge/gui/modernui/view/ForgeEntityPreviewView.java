package goblinbob.mobends.forge.gui.modernui.view;

import goblinbob.mobends.api.gui.IEntityRenderer;
import icyllis.arc3d.core.ImageInfo;
import icyllis.arc3d.core.Matrix4;
import icyllis.arc3d.core.Rect2i;
import icyllis.arc3d.engine.DirectContext;
import icyllis.arc3d.engine.DrawableInfo;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.CustomDrawable;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.RectF;
import icyllis.modernui.view.View;
import net.minecraft.client.Minecraft;

/**
 * Custom Modern UI View that renders Minecraft entity previews using
 * Modern UI's CustomDrawable API. This properly integrates MC's immediate-mode
 * GL rendering into Modern UI's deferred canvas pipeline, avoiding the
 * "Unbalanced save-restore pair" crash.
 */
public class ForgeEntityPreviewView extends View implements CustomDrawable
{
    private final IEntityRenderer renderer;
    private final Paint backgroundPaint;
    private final RectF bounds = new RectF();

    public ForgeEntityPreviewView(icyllis.modernui.core.Context context, IEntityRenderer renderer)
    {
        super(context);
        this.renderer = renderer;
        this.backgroundPaint = new Paint();
        this.backgroundPaint.setColor(0xFF1A1A1A);
    }

    public void setBackgroundColor(int color)
    {
        this.backgroundPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        // Draw background via canvas (deferred, safe)
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        if (!renderer.hasEntity())
            return;

        // Advance animation state
        renderer.update();

        // Delegate entity rendering to the canvas pipeline via CustomDrawable.
        // This ensures MC's GL calls happen at the right point during canvas flush,
        // with proper state management around them.
        bounds.set(0, 0, getWidth(), getHeight());
        canvas.drawCustomDrawable(this);

        // Request continuous redraw for animation
        invalidate();
    }

    @Override
    public RectF getBounds()
    {
        return bounds;
    }

    @Override
    public DrawHandler snapDrawHandler(int flags, Matrix4 matrix, Rect2i clip, ImageInfo info)
    {
        // Capture rendering parameters at snapshot time
        int[] location = new int[2];
        getLocationOnScreen(location);

        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        int guiX = (int) (location[0] / guiScale);
        int guiY = (int) (location[1] / guiScale);
        int guiWidth = (int) (getWidth() / guiScale);
        int guiHeight = (int) (getHeight() / guiScale);
        float partialTicks = Minecraft.getInstance().getFrameTime();

        return new DrawHandler()
        {
            @Override
            public void draw(DirectContext ctx, DrawableInfo drawableInfo)
            {
                // This runs during canvas flush with proper GL state bracketing.
                // MC rendering (GuiGraphics, shaders, scissor, etc.) is safe here.
                renderer.render(guiX, guiY, guiWidth, guiHeight, partialTicks);
            }
        };
    }
}
