package goblinbob.mobends.core.client.gui.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public final class GuiSound
{
    private GuiSound()
    {
    }

    public static void playClick()
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSoundManager() == null) return;

        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
