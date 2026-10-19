package goblinbob.mobends.platform.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IArmorModelProvider
{
    <E extends LivingEntity> Model getCustomArmorModel(
            E entity,
            ItemStack itemStack,
            EquipmentSlot slot,
            HumanoidModel<E> defaultModel
    );

    default HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack itemStack)
    {
        return null;
    }

    IArmorModelProvider DEFAULT = new IArmorModelProvider()
    {
        @Override
        public <E extends LivingEntity> Model getCustomArmorModel(
                E entity, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<E> defaultModel)
        {
            return defaultModel;
        }
    };
}
