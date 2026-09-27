package goblinbob.mobends.core.util;

import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

public class EntityHelper
{
    private static Predicate<Entity> provider = vehicle -> true;

    public static void setProvider(Predicate<Entity> newProvider)
    {
        provider = newProvider;
    }

    public static boolean shouldRiderSit(Entity vehicle)
    {
        return vehicle != null && provider.test(vehicle);
    }
}
