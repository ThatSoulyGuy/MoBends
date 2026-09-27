package goblinbob.mobends.forge.compat;

import goblinbob.mobends.compat.LegendsModCompat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
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

            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, eventType,
                    listener(getPlayer, getPlayerModel, LegendsModCompat::beginSetupAnimation));
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, eventType,
                    listener(getPlayer, getPlayerModel, LegendsModCompat::endSetupAnimation));
        }
        catch (Throwable ignored)
        {
        }
    }

    private static Consumer<Event> listener(Method getPlayer, Method getPlayerModel, BiConsumer<LivingEntity, Object> action)
    {
        return event -> {
            try
            {
                final Object player = getPlayer.invoke(event);
                action.accept(player instanceof LivingEntity living ? living : null, getPlayerModel.invoke(event));
            }
            catch (Throwable ignored)
            {
            }
        };
    }
}
