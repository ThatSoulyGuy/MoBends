package goblinbob.mobends.forge.platform;

import goblinbob.mobends.api.armor.IArmorModelProvider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * Forge 1.20.1 implementation of IArmorModelProvider.
 *
 * Uses Forge's IClientItemExtensions.getHumanoidArmorModel() to allow mods
 * to provide custom armor models.
 */
public class ForgeArmorModelProvider implements IArmorModelProvider
{
    @Override
    @SuppressWarnings("unchecked")
    public <E extends LivingEntity> HumanoidModel<E> getCustomArmorModel(
            E entity, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<E> defaultModel)
    {
        try
        {
            IClientItemExtensions extensions = IClientItemExtensions.of(itemStack);
            Model model = extensions.getHumanoidArmorModel(entity, itemStack, slot, defaultModel);

            if (model != defaultModel && model instanceof HumanoidModel<?>)
            {
                return (HumanoidModel<E>) model;
            }
        }
        catch (Exception e)
        {
            // Silently fall back to default model
        }

        return defaultModel;
    }
}
