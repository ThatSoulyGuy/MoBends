package goblinbob.mobends.forge.platform;

import goblinbob.mobends.api.rendering.IArmorLayerProvider;

import java.util.function.Consumer;

public class ForgeArmorLayerProvider implements IArmorLayerProvider
{
    @Override
    public void forEachLayer(Object armorItem, Consumer<Object> layerConsumer)
    {
        layerConsumer.accept(null);
    }

    @Override
    public String getLayerTexture(Object armorMaterial, Object layer, boolean isInnerModel)
    {
        return "";
    }

    @Override
    public boolean layerHasDyeColor(Object layer)
    {
        return false;
    }
}
