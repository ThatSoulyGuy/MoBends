package goblinbob.mobends.core.asset;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AssetTexture extends AbstractTexture
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private final AssetLocation assetLocation;

    @Nullable
    private NativeImage image;

    public AssetTexture(AssetLocation assetLocation)
    {
        this.assetLocation = assetLocation;
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException
    {
        this.close();

        try (InputStream inputStream = new FileInputStream(AssetsModule.INSTANCE.getAssetFile(assetLocation)))
        {
            this.image = NativeImage.read(inputStream);
        }
        catch (IOException ioexception)
        {
            LOGGER.error("Couldn't load asset texture {}", assetLocation.toString(), ioexception);
            throw ioexception;
        }

        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(this::uploadTexture);
        }
        else
        {
            this.uploadTexture();
        }
    }

    private void uploadTexture()
    {
        if (this.image != null)
        {
            TextureUtil.prepareImage(this.getId(), this.image.getWidth(), this.image.getHeight());
            this.image.upload(0, 0, 0, false);
        }
    }

    @Override
    public void close()
    {
        super.close();
        if (this.image != null)
        {
            this.image.close();
            this.image = null;
        }
    }
}
