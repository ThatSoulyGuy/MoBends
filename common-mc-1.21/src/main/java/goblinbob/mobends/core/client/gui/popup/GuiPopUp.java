package goblinbob.mobends.core.client.gui.popup;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.core.client.gui.elements.GuiCustomButton;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class GuiPopUp
{

    protected int x, y;
    protected int width;
    protected int height;
    private String[] title;
    protected Font font;
    private List<Button> buttons;

    public GuiPopUp(String title, ButtonProps[] buttonProps)
    {
        this(title, 120, 60, buttonProps);
    }

    public GuiPopUp(String title, int width, int height, ButtonProps[] buttonProps)
    {
        this.font = Minecraft.getInstance().font;
        this.width = width;
        this.height = height;
        this.title = GuiUtil.wrapText(font, title, width - 20);
        this.buttons = new ArrayList<>();

        for (ButtonProps buttonProp : buttonProps)
        {
            this.buttons.add(new Button(font, buttonProp));
        }
    }

    public void initGui(int x, int y)
    {
        this.x = x - width / 2;
        this.y = y - height / 2;

        int offset = this.x + width - 5;
        for (Button button : buttons)
        {
            button.buttonUI.setPosition(offset - button.buttonUI.getWidth(), this.y + height - 25);
            offset -= button.buttonUI.getWidth() + 5;
        }
    }

    public void update(int mouseX, int mouseY)
    {
        // No default functionality.
    }

    public void display(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GuiBendsMenu.ICONS_TEXTURE);
        /* Top-Left		*/
        Draw.texturedModalRect(x - 4, y - 4, 60, 64, 4, 4);
        /* Top 			*/
        Draw.texturedModalRect(x, y - 4, width, 4, 64, 64, 1, 4);
        /* Top-Right 	*/
        Draw.texturedModalRect(x + width, y - 4, 65, 64, 4, 4);
        /* Right 		*/
        Draw.texturedModalRect(x + width, y, 4, height, 65, 68, 4, 1);
        /* Bottom-Right */
        Draw.texturedModalRect(x + width, y + height, 65, 69, 4, 4);
        /* Bottom		*/
        Draw.texturedModalRect(x, y + height, width, 4, 64, 69, 1, 4);
        /* Bottom-Left	*/
        Draw.texturedModalRect(x - 4, y + height, 60, 69, 4, 4);
        /* Left 		*/
        Draw.texturedModalRect(x - 4, y, 4, height, 60, 68, 4, 1);
        /* Inside		*/
        Draw.texturedModalRect(x, y, width, height, 64, 68, 1, 1);

        int yOffset = 6;
        for (String line : title)
        {
            guiGraphics.drawString(font, line, x + (width - font.width(line)) / 2, y + yOffset, 0xffffff, true);
            yOffset += 9;
        }

        for (Button button : buttons)
        {
            button.buttonUI.drawButton(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int state)
    {
        for (Button button : buttons)
        {
            if (button.buttonUI.mousePressed(mouseX, mouseY))
            {
                // Performing the button action.
                button.props.action.performAction();
                return;
            }
        }
    }

    @FunctionalInterface
    public interface ButtonAction
    {
        void performAction();
    }

    public static class ButtonProps
    {
        private String label;
        private ButtonAction action;

        public ButtonProps(String label, ButtonAction action)
        {
            this.label = label;
            this.action = action;
        }
    }

    public static class Button
    {
        public ButtonProps props;
        public GuiCustomButton buttonUI;

        public Button(Font font, ButtonProps props)
        {
            this.props = props;
            int labelWidth = font.width(props.label);
            this.buttonUI = new GuiCustomButton(Math.max(labelWidth + 10, 50), 20, props.label);
        }
    }

}
