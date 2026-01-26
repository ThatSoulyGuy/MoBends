package goblinbob.mobends.neoforge.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.api.entity.IEntity;
import goblinbob.mobends.api.entity.IItemStack;
import goblinbob.mobends.api.entity.ILivingEntity;
import goblinbob.mobends.api.entity.IPlayer;
import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.api.platform.IPlatformServices;
import goblinbob.mobends.api.rendering.IArmorHelper;
import goblinbob.mobends.api.rendering.IBufferSource;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;
import goblinbob.mobends.api.rendering.IModelRenderHelper;
import goblinbob.mobends.api.rendering.IPoseStack;
import goblinbob.mobends.api.rendering.IRenderLayerProvider;
import goblinbob.mobends.api.rendering.ITesselator;
import net.minecraft.client.renderer.GameRenderer;
import goblinbob.mobends.api.resource.ILocalization;
import goblinbob.mobends.api.resource.IResourceManager;
import goblinbob.mobends.api.resource.IResourcePath;
import goblinbob.mobends.platform.McEntity;
import goblinbob.mobends.platform.McItemStack;
import goblinbob.mobends.platform.McLivingEntity;
import goblinbob.mobends.platform.McLocalization;
import goblinbob.mobends.platform.McPlayer;
import goblinbob.mobends.platform.McPoseStack;
import goblinbob.mobends.platform.McResourceManager;
import goblinbob.mobends.platform.McRenderLayerProvider;
import goblinbob.mobends.platform.McResourcePath;
import goblinbob.mobends.platform.VersionAdapter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;

import javax.annotation.Nullable;

/**
 * NeoForge implementation of IPlatformServices.
 * Provides platform-specific functionality for NeoForge 1.21.1.
 */
public class NeoForgePlatformServices implements IPlatformServices
{
    private final McResourceManager resourceManager = new McResourceManager();
    private final McLocalization localization = new McLocalization();
    private final McRenderLayerProvider renderLayerProvider = new McRenderLayerProvider();

    public NeoForgePlatformServices()
    {
        // Initialize the version adapter first (shared code depends on it)
        VersionAdapter.Holder.set(new NeoForgeVersionAdapter());

        // Initialize helper singletons
        IEntityVertexHelper.Holder.setHelper(new NeoForgeEntityVertexHelper());
        IArmorHelper.Holder.setHelper(new NeoForgeArmorHelper());
        IModelRenderHelper.Holder.setHelper(new NeoForgeModelRenderHelper());
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
    public IResourceManager getResourceManager()
    {
        return resourceManager;
    }

    @Override
    public ILocalization getLocalization()
    {
        return localization;
    }

    @Override
    public IRenderLayerProvider getRenderLayerProvider()
    {
        return renderLayerProvider;
    }

    @Override
    @Nullable
    public IEntity wrapEntity(Object nativeEntity)
    {
        if (nativeEntity instanceof Entity entity)
        {
            return new McEntity(entity);
        }
        return null;
    }

    @Override
    @Nullable
    public ILivingEntity wrapLivingEntity(Object nativeEntity)
    {
        if (nativeEntity instanceof LivingEntity entity)
        {
            return new McLivingEntity(entity);
        }
        return null;
    }

    @Override
    @Nullable
    public IPlayer wrapPlayer(Object nativeEntity)
    {
        if (nativeEntity instanceof Player player)
        {
            return new McPlayer(player);
        }
        return null;
    }

    @Override
    public IPoseStack wrapPoseStack(Object nativePoseStack)
    {
        if (nativePoseStack instanceof PoseStack poseStack)
        {
            return new McPoseStack(poseStack);
        }
        throw new IllegalArgumentException("Expected PoseStack, got: " + nativePoseStack.getClass().getName());
    }

    @Override
    public IBufferSource wrapBufferSource(Object nativeBufferSource)
    {
        if (nativeBufferSource instanceof MultiBufferSource bufferSource)
        {
            return new NeoForgeBufferSource(bufferSource);
        }
        throw new IllegalArgumentException("Expected MultiBufferSource, got: " + nativeBufferSource.getClass().getName());
    }

    @Override
    public IItemStack wrapItemStack(Object nativeItemStack)
    {
        if (nativeItemStack instanceof ItemStack itemStack)
        {
            return new McItemStack(itemStack);
        }
        throw new IllegalArgumentException("Expected ItemStack, got: " + nativeItemStack.getClass().getName());
    }

    @Override
    public IResourcePath wrapResourceLocation(Object nativeResourceLocation)
    {
        if (nativeResourceLocation instanceof ResourceLocation location)
        {
            return new McResourcePath(location);
        }
        throw new IllegalArgumentException("Expected ResourceLocation, got: " + nativeResourceLocation.getClass().getName());
    }

    @Override
    public IPoseStack createPoseStack()
    {
        return new McPoseStack();
    }

    @Override
    public IResourcePath createResourcePath(String namespace, String path)
    {
        return new McResourcePath(namespace, path);
    }

    @Override
    @Nullable
    public IResourcePath parseResourcePath(String location)
    {
        try
        {
            ResourceLocation loc = VersionAdapter.Holder.get().parseResourceLocation(location);
            return new McResourcePath(loc);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public ITesselator getTesselator()
    {
        return new NeoForgeTesselator();
    }

    @Override
    public void setPositionShader()
    {
        RenderSystem.setShader(GameRenderer::getPositionShader);
    }

    @Override
    public void setPositionColorShader()
    {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }

    @Override
    public void setPositionTexShader()
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    @Override
    public void setPositionTexColorShader()
    {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
    }
}
