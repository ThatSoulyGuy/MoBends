package goblinbob.mobends.forge.platform;

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
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraft.world.entity.Entity;

public class ForgePlatformServices extends McPlatformServices
{
    public ForgePlatformServices()
    {
        super(ForgeVertexConsumer::new);

        IEntityVertexHelper.Holder.setHelper(new ForgeEntityVertexHelper());
        IArmorHelper.Holder.setHelper(new ForgeArmorHelper());
        IModelRenderHelper.Holder.setHelper(new ForgeModelRenderHelper());

        ArmorModelProviderHolder.setProvider(new ForgeArmorModelProvider());
        IArmorTextureProvider.Holder.setProvider(new ForgeArmorTextureProvider());
        IArmorLayerProvider.Holder.setProvider(new ForgeArmorLayerProvider());
        IArmorColorProvider.Holder.setProvider(new ForgeArmorColorProvider());
        EntityHelper.setProvider(Entity::shouldRiderSit);
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
        return new ForgeTesselator();
    }

    @Override
    public void setConfigBoolean(String key, boolean value)
    {
        goblinbob.mobends.forge.ForgeConfig.set(key, value);
    }
}
