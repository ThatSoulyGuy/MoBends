package goblinbob.mobends.core.client.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class GuiCompactTextField extends EditBox
{

    private String placeholderText;

    public GuiCompactTextField(Font font, int width, int height)
    {
        super(font, 0, 0, width, height, Component.empty());
    }

    public GuiCompactTextField(Font font, int x, int y, int width, int height)
    {
        super(font, x, y, width, height, Component.empty());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        if (!this.isFocused() && this.getValue().isEmpty() && this.placeholderText != null)
        {
            Font font = Minecraft.getInstance().font;
            guiGraphics.drawString(font, this.placeholderText, this.getX() + 4, this.getY() + (this.getHeight() - 8) / 2, 0x707070, true);
        }
    }

    public GuiCompactTextField setPlaceholderText(String placeholderText)
    {
        this.placeholderText = placeholderText;
        return this;
    }

    public void setPosition(int x, int y)
    {
        this.setX(x);
        this.setY(y);
    }

}
