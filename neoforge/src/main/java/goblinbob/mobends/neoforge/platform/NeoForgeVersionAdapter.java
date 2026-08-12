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

public class NeoForgeVersionAdapter implements VersionAdapter
{
    @Override
    public String getPlayerModelName(AbstractClientPlayer player)
    {
        return player.getSkin().model().id();
    }

    @Override
    public ResourceLocation parseResourceLocation(String location)
    {
        return ResourceLocation.parse(location);
    }

    @Override
    public ResourceLocation createResourceLocation(String namespace, String path)
    {
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
        poseStack.mulPose(matrix);
    }

    @Override
    public boolean isItemEdible(ItemStack itemStack)
    {
        return itemStack.has(DataComponents.FOOD);
    }
}
