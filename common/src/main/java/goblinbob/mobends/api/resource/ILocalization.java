package goblinbob.mobends.api.resource;

/**
 * Platform-agnostic localization abstraction.
 * Provides access to translated strings.
 */
public interface ILocalization
{
    /**
     * Gets a translated string
     * @param key The translation key
     * @return The translated string, or the key if not found
     */
    String get(String key);

    /**
     * Gets a translated string with arguments
     * @param key The translation key
     * @param args The format arguments
     * @return The translated string with arguments substituted
     */
    String get(String key, Object... args);

    /**
     * Checks if a translation key exists
     * @param key The translation key
     * @return true if the key exists
     */
    boolean has(String key);
}
