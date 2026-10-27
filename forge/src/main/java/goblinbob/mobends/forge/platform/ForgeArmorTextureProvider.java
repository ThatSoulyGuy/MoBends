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
            EquipmentSlot slot, @Nullable Object layer, @Nullable String type, boolean isInnerModel)
    {
        String texture = armorItem.getArmorTexture(itemStack, entity, slot, type);
        if (texture != null)
        {
            return ResourceLocationFactory.parse(texture);
        }

        String materialName = armorItem.getMaterial().getName();
        String domain = "minecraft";
        int colonIndex = materialName.indexOf(':');
        if (colonIndex >= 0)
        {
            domain = materialName.substring(0, colonIndex);
            materialName = materialName.substring(colonIndex + 1);
        }

        return ResourceLocationFactory.create(domain, "textures/models/armor/" + materialName
                + "_layer_" + (isInnerModel ? 2 : 1) + (type == null ? "" : "_" + type) + ".png");
    }
}
