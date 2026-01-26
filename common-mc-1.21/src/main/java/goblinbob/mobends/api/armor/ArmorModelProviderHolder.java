package goblinbob.mobends.api.armor;

/**
 * Holds the platform-specific armor model provider.
 * Platforms should set this during initialization.
 */
public class ArmorModelProviderHolder
{
    private static IArmorModelProvider provider = IArmorModelProvider.DEFAULT;

    /**
     * Sets the armor model provider.
     * Should be called by platform-specific initialization code.
     */
    public static void setProvider(IArmorModelProvider provider)
    {
        ArmorModelProviderHolder.provider = provider;
    }

    /**
     * Gets the current armor model provider.
     */
    public static IArmorModelProvider getProvider()
    {
        return provider;
    }
}
