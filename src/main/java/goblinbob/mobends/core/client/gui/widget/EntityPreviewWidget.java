package goblinbob.mobends.core.client.gui.widget;

import goblinbob.mobends.core.client.gui.view.*;
import goblinbob.mobends.core.client.gui.EntityPreviewRenderer;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.resources.language.I18n;

import javax.annotation.Nullable;

public class EntityPreviewWidget
{
    private final FrameLayout rootLayout;
    private final View entityPreviewView;
    private final TextView titleView;
    private final TextView hintView;
    private final TextView statusView;
    private final EntityPreviewRenderer renderer;

    private boolean chromeVisible = true;
    private float scaleMultiplier = 1.0F;

    public EntityPreviewWidget(int width, int height)
    {
        this.renderer = new EntityPreviewRenderer();

        this.rootLayout = new FrameLayout();
        this.rootLayout.setLayoutParams(new LayoutParams(width, height));
        this.rootLayout.setBackgroundColor(MoBendsTheme.BG_CONTENT);

        this.entityPreviewView = new EntityPreviewView(renderer);
        this.entityPreviewView.setVisibility(View.GONE);
        rootLayout.addView(entityPreviewView, LayoutParams.matchParent());

        LinearLayout contentLayout = new LinearLayout(LinearLayout.VERTICAL);
        contentLayout.setLayoutParams(LayoutParams.matchParent());
        contentLayout.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                                MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        this.titleView = new TextView(I18n.get("mobends.gui.preview"));
        this.titleView.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        this.titleView.setTextSize(14);
        this.titleView.setBold(true);
        this.titleView.setGravity(LayoutParams.GRAVITY_CENTER_HORIZONTAL);
        contentLayout.addView(titleView, new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));

        this.statusView = new TextView(I18n.get("mobends.gui.preview.select"));
        this.statusView.setTextColor(MoBendsTheme.TEXT_HINT);
        this.statusView.setTextSize(12);
        this.statusView.setGravity(LayoutParams.GRAVITY_CENTER);
        LayoutParams statusParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        statusParams.setMargins(0, MoBendsTheme.SPACING, 0, MoBendsTheme.SPACING);
        contentLayout.addView(statusView, statusParams);

        this.hintView = new TextView(I18n.get("mobends.gui.preview.hint"));
        this.hintView.setTextColor(MoBendsTheme.TEXT_HINT);
        this.hintView.setTextSize(10);
        this.hintView.setGravity(LayoutParams.GRAVITY_CENTER_HORIZONTAL);
        contentLayout.addView(hintView, new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));

        rootLayout.addView(contentLayout, LayoutParams.matchParent());

        titleView.setVisibility(View.GONE);
        statusView.setVisibility(View.GONE);
        hintView.setVisibility(View.GONE);
    }

    public void setBender(@Nullable EntityBender<?> bender)
    {
        this.renderer.setBenderTyped(bender);

        if (bender != null)
        {
            titleView.setText(bender.getLocalizedName());
            titleView.setVisibility(View.VISIBLE);
            if (renderer.hasEntity())
            {
                entityPreviewView.setVisibility(View.VISIBLE);
                statusView.setVisibility(View.GONE);
            }
            else
            {
                entityPreviewView.setVisibility(View.GONE);

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

                statusView.setVisibility(View.VISIBLE);
            }
            hintView.setVisibility(renderer.hasEntity() ? View.VISIBLE : View.GONE);
        }
        else
        {
            entityPreviewView.setVisibility(View.GONE);
            titleView.setVisibility(View.GONE);
            statusView.setVisibility(View.GONE);
            hintView.setVisibility(View.GONE);
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
        if (entityPreviewView instanceof goblinbob.mobends.core.client.gui.view.EntityPreviewView preview)
        {
            preview.setInteractive(interactive);
        }
    }

    private void applyChrome()
    {
        if (chromeVisible) return;

        titleView.setVisibility(View.GONE);
        statusView.setVisibility(View.GONE);
        hintView.setVisibility(View.GONE);
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

    public View getView()
    {
        return rootLayout;
    }
}
