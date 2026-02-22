package goblinbob.mobends.forge.platform;

import goblinbob.mobends.api.armor.IArmorTextureProvider;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Forge 1.20.1 implementation of IArmorTextureProvider.
 *
 * Uses Forge's IForgeItem.getArmorTexture() hook to allow mods to override
 * the armor texture location.
 */
public class ForgeArmorTextureProvider implements IArmorTextureProvider
{
    @Override
    @Nullable
    public <E extends LivingEntity> ResourceLocation getArmorTexture(
            ArmorItem armorItem, ItemStack itemStack, E entity,
            EquipmentSlot slot, @Nullable Object layer, boolean isInnerModel)
    {
        // Forge 1.20.1: Item.getArmorTexture(ItemStack, Entity, EquipmentSlot, @Nullable String type)
        // type is null for base texture, "overlay" for dyed leather overlay
        String texture = armorItem.getArmorTexture(itemStack, entity, slot, null);
        if (texture != null)
        {
            return ResourceLocationFactory.parse(texture);
        }
        return null;
    }
}
