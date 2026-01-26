package goblinbob.mobends.api.rendering;

import goblinbob.mobends.api.platform.PlatformServices;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Platform-agnostic interface for iterating over armor material layers.
 *
 * In MC 1.21.1, armor materials have multiple layers (for trims, dyes, etc).
 * In MC 1.20.1, armor materials don't have layers - this provides compatibility.
 */
public interface IArmorLayerProvider
{
    /**
     * Iterates over the armor layers for a given armor material.
     *
     * @param armorMaterial The native ArmorMaterial object (type varies by MC version)
     * @param layerConsumer Consumer called for each layer - receives the layer object
     *                      (ArmorMaterial.Layer in 1.21.1, null in 1.20.1 for the single layer)
     */
    void forEachLayer(Object armorMaterial, Consumer<Object> layerConsumer);

    /**
     * Gets the texture location for an armor layer.
     *
     * @param armorMaterial The native ArmorMaterial object
     * @param layer The layer object (may be null for 1.20.1 compatibility)
     * @param isInnerModel Whether this is for the inner model (leggings)
     * @return The texture location string
     */
    String getLayerTexture(Object armorMaterial, Object layer, boolean isInnerModel);

    /**
     * Checks if the layer has a dye color.
     *
     * @param layer The layer object
     * @return True if this layer supports dyeing
     */
    boolean layerHasDyeColor(Object layer);

    /**
     * Singleton holder for the platform-specific implementation.
     */
    class Holder
    {
        private static IArmorLayerProvider provider;

        public static void setProvider(IArmorLayerProvider provider)
        {
            Holder.provider = provider;
        }

        public static IArmorLayerProvider getProvider()
        {
            return provider;
        }
    }
}
