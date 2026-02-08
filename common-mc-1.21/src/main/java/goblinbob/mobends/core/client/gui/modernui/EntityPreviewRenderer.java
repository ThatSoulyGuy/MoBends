package goblinbob.mobends.core.client.gui.modernui;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.api.gui.modernui.IEntityRenderer;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.IPreviewer;
import goblinbob.mobends.core.bender.PreviewHelper;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/**
 * Renders entity previews for use in Modern UI screens.
 * This bridges Modern UI's layout system with Minecraft's entity rendering.
 */
public class EntityPreviewRenderer implements IEntityRenderer
{
    private static final float DEFAULT_SCALE = 40.0f;
    private static final float MIN_SCALE = 20.0f;
    private static final float MAX_SCALE = 80.0f;

    // View state
    private float rotationX = -10;
    private float rotationY = 45;
    private float scale = DEFAULT_SCALE;

    // Animation state
    private String currentAnimationType = "idle";
    private float animationTicks = 0;
    private float limbSwing = 0;
    private float limbSwingAmount = 0;

    // Entity state
    @Nullable
    private LivingEntity previewEntity;
    @Nullable
    private EntityBender<?> currentBender;
    @Nullable
    private LivingEntityData<?> previewEntityData;

    @Override
    public void setBender(@Nullable Object bender)
    {
        // Clean up old preview entity
        if (previewEntity != null)
        {
            previewEntity = null;
            previewEntityData = null;
        }

        if (bender instanceof EntityBender<?> entityBender)
        {
            this.currentBender = entityBender;
            initializePreviewForBender(entityBender);
        }
        else
        {
            this.currentBender = null;
        }

        // Reset view for new entity
        resetView();
    }

    /**
     * Sets the bender with type safety.
     * Convenience method that accepts EntityBender directly.
     *
     * @param bender The bender to preview, or null to clear
     */
    public void setBenderTyped(@Nullable EntityBender<?> bender)
    {
        setBender(bender);
    }

