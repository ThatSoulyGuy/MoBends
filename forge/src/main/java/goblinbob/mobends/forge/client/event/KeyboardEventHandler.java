package goblinbob.mobends.forge.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Handles keyboard input events for Mo' Bends.
 */
public class KeyboardEventHandler
{
    public static final String KEY_CATEGORY = "key.categories." + ModStatics.MODID;

    public static KeyMapping openMenuKey;

    /**
     * Registers key mappings. Called during mod initialization.
     */
    public static void registerKeyMappings(RegisterKeyMappingsEvent event)
    {
        openMenuKey = new KeyMapping(
                "key." + ModStatics.MODID + ".menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KEY_CATEGORY
        );
        event.register(openMenuKey);
    }

    /**
     * Handles key input events.
     */
    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event)
    {
        Minecraft mc = Minecraft.getInstance();

        // Only process when in-game and no screen is open
        if (mc.player == null || mc.screen != null)
            return;

        // Check if our menu key was pressed
        if (openMenuKey != null && openMenuKey.consumeClick())
        {
            mc.setScreen(new GuiBendsMenu());
        }
    }
}
