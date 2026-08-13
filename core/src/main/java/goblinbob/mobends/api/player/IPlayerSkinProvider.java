package goblinbob.mobends.api.player;

public interface IPlayerSkinProvider
{
    boolean isSlimModel(Object player);

    Object getCapeTexture(Object player);

    Object getElytraTexture(Object player);

    class Holder
    {
        private static IPlayerSkinProvider provider;

        public static void setProvider(IPlayerSkinProvider provider)
        {
            Holder.provider = provider;
        }

        public static IPlayerSkinProvider getProvider()
        {
            return provider;
        }
    }
}
