package goblinbob.mobends.forge.platform;

import goblinbob.mobends.platform.armor.IArmorTextureProvider;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ForgeArmorTextureProvider implements IArmorTextureProvider
{
    @Override
    @Nullable
    public <E extends LivingEntity> ResourceLocation getArmorTexture(
            ArmorItem armorItem, ItemStack itemStack, E entity,
            EquipmentSlot slot, @Nullable Object layer, boolean isInnerModel)
    {
        String texture = armorItem.getArmorTexture(itemStack, entity, slot, null);
        if (texture != null)
        {
            return ResourceLocationFactory.parse(texture);
        }
        return null;
    }
}
