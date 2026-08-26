package goblinbob.mobends.standard.client;

import net.minecraft.resources.ResourceLocation;

public final class VillagerOverlayContext
{
    private static ResourceLocation currentTexture;

    private VillagerOverlayContext()
    {
    }

    public static void set(ResourceLocation texture)
    {
        currentTexture = texture;
    }

    public static void clear()
    {
        currentTexture = null;
    }

    public static boolean isTypePass()
    {
        return currentTexture != null && currentTexture.getPath().contains("/type/");
    }
}
