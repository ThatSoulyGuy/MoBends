package goblinbob.mobends.core.client.gui.widget;

import goblinbob.mobends.core.client.gui.vanilla.*;
import goblinbob.mobends.core.client.gui.EntityPreviewRenderer;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.client.gui.EntityPreviewRenderer;
import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import javax.annotation.Nullable;

public class EntityPreviewWidget
{
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

    private final VanillaViewFactory factory;
    private final VanillaFrameLayout rootLayout;
    private final VanillaView entityPreviewView;
    private final VanillaTextView titleView;
    private final VanillaTextView hintView;
    private final VanillaTextView statusView;
    private final EntityPreviewRenderer renderer;

    @Nullable
    private EntityBender<?> currentBender;
    private AnimationMode animationMode = AnimationMode.IDLE;
    private String currentAnimationType = "idle";

    private boolean isDragging = false;
    private int lastMouseX;
    private int lastMouseY;

    private boolean chromeVisible = true;
    private float scaleMultiplier = 1.0F;

    public EntityPreviewWidget(VanillaViewFactory factory, int width, int height)
    {
        this.factory = factory;
        this.renderer = new EntityPreviewRenderer();

        this.rootLayout = factory.createFrameLayout();
        this.rootLayout.setLayoutParams(factory.createLayoutParams(width, height));
        this.rootLayout.setBackgroundColor(MoBendsTheme.BG_CONTENT);

        this.entityPreviewView = factory.createEntityPreviewView(renderer);
        this.entityPreviewView.setVisibility(VanillaView.GONE);
        rootLayout.addView(entityPreviewView, factory.createMatchParent());

        VanillaLinearLayout contentLayout = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        contentLayout.setLayoutParams(factory.createMatchParent());
        contentLayout.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                                MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        this.titleView = factory.createTextView(I18n.get("mobends.gui.preview"));
        this.titleView.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        this.titleView.setTextSize(14);
        this.titleView.setBold(true);
        this.titleView.setGravity(VanillaLinearLayout.GRAVITY_CENTER_HORIZONTAL);
        contentLayout.addView(titleView, factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.WRAP_CONTENT
        ));

