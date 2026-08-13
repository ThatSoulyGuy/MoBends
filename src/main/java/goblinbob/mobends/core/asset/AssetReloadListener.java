package goblinbob.mobends.core.asset;

import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collection;

public class AssetReloadListener implements ResourceManagerReloadListener
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public AssetReloadListener()
    {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager)
    {
        if (AssetsModule.INSTANCE != null)
        {
            AssetsModule.INSTANCE.updateAssets();
        }

        if (AssetModels.INSTANCE != null)
        {
            AssetModels.INSTANCE.clearCache();
        }

        if (AssetsModule.INSTANCE == null)
        {
            return;
        }

        Collection<AssetDefinition> assets = AssetsModule.INSTANCE.getAssets();

        for (AssetDefinition asset : assets)
        {
            AssetLocation location = asset.getPath();
            AssetType assetType = location.getAssetType();

            if (assetType == AssetType.TEXTURE)
            {
                AssetTexture assetTexture = new AssetTexture(location);
                Minecraft.getInstance().getTextureManager().register(location.getResourceLocation(), assetTexture);
            }
            else if (assetType == AssetType.MODEL)
            {
                try
                {
                    AssetModels.INSTANCE.register(location);
                }
                catch (IOException | JsonSyntaxException e)
                {
                    LOGGER.error("Couldn't register asset model: {}", location.toString());
                    e.printStackTrace();
                }
            }
        }
    }
}
