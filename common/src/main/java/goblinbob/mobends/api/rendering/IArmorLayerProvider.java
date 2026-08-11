package goblinbob.mobends.api.rendering;

import goblinbob.mobends.api.platform.PlatformServices;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IArmorLayerProvider
{
    void forEachLayer(Object armorMaterial, Consumer<Object> layerConsumer);

    String getLayerTexture(Object armorMaterial, Object layer, boolean isInnerModel);

    boolean layerHasDyeColor(Object layer);

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
