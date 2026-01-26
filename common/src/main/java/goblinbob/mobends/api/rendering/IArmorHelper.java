package goblinbob.mobends.api.rendering;

/**
 * Platform-agnostic armor helper interface.
 *
 * ArmorItem.getMaterial() API:
 * - MC 1.21.1: getMaterial() returns Holder<ArmorMaterial> with unwrapKey()
 * - MC 1.20.1: getMaterial() returns ArmorMaterial enum directly
 */
public interface IArmorHelper
{
    /**
     * Gets the armor material location string for an armor item.
     *
     * @param armorItem The native ArmorItem object
     * @return The material location string (e.g., "leather", "iron", "diamond")
     */
    String getArmorMaterialName(Object armorItem);

    /**
     * Singleton holder for the platform-specific implementation.
     */
    class Holder
    {
        private static IArmorHelper helper;

        public static void setHelper(IArmorHelper helper)
        {
            Holder.helper = helper;
        }

        public static IArmorHelper getHelper()
        {
            return helper;
        }
    }
}
