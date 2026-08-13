package goblinbob.mobends.api.item;

public interface IItemCapabilityProvider
{
    boolean isFood(Object item);

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