        this.statusView = factory.createTextView(I18n.get("mobends.gui.preview.select"));
        this.statusView.setTextColor(MoBendsTheme.TEXT_HINT);
        this.statusView.setTextSize(12);
        this.statusView.setGravity(VanillaLinearLayout.GRAVITY_CENTER);
        VanillaLayoutParams statusParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.MATCH_PARENT
        );
        statusParams.setMargins(0, MoBendsTheme.SPACING, 0, MoBendsTheme.SPACING);
        contentLayout.addView(statusView, statusParams);

        this.hintView = factory.createTextView(I18n.get("mobends.gui.preview.hint"));
        this.hintView.setTextColor(MoBendsTheme.TEXT_HINT);
        this.hintView.setTextSize(10);
        this.hintView.setGravity(VanillaLinearLayout.GRAVITY_CENTER_HORIZONTAL);
        contentLayout.addView(hintView, factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.WRAP_CONTENT
        ));

        rootLayout.addView(contentLayout, factory.createMatchParent());

        titleView.setVisibility(VanillaView.GONE);
        statusView.setVisibility(VanillaView.GONE);
        hintView.setVisibility(VanillaView.GONE);
    }

    public void setBender(@Nullable EntityBender<?> bender)
    {
        this.currentBender = bender;
        this.renderer.setBenderTyped(bender);

        if (bender != null)
        {
            titleView.setText(bender.getLocalizedName());
            titleView.setVisibility(VanillaView.VISIBLE);
            if (renderer.hasEntity())
            {
                entityPreviewView.setVisibility(VanillaView.VISIBLE);
                statusView.setVisibility(VanillaView.GONE);
            }
            else
            {
                entityPreviewView.setVisibility(VanillaView.GONE);

                if (net.minecraft.client.Minecraft.getInstance().level == null)
                {
                    statusView.setText(I18n.get("mobends.gui.preview.needs_world"));
                    statusView.setTextColor(MoBendsTheme.TEXT_SECONDARY);
                }
                else
                {
                    statusView.setText(I18n.get("mobends.gui.preview.failed", bender.getLocalizedName()));
                    statusView.setTextColor(MoBendsTheme.ACCENT_ERROR);
                }

                statusView.setVisibility(VanillaView.VISIBLE);
            }
            hintView.setVisibility(renderer.hasEntity() ? VanillaView.VISIBLE : VanillaView.GONE);
        }
        else
        {
            entityPreviewView.setVisibility(VanillaView.GONE);
            titleView.setVisibility(VanillaView.GONE);
            statusView.setVisibility(VanillaView.GONE);
            hintView.setVisibility(VanillaView.GONE);
        }

        applyChrome();
        resetView();
        applyScaleMultiplier();
    }

    public void setScaleMultiplier(float scaleMultiplier)
    {
        this.scaleMultiplier = scaleMultiplier;
        applyScaleMultiplier();
    }

    public void fitToSize(float availablePixels, float minFitScale, float maxFitScale)
    {
        renderer.fitToSize(availablePixels, minFitScale, maxFitScale);
    }

    public void setBackgroundColor(int color)
    {
        rootLayout.setBackgroundColor(color);
    }

    private void applyScaleMultiplier()
    {
        if (scaleMultiplier == 1.0F) return;

        renderer.setScale(renderer.getScale() * scaleMultiplier);
    }

    public void setChromeVisible(boolean chromeVisible)
    {
        this.chromeVisible = chromeVisible;
        applyChrome();
    }

    public void setInteractive(boolean interactive)
    {
        if (entityPreviewView instanceof goblinbob.mobends.core.client.gui.vanilla.VanillaEntityPreviewView preview)
        {
            preview.setInteractive(interactive);
        }
    }

    private void applyChrome()
    {
        if (chromeVisible) return;

        titleView.setVisibility(VanillaView.GONE);
        statusView.setVisibility(VanillaView.GONE);
        hintView.setVisibility(VanillaView.GONE);
    }

    @Nullable
    public EntityBender<?> getBender()
    {
        return currentBender;
    }

    public void setAnimationMode(AnimationMode mode)
    {
        this.animationMode = mode;
        this.currentAnimationType = mode.name().toLowerCase();
        this.renderer.setAnimationType(currentAnimationType);
    }

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

    public AnimationMode getAnimationMode()
    {
        return animationMode;
    }

    public String getAnimationType()
    {
        return currentAnimationType;
    }

    public void resetView()
    {
        this.renderer.resetView();
    }

    public float getRotationX()
    {
        return renderer.getRotationX();
    }

    public float getRotationY()
    {
        return renderer.getRotationY();
    }

    public float getScale()
    {
        return renderer.getScale();
    }

    public void setRotation(float x, float y)
    {
        this.renderer.setRotation(x, y);
    }

    public void setScale(float scale)
    {
        this.renderer.setScale(scale);
    }

    public void startDrag(int mouseX, int mouseY)
    {
        this.isDragging = true;
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }

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

    public void endDrag()
    {
        this.isDragging = false;
    }

    public boolean isDragging()
    {
        return isDragging;
    }

    public void update()
    {
        renderer.update();
    }

    public void renderEntity(GuiGraphics guiGraphics, int x, int y, int width, int height, float partialTicks)
    {
        if (renderer.hasEntity())
        {
            renderer.render(guiGraphics, x, y, width, height, partialTicks);
        }
    }

    public EntityPreviewRenderer getRenderer()
    {
        return renderer;
    }

    public boolean hasEntity()
    {
        return renderer.hasEntity();
    }

    public VanillaView getView()
    {
        return rootLayout;
    }
}
