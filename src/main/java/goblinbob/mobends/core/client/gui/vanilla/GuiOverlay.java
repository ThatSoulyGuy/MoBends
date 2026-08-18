package goblinbob.mobends.core.client.gui.vanilla;

import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;

public final class GuiOverlay
{
    public static final int Z_OFFSET = 300;

    public interface Layer
    {
        void renderOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY);

        boolean handleOverlayClick(double mouseX, double mouseY, int button);
    }

    @Nullable
    private static Layer pending;

    private GuiOverlay()
    {
    }

    public static void request(Layer layer)
    {
        pending = layer;
    }

    public static void release(Layer layer)
    {
        if (pending == layer)
        {
            pending = null;
        }
    }

    public static void clear()
    {
        pending = null;
    }

    public static void renderPending(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        if (pending == null) return;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, Z_OFFSET);

        pending.renderOverlay(guiGraphics, mouseX, mouseY);

        guiGraphics.pose().popPose();
    }

    public static boolean clickPending(double mouseX, double mouseY, int button)
    {
        return pending != null && pending.handleOverlayClick(mouseX, mouseY, button);
    }
}
