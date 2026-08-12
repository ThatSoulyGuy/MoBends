package goblinbob.mobends.core.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.IPreviewer;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class EntityPreviewRenderer
{
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends/Preview");
    private static final float DEFAULT_SCALE = 40.0f;
    private static final float MIN_SCALE = 20.0f;
    private static final float MAX_SCALE = 80.0f;

    private float rotationX = -10;
    private float rotationY = 45;
    private float scale = DEFAULT_SCALE;

    private String currentAnimationType = "idle";
    private float animationTicks = 0;
    private float limbSwing = 0;
    private float limbSwingAmount = 0;
    private long lastUpdateNanos = -1;

    @Nullable
    private LivingEntity previewEntity;
    @Nullable
    private EntityBender<?> currentBender;
    @Nullable
    private LivingEntityData<?> previewEntityData;

    public void setBender(@Nullable Object bender)
    {
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

        resetView();
    }

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
            LOGGER.info("Entity creation for '{}': {}", bender.getLocalizedName(),
                    entity != null ? entity.getClass().getSimpleName() : "FAILED (null)");

            if (entity != null && bender.getDataFactory() != null)
            {
                this.previewEntityData = EntityDatabase.instance.getOrMake(
                    bender.getDataFactory(), entity);
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to initialize preview for '{}'", bender.getLocalizedName(), e);
            this.previewEntity = null;
            this.previewEntityData = null;
        }
    }

    @Nullable
    private <T extends LivingEntity> T createPreviewEntityForBender(EntityBender<T> bender)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
        {
            LOGGER.warn("Cannot create preview entity: mc.level is null");
            return null;
        }

        try
        {
            return bender.createPreviewEntity();
        }
        catch (Exception e)
        {
            LOGGER.error("createPreviewEntity threw for '{}'", bender.getLocalizedName(), e);
            return null;
        }
    }

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

    public void setRotation(float rotationX, float rotationY)
    {
        this.rotationX = Math.max(-80, Math.min(80, rotationX));
        this.rotationY = rotationY;
    }

    public void setScale(float scale)
    {
        this.scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
    }

    public float getRotationX()
    {
        return rotationX;
    }

    public float getRotationY()
    {
        return rotationY;
    }

    public float getScale()
    {
        return scale;
    }

    public void update()
    {
        if (previewEntity == null) return;

        long now = System.nanoTime();
        if (lastUpdateNanos < 0)
        {
            lastUpdateNanos = now;
        }
        float deltaSec = (now - lastUpdateNanos) / 1_000_000_000.0f;
        lastUpdateNanos = now;

        deltaSec = Math.min(deltaSec, 0.1f);

        animationTicks += deltaSec * 20.0f;
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

            default:
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

    public void render(int x, int y, int width, int height, float partialTicks)
    {
        if (previewEntity == null) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics guiGraphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());

        renderEntity(guiGraphics, x, y, width, height, partialTicks);
    }

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
        float centerY = y + height / 2.0f;
        float entityHeight = previewEntity.getBbHeight();
        float entityScale = previewEntity.getScale();

        IPreviewer previewer = currentBender != null ? currentBender.getPreviewer() : null;

        float savedTicksPerFrame = DataUpdateHandler.ticksPerFrame;
        DataUpdateHandler.setPreviewTicks(animationTicks);
        DataUpdateHandler.ticksPerFrame = 1.0f;

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
            }
        }

        float prevBodyRot = previewEntity.yBodyRot;
        float prevYRot = previewEntity.getYRot();
        float prevXRot = previewEntity.getXRot();
        float prevHeadRot = previewEntity.yHeadRotO;
        float prevHeadRot2 = previewEntity.yHeadRot;
        int prevTickCount = previewEntity.tickCount;

        previewEntity.yBodyRot = 180.0f;
        previewEntity.setYRot(180.0f);
        previewEntity.setXRot(0);
        previewEntity.yHeadRotO = previewEntity.getYRot();
        previewEntity.yHeadRot = previewEntity.getYRot();
        previewEntity.tickCount = (int) animationTicks;

        boolean prevDepthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean prevScissorTest = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        int prevDepthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        boolean prevDepthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);

        Matrix4f savedProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        Matrix4f mcProjection = new Matrix4f().setOrtho(
            0.0f, (float) mc.getWindow().getGuiScaledWidth(),
            (float) mc.getWindow().getGuiScaledHeight(), 0.0f,
            1000.0f, 21000.0f
        );
        RenderSystem.setProjectionMatrix(mcProjection, VertexSorting.ORTHOGRAPHIC_Z);

        GL11.glViewport(0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight());

        double guiScale = mc.getWindow().getGuiScale();
        int windowHeight = mc.getWindow().getHeight();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
            (int) (x * guiScale),
            windowHeight - (int) ((y + height) * guiScale),
            (int) (width * guiScale),
            (int) (height * guiScale)
        );

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 50.0);

        float adjustedScale = scale / entityScale;
        guiGraphics.pose().scale(adjustedScale, adjustedScale, -adjustedScale);

        Vector3f translation = new Vector3f(0, entityHeight / 2.0f, 0);
        guiGraphics.pose().translate(translation.x(), translation.y(), translation.z());

        Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf cameraRot = new Quaternionf()
            .rotateX((float) Math.toRadians(rotationX));
        pose.mul(cameraRot);

        Quaternionf yRotation = new Quaternionf().rotateY((float) Math.toRadians(rotationY));
        pose.mul(yRotation);

        guiGraphics.pose().mulPose(pose);

        Lighting.setupForEntityInInventory();

        Quaternionf cameraOrientation = cameraRot.conjugate(new Quaternionf()).rotateY((float) Math.PI);
        dispatcher.overrideCameraOrientation(cameraOrientation);
        dispatcher.setRenderShadow(false);

        try
        {
            RenderSystem.runAsFancy(() -> {
                dispatcher.render(
                    previewEntity,
                    0, 0, 0,
                    0,
                    1.0f,
                    guiGraphics.pose(),
                    guiGraphics.bufferSource(),
                    LightTexture.FULL_BRIGHT
                );
            });
            guiGraphics.flush();
        }
        catch (Exception e)
        {
            LOGGER.error("Entity render failed", e);
        }

        dispatcher.setRenderShadow(true);
        guiGraphics.pose().popPose();

        Lighting.setupFor3DItems();

        RenderSystem.setProjectionMatrix(savedProjection, VertexSorting.ORTHOGRAPHIC_Z);
        if (!prevScissorTest) GL11.glDisable(GL11.GL_SCISSOR_TEST);
        if (!prevDepthTest) GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(prevDepthFunc);
        GL11.glDepthMask(prevDepthMask);

        if (previewer != null && previewEntityData != null)
        {
            try
            {
                previewer.postPreview((EntityData) previewEntityData, currentAnimationType);
            }
            catch (Exception e)
            {
            }
        }

        DataUpdateHandler.clearPreviewTicks();
        DataUpdateHandler.ticksPerFrame = savedTicksPerFrame;

        previewEntity.yBodyRot = prevBodyRot;
        previewEntity.setYRot(prevYRot);
        previewEntity.setXRot(prevXRot);
        previewEntity.yHeadRotO = prevHeadRot;
        previewEntity.yHeadRot = prevHeadRot2;
        previewEntity.tickCount = prevTickCount;
    }

    public void resetView()
    {
        this.rotationX = -10;
        this.rotationY = 45;
        this.scale = DEFAULT_SCALE;
        this.animationTicks = 0;
        this.limbSwing = 0;
        this.limbSwingAmount = 0;
        this.currentAnimationType = "idle";
        this.lastUpdateNanos = -1;
    }

    public boolean hasEntity()
    {
        return previewEntity != null;
    }

    @Nullable
    public EntityBender<?> getCurrentBender()
    {
        return currentBender;
    }
}
