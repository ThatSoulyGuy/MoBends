package goblinbob.mobends.core.asset;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public class AssetLocation
{
    private static final String PREFIX = "assets/";

    private final ResourceLocation resourceLocation;
    private final AssetType assetType;
    private final String assetPath;

    public AssetLocation(String assetPath)
    {
        this.resourceLocation = ResourceLocationFactory.create(ModStatics.MODID, PREFIX + assetPath);
        this.assetPath = assetPath;

        if (assetPath.startsWith("models/"))
        {
            this.assetType = AssetType.MODEL;
        }
        else if (assetPath.startsWith("textures/"))
        {
            this.assetType = AssetType.TEXTURE;
        }
        else if (assetPath.endsWith(".json"))
        {
            this.assetType = AssetType.JSON;
        }
        else
        {
            this.assetType = AssetType.UNKNOWN;
        }
    }

    public AssetLocation(String assetPath, AssetType assetType)
    {
        this.resourceLocation = ResourceLocationFactory.create(ModStatics.MODID, PREFIX + assetPath);
        this.assetPath = assetPath;
        this.assetType = assetType;
    }

    public ResourceLocation getResourceLocation()
    {
        return resourceLocation;
    }

    public String getNamespace()
    {
        return resourceLocation.getNamespace();
    }

    public String getPath()
    {
        return resourceLocation.getPath();
    }

    public String getAssetPath()
    {
        return assetPath;
    }

    public AssetType getAssetType()
    {
        return assetType;
    }

    @Override
    public String toString()
    {
        return resourceLocation.toString();
    }

    public static class Adapter extends TypeAdapter<AssetLocation>
    {
        @Override
        public void write(JsonWriter out, AssetLocation value) throws IOException
        {
            out.value(value.assetPath);
        }

        @Override
        public AssetLocation read(JsonReader in) throws IOException
        {
            return new AssetLocation(in.nextString());
        }
    }
}
