package goblinbob.mobends.forge.compat;

import goblinbob.mobends.compat.LegendsModCompat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class LegendsEventBridge
{
    private static final String SETUP_ANIMATION_EVENT = "com.tihyo.legends.client.events.SetupAnimationEvent";

    private static boolean registered = false;

    private LegendsEventBridge()
    {
    }

    public static void register()
    {
        if (registered || !LegendsModCompat.isModLoaded())
        {
            return;
        }
        registered = true;

        try
        {
            final Class<?> eventClass = Class.forName(SETUP_ANIMATION_EVENT);
            if (!Event.class.isAssignableFrom(eventClass))
            {
                return;
            }

            final Method getPlayer = eventClass.getMethod("getPlayer");
            final Method getPlayerModel = eventClass.getMethod("getPlayerModel");

            @SuppressWarnings("unchecked")
            final Class<Event> eventType = (Class<Event>) eventClass;

            final Consumer<Event> before = event -> {
                try
                {
                    final Object player = getPlayer.invoke(event);
                    LegendsModCompat.beginSetupAnimation(
                            player instanceof LivingEntity living ? living : null,
                            getPlayerModel.invoke(event));
                }
                catch (Throwable ignored)
                {
                }
            };

            final Consumer<Event> after = event -> {
                try
                {
                    final Object player = getPlayer.invoke(event);
                    LegendsModCompat.endSetupAnimation(
                            player instanceof LivingEntity living ? living : null,
                            getPlayerModel.invoke(event));
                }
                catch (Throwable ignored)
                {
                }
            };

            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, eventType, before);
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, eventType, after);
        }
        catch (Throwable ignored)
        {
        }
    }
}
