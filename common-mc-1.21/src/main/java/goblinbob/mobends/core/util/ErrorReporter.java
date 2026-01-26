package goblinbob.mobends.core.util;

import goblinbob.mobends.core.pack.InvalidPackFormatException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ErrorReporter
{

    public static MutableComponent createErrorHeader()
    {
        return Component.literal("[Mo' Bends] ").withStyle(ChatFormatting.YELLOW);
    }

    public static void showErrorToPlayer(Component textComponent)
    {
        if (Minecraft.getInstance().player == null)
        {
            return;
        }

        MutableComponent base = Component.literal("").withStyle(ChatFormatting.WHITE);
        base.append(createErrorHeader());
        base.append(textComponent);

        Minecraft.getInstance().player.sendSystemMessage(base);
    }

    public static void showErrorToPlayer(String error)
    {
        showErrorToPlayer(Component.literal(error));
    }

    public static void showErrorToPlayer(InvalidPackFormatException ex)
    {
        MutableComponent textComponent = Component.literal("A pack has been disabled due to it's wrong format: ");

        MutableComponent packName = Component.literal(ex.getPackName()).withStyle(ChatFormatting.BOLD);
        textComponent.append(packName);

        textComponent.append(Component.literal(". Check the logs for more details..."));

        showErrorToPlayer(textComponent);
    }

}
