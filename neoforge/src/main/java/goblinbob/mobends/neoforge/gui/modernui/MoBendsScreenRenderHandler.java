package goblinbob.mobends.neoforge.gui.modernui;

import goblinbob.mobends.api.gui.modernui.IMuiScreenBuilder;
import goblinbob.mobends.core.client.gui.modernui.MuiMoBendsScreen;
import goblinbob.mobends.neoforge.gui.modernui.view.NeoForgeEntityPreviewView;
import icyllis.modernui.view.View;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;

/**
 * Handles rendering of entity previews in Modern UI screens.
 * Uses NeoForge's ScreenEvent.Render.Post to render Minecraft entities
 * after Modern UI has finished its rendering pass.
 */
public class MoBendsScreenRenderHandler
{
    private static MoBendsScreenRenderHandler INSTANCE;

    @Nullable
    private IMuiScreenBuilder currentScreenBuilder;
    private boolean registered = false;

    private MoBendsScreenRenderHandler()
    {
    }

    public static MoBendsScreenRenderHandler getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new MoBendsScreenRenderHandler();
        }
        return INSTANCE;
    }

    /**
     * Registers the render handler and sets the active screen builder.
     *
     * @param screenBuilder The screen builder to use for entity rendering
     */
    public void activate(IMuiScreenBuilder screenBuilder)
    {
        this.currentScreenBuilder = screenBuilder;
        if (!registered)
        {
            NeoForge.EVENT_BUS.register(this);
            registered = true;
        }
    }

    /**
     * Deactivates the render handler when the screen closes.
     */
    public void deactivate()
    {
        this.currentScreenBuilder = null;
        if (registered)
        {
            NeoForge.EVENT_BUS.unregister(this);
            registered = false;
        }
        // Clear any pending renders
        NeoForgeEntityPreviewView.clearPendingRender();
    }

    /**
     * Called after the screen is rendered.
     * This is where we render Minecraft entities on top of Modern UI's content.
     */
    @SubscribeEvent
    public void onScreenRenderPost(ScreenEvent.Render.Post event)
    {
        if (currentScreenBuilder == null)
        {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        float partialTick = event.getPartialTick();

        // Render any pending entity previews from NeoForgeEntityPreviewView
        NeoForgeEntityPreviewView.renderPendingEntity(guiGraphics, partialTick);

        // Also handle direct entity preview rendering
        if (currentScreenBuilder instanceof MuiMoBendsScreen muiScreen)
        {
            var entityPreview = muiScreen.getEntityPreview();
            if (entityPreview != null)
            {
                // Update animation state
                entityPreview.update();

                // Get the preview view's position and render
                var previewView = entityPreview.getView();
                if (previewView != null && previewView.getNativeView() instanceof View nativeView)
                {
                    int[] location = new int[2];
                    nativeView.getLocationOnScreen(location);
                    entityPreview.renderEntity(guiGraphics,
                            location[0], location[1],
                            nativeView.getWidth(), nativeView.getHeight(),
                            partialTick);
                }
            }
        }
    }
}
