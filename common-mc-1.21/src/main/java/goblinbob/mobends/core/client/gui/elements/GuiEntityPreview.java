package goblinbob.mobends.core.client.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.IPreviewer;
import goblinbob.mobends.core.bender.PreviewHelper;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.joml.Quaternionf;

import javax.annotation.Nullable;

/**
 * A widget that renders a 3D entity preview that can be rotated by dragging.
 * Supports showing entities with their MoBends animations applied.
 */
public class GuiEntityPreview
{
    private static final int BACKGROUND_COLOR = 0x80000000;
    private static final int BORDER_COLOR = 0xFF333333;
    private static final float DEFAULT_SCALE = 40.0f;
    private static final float MIN_SCALE = 20.0f;
    private static final float MAX_SCALE = 80.0f;

    /**
     * Animation modes that can be displayed in the preview.
     */
    public enum AnimationMode
    {
        IDLE,
        WALK,
        SPRINT,
        JUMP,
        FALL,
        SNEAK,
        SWIM,
        ATTACK,
        USE_ITEM,
        RIDE,
        CLIMB,
        SIT
    }

    private int x;
    private int y;
    private int width;
    private int height;
    private float rotationX; // Pitch (vertical rotation)
    private float rotationY; // Yaw (horizontal rotation)
    private float scale;
    private boolean isDragging;
    private int lastMouseX;
    private int lastMouseY;

    // Animation state
    private AnimationMode animationMode = AnimationMode.IDLE;
    private String currentAnimationType = "idle";
    private float animationTicks = 0;
    private float limbSwing = 0;
    private float limbSwingAmount = 0;

    @Nullable
    private LivingEntity previewEntity;
    @Nullable
    private EntityBender<?> currentBender;
    @Nullable
    private LivingEntityData<?> previewEntityData;

