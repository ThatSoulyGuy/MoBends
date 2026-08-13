package goblinbob.mobends.core.util;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;
import org.slf4j.Logger;

import java.net.URI;

public class GuiHelper
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void closeGui()
    {
        Minecraft.getInstance().setScreen(null);
    }

    public static void playButtonSound(SoundManager soundManager)
    {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public static boolean openUrlInBrowser(String url)
    {
        try
        {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop").invoke((Object)null);
            oclass.getMethod("browse", URI.class).invoke(object, new URI(url));
            return true;
        }
        catch (Throwable throwable)
        {
            LOGGER.warn(String.format("Couldn't open link %s", url));
            return false;
        }
    }

}
