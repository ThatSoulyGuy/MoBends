package goblinbob.mobends.core.client.gui;

import goblinbob.mobends.core.client.gui.vanilla.VanillaMoBendsScreen;
import net.minecraft.client.Minecraft;

public final class UIBridge
{
    private UIBridge()
    {
    }

    public static void openSettingsScreen()
    {
        Minecraft.getInstance().setScreen(new VanillaMoBendsScreen(new MoBendsScreenBuilder()));
    }
}
