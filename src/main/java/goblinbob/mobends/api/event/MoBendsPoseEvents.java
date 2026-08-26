package goblinbob.mobends.api.event;

import goblinbob.mobends.api.skeleton.IAnimatedSkeleton;
import goblinbob.mobends.api.skeleton.MoBendsAPI;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MoBendsPoseEvents
{
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends");

    private static final List<Entry> LISTENERS = new CopyOnWriteArrayList<>();

    private MoBendsPoseEvents()
    {
    }

    public static void register(String modId, IPoseListener listener)
    {
        if (modId == null || modId.isEmpty() || listener == null)
        {
            return;
        }

        LISTENERS.removeIf(entry -> entry.modId.equals(modId));
        LISTENERS.add(new Entry(modId, listener));
    }

    public static void unregister(String modId)
    {
        if (modId != null)
        {
            LISTENERS.removeIf(entry -> entry.modId.equals(modId));
        }
    }

    public static boolean hasListeners()
    {
        return !LISTENERS.isEmpty();
    }

    public static void dispatch(LivingEntity entity, float partialTicks)
    {
        if (LISTENERS.isEmpty() || entity == null)
        {
            return;
        }

        final IAnimatedSkeleton skeleton = MoBendsAPI.getRenderingSkeleton();

        for (final Entry entry : LISTENERS)
        {
            try
            {
                entry.listener.onPosed(entity, skeleton, partialTicks);
            }
            catch (Throwable t)
            {
                LISTENERS.remove(entry);
                LOGGER.warn("Mo'Bends pose listener from '{}' threw and was removed.", entry.modId, t);
            }
        }
    }

    private static final class Entry
    {
        private final String modId;
        private final IPoseListener listener;

        private Entry(String modId, IPoseListener listener)
        {
            this.modId = modId;
            this.listener = listener;
        }
    }
}
