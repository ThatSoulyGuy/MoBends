package goblinbob.mobends.forge.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.platform.VersionAdapter;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import org.joml.Matrix4f;

/**
 * Forge 1.20.1 implementation of VersionAdapter.
 * Provides MC 1.20.1 specific API implementations.
 */
public class ForgeVersionAdapter implements VersionAdapter
{
    @Override
    public String getPlayerModelName(AbstractClientPlayer player)
    {
        // MC 1.20.1 uses getModelName()
        return player.getModelName();
    }

    @Override
    public ResourceLocation parseResourceLocation(String location)
    {
        // MC 1.20.1 uses constructor
        return new ResourceLocation(location);
    }

    @Override
    public ResourceLocation createResourceLocation(String namespace, String path)
    {
        // MC 1.20.1 uses constructor
        return new ResourceLocation(namespace, path);
    }

    @Override
    public boolean isClient()
    {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public boolean isDevelopmentEnvironment()
    {
        return !FMLLoader.isProduction();
    }

    @Override
    public String getPlatformName()
    {
        return "Forge";
    }

    @Override
    public String getMinecraftVersion()
    {
        return "1.20.1";
    }

    @Override
    public void mulPoseMatrix(PoseStack poseStack, Matrix4f matrix)
    {
        // MC 1.20.1 uses mulPoseMatrix(Matrix4f)
        poseStack.mulPoseMatrix(matrix);
    }

    @Override
    public boolean isItemEdible(ItemStack itemStack)
    {
        // MC 1.20.1 uses isEdible()
        return itemStack.isEdible();
    }
}
