package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.core.client.gui.MoBendsScreenBuilder;

import goblinbob.mobends.core.util.ScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class VanillaMoBendsScreen extends Screen
{
    private final MoBendsScreenBuilder screenBuilder;
    @Nullable
    private VanillaView rootView;

    public VanillaMoBendsScreen(MoBendsScreenBuilder screenBuilder)
    {
        super(Component.literal(screenBuilder.getTitle()));
        this.screenBuilder = screenBuilder;
    }

    @Override
    public void removed()
    {
        super.removed();
        screenBuilder.dispose();
    }

    protected void init()
    {
        super.init();

        VanillaViewFactory factory = new VanillaViewFactory();
        VanillaView content = screenBuilder.buildContent(factory);

        this.rootView = content;
        rootView.measure(this.width, this.height);
        rootView.layout(0, 0, this.width, this.height);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        ScreenHelper.renderBackground(this, guiGraphics, mouseX, mouseY, partialTick);

        if (rootView != null)
        {
            rootView.measure(this.width, this.height);
            rootView.layout(0, 0, this.width, this.height);

            rootView.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (rootView != null && rootView.handleClick(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (rootView != null)
        {
            rootView.handleMouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (rootView != null && rootView.handleMouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        if (rootView != null && rootView.handleMouseScrolled(mouseX, mouseY, scrollY)) return true;
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY)
    {
        if (rootView != null && rootView.handleMouseScrolled(mouseX, mouseY, scrollY)) return true;
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (rootView != null && rootView.handleKeyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char ch, int modifiers)
    {
        if (rootView != null && rootView.handleCharTyped(ch, modifiers)) return true;
        return super.charTyped(ch, modifiers);
    }

    public boolean isPauseScreen()
    {
        return false;
    }
}
