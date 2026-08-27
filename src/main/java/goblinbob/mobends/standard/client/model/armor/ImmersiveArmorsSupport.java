package goblinbob.mobends.standard.client.model.armor;

import goblinbob.mobends.core.util.ResourceLocationFactory;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ImmersiveArmorsSupport
{
    private static final boolean AVAILABLE;

    private static Class<?> materialClass;
    private static Class<?> layerPieceClass;
    private static Class<?> extendedItemClass;
    private static Method itemGetExtendedMaterial;
    private static Method materialGetPieces;
    private static Method pieceGetTexture;
    private static Method pieceIsTranslucent;
    private static Method pieceIsGlowing;
    private static Method pieceIsColored;
    private static Method pieceGetModel;

    private static final Map<String, List<Layer>> CACHE = new ConcurrentHashMap<>();

    static
    {
        boolean available = false;

        try
        {
            materialClass = Class.forName("immersive_armors.item.ExtendedArmorMaterial");
            layerPieceClass = Class.forName("immersive_armors.client.render.entity.piece.LayerPiece");

            Class<?> pieceClass = Class.forName("immersive_armors.client.render.entity.piece.Piece");

            materialGetPieces = materialClass.getMethod("getPieces", EquipmentSlot.class);
            pieceGetTexture = pieceClass.getMethod("getTexture");
            pieceIsTranslucent = optionalMethod(pieceClass, "isTranslucent");
            pieceIsGlowing = optionalMethod(pieceClass, "isGlowing");
            pieceIsColored = optionalMethod(pieceClass, "isColored");

            pieceGetModel = layerPieceClass.getDeclaredMethod("getModel");
            pieceGetModel.setAccessible(true);

            try
            {
                extendedItemClass = Class.forName("immersive_armors.item.ExtendedArmorItem");
                itemGetExtendedMaterial = extendedItemClass.getMethod("getExtendedMaterial");
            }
            catch (Throwable ignored)
            {
                itemGetExtendedMaterial = null;
            }

            available = true;
        }
        catch (Throwable t)
        {
        }

        AVAILABLE = available;
    }

    private ImmersiveArmorsSupport()
    {
    }

    public static boolean isAvailable()
    {
        return AVAILABLE;
    }

    @Nullable
    private static Method optionalMethod(Class<?> owner, String name)
    {
        try
        {
            return owner.getMethod(name);
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }

    private static boolean flag(@Nullable Method method, Object piece)
    {
        if (method == null)
        {
            return false;
        }

        try
        {
            return Boolean.TRUE.equals(method.invoke(piece));
        }
        catch (Throwable ignored)
        {
            return false;
        }
    }

    @Nullable
    private static Object resolveExtendedMaterial(ArmorItem armorItem)
    {
        if (itemGetExtendedMaterial != null && extendedItemClass.isInstance(armorItem))
        {
            try
            {
                Object extended = itemGetExtendedMaterial.invoke(armorItem);
                if (materialClass.isInstance(extended))
                {
                    return extended;
                }
            }
            catch (Throwable ignored)
            {
            }
        }

        Object material = armorItem.getMaterial();
        return materialClass.isInstance(material) ? material : null;
    }

    public static List<Layer> getLayers(ArmorItem armorItem, EquipmentSlot slot, String materialName)
    {
        if (!AVAILABLE || armorItem == null || materialName == null || materialName.isEmpty())
        {
            return Collections.emptyList();
        }

        Object material = resolveExtendedMaterial(armorItem);
        if (material == null)
        {
            return Collections.emptyList();
        }

        String key = armorItem.getDescriptionId() + "|" + slot;
        List<Layer> cached = CACHE.get(key);
        if (cached != null)
        {
            return cached;
        }

        List<Layer> layers = new ArrayList<>();

        try
        {
            Object pieces = materialGetPieces.invoke(material, slot);

            if (pieces instanceof List<?> pieceList)
            {
                for (Object piece : pieceList)
                {
                    Layer layer = readLayer(piece, materialName);

                    if (layer != null)
                    {
                        layers.add(layer);
                    }
                }
            }
        }
        catch (Throwable ignored)
        {
        }

        List<Layer> result = Collections.unmodifiableList(layers);
        CACHE.put(key, result);

        return result;
    }

    private static Layer readLayer(Object piece, String materialName) throws Exception
    {
        if (piece == null || !layerPieceClass.isInstance(piece))
        {
            return null;
        }

        Object textureName = pieceGetTexture.invoke(piece);
        if (!(textureName instanceof String texture) || texture.isEmpty())
        {
            return null;
        }

        Object model = pieceGetModel.invoke(piece);
        if (!(model instanceof HumanoidModel<?> humanoidModel))
        {
            return null;
        }

        String base = "textures/models/armor/" + materialName + "/" + texture;

        return new Layer(
                humanoidModel,
                ResourceLocationFactory.create("immersive_armors", base + ".png"),
                ResourceLocationFactory.create("immersive_armors", base + "_overlay.png"),
                flag(pieceIsTranslucent, piece),
                flag(pieceIsGlowing, piece),
                pieceIsColored == null || flag(pieceIsColored, piece));
    }

    public static final class Layer
    {
        public final HumanoidModel<?> model;
        public final ResourceLocation texture;
        public final ResourceLocation overlayTexture;
        public final boolean translucent;
        public final boolean glowing;
        public final boolean colored;

        private Layer(HumanoidModel<?> model, ResourceLocation texture, ResourceLocation overlayTexture,
                      boolean translucent, boolean glowing, boolean colored)
        {
            this.model = model;
            this.texture = texture;
            this.overlayTexture = overlayTexture;
            this.translucent = translucent;
            this.glowing = glowing;
            this.colored = colored;
        }
    }
}
