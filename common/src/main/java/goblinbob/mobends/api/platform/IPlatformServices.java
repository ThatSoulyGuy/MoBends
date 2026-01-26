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

/**
 * Central platform services interface.
 * Each platform (NeoForge, Forge, Fabric) provides an implementation of this interface.
 */
public interface IPlatformServices
{
    /**
     * @return The platform name (e.g., "NeoForge", "Forge", "Fabric")
     */
    String getPlatformName();

    /**
     * @return The Minecraft version (e.g., "1.21.1", "1.20.1")
     */
    String getMinecraftVersion();

    /**
     * @return Whether we're running on the client side
     */
    boolean isClient();

    /**
     * @return Whether we're running in a development environment
     */
    boolean isDevelopmentEnvironment();

    /**
     * @return The resource manager for loading resources
     */
    IResourceManager getResourceManager();

    /**
     * @return The localization service for translations
     */
    ILocalization getLocalization();

    /**
     * @return The render layer provider for creating render types
     */
    IRenderLayerProvider getRenderLayerProvider();

    /**
     * Wraps a native entity object into an IEntity
     * @param nativeEntity The native Minecraft entity
     * @return The wrapped entity, or null if not applicable
     */
    @Nullable
    IEntity wrapEntity(Object nativeEntity);

    /**
     * Wraps a native entity object into an ILivingEntity
     * @param nativeEntity The native Minecraft LivingEntity
     * @return The wrapped living entity, or null if not applicable
     */
    @Nullable
    ILivingEntity wrapLivingEntity(Object nativeEntity);

    /**
     * Wraps a native entity object into an IPlayer
     * @param nativeEntity The native Minecraft Player
     * @return The wrapped player, or null if not applicable
     */
    @Nullable
    IPlayer wrapPlayer(Object nativeEntity);

    /**
     * Wraps a native PoseStack object
     * @param nativePoseStack The native Minecraft PoseStack
     * @return The wrapped pose stack
     */
    IPoseStack wrapPoseStack(Object nativePoseStack);

    /**
     * Wraps a native MultiBufferSource object
     * @param nativeBufferSource The native Minecraft MultiBufferSource
     * @return The wrapped buffer source
     */
    IBufferSource wrapBufferSource(Object nativeBufferSource);

    /**
     * Wraps a native ItemStack object
     * @param nativeItemStack The native Minecraft ItemStack
     * @return The wrapped item stack
     */
    IItemStack wrapItemStack(Object nativeItemStack);

    /**
     * Wraps a native ResourceLocation object
     * @param nativeResourceLocation The native Minecraft ResourceLocation
     * @return The wrapped resource path
     */
    IResourcePath wrapResourceLocation(Object nativeResourceLocation);

    /**
     * Creates a new pose stack
     * @return A new pose stack instance
     */
    IPoseStack createPoseStack();

    /**
     * Creates a resource path from namespace and path
     * @param namespace The namespace
     * @param path The path
     * @return The resource path
     */
    IResourcePath createResourcePath(String namespace, String path);

    /**
     * Parses a resource path from a string (e.g., "minecraft:textures/entity/player.png")
     * @param location The location string
     * @return The resource path, or null if invalid
     */
    @Nullable
    IResourcePath parseResourcePath(String location);

    /**
     * @return The tesselator instance for immediate-mode rendering
     */
    ITesselator getTesselator();

    /**
     * Sets the current shader to position-only (no color, no texture)
     */
    void setPositionShader();

    /**
     * Sets the current shader to position + color
     */
    void setPositionColorShader();

    /**
     * Sets the current shader to position + texture
     */
    void setPositionTexShader();

    /**
     * Sets the current shader to position + texture + color
     */
    void setPositionTexColorShader();
}
