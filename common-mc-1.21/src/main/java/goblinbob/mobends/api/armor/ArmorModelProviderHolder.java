package goblinbob.mobends.api.armor;

public class ArmorModelProviderHolder
{
    private static IArmorModelProvider provider = IArmorModelProvider.DEFAULT;

    public static void setProvider(IArmorModelProvider provider)
    {
        ArmorModelProviderHolder.provider = provider;
    }

    public static IArmorModelProvider getProvider()
    {
        return provider;
    }
}
