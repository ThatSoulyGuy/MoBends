package goblinbob.mobends.core.configuration;

import java.util.List;

/**
 * Client-side configuration abstraction.
 * Platform-specific implementations provide the actual config values.
 */
public class CoreClientConfig
{
    private static CoreClientConfig instance;

    public static CoreClientConfig getInstance()
    {
        if (instance == null)
        {
            instance = new CoreClientConfig();
        }
        return instance;
    }

    /**
     * @return List of applied pack names
     */
    public List<String> getAppliedPacks()
    {
        return List.of();
    }

    /**
     * Sets the applied packs list
     * @param packs List of pack names
     */
    public void setAppliedPacks(List<String> packs)
    {
        // Platform implementation handles persistence
    }

    /**
     * @return Whether the mod is globally enabled
     */
    public boolean isEnabled()
    {
        return true;
    }

    /**
     * @param entity Entity key
     * @return Whether the entity is enabled for animation
     */
    public boolean isEntityEnabled(String entity)
    {
        return true;
    }

    /**
     * @param entity Entity key
     * @return Whether the entity animation is enabled (alias for isEntityEnabled)
     */
    public boolean isEntityAnimated(String entity)
    {
        return isEntityEnabled(entity);
    }
}
