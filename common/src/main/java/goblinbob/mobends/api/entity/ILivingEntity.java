package goblinbob.mobends.api.entity;

import javax.annotation.Nullable;

public interface ILivingEntity extends IEntity
{
    float getHealth();

    float getMaxHealth();

    float getLimbSwing();

    float getLimbSwingAmount();

    float getAttackAnim();

    int getHurtTime();

    int getDeathTime();

    float getHeadYaw();

    float getPrevHeadYaw();

    float getBodyYaw();

    float getPrevBodyYaw();

    boolean isBaby();

    boolean isSleeping();

    boolean isDead();

    boolean isUsingItem();

    int getTicksUsingItem();

    int getUsedItemHand();

    boolean isFallFlying();

    boolean isSwimming();

    boolean isVisuallySwimming();

    boolean isCrouching();

    IItemStack getItemBySlot(IEquipmentSlot slot);

    default IItemStack getMainHandItem()
    {
        return getItemBySlot(IEquipmentSlot.MAINHAND);
    }

    default IItemStack getOffhandItem()
    {
        return getItemBySlot(IEquipmentSlot.OFFHAND);
    }

    @Nullable
    IEntity getVehicle();

    default float getLerpedBodyYaw(float partialTicks)
    {
        return getPrevBodyYaw() + (getBodyYaw() - getPrevBodyYaw()) * partialTicks;
    }

    default float getLerpedHeadYaw(float partialTicks)
    {
        return getPrevHeadYaw() + (getHeadYaw() - getPrevHeadYaw()) * partialTicks;
    }
}
