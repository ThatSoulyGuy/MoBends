package goblinbob.mobends.core.util;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class UIScissorHelper
{

    public static final UIScissorHelper INSTANCE = new UIScissorHelper();

    private int x;
    private int y;
    private int width;
    private int height;

    public UIScissorHelper()
    {
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
    }

    public void setUIBounds(int uiX, int uiY, int uiWidth, int uiHeight)
    {
        Window window = Minecraft.getInstance().getWindow();
        final int scaledWidth = window.getGuiScaledWidth();
        final int scaledHeight = window.getGuiScaledHeight();
        final int displayWidth = window.getWidth();
        final int displayHeight = window.getHeight();

        this.x = uiX * displayWidth / scaledWidth;
        this.y = (scaledHeight - uiY - uiHeight) * displayHeight / scaledHeight;
        this.width = uiWidth * displayWidth / scaledWidth;
        this.height = uiHeight * displayHeight / scaledHeight;
    }

    public void enable()
    {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, width, height);
    }

    public void disable()
    {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

}
