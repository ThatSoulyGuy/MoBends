package goblinbob.mobends.api.rendering;

public interface IArmorColorProvider
{
    int getDyedColor(Object itemStack);

    boolean hasDyedColor(Object itemStack);

    boolean isDyeable(Object itemStack);

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
