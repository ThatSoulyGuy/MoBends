package goblinbob.mobends.api.rendering;

/**
 * Platform-agnostic interface for getting armor color information.
 *
 * In MC 1.21.1, armor colors are stored via DataComponents.DYED_COLOR.
 * In MC 1.20.1, armor colors are stored in NBT and accessed via DyeableLeatherItem.
 */
public interface IArmorColorProvider
{
    /**
     * Gets the dyed color of an armor item stack.
     *
     * @param itemStack The native ItemStack object
     * @return The RGB color (no alpha), or -1 if not dyed
     */
    int getDyedColor(Object itemStack);

    /**
     * Checks if an armor item stack has a dyed color.
     *
     * @param itemStack The native ItemStack object
     * @return True if the item has a dyed color
     */
    boolean hasDyedColor(Object itemStack);

    /**
     * Checks if an armor item stack is dyeable (e.g., leather armor).
     *
     * @param itemStack The native ItemStack object
     * @return True if the item can be dyed
     */
    boolean isDyeable(Object itemStack);

    /**
     * Singleton holder for the platform-specific implementation.
     */
    class Holder
    {
        private static IArmorColorProvider provider;

        public static void setProvider(IArmorColorProvider provider)
        {
            Holder.provider = provider;
        }

        public static IArmorColorProvider getProvider()
        {
            return provider;
        }
    }
}
