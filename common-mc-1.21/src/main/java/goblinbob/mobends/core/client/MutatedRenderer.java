package goblinbob.mobends.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.util.GlHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
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
        // NOTE: The poseStack is already translated to the entity's position by
        // EntityRenderDispatcher. Do NOT add an extra entity-to-view translation here,
        // as that creates a double-offset that causes remote players to visually teleport
        // during rotational animations (attack spins, etc.).

        // Remove body rotation so we can apply animations in entity-local space
        poseStack.mulPose(Axis.YP.rotationDegrees(-interpolateRotation(entity.yBodyRotO, entity.yBodyRot, partialTicks)));

        this.renderLocalAccessories(entity, data, partialTicks, poseStack);

        float globalScale = entity.isBaby() ? getChildScale() : 1;

        // Scale down the model for baby entities (vanilla uses 0.5x scale for babies)
        if (globalScale != 1.0f)
        {
            poseStack.scale(globalScale, globalScale, globalScale);
        }

        poseStack.translate(data.globalOffset.getX() * scale,
                data.globalOffset.getY() * scale,
                data.globalOffset.getZ() * scale);
        // Compensate center rotation pivot for baby scaling (poseStack is already scaled)
        float centerY = entity.getBbHeight() / (2.0f * globalScale);
        poseStack.translate(0, centerY, 0);
        GlHelper.rotate(poseStack, data.centerRotation.getSmooth());
        poseStack.translate(0, -centerY, 0);
        GlHelper.rotate(poseStack, data.renderRotation.getSmooth());

        poseStack.translate(data.localOffset.getX() * scale,
                data.localOffset.getY() * scale,
                data.localOffset.getZ() * scale);

        this.transformLocally(entity, data, partialTicks, poseStack);

        // Re-add body rotation
        poseStack.mulPose(Axis.YP.rotationDegrees(interpolateRotation(entity.yBodyRotO, entity.yBodyRot, partialTicks)));
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
