package goblinbob.mobends.core.client.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class GuiCustomButton extends Button
{

    private final Minecraft mc;

    public GuiCustomButton(int width, int height, String text)
    {
        super(0, 0, width, height, Component.literal(text), button -> {}, DEFAULT_NARRATION);
        mc = Minecraft.getInstance();
    }

    public GuiCustomButton setButtonPosition(int x, int y)
    {
        this.setX(x);
        this.setY(y);
        return this;
    }

    public void drawButton(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        this.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public boolean mousePressed(int mouseX, int mouseY)
    {
        final boolean clicked = this.isMouseOver(mouseX, mouseY);

        if (clicked)
        {
            this.playDownSound(mc.getSoundManager());
        }

        return clicked;
    }

    public GuiCustomButton setText(String text)
    {
        this.setMessage(Component.literal(text));
        return this;
    }

}
