package goblinbob.mobends.core.client.gui;

import goblinbob.mobends.core.client.gui.vanilla.VanillaMoBendsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class UIBridge
{
    private UIBridge()
    {
    }

    public static void openSettingsScreen()
    {
        Minecraft.getInstance().setScreen(new VanillaMoBendsScreen(new MoBendsScreenBuilder()));
    }

    public static Screen createConfigScreen()
    {
        MoBendsScreenBuilder builder = new MoBendsScreenBuilder();
        builder.setOpenConfigOnBuild(true);
        return new VanillaMoBendsScreen(builder);
    }
}
