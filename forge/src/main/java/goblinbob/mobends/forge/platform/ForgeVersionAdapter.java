package goblinbob.mobends.forge.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.platform.VersionAdapter;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import org.joml.Matrix4f;

public class ForgeVersionAdapter implements VersionAdapter
{
    @Override
    public String getPlayerModelName(AbstractClientPlayer player)
    {
        return player.getModelName();
    }

    @Override
    public ResourceLocation parseResourceLocation(String location)
    {
        return new ResourceLocation(location);
    }

    @Override
    public ResourceLocation createResourceLocation(String namespace, String path)
    {
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
        poseStack.mulPoseMatrix(matrix);
    }

    @Override
    public boolean isItemEdible(ItemStack itemStack)
    {
        return itemStack.isEdible();
    }
}
