package goblinbob.mobends.core.client.event;

import goblinbob.mobends.lib.time.ITickSource;

public class DataUpdateHandler
{
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
