package goblinbob.mobends.api.platform;

import goblinbob.mobends.api.entity.IEntity;
import goblinbob.mobends.api.entity.IItemStack;
import goblinbob.mobends.api.entity.ILivingEntity;
import goblinbob.mobends.api.entity.IPlayer;
import goblinbob.mobends.api.rendering.IBufferSource;
import goblinbob.mobends.api.rendering.IPoseStack;
import goblinbob.mobends.api.rendering.IRenderLayerProvider;
import goblinbob.mobends.api.rendering.ITesselator;
import goblinbob.mobends.api.rendering.ITesselator;
import goblinbob.mobends.api.resource.ILocalization;
import goblinbob.mobends.api.resource.IResourceManager;
import goblinbob.mobends.api.resource.IResourcePath;

import javax.annotation.Nullable;

public interface IPlatformServices
{
    String getPlatformName();

    String getMinecraftVersion();

    boolean isClient();

    boolean isDevelopmentEnvironment();

    IResourceManager getResourceManager();

    ILocalization getLocalization();

    IRenderLayerProvider getRenderLayerProvider();

    @Nullable
    IEntity wrapEntity(Object nativeEntity);

    @Nullable
    ILivingEntity wrapLivingEntity(Object nativeEntity);

    @Nullable
    IPlayer wrapPlayer(Object nativeEntity);

    IPoseStack wrapPoseStack(Object nativePoseStack);

    IBufferSource wrapBufferSource(Object nativeBufferSource);

    IItemStack wrapItemStack(Object nativeItemStack);

    IResourcePath wrapResourceLocation(Object nativeResourceLocation);

    IPoseStack createPoseStack();

    IResourcePath createResourcePath(String namespace, String path);

    @Nullable
    IResourcePath parseResourcePath(String location);

    ITesselator getTesselator();

    void setPositionShader();

    void setPositionColorShader();

    void setPositionTexShader();

    void setPositionTexColorShader();
}
