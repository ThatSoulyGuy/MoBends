package goblinbob.mobends.core.client.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class GuiSmallToggleButton
{

    protected static final ResourceLocation BUTTON_TEXTURES = ResourceLocationFactory.parse("textures/gui/widgets.png");

    private static final int WIDTH = 30;
    private static final int HEIGHT = 20;

    protected int x;
    protected int y;
    protected boolean hovered;
    protected boolean enabled;
    protected boolean toggleState;

    public GuiSmallToggleButton()
    {
        this.x = 0;
        this.y = 0;
        this.enabled = true;
        this.hovered = false;
        this.toggleState = false;
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void update(int mouseX, int mouseY)
    {
        this.hovered = mouseX >= x && mouseX <= x + WIDTH &&
                mouseY >= y && mouseY <= y + HEIGHT;
    }

    public void draw(GuiGraphics guiGraphics)
    {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int k = this.hovered ? 1 : 0;

        if (this.toggleState)
        {
            RenderSystem.setShaderColor(0.3F, 1.0F, 0.7F, 1.0F);
        }
        else
        {
            RenderSystem.setShaderColor(1.0F, 0.3F, 0.4F, 1.0F);
        }

        guiGraphics.blit(BUTTON_TEXTURES, this.x, this.y, 0, 66 + k * 20, WIDTH / 2, HEIGHT);
        guiGraphics.blit(BUTTON_TEXTURES, this.x + WIDTH / 2, this.y, 200 - WIDTH / 2, 66 + k * 20, WIDTH / 2, HEIGHT);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int l = 14737632;

        if (!this.enabled)
        {
            l = 10526880;
        }
        else if (this.hovered)
        {
            l = 16777120;
        }

        String stateText = this.toggleState ? "ON" : "OFF";
        int textWidth = font.width(stateText);
        guiGraphics.drawString(font, stateText, this.x - textWidth/2 + WIDTH /2, this.y + (HEIGHT - 8) / 2, l, false);

        RenderSystem.defaultBlendFunc();
    }

    public void setToggleState(boolean state)
    {
        this.toggleState = state;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        if (hovered && button == 0)
        {
            this.toggleState = !this.toggleState;
            return true;
        }

        return false;
    }

    public boolean getToggleState()
    {
        return this.toggleState;
    }

}
