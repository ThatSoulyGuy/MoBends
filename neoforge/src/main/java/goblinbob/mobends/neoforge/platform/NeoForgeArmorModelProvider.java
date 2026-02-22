package goblinbob.mobends.neoforge.platform;

import goblinbob.mobends.api.armor.IArmorModelProvider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * NeoForge 1.21.1 implementation of IArmorModelProvider.
 *
 * Returns the default model. Custom model handling in NeoForge 1.21.1
 * is done through the data-driven armor system.
 */
public class NeoForgeArmorModelProvider implements IArmorModelProvider
{
    @Override
    public <E extends LivingEntity> HumanoidModel<E> getCustomArmorModel(
            E entity, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<E> defaultModel)
    {
        return defaultModel;
    }
}