    @SuppressWarnings("unchecked")
    private <T extends LivingEntity> void initializePreviewForBender(EntityBender<T> bender)
    {
        try
        {
            T entity = createPreviewEntityForBender(bender);
            this.previewEntity = entity;

            if (entity != null && bender.getDataFactory() != null)
            {
                this.previewEntityData = EntityDatabase.instance.getOrMake(
                    bender.getDataFactory(), entity);
            }
        }
        catch (Exception e)
        {
            this.previewEntity = null;
            this.previewEntityData = null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T extends LivingEntity> T createPreviewEntityForBender(EntityBender<T> bender)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
        {
            return null;
        }

        try
        {
            java.lang.reflect.Method method = EntityBender.class.getDeclaredMethod("createPreviewEntity");
            method.setAccessible(true);
            T entity = (T) method.invoke(bender);

            if (entity != null)
            {
                PreviewHelper.registerPreviewEntity(entity);
            }

            return entity;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public void setAnimationType(String animationType)
    {
        if (!this.currentAnimationType.equals(animationType.toLowerCase()))
        {
            this.currentAnimationType = animationType.toLowerCase();
            this.animationTicks = 0;
            this.limbSwing = 0;
            this.limbSwingAmount = 0;
        }
    }

    @Override
    public void setRotation(float rotationX, float rotationY)
    {
        this.rotationX = Math.max(-80, Math.min(80, rotationX));
        this.rotationY = rotationY;
    }

    @Override
    public void setScale(float scale)
    {
        this.scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
    }

    @Override
    public float getRotationX()
    {
        return rotationX;
    }

    @Override
    public float getRotationY()
    {
        return rotationY;
    }

    @Override
    public float getScale()
    {
        return scale;
    }

    @Override
    public void update()
    {
        if (previewEntity == null) return;

        animationTicks += 1.0f;
        updateAnimationState();
    }

    private void updateAnimationState()
    {
        if (previewEntity == null) return;

        switch (currentAnimationType)
        {
            case "walk":
                limbSwing += 0.5f;
                limbSwingAmount = 0.5f;
                previewEntity.setOnGround(true);
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                break;

            case "sprint":
                limbSwing += 0.8f;
                limbSwingAmount = 1.0f;
                previewEntity.setOnGround(true);
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                break;

            case "jump":
                float jumpPhase = (animationTicks % 40) / 40.0f;
                previewEntity.setOnGround(jumpPhase > 0.5f);
                limbSwing += jumpPhase < 0.5f ? 0.3f : 0.5f;
                limbSwingAmount = jumpPhase < 0.5f ? 0.2f : 0.5f;
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                break;

            case "fall":
                previewEntity.setOnGround(false);
                limbSwing = 0;
                limbSwingAmount = 0;
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                break;

            case "sneak":
                limbSwing += 0.3f;
                limbSwingAmount = 0.3f;
                previewEntity.setOnGround(true);
                previewEntity.setPose(Pose.CROUCHING);
                previewEntity.setSwimming(false);
                break;

            case "swim":
                limbSwing += 0.4f;
                limbSwingAmount = 0.6f;
                previewEntity.setOnGround(false);
                previewEntity.setPose(Pose.SWIMMING);
                previewEntity.setSwimming(true);
                break;

            case "attack":
                float attackPhase = (animationTicks % 20) / 20.0f;
                if (attackPhase < 0.3f)
                {
                    previewEntity.swingTime = (int) (attackPhase / 0.3f * 6);
                    previewEntity.swinging = true;
                }
                else
                {
                    previewEntity.swinging = false;
                }
                limbSwing += 0.3f;
                limbSwingAmount = 0.3f;
                previewEntity.setOnGround(true);
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                break;

            case "ride":
                limbSwing = 0;
                limbSwingAmount = 0;
                previewEntity.setOnGround(false);
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                break;

            case "climb":
                limbSwing += 0.4f;
                limbSwingAmount = 0.5f;
                previewEntity.setOnGround(false);
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                previewEntity.horizontalCollision = true;
                break;

            case "sit":
                limbSwing = 0;
                limbSwingAmount = 0;
                previewEntity.setOnGround(true);
                previewEntity.setPose(Pose.SITTING);
                previewEntity.setSwimming(false);
                break;

            default: // idle
                limbSwing = 0;
                limbSwingAmount = 0;
                previewEntity.setOnGround(true);
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                previewEntity.swinging = false;
                break;
        }

        previewEntity.walkAnimation.update(limbSwingAmount, 0.4f);
    }

    @Override
    public void render(int x, int y, int width, int height, float partialTicks)
    {
        if (previewEntity == null) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics guiGraphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());

        renderEntity(guiGraphics, x, y, width, height, partialTicks);
    }

    /**
     * Renders the entity using the provided GuiGraphics.
     * This is the preferred method when a GuiGraphics is already available.
     */
    public void render(GuiGraphics guiGraphics, int x, int y, int width, int height, float partialTicks)
    {
        if (previewEntity == null) return;
        renderEntity(guiGraphics, x, y, width, height, partialTicks);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void renderEntity(GuiGraphics guiGraphics, int x, int y, int width, int height, float partialTicks)
    {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

        float centerX = x + width / 2.0f;
        float centerY = y + height - 20;
        float entityHeight = previewEntity.getBbHeight();

        IPreviewer previewer = currentBender != null ? currentBender.getPreviewer() : null;
        if (previewer != null && previewEntityData != null)
        {
            try
            {
                previewEntityData.updateClient();
                previewEntityData.update(partialTicks);
                previewer.prePreview((EntityData) previewEntityData, currentAnimationType);
            }
            catch (Exception e)
            {
                // Continue with fallback
            }
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 50.0f);
        guiGraphics.pose().scale(scale, -scale, scale);

        Quaternionf rotation = new Quaternionf()
            .rotateX((float) Math.toRadians(rotationX))
            .rotateY((float) Math.toRadians(rotationY));
        guiGraphics.pose().mulPose(rotation);

        guiGraphics.pose().translate(0, -entityHeight / 2.0f, 0);

        // Store original entity state
        float prevBodyRot = previewEntity.yBodyRot;
        float prevYRot = previewEntity.getYRot();
        float prevXRot = previewEntity.getXRot();
        float prevHeadRot = previewEntity.yHeadRotO;
        float prevHeadRot2 = previewEntity.yHeadRot;
        int prevTickCount = previewEntity.tickCount;

        previewEntity.yBodyRot = 0;
        previewEntity.setYRot(0);
        previewEntity.setXRot(0);
        previewEntity.yHeadRotO = 0;
        previewEntity.yHeadRot = 0;
        previewEntity.tickCount = (int) animationTicks;

        RenderSystem.setShaderLights(
            new Vector3f(-0.2f, 1.0f, -1.0f).normalize(),
            new Vector3f(0.2f, 1.0f, 1.0f).normalize()
        );

        dispatcher.setRenderShadow(false);

        try
        {
            dispatcher.render(
                previewEntity,
                0, 0, 0,
                0,
                partialTicks,
                guiGraphics.pose(),
                guiGraphics.bufferSource(),
                LightTexture.FULL_BRIGHT
            );
            guiGraphics.flush();
        }
        catch (Exception e)
        {
            // Rendering failed
        }

        dispatcher.setRenderShadow(true);

        if (previewer != null && previewEntityData != null)
        {
            try
            {
                previewer.postPreview((EntityData) previewEntityData, currentAnimationType);
            }
            catch (Exception e)
            {
                // Cleanup failed
            }
        }

        // Restore entity state
        previewEntity.yBodyRot = prevBodyRot;
        previewEntity.setYRot(prevYRot);
        previewEntity.setXRot(prevXRot);
        previewEntity.yHeadRotO = prevHeadRot;
        previewEntity.yHeadRot = prevHeadRot2;
        previewEntity.tickCount = prevTickCount;

        guiGraphics.pose().popPose();
    }

    @Override
    public void resetView()
    {
        this.rotationX = -10;
        this.rotationY = 45;
        this.scale = DEFAULT_SCALE;
        this.animationTicks = 0;
        this.limbSwing = 0;
        this.limbSwingAmount = 0;
        this.currentAnimationType = "idle";
    }

    @Override
    public boolean hasEntity()
    {
        return previewEntity != null;
    }

    /**
     * @return The current bender being previewed
     */
    @Nullable
    public EntityBender<?> getCurrentBender()
    {
        return currentBender;
    }
}
