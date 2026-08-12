package goblinbob.mobends.neoforge.platform;

import goblinbob.mobends.api.armor.IArmorTextureProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class NeoForgeArmorTextureProvider implements IArmorTextureProvider
{
    @Override
    @Nullable
    public <E extends LivingEntity> ResourceLocation getArmorTexture(
            ArmorItem armorItem, ItemStack itemStack, E entity,
            EquipmentSlot slot, @Nullable Object layer, boolean isInnerModel)
    {
        ArmorMaterial.Layer materialLayer = (layer instanceof ArmorMaterial.Layer) ? (ArmorMaterial.Layer) layer : null;

        if (materialLayer == null)
        {
            materialLayer = getFirstLayer(armorItem);
        }

        if (materialLayer == null)
        {
            return null;
        }

        ResourceLocation overridden = armorItem.getArmorTexture(itemStack, entity, slot, materialLayer, isInnerModel);
        if (overridden != null)
        {
            return overridden;
        }

        return materialLayer.texture(isInnerModel);
    }

    @Nullable
    private static ArmorMaterial.Layer getFirstLayer(ArmorItem armorItem)
    {
        List<ArmorMaterial.Layer> layers = armorItem.getMaterial().value().layers();
        return layers.isEmpty() ? null : layers.get(0);
    }
}
