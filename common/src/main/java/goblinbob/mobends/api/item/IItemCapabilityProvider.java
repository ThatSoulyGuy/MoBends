package goblinbob.mobends.api.item;

/**
 * Platform-agnostic interface for checking item capabilities.
 *
 * In MC 1.21.1, item data is stored via DataComponents.
 * In MC 1.20.1, item data was accessed via dedicated methods like isEdible().
 */
public interface IItemCapabilityProvider
{
    /**
     * Checks if an item is food (edible).
     *
     * @param item The native Item object
     * @return True if the item is food
     */
    boolean isFood(Object item);

    /**
     * Singleton holder for the platform-specific implementation.
     */
    class Holder
    {
        private static IItemCapabilityProvider provider;

        public static void setProvider(IItemCapabilityProvider provider)
        {
            Holder.provider = provider;
        }

        public static IItemCapabilityProvider getProvider()
        {
            return provider;
        }
    }
}
