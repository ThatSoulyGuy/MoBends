package goblinbob.mobends.forge.platform;

import goblinbob.mobends.api.armor.IArmorModelProvider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class ForgeArmorModelProvider implements IArmorModelProvider
{
    @Override
    public <E extends LivingEntity> Model getCustomArmorModel(
            E entity, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<E> defaultModel)
    {
        try
        {
            IClientItemExtensions extensions = IClientItemExtensions.of(itemStack);
            Model model = extensions.getGenericArmorModel(entity, itemStack, slot, defaultModel);

            if (model != null)
            {
                return model;
            }
        }
        catch (Exception e)
        {
        }

        return defaultModel;
    }
}
