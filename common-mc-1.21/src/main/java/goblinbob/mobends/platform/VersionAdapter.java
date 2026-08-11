package goblinbob.mobends.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public interface VersionAdapter
{
    String getPlayerModelName(AbstractClientPlayer player);

    ResourceLocation parseResourceLocation(String location);

    ResourceLocation createResourceLocation(String namespace, String path);

    boolean isClient();

    boolean isDevelopmentEnvironment();

    String getPlatformName();

    String getMinecraftVersion();

    void mulPoseMatrix(PoseStack poseStack, Matrix4f matrix);

    boolean isItemEdible(ItemStack itemStack);

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
