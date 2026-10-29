package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

public class ArmorModelApiCompat
{
    private static final String MOD_ID = "armor_model_api";

    private static boolean initialized = false;
    private static boolean available = false;
    private static Method getRendererMethod = null;
    private static Method modelMethod = null;
    private static Method configMethod = null;
    private static Method textureMethod = null;
    private static Method applySlotVisibilityMethod = null;
    private static Method layersMethod = null;
    private static Class<?> trimLayerClass = null;
    private static Field texturePermutationsField = null;
    private static Field fallbackTextureField = null;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        try
        {
            final Class<?> renderers = Class.forName("net.rpg_foundation.armor_api.client.ArmorRenderers");
            final Class<?> renderer = Class.forName("net.rpg_foundation.armor_api.client.GeoArmorRenderer");
            final Class<?> config = Class.forName("net.rpg_foundation.armor_api.client.GeoArmorRenderer$Config");
            final Class<?> model = Class.forName("net.rpg_foundation.armor_api.client.model.GeoArmorModel");

            getRendererMethod = renderers.getMethod("get", Item.class);
            modelMethod = renderer.getMethod("model");
            configMethod = renderer.getMethod("config");
            textureMethod = config.getMethod("texture");
            applySlotVisibilityMethod = model.getMethod("applySlotVisibility", EquipmentSlot.class);
            layersMethod = config.getMethod("layers");
            available = true;
        }
        catch (Throwable e)
        {
            available = false;
        }

        try
        {
            trimLayerClass = Class.forName("net.rpg_foundation.armor_api.client.layer.TrimLayer");
            texturePermutationsField = trimLayerClass.getDeclaredField("texturePermutations");
            texturePermutationsField.setAccessible(true);
            fallbackTextureField = trimLayerClass.getDeclaredField("fallbackTexture");
            fallbackTextureField.setAccessible(true);
        }
        catch (Throwable e)
        {
            trimLayerClass = null;
        }
    }

    @Nullable
    private static Object getRenderer(ItemStack itemStack)
    {
        if (!initialized)
        {
            init();
        }

        if (!available)
        {
            return null;
        }

        try
        {
            return getRendererMethod.invoke(null, itemStack.getItem());
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    @Nullable
    public static Model getArmorModel(ItemStack itemStack, EquipmentSlot slot)
    {
        final Object renderer = getRenderer(itemStack);

        if (renderer == null)
        {
            return null;
        }

        try
        {
            final Object model = modelMethod.invoke(renderer);

            if (model == null)
            {
                return null;
            }

            applySlotVisibilityMethod.invoke(model, slot);
            return (Model) model;
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    @Nullable
    public static ResourceLocation getTexture(ItemStack itemStack)
    {
        final Object renderer = getRenderer(itemStack);

        if (renderer == null)
        {
            return null;
        }

        try
        {
            return (ResourceLocation) textureMethod.invoke(configMethod.invoke(renderer));
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    public static boolean isArmor(ItemStack itemStack)
    {
        return getRenderer(itemStack) != null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static TextureAtlasSprite getTrimSprite(ItemStack itemStack, ArmorTrim trim, TextureAtlas atlas)
    {
        final Object renderer = getRenderer(itemStack);

        if (renderer == null || trimLayerClass == null)
        {
            return null;
        }

        try
        {
            for (Object layer : (java.util.List<?>) layersMethod.invoke(configMethod.invoke(renderer)))
            {
                if (trimLayerClass.isInstance(layer))
                {
                    final Function<ArmorTrim, ResourceLocation> permutations =
                            (Function<ArmorTrim, ResourceLocation>) texturePermutationsField.get(layer);
                    if (permutations == null)
                    {
                        return null;
                    }

                    final TextureAtlasSprite sprite = atlas.getSprite(permutations.apply(trim));
                    if (!isMissing(sprite))
                    {
                        return sprite;
                    }

                    final ResourceLocation fallback = (ResourceLocation) fallbackTextureField.get(layer);
                    final TextureAtlasSprite fallbackSprite = fallback == null ? null : atlas.getSprite(fallback);
                    return fallbackSprite == null || isMissing(fallbackSprite) ? null : fallbackSprite;
                }
            }
        }
        catch (Throwable e)
        {
            return null;
        }

        return null;
    }

    private static boolean isMissing(TextureAtlasSprite sprite)
    {
        return sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation());
    }
}
