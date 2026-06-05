package goblinbob.mobends.core.client.gui;

import goblinbob.mobends.core.client.gui.vanilla.VanillaMoBendsScreen;
import net.minecraft.client.Minecraft;

/**
 * Entry point for opening MoBends' UI.
 * The UI is rendered directly with Minecraft's own screen/widget primitives, so it is
 * always available - there is no external UI backend to depend on.
 */
public final class UIBridge
{
    private UIBridge()
    {
        // Utility class
    }

    /**
     * Opens the MoBends settings screen.
     */
    public static void openSettingsScreen()
    {
        Minecraft.getInstance().setScreen(new VanillaMoBendsScreen(new MoBendsScreenBuilder()));
    }
}
