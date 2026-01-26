package goblinbob.mobends.core.util;

import net.minecraft.world.entity.Entity;

/**
 * Entity-related utility methods with cross-version compatibility.
 */
public class EntityHelper
{
    /**
     * Checks if the rider should use sitting animation on the vehicle.
     * This abstracts the shouldRiderSit() method for cross-version compatibility.
     *
     * @param vehicle The vehicle entity
     * @return true if riders should sit
     */
    /**
     * Provider interface for platform-specific shouldRiderSit implementation.
     */
    public interface ShouldRiderSitProvider
    {
        boolean shouldRiderSit(Entity vehicle);
    }

    private static ShouldRiderSitProvider provider = vehicle -> true;

    /**
     * Sets the platform-specific provider for shouldRiderSit checks.
     * @param newProvider The provider implementation
     */
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
