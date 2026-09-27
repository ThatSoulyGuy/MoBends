package goblinbob.mobends.core.asset;

import goblinbob.mobends.core.module.IModule;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

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
    }

    public Collection<AssetDefinition> getAssets()
    {
        return Collections.emptyList();
    }

    public File getAssetFile(AssetLocation location)
    {
        return new File(".");
    }

    public void updateAssets()
    {
    }
}
