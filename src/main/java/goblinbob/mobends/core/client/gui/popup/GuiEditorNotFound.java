package goblinbob.mobends.core.client.gui.popup;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

public class GuiEditorNotFound extends GuiPopUp
{
    boolean errorOccurred;

    public GuiEditorNotFound(ButtonAction onBack, ButtonAction onGetEditor)
    {
        super(I18n.get("mobends.gui.editornotfound"), 200, 100, new ButtonProps[] {
                new ButtonProps(I18n.get("mobends.gui.back"), onBack),
                new ButtonProps(I18n.get("mobends.gui.geteditor"), onGetEditor)
        });
    }

    public void setErrorOccurred(boolean errorOccurred)
    {
        this.errorOccurred = errorOccurred;
    }

    @Override
    public void display(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        super.display(guiGraphics, mouseX, mouseY, partialTicks);

        if (errorOccurred)
        {
            String message = "There seems to be something wrong. Please check your internet connection," +
                    " or contact the developers.";
            guiGraphics.drawString(font, message, 5, 5, 0xffff0000, true);
        }
    }
}
