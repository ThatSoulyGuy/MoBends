package goblinbob.mobends.api.rendering;

import goblinbob.mobends.api.entity.ILivingEntity;

public interface IRenderContext
{
    IPoseStack getPoseStack();

    IBufferSource getBufferSource();

    ILivingEntity getEntity();

    int getPackedLight();

    int getPackedOverlay();

    float getPartialTicks();

    float getYBodyRot();

    float getHeadYaw();

    float getHeadPitch();

    float getLimbSwing();

    float getLimbSwingAmount();

    float getAgeInTicks();

    IRenderContext withEntity(ILivingEntity entity);
}
