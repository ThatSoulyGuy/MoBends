package goblinbob.mobends.core.client.gui.widget;

import goblinbob.mobends.core.client.gui.vanilla.*;
import goblinbob.mobends.core.client.gui.EntityPreviewRenderer;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.resources.language.I18n;

import javax.annotation.Nullable;

public class EntityPreviewWidget
{
    private final VanillaFrameLayout rootLayout;
    private final VanillaView entityPreviewView;
    private final VanillaTextView titleView;
    private final VanillaTextView hintView;
    private final VanillaTextView statusView;
    private final EntityPreviewRenderer renderer;

    private boolean chromeVisible = true;
    private float scaleMultiplier = 1.0F;

    public EntityPreviewWidget(int width, int height)
    {
        this.renderer = new EntityPreviewRenderer();

        this.rootLayout = new VanillaFrameLayout();
        this.rootLayout.setLayoutParams(new VanillaLayoutParams(width, height));
        this.rootLayout.setBackgroundColor(MoBendsTheme.BG_CONTENT);

        this.entityPreviewView = new VanillaEntityPreviewView(renderer);
        this.entityPreviewView.setVisibility(VanillaView.GONE);
        rootLayout.addView(entityPreviewView, VanillaLayoutParams.matchParent());

        VanillaLinearLayout contentLayout = new VanillaLinearLayout(VanillaLinearLayout.VERTICAL);
        contentLayout.setLayoutParams(VanillaLayoutParams.matchParent());
        contentLayout.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                                MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        this.titleView = new VanillaTextView(I18n.get("mobends.gui.preview"));
        this.titleView.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        this.titleView.setTextSize(14);
        this.titleView.setBold(true);
        this.titleView.setGravity(VanillaLayoutParams.GRAVITY_CENTER_HORIZONTAL);
        contentLayout.addView(titleView, new VanillaLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.WRAP_CONTENT
        ));

        this.statusView = new VanillaTextView(I18n.get("mobends.gui.preview.select"));
        this.statusView.setTextColor(MoBendsTheme.TEXT_HINT);
        this.statusView.setTextSize(12);
        this.statusView.setGravity(VanillaLayoutParams.GRAVITY_CENTER);
        VanillaLayoutParams statusParams = new VanillaLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.MATCH_PARENT
        );
        statusParams.setMargins(0, MoBendsTheme.SPACING, 0, MoBendsTheme.SPACING);
        contentLayout.addView(statusView, statusParams);

        this.hintView = new VanillaTextView(I18n.get("mobends.gui.preview.hint"));
        this.hintView.setTextColor(MoBendsTheme.TEXT_HINT);
        this.hintView.setTextSize(10);
        this.hintView.setGravity(VanillaLayoutParams.GRAVITY_CENTER_HORIZONTAL);
        contentLayout.addView(hintView, new VanillaLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.WRAP_CONTENT
        ));

        rootLayout.addView(contentLayout, VanillaLayoutParams.matchParent());

        titleView.setVisibility(VanillaView.GONE);
        statusView.setVisibility(VanillaView.GONE);
        hintView.setVisibility(VanillaView.GONE);
    }

    public void setBender(@Nullable EntityBender<?> bender)
    {
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

    public void setRotation(float x, float y)
    {
        this.renderer.setRotation(x, y);
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
