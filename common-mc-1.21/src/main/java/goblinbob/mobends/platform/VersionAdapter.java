package goblinbob.mobends.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * Adapter interface for MC version-specific API differences.
 * Each platform module provides an implementation for its MC version.
 */
public interface VersionAdapter
{
    /**
     * Get the player's skin model name ("default" or "slim").
     * MC 1.20.1: Uses AbstractClientPlayer.getModelName()
     * MC 1.21.1: Uses AbstractClientPlayer.getSkin().model().id()
     */
    String getPlayerModelName(AbstractClientPlayer player);

    /**
     * Parse a resource location from a string.
     * MC 1.20.1: new ResourceLocation(location)
     * MC 1.21.1: ResourceLocation.parse(location)
     */
    ResourceLocation parseResourceLocation(String location);

    /**
     * Create a resource location from namespace and path.
     * MC 1.20.1: new ResourceLocation(namespace, path)
     * MC 1.21.1: ResourceLocation.fromNamespaceAndPath(namespace, path)
     */
    ResourceLocation createResourceLocation(String namespace, String path);

    /**
     * Check if running on client side.
     * Uses platform-specific FML/NeoForge APIs.
     */
    boolean isClient();

    /**
     * Check if running in development environment.
     * Uses platform-specific FML/NeoForge APIs.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Get the platform name (e.g., "Forge" or "NeoForge").
     */
    String getPlatformName();

    /**
     * Get the Minecraft version.
     */
    String getMinecraftVersion();

    /**
     * Multiply pose stack by a matrix.
     * MC 1.20.1: Uses PoseStack.mulPoseMatrix(Matrix4f)
     * MC 1.21.1: Uses PoseStack.mulPose(Matrix4f)
     */
    void mulPoseMatrix(PoseStack poseStack, Matrix4f matrix);

    /**
     * Check if an item stack is edible (food).
     * MC 1.20.1: Uses ItemStack.isEdible()
     * MC 1.21.1: Uses ItemStack.has(DataComponents.FOOD)
     */
    boolean isItemEdible(ItemStack itemStack);

    /**
     * Holder for the singleton instance.
     */
    class Holder
    {
        private static VersionAdapter instance;

        public static void set(VersionAdapter adapter)
        {
            instance = adapter;
        }

        public static VersionAdapter get()
        {
            if (instance == null)
            {
                throw new IllegalStateException("VersionAdapter not initialized");
            }
            return instance;
        }
    }
}
