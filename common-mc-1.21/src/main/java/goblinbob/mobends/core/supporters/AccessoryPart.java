package goblinbob.mobends.core.supporters;

import goblinbob.mobends.core.asset.AssetLocation;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/**
 * A part of an accessory.
 */
public class AccessoryPart
{
    private final AssetLocation modelPath;
    private final AssetLocation diffuseTexturePath;
    private final AssetLocation inkedTexturePath;
    private final BindPoint bindPoint;
    private final Vector3f translation;
    private final Vector3f rotation;
    private final Vector3f scale;

    public AccessoryPart(AssetLocation modelPath, AssetLocation diffuseTexturePath,
                        @Nullable AssetLocation inkedTexturePath, BindPoint bindPoint,
                        @Nullable Vector3f translation, @Nullable Vector3f rotation, @Nullable Vector3f scale)
    {
        this.modelPath = modelPath;
        this.diffuseTexturePath = diffuseTexturePath;
        this.inkedTexturePath = inkedTexturePath;
        this.bindPoint = bindPoint;
        this.translation = translation;
        this.rotation = rotation;
        this.scale = scale;
    }

    public AssetLocation getModelPath()
    {
        return modelPath;
    }

    public AssetLocation getDiffuseTexturePath()
    {
        return diffuseTexturePath;
    }

    @Nullable
    public AssetLocation getInkedTexturePath()
    {
        return inkedTexturePath;
    }

    public BindPoint getBindPoint()
    {
        return bindPoint;
    }

    @Nullable
    public Vector3f getTranslation()
    {
        return translation;
    }

    @Nullable
    public Vector3f getRotation()
    {
        return rotation;
    }

    @Nullable
    public Vector3f getScale()
    {
        return scale;
    }
}
