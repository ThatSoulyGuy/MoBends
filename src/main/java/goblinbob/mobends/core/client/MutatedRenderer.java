package goblinbob.mobends.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.util.GlHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public abstract class MutatedRenderer<T extends LivingEntity>
{

    protected final float scale = 0.0625F;
    protected final TextureManager textureManager;

    public MutatedRenderer()
    {
        textureManager = Minecraft.getInstance().getTextureManager();
    }

    /**
     * Called right before the entity is rendered
     */
    public void beforeRender(EntityData<T> data, T entity, float partialTicks, PoseStack poseStack)
    {
        double entityX = Mth.lerp(partialTicks, entity.xOld, entity.getX());
        double entityY = Mth.lerp(partialTicks, entity.yOld, entity.getY());
        double entityZ = Mth.lerp(partialTicks, entity.zOld, entity.getZ());

        Entity viewEntity = Minecraft.getInstance().getCameraEntity();
        double viewX = entityX, viewY = entityY, viewZ = entityZ;
        if (viewEntity != null)
        {
            // Checking in case of Main Menu or GUI rendering.
            viewX = Mth.lerp(partialTicks, viewEntity.xOld, viewEntity.getX());
            viewY = Mth.lerp(partialTicks, viewEntity.yOld, viewEntity.getY());
            viewZ = Mth.lerp(partialTicks, viewEntity.zOld, viewEntity.getZ());
        }
        poseStack.translate(entityX - viewX, entityY - viewY, entityZ - viewZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(-interpolateRotation(entity.yBodyRotO, entity.yBodyRot, partialTicks)));

        this.renderLocalAccessories(entity, data, partialTicks, poseStack);

        float globalScale = entity.isBaby() ? getChildScale() : 1;

        // Scale down the model for baby entities (vanilla uses 0.5x scale for babies)
        if (globalScale != 1.0f)
        {
            poseStack.scale(globalScale, globalScale, globalScale);
        }

        poseStack.translate(data.globalOffset.getX() * scale * globalScale,
                data.globalOffset.getY() * scale * globalScale,
                data.globalOffset.getZ() * scale * globalScale);
        poseStack.translate(0, entity.getBbHeight() / 2, 0);
        GlHelper.rotate(poseStack, data.centerRotation.getSmooth());
        poseStack.translate(0, -entity.getBbHeight() / 2, 0);
        GlHelper.rotate(poseStack, data.renderRotation.getSmooth());

        poseStack.translate(data.localOffset.getX() * scale * globalScale,
                data.localOffset.getY() * scale * globalScale,
                data.localOffset.getZ() * scale * globalScale);

        this.transformLocally(entity, data, partialTicks, poseStack);

        poseStack.mulPose(Axis.YP.rotationDegrees(interpolateRotation(entity.yBodyRotO, entity.yBodyRot, partialTicks)));
        poseStack.translate(viewX - entityX, viewY - entityY, viewZ - entityZ);
    }

    /**
     * Called right after the entity is rendered.
     */
    public void afterRender(T entity, float partialTicks, PoseStack poseStack)
    {
        // No default behaviour
    }

    /**
     * Used to render accessories for that entity, e.g. Sword trails. Also used to transform the entity, like offset or
     * rotate it.
     */
    protected void renderLocalAccessories(T entity, EntityData<?> data, float partialTicks, PoseStack poseStack)
    {
        // No default behaviour
    }

    protected void transformLocally(T entity, EntityData<?> data, float partialTicks, PoseStack poseStack)
    {
        // No default behaviour
    }

    protected static float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks)
    {
        float f;
        for (f = yawOffset - prevYawOffset; f < -180.0F; f += 360.0F) ;

        while (f >= 180.0F)
            f -= 360.0F;

        return prevYawOffset + partialTicks * f;
    }

    protected float getChildScale()
    {
        return 0.5F;
    }

}
