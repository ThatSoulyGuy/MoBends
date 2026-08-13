package goblinbob.mobends.core.util;

import net.minecraft.world.entity.Entity;

public class EntityHelper
{
    public interface ShouldRiderSitProvider
    {
        boolean shouldRiderSit(Entity vehicle);
    }

    private static ShouldRiderSitProvider provider = vehicle -> true;

    public static void setProvider(ShouldRiderSitProvider newProvider)
    {
        provider = newProvider;
    }

    public static boolean shouldRiderSit(Entity vehicle)
    {
        if (vehicle == null)
        {
            return false;
        }
        return provider.shouldRiderSit(vehicle);
    }
}
