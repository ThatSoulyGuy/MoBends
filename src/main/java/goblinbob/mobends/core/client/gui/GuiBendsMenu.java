package goblinbob.mobends.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.core.WebAPI;
import goblinbob.mobends.core.client.gui.elements.GuiSectionButton;
import goblinbob.mobends.core.client.gui.packswindow.GuiPacksWindow;
import goblinbob.mobends.core.client.gui.popup.GuiEditorNotFound;
import goblinbob.mobends.core.client.gui.popup.GuiPopUp;
import goblinbob.mobends.core.client.gui.settingswindow.GuiSettingsWindow;
import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.GuiHelper;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiBendsMenu extends Screen
{

	private static final ResourceLocation MENU_TITLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(ModStatics.MODID,
			"textures/gui/title.png");
	public static final ResourceLocation ICONS_TEXTURE = ResourceLocation.fromNamespaceAndPath(ModStatics.MODID,
			"textures/gui/icons.png");

	private GuiSectionButton settingsButton;
	private GuiSectionButton packsButton;
	private GuiSectionButton customizeButton;
	private GuiPopUp popUp;

	public GuiBendsMenu()
	{
		super(Component.translatable("mobends.gui.title"));

		this.settingsButton = new GuiSectionButton(Component.translatable("mobends.gui.section.settings").getString(), 0xFFDA3A00)
				.setLeftIcon(0, 43, 19, 19).setRightIcon(19, 43, 19, 19);
		this.packsButton = new GuiSectionButton(Component.translatable("mobends.gui.section.packs").getString(), 0xFF4577DE)
				.setLeftIcon(38, 43, 23, 20).setRightIcon(38, 43, 23, 20);
		this.customizeButton = new GuiSectionButton(Component.translatable("mobends.gui.section.customize").getString(), 0xFF26DAA3)
				.setLeftIcon(80, 43, 19, 14).setRightIcon(80, 43, 19, 14);

		this.popUp = null;
	}

	@Override
	protected void init()
	{
		super.init();
		this.clearWidgets();

		if (this.popUp != null)
			this.popUp.initGui(this.width / 2, this.height / 2);

		int startY = height / 2 - 32;
		int distance = 49;

		if (NetworkConfiguration.instance.areBendsPacksAllowed())
		{
			this.settingsButton.initGui((this.width - 318) / 2, startY);
			this.packsButton.initGui((this.width - 318) / 2, startY + distance);
			this.customizeButton.initGui((this.width - 318) / 2, startY + distance * 2);
		}
		else
		{
			this.settingsButton.initGui((this.width - 318) / 2, startY);
			this.customizeButton.initGui((this.width - 318) / 2, startY + distance);
		}
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
			this.onClose();
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void onClose()
	{
		super.onClose();
	}

	@Override
	public void tick()
	{
		super.tick();

		if (this.minecraft == null) return;

		double mouseX = this.minecraft.mouseHandler.xpos() * (double)this.width / (double)this.minecraft.getWindow().getScreenWidth();
		double mouseY = this.minecraft.mouseHandler.ypos() * (double)this.height / (double)this.minecraft.getWindow().getScreenHeight();

		if (this.popUp != null)
		{
			this.popUp.update((int)mouseX, (int)mouseY);
			return;
		}

		this.settingsButton.update((int)mouseX, (int)mouseY);
		this.packsButton.update((int)mouseX, (int)mouseY);
		this.customizeButton.update((int)mouseX, (int)mouseY);
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

		if (settingsButton.mouseClicked(x, y, button))
		{
			minecraft.setScreen(new GuiSettingsWindow());
			return true;
		}
		else if (packsButton.mouseClicked(x, y, button))
		{
			minecraft.setScreen(new GuiPacksWindow());
			return true;
		}
		else if (customizeButton.mouseClicked(x, y, button))
		{
			IAnimationEditor editor = AnimationEditorRegistry.INSTANCE.getPrimaryEditor();

			if (editor == null)
			{
				openPopUp(new GuiEditorNotFound(this::closePopUp, () -> {
					GuiEditorNotFound editorNotFoundPopup = (GuiEditorNotFound) popUp;
					String downloadUrl = WebAPI.INSTANCE.getOfficialAnimationEditorUrl();

					if (downloadUrl == null || !GuiHelper.openUrlInBrowser(downloadUrl))
					{
						editorNotFoundPopup.setErrorOccurred(true);
					}
					else
					{
						closePopUp();
					}
				}));
			}
			else
			{
				// Opens the animation editor.
				editor.openEditorGui();
			}
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		int x = (int) mouseX;
		int y = (int) mouseY;

		this.settingsButton.mouseReleased(x, y, button);
		this.packsButton.mouseReleased(x, y, button);
		this.customizeButton.mouseReleased(x, y, button);

		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		int titleWidth = 167 * 2;
		int titleHeight = 37 * 2;

		guiGraphics.blit(MENU_TITLE_TEXTURE, (width - titleWidth) / 2, (height - titleHeight) / 2 - 70,
				0, 0, titleWidth, titleHeight, titleWidth, titleHeight);

		this.settingsButton.display(guiGraphics);
		if (NetworkConfiguration.instance.areBendsPacksAllowed())
		{
			this.packsButton.display(guiGraphics);
		}
		this.customizeButton.display(guiGraphics);

		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		if (this.popUp != null)
		{
			this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
			this.popUp.display(guiGraphics, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}

	private void closePopUp()
	{
		this.popUp = null;
		this.init();
	}

	private void openPopUp(GuiPopUp popUp)
	{
		this.popUp = popUp;
		this.init();
	}

}
