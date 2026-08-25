package goblinbob.mobends.core.client.event;

import goblinbob.mobends.lib.time.ITickSource;

public class DataUpdateHandler
{
    // The animation runtime lives in the loader-independent core module and reads the clock
    // through ITickSource. This is its ONLY installer, and it is sufficient by class-init
    // ordering rather than by explicit wiring: both loaders' render handlers write ticksPerFrame
    // and partialTicks here every frame, before any entity renders, so this class is always
    // initialised before anything can read the clock.
    //
    // Worth stating because the failure mode is silent: an uninstalled source reads a constant
    // zero, so every elapsed-time condition sees zero ticks elapsed and simply never fires.
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
