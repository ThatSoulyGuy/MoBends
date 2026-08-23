package goblinbob.mobends.core.client.event;

import goblinbob.mobends.lib.time.ITickSource;

public class DataUpdateHandler
{
    // The animation runtime lives in the loader-independent core module and reads the clock
    // through ITickSource. Installing the source here, as well as from each loader's client
    // setup, guarantees it is live: a missing tick source fails SILENTLY -- every elapsed-time
    // condition sees zero elapsed ticks and therefore never fires -- and this class is written
    // every frame before any entity renders, so this initialiser always runs first.
    static
    {
        ITickSource.Holder.setSource(DataUpdateHandler::getTicks);
    }

    public static float partialTicks = 0.0f;

    protected static float ticks = 0.0f;

    public static float ticksPerFrame = 0.0f;

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

    public static void update(float newPartialTicks, float newTicks)
    {
        partialTicks = newPartialTicks;
        ticksPerFrame = Math.min(Math.max(0F, newTicks - ticks), 1F);
        ticks = newTicks;
    }

    public static void onPaused()
    {
        ticksPerFrame = 0F;
    }

    public static boolean checkTicksRestart(float newTicks)
    {
        return ticks > newTicks;
    }
}
