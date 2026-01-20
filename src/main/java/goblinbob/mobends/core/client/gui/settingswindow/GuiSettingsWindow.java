package goblinbob.mobends.core.client.gui.settingswindow;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.core.client.gui.elements.GuiCompactTextField;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.GuiHelper;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiSettingsWindow extends Screen
{

    public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(ModStatics.MODID,
            "textures/gui/pack_window.png");
    public static final int EDITOR_WIDTH = 280;
    public static final int EDITOR_HEIGHT = 177;

    private int x, y;

    private GuiCompactTextField filterQueryInput;
    private final GuiBenderList bendsSettingsListUI = new GuiBenderList(0, 0, EDITOR_WIDTH - 10, EDITOR_HEIGHT - 10 - 20);

    private final EntityBenderRegistry.Filter filter = new EntityBenderRegistry.Filter();

    public GuiSettingsWindow()
    {
        super(Component.translatable("mobends.gui.settings"));

        fetchBenders();
    }

    @Override
    protected void init()
    {
        super.init();

        this.x = (this.width - EDITOR_WIDTH) / 2;
        this.y = (this.height - EDITOR_HEIGHT) / 2;

        clearWidgets();
        addRenderableWidget(Button.builder(Component.translatable("mobends.gui.back"), button -> goBack())
                .bounds(10, height - 30, 60, 20)
                .build());
        filterQueryInput = new GuiCompactTextField(this.font, x + 6, y + 6, 150, 16);
        filterQueryInput.setFocused(true);
        filterQueryInput.setPlaceholderText(I18n.get("mobends.gui.search"));
        addRenderableWidget(filterQueryInput);
        bendsSettingsListUI.initGui(this.x + 9, this.y + 9 + 20);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(guiGraphics);

        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        // Container
        Draw.borderBox(x + 4, y + 4, EDITOR_WIDTH, EDITOR_HEIGHT, 4, 36, 126);
        // Title background
        Draw.texturedModalRect(x, y - 13, 101, 0, 4, 16);
        Draw.texturedModalRect(x + 4, y - 13, EDITOR_WIDTH - 16, 16, 105, 0, 1, 16);
        Draw.texturedModalRect(x + EDITOR_WIDTH - 17, y - 13, 106, 0, 19, 16);

        bendsSettingsListUI.draw(guiGraphics, DataUpdateHandler.partialTicks);

        guiGraphics.drawString(font, I18n.get("mobends.gui.settings"), this.x + 6, this.y - 9, 0xffffff, true);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (this.minecraft == null) return;

        final double mouseX = this.minecraft.mouseHandler.xpos() * (double)this.width / (double)this.minecraft.getWindow().getScreenWidth();
        final double mouseY = this.minecraft.mouseHandler.ypos() * (double)this.height / (double)this.minecraft.getWindow().getScreenHeight();

        bendsSettingsListUI.update((int)mouseX, (int)mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) return true;

        bendsSettingsListUI.handleMouseClicked((int)mouseX, (int)mouseY, mouseButton);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton)
    {
        super.mouseReleased(mouseX, mouseY, mouseButton);

        bendsSettingsListUI.handleMouseReleased((int)mouseX, (int)mouseY, mouseButton);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (bendsSettingsListUI.handleMouseScroll(mouseX, mouseY, delta))
        {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        // Let the text field handle key presses first
        if (filterQueryInput.isFocused() && filterQueryInput.keyPressed(keyCode, scanCode, modifiers))
        {
            checkFilterChanged();
            return true;
        }

        // ESC key
        if (keyCode == 256)
        {
            Core.saveConfiguration();
            GuiHelper.closeGui();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        if (filterQueryInput.isFocused() && filterQueryInput.charTyped(chr, modifiers))
        {
            checkFilterChanged();
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    private void checkFilterChanged()
    {
        if (!filterQueryInput.getValue().equals(filter.query))
        {
            filter.query = filterQueryInput.getValue();
            fetchBenders();
        }
    }

    private void goBack()
    {
        Core.saveConfiguration();
        this.minecraft.setScreen(new GuiBendsMenu());
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    public void fetchBenders()
    {
        bendsSettingsListUI.clearElements();
        for (final EntityBender<?> bender : EntityBenderRegistry.instance.getRegistered(filter))
        {
            bendsSettingsListUI.addElement(new GuiBenderSettings(bender));
        }
    }

}
