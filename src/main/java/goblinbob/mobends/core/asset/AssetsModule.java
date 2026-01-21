package goblinbob.mobends.core.asset;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.module.IModule;
import goblinbob.mobends.core.util.ConnectionHelper;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static goblinbob.mobends.core.util.ConnectionHelper.sendGetRequest;

public class AssetsModule
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static AssetsModule INSTANCE;

    private final String apiUrl;
    private final File assetsDirectory;
    private final File localManifestFile;

    private AssetManifest localManifest;

    public AssetsModule(File configDirectory)
    {
        this.apiUrl = EnvironmentModule.getConfig().getApiUrl();
        File modConfigDirectory = new File(configDirectory, "mobends");
        this.assetsDirectory = new File(modConfigDirectory, "assets");
        this.assetsDirectory.mkdirs();

        this.localManifestFile = new File(modConfigDirectory, "asset_manifest.json");

        this.updateAssets();
    }

    private void fetchLocalManifest()
    {
        this.localManifest = null;

        if (this.localManifestFile.isFile())
        {
            Gson gson = ConnectionHelper.INSTANCE.getGson();

            try
            {
                this.localManifest = gson.fromJson(new BufferedReader(new FileReader(this.localManifestFile)), AssetManifest.class);
            }
            catch (JsonParseException | FileNotFoundException e)
            {
                LOGGER.warn("Failed to get local asset manifest.");
                e.printStackTrace();
            }
        }
    }

    private AssetManifest fetchOnlineManifest()
    {
        Map<String, String> params = new HashMap<>();

        try
        {
            return sendGetRequest(new URL(apiUrl + "/api/asset/manifest"), params, AssetManifest.class);
        }
        catch (HttpHostConnectException e)
        {
            // No internet, do nothing.
        }
        catch(JsonParseException e)
        {
            LOGGER.warn("Failed to parse online asset manifest.");
            e.printStackTrace();
        }
        catch(IOException|URISyntaxException e)
        {
            LOGGER.warn("Failed to get online asset manifest.");
            e.printStackTrace();
        }

        return null;
    }

    private void storeManifestLocally(AssetManifest manifest)
    {
        try (FileWriter writer = new FileWriter(localManifestFile))
        {
            Gson gson = ConnectionHelper.INSTANCE.getGson();
            gson.toJson(manifest, writer);
        }
        catch (JsonParseException | IOException e)
        {
            LOGGER.warn("Failed to save local asset manifest.");
            e.printStackTrace();
        }

        this.localManifest = manifest;
    }

    private void downloadAsset(AssetManifest manifest, AssetDefinition asset) throws MalformedAssetException
    {
        try
        {
            URL url = new URL(manifest.getBaseUrl() + asset.getPath().getAssetPath());
            URLConnection connection = url.openConnection();
            DataInputStream dis = new DataInputStream(connection.getInputStream());
            byte[] fileData = new byte[connection.getContentLength()];
            for (int q = 0; q < fileData.length; q++)
            {
                fileData[q] = dis.readByte();
            }
            dis.close();

            // Making sure the path exists.
            File localAssetPath = getAssetFile(asset.getPath());
            localAssetPath.getParentFile().mkdirs();
            // Saving the file.
            FileOutputStream fos = new FileOutputStream(localAssetPath);
            fos.write(fileData);
            fos.close();
        }
        catch (IOException m)
        {
            throw new MalformedAssetException(String.format("Couldn't download asset: %s", asset.getPath()), m);
        }
    }

    public void updateAssets()
    {
        fetchLocalManifest();

        AssetManifest onlineManifest = fetchOnlineManifest();

        if (onlineManifest == null)
        {
            return;
        }

        LOGGER.info("New assets detected");
        Iterable<AssetDefinition> assetsToUpdate = AssetManifest.getAssetsToUpdate(localManifest, onlineManifest);

        try
        {
            for (AssetDefinition asset : assetsToUpdate)
            {
                downloadAsset(onlineManifest, asset);
            }

            // If we fail to download an asset, this isn't gonna get called.
            this.storeManifestLocally(onlineManifest);
        }
        catch(MalformedAssetException e)
        {
            LOGGER.warn(e.getMessage());
        }
    }

    public Collection<AssetDefinition> getAssets()
    {
        return localManifest != null ? localManifest.getAssets() : Collections.emptyList();
    }

    public File getAssetFile(AssetLocation location)
    {
        return new File(assetsDirectory, location.getAssetPath());
    }

    public static class Factory implements IModule
    {
        @Override
        public void init()
        {
            AssetsModule.INSTANCE = new AssetsModule(FMLPaths.CONFIGDIR.get().toFile());
        }

        @Override
        public void onRefresh()
        {
            AssetsModule.INSTANCE.updateAssets();
        }
    }
}
