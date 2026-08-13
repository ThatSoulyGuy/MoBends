package goblinbob.mobends.core.client.gui.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public final class GuiTooltip
{
    @Nullable
    private static String pending;

    private GuiTooltip()
    {
    }

    public static void request(@Nullable String text)
    {
        pending = text;
    }

    public static void clear()
    {
        pending = null;
    }

    public static void renderPending(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        if (pending == null || pending.isEmpty()) return;

        guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.literal(pending), mouseX, mouseY);
        pending = null;
    }
}
