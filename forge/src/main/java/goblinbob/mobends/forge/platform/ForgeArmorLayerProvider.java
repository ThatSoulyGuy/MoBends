package goblinbob.mobends.forge.platform;

import goblinbob.mobends.api.rendering.IArmorLayerProvider;

import java.util.function.Consumer;

/**
 * Forge 1.20.1 implementation of IArmorLayerProvider.
 *
 * MC 1.20.1 doesn't have armor material layers (that's a 1.21+ feature).
 * This provider calls the consumer once with null to represent the single layer.
 */
public class ForgeArmorLayerProvider implements IArmorLayerProvider
{
    @Override
    public void forEachLayer(Object armorItem, Consumer<Object> layerConsumer)
    {
        // MC 1.20.1 has no material layers - just a single layer
        layerConsumer.accept(null);
    }

    @Override
    public String getLayerTexture(Object armorMaterial, Object layer, boolean isInnerModel)
    {
        // Not used directly - texture resolution goes through ForgeArmorTextureProvider
        return "";
    }

    @Override
    public boolean layerHasDyeColor(Object layer)
    {
        return false;
    }
}
