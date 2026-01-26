package goblinbob.mobends.core.asset;

import goblinbob.mobends.core.module.IModule;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * Stub AssetsModule for common-mc.
 * Platform-specific implementations provide the actual asset management.
 */
public class AssetsModule implements IModule
{
    public static AssetsModule INSTANCE;

    @Override
    public void init()
    {
        INSTANCE = this;
    }

    @Override
    public void onRefresh()
    {
        // No-op stub
    }

    /**
     * Factory for creating AssetsModule instances.
     */
    public static class Factory implements IModule.Factory
    {
        @Override
        public IModule create()
        {
            return new AssetsModule();
        }
    }

    /**
     * Gets the assets defined in the manifest.
     * @return Collection of asset definitions
     */
    public Collection<AssetDefinition> getAssets()
    {
        return Collections.emptyList();
    }

    /**
     * Gets the local file for an asset location.
     * @param location The asset location
     * @return The local file
     */
    public File getAssetFile(AssetLocation location)
    {
        return new File(".");
    }

    /**
     * Updates assets from the remote server.
     */
    public void updateAssets()
    {
        // No-op stub
    }
}
