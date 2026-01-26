package goblinbob.mobends.api.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Platform-specific provider for custom armor models.
 * NeoForge and Forge have different APIs for getting custom armor models.
 */
public interface IArmorModelProvider
{
    /**
     * Gets the custom armor model for the given item stack, if any.
     *
     * @param entity The entity wearing the armor
     * @param itemStack The armor item stack
     * @param slot The equipment slot
     * @param defaultModel The default model to use if no custom model exists
     * @return The custom model, or the default model if no custom model exists
     */
    <E extends LivingEntity> HumanoidModel<E> getCustomArmorModel(
            E entity,
            ItemStack itemStack,
            EquipmentSlot slot,
            HumanoidModel<E> defaultModel
    );

    /**
     * Default implementation that always returns the default model.
     */
    IArmorModelProvider DEFAULT = new IArmorModelProvider()
    {
        @Override
        public <E extends LivingEntity> HumanoidModel<E> getCustomArmorModel(
                E entity, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<E> defaultModel)
        {
            return defaultModel;
        }
    };
}
