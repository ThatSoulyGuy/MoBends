package goblinbob.mobends.neoforge.platform;

import goblinbob.mobends.api.armor.IArmorTextureProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * NeoForge 1.21.1 implementation of IArmorTextureProvider.
 *
 * Uses NeoForge's IItemExtension.getArmorTexture() hook to allow mods
 * to override the armor texture location.
 */
public class NeoForgeArmorTextureProvider implements IArmorTextureProvider
{
    @Override
    @Nullable
    public <E extends LivingEntity> ResourceLocation getArmorTexture(
            ArmorItem armorItem, ItemStack itemStack, E entity,
            EquipmentSlot slot, @Nullable Object layer, boolean isInnerModel)
    {
        // NeoForge 1.21.1: ArmorItem.getArmorTexture(ItemStack, Entity, EquipmentSlot, ArmorMaterial.Layer, boolean)
        ArmorMaterial.Layer materialLayer = (layer instanceof ArmorMaterial.Layer) ? (ArmorMaterial.Layer) layer : null;
        return armorItem.getArmorTexture(itemStack, entity, slot, materialLayer, isInnerModel);
    }
}
