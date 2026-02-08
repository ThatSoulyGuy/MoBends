package goblinbob.mobends.core.client.gui.modernui.widget;

import goblinbob.mobends.api.gui.modernui.ILayoutParams;
import goblinbob.mobends.api.gui.modernui.IViewFactory;
import goblinbob.mobends.api.gui.modernui.view.IMuiFrameLayout;
import goblinbob.mobends.api.gui.modernui.view.IMuiLinearLayout;
import goblinbob.mobends.api.gui.modernui.view.IMuiTextView;
import goblinbob.mobends.api.gui.modernui.view.IMuiView;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.client.gui.modernui.EntityPreviewRenderer;
import goblinbob.mobends.core.client.gui.modernui.theme.MoBendsTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import javax.annotation.Nullable;

/**
 * Modern UI entity preview widget.
 * Provides a container for entity preview with controls overlay.
 * Uses EntityPreviewRenderer for actual Minecraft entity rendering.
 */
public class MuiEntityPreview
{
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

    private final IViewFactory factory;
    private final IMuiFrameLayout rootLayout;
    private final IMuiTextView titleView;
    private final IMuiTextView hintView;
    private final IMuiTextView statusView;
    private final EntityPreviewRenderer renderer;

    @Nullable
    private EntityBender<?> currentBender;
    private AnimationMode animationMode = AnimationMode.IDLE;
    private String currentAnimationType = "idle";

    // Drag state
    private boolean isDragging = false;
    private int lastMouseX;
    private int lastMouseY;

    /**
     * Creates a new entity preview widget.
     *
     * @param factory The view factory
     * @param width   The width in dp
     * @param height  The height in dp
     */
    public MuiEntityPreview(IViewFactory factory, int width, int height)
    {
        this.factory = factory;
        this.renderer = new EntityPreviewRenderer();

        // Create root frame layout (allows stacking views)
        this.rootLayout = factory.createFrameLayout();
        this.rootLayout.setLayoutParams(factory.createLayoutParams(width, height));
        this.rootLayout.setBackgroundColor(MoBendsTheme.BG_CONTENT);

        // Create vertical layout for content
        IMuiLinearLayout contentLayout = factory.createLinearLayout(IViewFactory.VERTICAL);
        contentLayout.setLayoutParams(factory.createMatchParent());
        contentLayout.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                                MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        // Title at top
        this.titleView = factory.createTextView(I18n.get("mobends.gui.preview"));
        this.titleView.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        this.titleView.setTextSize(14);
        this.titleView.setBold(true);
        this.titleView.setGravity(IMuiLinearLayout.GRAVITY_CENTER_HORIZONTAL);
        contentLayout.addView(titleView, factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.WRAP_CONTENT
        ));

