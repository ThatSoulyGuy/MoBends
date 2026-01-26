package goblinbob.mobends.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.WebAPI;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.gui.elements.GuiEntityPreview;
import goblinbob.mobends.core.client.gui.elements.GuiTab;
import goblinbob.mobends.core.client.gui.elements.GuiTabBar;
import goblinbob.mobends.core.client.gui.elements.GuiTooltip;
import goblinbob.mobends.core.client.gui.packswindow.GuiLocalPacks;
import goblinbob.mobends.core.client.gui.popup.GuiEditorNotFound;
import goblinbob.mobends.core.client.gui.popup.GuiPopUp;
import goblinbob.mobends.core.client.gui.settingswindow.GuiExpandableBenderList;
import goblinbob.mobends.core.client.gui.settingswindow.GuiExpandableBenderSettings;
import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.GuiHelper;
import goblinbob.mobends.core.util.ScreenHelper;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * The redesigned main MoBends screen with tab-based navigation.
 * Features:
 * - Tab-based single screen (Settings, Packs, Customize)
 * - Entity preview panel with 3D rotating entity that plays selected animations
 * - Per-animation toggle controls
 * - Modern styling with tooltips
 */
public class GuiMoBendsScreen extends Screen
{
    private static final ResourceLocation LOGO_TEXTURE = ModStatics.getResource("textures/gui/title.png");

    // Layout constants
    private static final int CONTENT_PADDING = 10;
    private static final int HEADER_HEIGHT = 50;
    private static final int TAB_BAR_Y = 40;

    // Colors
    private static final int COLOR_SETTINGS = 0xFFDA3A00;
    private static final int COLOR_PACKS = 0xFF4577DE;
    private static final int COLOR_CUSTOMIZE = 0xFF26DAA3;
    private static final int BG_PANEL = 0xCC1A1A1A;
    private static final int BG_CONTENT = 0xAA222222;

    // Tab indices
    private static final int TAB_SETTINGS = 0;
    private static final int TAB_PACKS = 1;
    private static final int TAB_CUSTOMIZE = 2;

    // Components
    private GuiTabBar tabBar;
    private GuiEntityPreview entityPreview;
    private GuiExpandableBenderList benderList;
    private GuiLocalPacks packsPanel;
    private EditBox searchBox;
    private GuiPopUp popUp;

    // State
    private int contentX;
    private int contentY;
    private int contentWidth;
    private int contentHeight;
    private int previewWidth;

    @Nullable
    private EntityBender<?> selectedBender;
    private final EntityBenderRegistry.Filter filter = new EntityBenderRegistry.Filter();

    public GuiMoBendsScreen()
    {
        super(Component.translatable("mobends.gui.title"));
    }

    @Override
    protected void init()
    {
        super.init();
        clearWidgets();

        // Calculate layout
        int screenPadding = Math.max(20, (width - 700) / 2);
        contentX = screenPadding;
        contentY = HEADER_HEIGHT + GuiTab.HEIGHT + 10;
        contentWidth = width - screenPadding * 2;
        contentHeight = height - contentY - 20;
        previewWidth = Math.min(180, contentWidth / 3);

        // Initialize tab bar
        tabBar = new GuiTabBar();
        tabBar.addTab("mobends.gui.section.settings", COLOR_SETTINGS);
        if (NetworkConfiguration.instance.areBendsPacksAllowed())
        {
            tabBar.addTab("mobends.gui.section.packs", COLOR_PACKS);
        }
        tabBar.addTab("mobends.gui.section.customize", COLOR_CUSTOMIZE);
        tabBar.setOnTabChanged(this::onTabChanged);
        tabBar.initGui(contentX, TAB_BAR_Y, contentWidth);

        // Initialize entity preview (right side)
        // Leave room for the hint text at the bottom (30 pixels instead of 60)
        entityPreview = new GuiEntityPreview(previewWidth - 20, contentHeight - 80);
        entityPreview.initGui(contentX + contentWidth - previewWidth + 10, contentY + 50);

        // Calculate left panel width (area for bender list and controls)
        int leftPanelWidth = contentWidth - previewWidth - 20;

        // Initialize search box at top
        int searchWidth = Math.min(leftPanelWidth - 20, 250);
        searchBox = new EditBox(font, contentX + 10, contentY + 10, searchWidth, 18, Component.empty());
        searchBox.setHint(Component.translatable("mobends.gui.search"));
        searchBox.setMaxLength(50);
        searchBox.setResponder(this::onSearchChanged);
        addRenderableWidget(searchBox);

        // Initialize bender list (left side, below search)
        int listY = contentY + 35;
        int listHeight = contentHeight - 45;
        benderList = new GuiExpandableBenderList(0, 0, leftPanelWidth, listHeight);
        benderList.initGui(contentX + 10, listY);
        benderList.setOnBenderSelected(this::onBenderSelected);
        benderList.setOnAnimationSelected(this::onAnimationSelected);
        fetchBenders();

        // Initialize popup if present
        if (popUp != null)
        {
            popUp.initGui(width / 2, height / 2);
        }

        // Select first bender by default
        Collection<EntityBender<?>> benders = EntityBenderRegistry.instance.getRegistered(filter);
        if (!benders.isEmpty())
        {
            selectedBender = benders.iterator().next();
            entityPreview.setBender(selectedBender);
        }
    }

