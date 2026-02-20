package goblinbob.mobends.core.client.event;

/**
 * Holds timing information for animation updates.
 * Platform-specific event handlers update these values each frame.
 */
public class DataUpdateHandler
{
    /**
     * The current partial tick time (0.0 to 1.0 between server ticks)
     */
    public static float partialTicks = 0.0f;

    /**
     * The current tick count (for animation timing)
     */
    protected static float ticks = 0.0f;

    /**
     * The number of ticks that have passed since the last frame
     */
    public static float ticksPerFrame = 0.0f;

    /**
     * When >= 0, getTicks() returns this value instead of the real ticks.
     * Used by EntityPreviewRenderer to drive animations while the game is paused.
     */
    private static float previewTicksOverride = -1;

    public static float getTicks()
    {
        if (previewTicksOverride >= 0) return previewTicksOverride;
        return ticks;
    }

    public static void setPreviewTicks(float ticks)
    {
        previewTicksOverride = ticks;
    }

    public static void clearPreviewTicks()
    {
        previewTicksOverride = -1;
    }

    /**
     * Called by platform-specific event handlers to update timing values.
     * @param newPartialTicks The new partial ticks value
     * @param newTicks The new total ticks value
     */
    public static void update(float newPartialTicks, float newTicks)
    {
        partialTicks = newPartialTicks;
        ticksPerFrame = Math.min(Math.max(0F, newTicks - ticks), 1F);
        ticks = newTicks;
    }

    /**
     * Called when the game is paused
     */
    public static void onPaused()
    {
        ticksPerFrame = 0F;
    }

    /**
     * Checks if ticks have restarted
     * @param newTicks The new ticks value
     * @return true if ticks restarted
     */
    public static boolean checkTicksRestart(float newTicks)
    {
        return ticks > newTicks;
    }
}
