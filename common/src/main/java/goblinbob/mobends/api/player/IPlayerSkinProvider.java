package goblinbob.mobends.api.player;

/**
 * Platform-agnostic player skin information provider.
 *
 * In MC 1.21.1: player.getSkin().model().id() for model type, player.getSkin().capeTexture(), player.getSkin().elytraTexture()
 * In MC 1.20.1: player.getModelName() for model type, player.getCloakTextureLocation(), player.getElytraTextureLocation()
 */
public interface IPlayerSkinProvider
{
    /**
     * Checks if the player model is "slim" (Alex-style).
     *
     * @param player The native AbstractClientPlayer
     * @return True if the player uses slim arms
     */
    boolean isSlimModel(Object player);

    /**
     * Gets the cape texture location for the player.
     *
     * @param player The native AbstractClientPlayer
     * @return The cape ResourceLocation, or null if none
     */
    Object getCapeTexture(Object player);

    /**
     * Gets the elytra texture location for the player.
     *
     * @param player The native AbstractClientPlayer
     * @return The elytra ResourceLocation, or null if none
     */
    Object getElytraTexture(Object player);

    /**
     * Singleton holder for the platform-specific implementation.
     */
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
