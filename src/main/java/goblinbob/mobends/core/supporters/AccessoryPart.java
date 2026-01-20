package goblinbob.mobends.core.supporters;

import goblinbob.mobends.core.asset.AssetLocation;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Collection;

public class AccessoryPart
{
    private String key;
    private BindPoint bindPoint;
    private AssetLocation modelPath;
    private AssetLocation diffuseTexturePath;
    private AssetLocation inkedTexturePath;
    private Vector3f translation;
    private Vector3f rotation;
    private Vector3f scale;

    public String getKey()
    {
        return key;
    }

    public BindPoint getBindPoint()
    {
        return bindPoint;
    }

    public AssetLocation getModelPath()
    {
        return modelPath;
    }

    public AssetLocation getDiffuseTexturePath()
    {
        return diffuseTexturePath;
    }

    public AssetLocation getInkedTexturePath()
    {
        return inkedTexturePath;
    }

    public Collection<AssetLocation> getAssetLocations()
    {
        return Arrays.asList(modelPath, diffuseTexturePath, inkedTexturePath);
    }

    public Vector3f getTranslation()
    {
        return translation;
    }

    public Vector3f getRotation()
    {
        return rotation;
    }

    public Vector3f getScale()
    {
        return scale;
    }
}
