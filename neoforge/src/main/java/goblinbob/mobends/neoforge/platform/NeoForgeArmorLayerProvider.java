package goblinbob.mobends.neoforge.platform;

import goblinbob.mobends.api.rendering.IArmorLayerProvider;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

import java.util.function.Consumer;

public class NeoForgeArmorLayerProvider implements IArmorLayerProvider
{
    @Override
    public void forEachLayer(Object armorItem, Consumer<Object> layerConsumer)
    {
        if (armorItem instanceof ArmorItem item)
        {
            ArmorMaterial material = item.getMaterial().value();
            for (ArmorMaterial.Layer layer : material.layers())
            {
                layerConsumer.accept(layer);
            }
        }
    }

    @Override
    public String getLayerTexture(Object armorMaterial, Object layer, boolean isInnerModel)
    {
        if (layer instanceof ArmorMaterial.Layer materialLayer)
        {
            return materialLayer.texture(isInnerModel).toString();
        }
        return "";
    }

    @Override
    public boolean layerHasDyeColor(Object layer)
    {
        if (layer instanceof ArmorMaterial.Layer materialLayer)
        {
            return materialLayer.dyeable();
        }
        return false;
    }
}