    public GuiEntityPreview(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.rotationX = -10;
        this.rotationY = 45;
        this.scale = DEFAULT_SCALE;
        this.isDragging = false;
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void setSize(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    /**
     * Sets the entity bender to preview.
     * This will create a preview entity for the bender.
     */
    public void setBender(@Nullable EntityBender<?> bender)
    {
        // Clean up old preview entity
        if (previewEntity != null)
        {
            previewEntity = null;
            previewEntityData = null;
        }

        this.currentBender = bender;

        if (bender != null)
        {
            initializePreviewForBender(bender);
        }

        // Reset rotation for new entity
        this.rotationX = -10;
        this.rotationY = 45;
        this.scale = DEFAULT_SCALE;
        this.animationTicks = 0;
        this.limbSwing = 0;
        this.limbSwingAmount = 0;
        this.currentAnimationType = "idle";
        this.animationMode = AnimationMode.IDLE;
    }

    @SuppressWarnings("unchecked")
    private <T extends LivingEntity> void initializePreviewForBender(EntityBender<T> bender)
    {
        try
        {
            // Create preview entity using the bender's factory
            T entity = createPreviewEntityForBender(bender);
            this.previewEntity = entity;

            // Get or create EntityData for the preview entity
            if (entity != null && bender.getDataFactory() != null)
            {
                this.previewEntityData = EntityDatabase.instance.getOrMake(
                    bender.getDataFactory(), entity);
            }
        }
        catch (Exception e)
        {
            // Failed to create entity - leave as null
            this.previewEntity = null;
            this.previewEntityData = null;
        }
    }

    /**
     * Sets the animation mode to display on the preview entity.
     */
    public void setAnimationMode(AnimationMode mode)
    {
        if (this.animationMode != mode)
        {
            this.animationMode = mode;
            this.animationTicks = 0;
            this.limbSwing = 0;
            this.limbSwingAmount = 0;
        }
    }

    /**
     * Sets the animation mode by name (e.g., "walk", "sprint").
     */
    public void setAnimationModeByName(String name)
    {
        this.currentAnimationType = name.toLowerCase();

        AnimationMode mode = switch (name.toLowerCase())
        {
            case "walk" -> AnimationMode.WALK;
            case "sprint" -> AnimationMode.SPRINT;
            case "jump" -> AnimationMode.JUMP;
            case "fall" -> AnimationMode.FALL;
            case "sneak" -> AnimationMode.SNEAK;
            case "swim" -> AnimationMode.SWIM;
            case "attack" -> AnimationMode.ATTACK;
            case "use_item" -> AnimationMode.USE_ITEM;
            case "ride" -> AnimationMode.RIDE;
            case "climb" -> AnimationMode.CLIMB;
            case "sit" -> AnimationMode.SIT;
            default -> AnimationMode.IDLE;
        };
        setAnimationMode(mode);
    }

    public AnimationMode getAnimationMode()
    {
        return animationMode;
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
            // Use reflection to call the protected createPreviewEntity method
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

    public void update(int mouseX, int mouseY)
    {
        if (isDragging)
        {
            int deltaX = mouseX - lastMouseX;
            int deltaY = mouseY - lastMouseY;

            rotationY += deltaX * 0.5f;
            rotationX += deltaY * 0.5f;

            // Clamp vertical rotation
            rotationX = Math.max(-80, Math.min(80, rotationX));

            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        // Update animation state
        updateAnimationState();
    }

    /**
     * Updates the animation state each tick to simulate movement.
     */
    private void updateAnimationState()
    {
        if (previewEntity == null) return;

        animationTicks += 1.0f;

        // Update entity state based on animation mode
        switch (animationMode)
        {
            case WALK:
                limbSwing += 0.5f;
                limbSwingAmount = 0.5f;
                previewEntity.setOnGround(true);
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                break;

            case SPRINT:
                limbSwing += 0.8f;
                limbSwingAmount = 1.0f;
                previewEntity.setOnGround(true);
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                break;

            case JUMP:
                // Simulate periodic jumping
                float jumpPhase = (animationTicks % 40) / 40.0f;
                previewEntity.setOnGround(jumpPhase > 0.5f);
                limbSwing += jumpPhase < 0.5f ? 0.3f : 0.5f;
                limbSwingAmount = jumpPhase < 0.5f ? 0.2f : 0.5f;
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                break;

            case FALL:
                previewEntity.setOnGround(false);
                limbSwing = 0;
                limbSwingAmount = 0;
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                break;

            case SNEAK:
                limbSwing += 0.3f;
                limbSwingAmount = 0.3f;
                previewEntity.setOnGround(true);
                previewEntity.setPose(Pose.CROUCHING);
                previewEntity.setSwimming(false);
                break;

            case SWIM:
                limbSwing += 0.4f;
                limbSwingAmount = 0.6f;
                previewEntity.setOnGround(false);
                previewEntity.setPose(Pose.SWIMMING);
                previewEntity.setSwimming(true);
                break;

            case ATTACK:
                // Simulate periodic attack swinging
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

            case USE_ITEM:
                limbSwing += 0.2f;
                limbSwingAmount = 0.2f;
                previewEntity.setOnGround(true);
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                break;

            case RIDE:
                limbSwing = 0;
                limbSwingAmount = 0;
                previewEntity.setOnGround(false);
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                break;

            case CLIMB:
                limbSwing += 0.4f;
                limbSwingAmount = 0.5f;
                previewEntity.setOnGround(false);
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                // Mark as on climbable
                previewEntity.horizontalCollision = true;
                break;

            case SIT:
                limbSwing = 0;
                limbSwingAmount = 0;
                previewEntity.setOnGround(true);
                previewEntity.setPose(Pose.SITTING);
                previewEntity.setSwimming(false);
                break;

            case IDLE:
            default:
                limbSwing = 0;
                limbSwingAmount = 0;
                previewEntity.setOnGround(true);
                previewEntity.setPose(Pose.STANDING);
                previewEntity.setSwimming(false);
                previewEntity.swinging = false;
                break;
        }

        // Apply limb swing values to entity
        previewEntity.walkAnimation.update(limbSwingAmount, 0.4f);
    }

    public void draw(GuiGraphics guiGraphics, float partialTicks)
    {
        // Draw background
        Draw.rectangle(x, y, x + width, y + height, BACKGROUND_COLOR);

        // Draw border
        Draw.rectangle(x, y, x + width, y + 1, BORDER_COLOR);
        Draw.rectangle(x, y + height - 1, x + width, y + height, BORDER_COLOR);
        Draw.rectangle(x, y, x + 1, y + height, BORDER_COLOR);
        Draw.rectangle(x + width - 1, y, x + width, y + height, BORDER_COLOR);

        if (previewEntity == null)
        {
            // Draw placeholder text
            Minecraft mc = Minecraft.getInstance();
            String text = currentBender != null ? "Preview unavailable" : "Select an entity";
            int textWidth = mc.font.width(text);
            guiGraphics.drawString(mc.font, text, x + (width - textWidth) / 2, y + height / 2 - 4, 0x888888, false);
            return;
        }

        // Render the entity
        renderEntity(guiGraphics, partialTicks);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void renderEntity(GuiGraphics guiGraphics, float partialTicks)
    {
        if (previewEntity == null) return;

        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

        float centerX = x + width / 2.0f;
        float centerY = y + height - 20;

        // Calculate entity height to center it properly
        float entityHeight = previewEntity.getBbHeight();

        // Use the previewer to set up animation state on EntityData
        IPreviewer previewer = currentBender != null ? currentBender.getPreviewer() : null;
        if (previewer != null && previewEntityData != null)
        {
            try
            {
                // Update entity data before preview
                previewEntityData.updateClient();
                previewEntityData.update(partialTicks);

                // Set up animation preview state
                previewer.prePreview((EntityData) previewEntityData, currentAnimationType);
            }
            catch (Exception e)
            {
                // Failed to set up preview - continue with fallback
            }
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 50.0f);
        // Use negative Y scale to flip entity right-side up in GUI space
        guiGraphics.pose().scale(scale, -scale, scale);

        // Apply rotation
        Quaternionf rotation = new Quaternionf()
            .rotateX((float) Math.toRadians(rotationX))
            .rotateY((float) Math.toRadians(rotationY));
        guiGraphics.pose().mulPose(rotation);

        // Center the entity vertically
        guiGraphics.pose().translate(0, -entityHeight / 2.0f, 0);

        // Store original entity state
        float prevBodyRot = previewEntity.yBodyRot;
        float prevYRot = previewEntity.getYRot();
        float prevXRot = previewEntity.getXRot();
        float prevHeadRot = previewEntity.yHeadRotO;
        float prevHeadRot2 = previewEntity.yHeadRot;
        int prevTickCount = previewEntity.tickCount;

        // Set entity state for rendering
        previewEntity.yBodyRot = 0;
        previewEntity.setYRot(0);
        previewEntity.setXRot(0);
        previewEntity.yHeadRotO = 0;
        previewEntity.yHeadRot = 0;
        previewEntity.tickCount = (int) animationTicks;

        RenderSystem.setShaderLights(
            new org.joml.Vector3f(-0.2f, 1.0f, -1.0f).normalize(),
            new org.joml.Vector3f(0.2f, 1.0f, 1.0f).normalize()
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
            // Rendering failed - ignore
        }

        dispatcher.setRenderShadow(true);

        // Call postPreview to clean up
        if (previewer != null && previewEntityData != null)
        {
            try
            {
                previewer.postPreview((EntityData) previewEntityData, currentAnimationType);
            }
            catch (Exception e)
            {
                // Failed to clean up - ignore
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

    public boolean handleMouseClicked(int mouseX, int mouseY, int button)
    {
        if (isInBounds(mouseX, mouseY) && button == 0)
        {
            isDragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return false;
    }

    public boolean handleMouseReleased(int mouseX, int mouseY, int button)
    {
        if (button == 0)
        {
            isDragging = false;
            return true;
        }
        return false;
    }

    public boolean handleMouseScroll(double mouseX, double mouseY, double scrollDelta)
    {
        if (isInBounds((int) mouseX, (int) mouseY))
        {
            scale += (float) scrollDelta * 3.0f;
            scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
            return true;
        }
        return false;
    }

    private boolean isInBounds(int mouseX, int mouseY)
    {
        return mouseX >= x && mouseX < x + width &&
               mouseY >= y && mouseY < y + height;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    @Nullable
    public LivingEntity getPreviewEntity()
    {
        return previewEntity;
    }

    public void resetRotation()
    {
        this.rotationX = -10;
        this.rotationY = 45;
        this.scale = DEFAULT_SCALE;
    }
}