        // Status/placeholder text in center
        this.statusView = factory.createTextView(I18n.get("mobends.gui.preview.select"));
        this.statusView.setTextColor(MoBendsTheme.TEXT_HINT);
        this.statusView.setTextSize(12);
        this.statusView.setGravity(IMuiLinearLayout.GRAVITY_CENTER);
        ILayoutParams statusParams = factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.MATCH_PARENT
        );
        statusParams.setMargins(0, MoBendsTheme.SPACING, 0, MoBendsTheme.SPACING);
        contentLayout.addView(statusView, statusParams);

        // Hint at bottom
        this.hintView = factory.createTextView(I18n.get("mobends.gui.preview.hint"));
        this.hintView.setTextColor(MoBendsTheme.TEXT_HINT);
        this.hintView.setTextSize(10);
        this.hintView.setGravity(IMuiLinearLayout.GRAVITY_CENTER_HORIZONTAL);
        contentLayout.addView(hintView, factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.WRAP_CONTENT
        ));

        rootLayout.addView(contentLayout, factory.createMatchParent());
    }

    /**
     * Sets the entity bender to preview.
     *
     * @param bender The bender, or null to clear
     */
    public void setBender(@Nullable EntityBender<?> bender)
    {
        this.currentBender = bender;
        this.renderer.setBenderTyped(bender);

        if (bender != null)
        {
            titleView.setText(bender.getLocalizedName());
            statusView.setText(""); // Clear placeholder when entity is selected
            statusView.setVisibility(IMuiView.GONE);
            hintView.setVisibility(IMuiView.VISIBLE);
        }
        else
        {
            titleView.setText(I18n.get("mobends.gui.preview"));
            statusView.setText(I18n.get("mobends.gui.preview.select"));
            statusView.setVisibility(IMuiView.VISIBLE);
            hintView.setVisibility(IMuiView.GONE);
        }

        // Reset view state
        resetView();
    }

    /**
     * @return The current bender being previewed
     */
    @Nullable
    public EntityBender<?> getBender()
    {
        return currentBender;
    }

    /**
     * Sets the animation mode to display.
     *
     * @param mode The animation mode
     */
    public void setAnimationMode(AnimationMode mode)
    {
        this.animationMode = mode;
        this.currentAnimationType = mode.name().toLowerCase();
        this.renderer.setAnimationType(currentAnimationType);
    }

    /**
     * Sets the animation mode by name.
     *
     * @param name The animation name (e.g., "walk", "sprint")
     */
    public void setAnimationModeByName(String name)
    {
        this.currentAnimationType = name.toLowerCase();
        this.renderer.setAnimationType(currentAnimationType);

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
        this.animationMode = mode;
    }

    /**
     * @return The current animation mode
     */
    public AnimationMode getAnimationMode()
    {
        return animationMode;
    }

    /**
     * @return The current animation type name
     */
    public String getAnimationType()
    {
        return currentAnimationType;
    }

    /**
     * Resets the view rotation and scale to defaults.
     */
    public void resetView()
    {
        this.renderer.resetView();
    }

    /**
     * @return The X rotation (pitch)
     */
    public float getRotationX()
    {
        return renderer.getRotationX();
    }

    /**
     * @return The Y rotation (yaw)
     */
    public float getRotationY()
    {
        return renderer.getRotationY();
    }

    /**
     * @return The view scale
     */
    public float getScale()
    {
        return renderer.getScale();
    }

    /**
     * Sets the view rotation.
     *
     * @param x Pitch
     * @param y Yaw
     */
    public void setRotation(float x, float y)
    {
        this.renderer.setRotation(x, y);
    }

    /**
     * Sets the view scale.
     *
     * @param scale Scale factor (clamped to 20-80)
     */
    public void setScale(float scale)
    {
        this.renderer.setScale(scale);
    }

    /**
     * Called when dragging starts.
     *
     * @param mouseX Mouse X
     * @param mouseY Mouse Y
     */
    public void startDrag(int mouseX, int mouseY)
    {
        this.isDragging = true;
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }

    /**
     * Called when dragging continues.
     *
     * @param mouseX Mouse X
     * @param mouseY Mouse Y
     */
    public void updateDrag(int mouseX, int mouseY)
    {
        if (isDragging)
        {
            int deltaX = mouseX - lastMouseX;
            int deltaY = mouseY - lastMouseY;

            float newRotationY = renderer.getRotationY() + deltaX * 0.5f;
            float newRotationX = renderer.getRotationX() + deltaY * 0.5f;
            renderer.setRotation(newRotationX, newRotationY);

            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
    }

    /**
     * Called when dragging ends.
     */
    public void endDrag()
    {
        this.isDragging = false;
    }

    /**
     * @return Whether currently dragging
     */
    public boolean isDragging()
    {
        return isDragging;
    }

    /**
     * Updates the preview animation state.
     * Should be called each frame.
     */
    public void update()
    {
        renderer.update();
    }

    /**
     * Renders the entity preview using Minecraft's rendering system.
     * This should be called during the screen's render phase.
     *
     * @param guiGraphics  The GUI graphics context
     * @param x            Screen X position
     * @param y            Screen Y position
     * @param width        Width of render area
     * @param height       Height of render area
     * @param partialTicks Partial ticks for interpolation
     */
    public void renderEntity(GuiGraphics guiGraphics, int x, int y, int width, int height, float partialTicks)
    {
        if (renderer.hasEntity())
        {
            renderer.render(guiGraphics, x, y, width, height, partialTicks);
        }
    }

    /**
     * @return The underlying entity renderer
     */
    public EntityPreviewRenderer getRenderer()
    {
        return renderer;
    }

    /**
     * @return Whether an entity is loaded for preview
     */
    public boolean hasEntity()
    {
        return renderer.hasEntity();
    }

    /**
     * @return The root view to add to the layout
     */
    public IMuiView getView()
    {
        return rootLayout;
    }
}