    private void onTabChanged(GuiTab tab)
    {
        // Reset search when changing tabs
        if (searchBox != null)
        {
            searchBox.setValue("");
        }
    }

    private void onSearchChanged(String query)
    {
        if (!query.equals(filter.query))
        {
            filter.query = query;
            fetchBenders();
        }
    }

    private void onBenderSelected(EntityBender<?> bender)
    {
        selectedBender = bender;
        entityPreview.setBender(bender);
    }

    private void onAnimationSelected(String animationType)
    {
        // Update the entity preview to play the selected animation
        entityPreview.setAnimationModeByName(animationType);
    }

    private void fetchBenders()
    {
        if (benderList == null) return;

        benderList.clearElements();
        for (EntityBender<?> bender : EntityBenderRegistry.instance.getRegistered(filter))
        {
            benderList.addElement(new GuiExpandableBenderSettings(bender, benderList::notifyBenderSelected));
        }
    }

    @Override
    public void tick()
    {
        super.tick();

        if (minecraft == null) return;

        double mouseX = minecraft.mouseHandler.xpos() * (double) width / (double) minecraft.getWindow().getScreenWidth();
        double mouseY = minecraft.mouseHandler.ypos() * (double) height / (double) minecraft.getWindow().getScreenHeight();

        if (popUp != null)
        {
            popUp.update((int) mouseX, (int) mouseY);
            return;
        }

        tabBar.update((int) mouseX, (int) mouseY);
        benderList.update((int) mouseX, (int) mouseY);
        entityPreview.update((int) mouseX, (int) mouseY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        ScreenHelper.renderBackground(this, guiGraphics, mouseX, mouseY, partialTicks);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw logo
        int logoWidth = 167;
        int logoHeight = 37;
        guiGraphics.blit(LOGO_TEXTURE, (width - logoWidth) / 2, 5, 0, 0, logoWidth, logoHeight, logoWidth, logoHeight);

        // Draw tab bar
        tabBar.draw(guiGraphics, mouseX, mouseY);

        // Draw content panel background
        Draw.rectangle(contentX, contentY, contentX + contentWidth, contentY + contentHeight, BG_PANEL);

        int selectedTab = tabBar.getSelectedIndex();
        if (selectedTab == TAB_SETTINGS || (selectedTab == TAB_CUSTOMIZE && !NetworkConfiguration.instance.areBendsPacksAllowed()))
        {
            renderSettingsTab(guiGraphics, mouseX, mouseY, partialTicks);
        }
        else if (selectedTab == TAB_PACKS || (selectedTab == 1 && NetworkConfiguration.instance.areBendsPacksAllowed()))
        {
            renderPacksTab(guiGraphics, mouseX, mouseY, partialTicks);
        }
        else
        {
            renderCustomizeTab(guiGraphics, mouseX, mouseY, partialTicks);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Draw popup overlay
        if (popUp != null)
        {
            ScreenHelper.renderBackground(this, guiGraphics, mouseX, mouseY, partialTicks);
            popUp.display(guiGraphics, mouseX, mouseY, partialTicks);
        }

        // Render any queued tooltips last (so they appear on top)
        if (popUp == null)
        {
            GuiTooltip.renderQueuedTooltip(guiGraphics);
        }
    }

    private void renderSettingsTab(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        // Draw preview panel background (right side)
        int previewPanelX = contentX + contentWidth - previewWidth;
        Draw.rectangle(previewPanelX, contentY, previewPanelX + previewWidth, contentY + contentHeight, BG_CONTENT);

        // Draw preview label
        String previewLabel = selectedBender != null ? selectedBender.getLocalizedName() : I18n.get("mobends.gui.preview");
        int labelWidth = font.width(previewLabel);
        guiGraphics.drawString(font, previewLabel, previewPanelX + (previewWidth - labelWidth) / 2, contentY + 10, 0xFFFFFF, true);

        // Draw entity preview
        entityPreview.draw(guiGraphics, partialTicks);

        // Draw controls hint at bottom of preview panel
        String hint = I18n.get("mobends.gui.preview.hint");
        int hintWidth = font.width(hint);
        guiGraphics.drawString(font, hint, previewPanelX + (previewWidth - hintWidth) / 2, contentY + contentHeight - 20, 0x888888, false);

        // Draw separator line between left and right panels
        Draw.rectangle(previewPanelX - 1, contentY + 5, previewPanelX, contentY + contentHeight - 5, 0xFF444444);

        // Draw bender list (left side)
        benderList.draw(guiGraphics, partialTicks);
    }

    private void renderPacksTab(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        // Render packs content (reuse existing packs window logic)
        String message = I18n.get("mobends.gui.packs.coming_soon");
        int msgWidth = font.width(message);
        guiGraphics.drawString(font, message, contentX + (contentWidth - msgWidth) / 2, contentY + contentHeight / 2, 0xFFFFFF, true);
    }

    private void renderCustomizeTab(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        String message = I18n.get("mobends.gui.customize.editor_info");
        int msgWidth = font.width(message);
        guiGraphics.drawString(font, message, contentX + (contentWidth - msgWidth) / 2, contentY + contentHeight / 2 - 20, 0xFFFFFF, true);

        IAnimationEditor editor = AnimationEditorRegistry.INSTANCE.getPrimaryEditor();
        if (editor != null)
        {
            String editorName = "Animation Editor Available";
            int editorWidth = font.width(editorName);
            guiGraphics.drawString(font, editorName, contentX + (contentWidth - editorWidth) / 2, contentY + contentHeight / 2, 0x88FF88, true);
        }
        else
        {
            String noEditor = I18n.get("mobends.gui.customize.no_editor");
            int noEditorWidth = font.width(noEditor);
            guiGraphics.drawString(font, noEditor, contentX + (contentWidth - noEditorWidth) / 2, contentY + contentHeight / 2, 0xFF8888, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int x = (int) mouseX;
        int y = (int) mouseY;

        if (popUp != null)
        {
            popUp.mouseClicked(x, y, button);
            return true;
        }

        if (tabBar.mouseClicked(x, y, button))
        {
            return true;
        }

        int selectedTab = tabBar.getSelectedIndex();
        if (selectedTab == TAB_SETTINGS)
        {
            if (entityPreview.handleMouseClicked(x, y, button))
            {
                return true;
            }
            if (benderList.handleMouseClicked(x, y, button))
            {
                return true;
            }
        }
        else if (selectedTab == getCustomizeTabIndex())
        {
            // Handle customize tab click - open editor
            IAnimationEditor editor = AnimationEditorRegistry.INSTANCE.getPrimaryEditor();
            if (editor == null)
            {
                openPopUp(new GuiEditorNotFound(this::closePopUp, () -> {
                    String downloadUrl = WebAPI.INSTANCE.getOfficialAnimationEditorUrl();
                    if (downloadUrl == null || !GuiHelper.openUrlInBrowser(downloadUrl))
                    {
                        ((GuiEditorNotFound) popUp).setErrorOccurred(true);
                    }
                    else
                    {
                        closePopUp();
                    }
                }));
            }
            else
            {
                editor.openEditorGui();
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        entityPreview.handleMouseReleased((int) mouseX, (int) mouseY, button);
        benderList.handleMouseReleased((int) mouseX, (int) mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    // MC 1.21.1 version (4 params)
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        return handleMouseScrolled(mouseX, mouseY, scrollY);
    }

    // MC 1.20.1 version (3 params)
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY)
    {
        return handleMouseScrolled(mouseX, mouseY, scrollY);
    }

    private boolean handleMouseScrolled(double mouseX, double mouseY, double scrollY)
    {
        if (popUp != null) return false;

        if (entityPreview.handleMouseScroll(mouseX, mouseY, scrollY))
        {
            return true;
        }
        if (benderList.handleMouseScroll(mouseX, mouseY, scrollY))
        {
            return true;
        }
        return ScreenHelper.superMouseScrolled(this, mouseX, mouseY, 0, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (popUp != null)
        {
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
        if (popUp != null)
        {
            return true;
        }

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    // Note: This method only exists in MC 1.21.1+
    protected void renderBlurredBackground(float partialTick)
    {
        // Skip blur effect
    }

    private int getCustomizeTabIndex()
    {
        return NetworkConfiguration.instance.areBendsPacksAllowed() ? TAB_CUSTOMIZE : TAB_PACKS;
    }

    private void openPopUp(GuiPopUp popup)
    {
        this.popUp = popup;
        this.init();
    }

    private void closePopUp()
    {
        this.popUp = null;
        this.init();
    }
}
