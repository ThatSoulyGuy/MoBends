package goblinbob.mobends.core.pack;

import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class ThumbnailProvider
{
    public static final ResourceLocation DEFAULT_THUMBNAIL_LOCATION = new ResourceLocation(ModStatics.MODID,
            "textures/gui/default_pack_thumbnail.png");

    private final PackCache packCache;

    public ThumbnailProvider(PackCache packCache)
    {
        this.packCache = packCache;
    }

    public ResourceLocation getThumbnailLocation(String packName, String thumbnailUrl)
    {
        final ResourceLocation resourceLocation = new ResourceLocation(ModStatics.MODID,
                "bendsPackThumbnails/" + packName);
        @Nullable AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(resourceLocation, null);

        if (texture == null)
        {
            HttpTexture httpTexture = new HttpTexture(
                    packCache.getThumbnailFile(packName),
                    thumbnailUrl,
                    DEFAULT_THUMBNAIL_LOCATION,
                    false,  // legacySkin
                    null    // callback
            );

            Minecraft.getInstance().getTextureManager().register(resourceLocation, httpTexture);
            return resourceLocation;
        }

        return resourceLocation;
    }
}
