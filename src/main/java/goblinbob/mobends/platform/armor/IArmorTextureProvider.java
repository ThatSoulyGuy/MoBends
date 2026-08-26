package goblinbob.mobends.platform.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IArmorTextureProvider
{
    @Nullable
    <E extends LivingEntity> ResourceLocation getArmorTexture(
            ArmorItem armorItem,
            ItemStack itemStack,
            E entity,
            EquipmentSlot slot,
            @Nullable Object layer,
            @Nullable String type,
            boolean isInnerModel
    );

    IArmorTextureProvider DEFAULT = new IArmorTextureProvider()
    {
        @Override
        public <E extends LivingEntity> ResourceLocation getArmorTexture(
                ArmorItem armorItem, ItemStack itemStack, E entity,
                EquipmentSlot slot, Object layer, String type, boolean isInnerModel)
        {
            return null;
        }
    };

    class Holder
    {
        private static IArmorTextureProvider provider = DEFAULT;

        public static void setProvider(IArmorTextureProvider provider)
        {
            Holder.provider = provider;
        }

        public static IArmorTextureProvider getProvider()
        {
            return provider;
        }
    }
}
