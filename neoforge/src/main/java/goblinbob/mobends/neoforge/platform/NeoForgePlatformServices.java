package goblinbob.mobends.neoforge.platform;

import goblinbob.mobends.api.rendering.IArmorColorProvider;
import goblinbob.mobends.api.rendering.IArmorHelper;
import goblinbob.mobends.api.rendering.IArmorLayerProvider;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;
import goblinbob.mobends.api.rendering.IModelRenderHelper;
import goblinbob.mobends.api.rendering.ITesselator;
import goblinbob.mobends.core.util.EntityHelper;
import goblinbob.mobends.platform.McPlatformServices;
import goblinbob.mobends.platform.armor.ArmorModelProviderHolder;
import goblinbob.mobends.platform.armor.IArmorTextureProvider;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.minecraft.world.entity.Entity;

public class NeoForgePlatformServices extends McPlatformServices
{
    public NeoForgePlatformServices()
    {
        super(NeoForgeVertexConsumer::new);

        IEntityVertexHelper.Holder.setHelper(new NeoForgeEntityVertexHelper());
        IArmorHelper.Holder.setHelper(new NeoForgeArmorHelper());
        IModelRenderHelper.Holder.setHelper(new NeoForgeModelRenderHelper());

        ArmorModelProviderHolder.setProvider(new NeoForgeArmorModelProvider());
        IArmorTextureProvider.Holder.setProvider(new NeoForgeArmorTextureProvider());
        IArmorLayerProvider.Holder.setProvider(new NeoForgeArmorLayerProvider());
        IArmorColorProvider.Holder.setProvider(new NeoForgeArmorColorProvider());
        EntityHelper.setProvider(Entity::shouldRiderSit);
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
    public ITesselator getTesselator()
    {
        return new NeoForgeTesselator();
    }

    @Override
    public void setConfigBoolean(String key, boolean value)
    {
        goblinbob.mobends.neoforge.main.NeoForgeConfig.set(key, value);
    }
}
