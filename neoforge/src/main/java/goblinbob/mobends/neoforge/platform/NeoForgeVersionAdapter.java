package goblinbob.mobends.neoforge.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.platform.VersionAdapter;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import org.joml.Matrix4f;

/**
 * NeoForge 1.21.1 implementation of VersionAdapter.
 * Provides MC 1.21.1 specific API implementations.
 */
public class NeoForgeVersionAdapter implements VersionAdapter
{
    @Override
    public String getPlayerModelName(AbstractClientPlayer player)
    {
        // MC 1.21.1 uses getSkin().model().id()
        return player.getSkin().model().id();
    }

    @Override
    public ResourceLocation parseResourceLocation(String location)
    {
        // MC 1.21.1 uses ResourceLocation.parse()
        return ResourceLocation.parse(location);
    }

    @Override
    public ResourceLocation createResourceLocation(String namespace, String path)
    {
        // MC 1.21.1 uses ResourceLocation.fromNamespaceAndPath()
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
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
        return "NeoForge";
    }

    @Override
    public String getMinecraftVersion()
    {
        return "1.21.1";
    }

    @Override
    public void mulPoseMatrix(PoseStack poseStack, Matrix4f matrix)
    {
        // MC 1.21.1 uses mulPose(Matrix4f)
        poseStack.mulPose(matrix);
    }

    @Override
    public boolean isItemEdible(ItemStack itemStack)
    {
        // MC 1.21.1 uses DataComponents
        return itemStack.has(DataComponents.FOOD);
    }
}
