package goblinbob.mobends.core.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.standard.client.gui.ArmorDebugScreen;
import goblinbob.mobends.standard.main.MoBends;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class KeyboardHandler
{

    private static final String MAIN_CATEGORY = "Mo' Bends";
    private static final KeyMapping KEY_MENU = new KeyMapping(
            "key.mobends.menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            MAIN_CATEGORY);
    private static final KeyMapping KEY_REFRESH = new KeyMapping(
            "key.mobends.refresh",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F10,
            MAIN_CATEGORY);
    private static final KeyMapping KEY_ARMOR_DEBUG = new KeyMapping(
            "key.mobends.armor_debug",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            MAIN_CATEGORY);

    public static void registerKeyMappings(RegisterKeyMappingsEvent event)
    {
        event.register(KEY_MENU);
        event.register(KEY_REFRESH);
        event.register(KEY_ARMOR_DEBUG);
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.Key event)
    {
        if (KEY_MENU.consumeClick())
        {
            Minecraft.getInstance().setScreen(new GuiBendsMenu());
        }
        else if (KEY_REFRESH.consumeClick())
        {
            MoBends.refreshSystems();
        }
        else if (KEY_ARMOR_DEBUG.consumeClick())
        {
            Minecraft.getInstance().setScreen(new ArmorDebugScreen());
        }
    }

}
