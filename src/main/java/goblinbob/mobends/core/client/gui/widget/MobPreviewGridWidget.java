package goblinbob.mobends.core.client.gui.widget;

import goblinbob.mobends.core.bender.BenderDiscovery;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.gui.EntityPreviewRenderer;
import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import goblinbob.mobends.core.client.gui.view.*;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MobPreviewGridWidget
{
    public enum SpinMode
    {
        OFF,
        HOVER,
        ALWAYS
    }

    public static final String IDLE_ANIMATION = "idle";

    private static final String[] ANIMATION_ORDER = {
            IDLE_ANIMATION, "walk", "sprint", "jump", "fall", "sneak",
            "swim", "attack", "use_item", "ride", "climb", "sit"
    };

    private static final int CARD_WIDTH = 84;
    private static final int CARD_HEIGHT = 98;
    private static final int CARD_SPACING = 4;
    private static final int ACCENT_HEIGHT = 2;
    private static final int NAME_HEIGHT = 15;
    private static final int TOGGLE_HEIGHT = 14;
    private static final int TOGGLE_TRACK_WIDTH = 20;
    private static final int TOGGLE_TRACK_HEIGHT = 8;
    private static final int NAME_TEXT_SIZE = 9;
    private static final float PREVIEW_FIT_PIXELS = 52.0F;
    private static final float PREVIEW_MIN_FIT_SCALE = 20.0F;
    private static final float PREVIEW_MAX_FIT_SCALE = 38.0F;
    private static final float SPIN_DEGREES_PER_SECOND = 24.0F;

    private final ScrollView scrollView;
    private final GridLayout grid;
    private final List<MobCard> cards = new ArrayList<>();

    private String animationType = IDLE_ANIMATION;
    private SpinMode spinMode = SpinMode.HOVER;

    public MobPreviewGridWidget()
    {
        this.scrollView = new ScrollView();
        this.scrollView.setLayoutParams(LayoutParams.matchParent());
        this.scrollView.setBackgroundColor(MoBendsTheme.BG_LIST);
        this.scrollView.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        this.grid = new GridLayout();
        this.grid.setCellSize(CARD_WIDTH, CARD_HEIGHT);
        this.grid.setSpacing(CARD_SPACING, CARD_SPACING);

        scrollView.addView(grid, new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));
    }

    public View getView()
    {
        return scrollView;
    }

    public void populateFromRegistry()
    {
        clear();

        BenderDiscovery.scanForDerivedBenders();

        for (EntityBender<?> bender : EntityBenderRegistry.instance.getRegisteredUnique())
        {
            MobCard card = createCard(bender);
            cards.add(card);
            grid.addView(card.view);
        }
    }

    public void clear()
    {
        for (MobCard card : cards)
        {
            card.preview.getRenderer().dispose();
        }

        cards.clear();
        grid.removeAllViews();
    }

    public void dispose()
    {
        clear();
    }

    public void filter(String query)
    {
        String lowerQuery = query.toLowerCase(Locale.ROOT);

        for (MobCard card : cards)
        {
            boolean matches = lowerQuery.isEmpty()
                    || card.bender.getLocalizedName().toLowerCase(Locale.ROOT).contains(lowerQuery);

            card.view.setVisibility(matches ? View.VISIBLE : View.GONE);
        }
    }

    public List<String> getAvailableAnimations()
    {
        Set<String> supported = new LinkedHashSet<>();
        supported.add(IDLE_ANIMATION);

        for (MobCard card : cards)
        {
            for (String animation : card.bender.getSupportedAnimations())
            {
                supported.add(animation.toLowerCase(Locale.ROOT));
            }
        }

        List<String> ordered = new ArrayList<>();
        for (String animation : ANIMATION_ORDER)
        {
            if (supported.remove(animation))
            {
                ordered.add(animation);
            }
        }
        ordered.addAll(supported);

        return ordered;
    }

    public String getAnimationType()
    {
        return animationType;
    }

    public void setSpinMode(SpinMode spinMode)
    {
        this.spinMode = spinMode;

        for (MobCard card : cards)
        {
            card.lastSpinNanos = -1L;

            if (spinMode == SpinMode.OFF)
            {
                card.preview.setRotation(EntityPreviewRenderer.DEFAULT_ROTATION_X,
                        EntityPreviewRenderer.DEFAULT_ROTATION_Y);
            }
        }
    }

    public void setAnimationType(String animationType)
    {
        this.animationType = animationType.toLowerCase(Locale.ROOT);

        for (MobCard card : cards)
        {
            applyAnimationTo(card);
        }
    }

    private void applyAnimationTo(MobCard card)
    {
        card.preview.getRenderer().setAnimationType(
                supportsAnimation(card.bender, animationType) ? animationType : IDLE_ANIMATION);
    }

    private static boolean supportsAnimation(EntityBender<?> bender, String animation)
    {
        if (IDLE_ANIMATION.equals(animation)) return true;

        for (String supported : bender.getSupportedAnimations())
        {
            if (supported.equalsIgnoreCase(animation)) return true;
        }
        return false;
    }

    private MobCard createCard(EntityBender<?> bender)
    {
        TileView view = new TileView();
        view.setOrientation(LinearLayout.VERTICAL);
        view.setGravity(LayoutParams.GRAVITY_CENTER_HORIZONTAL);
        view.setBulge(0);
        view.setIdleColor(MoBendsTheme.BG_LIST_ITEM_HOVER);
        view.setPadding(0, 0, 0, MoBendsTheme.PADDING_SMALL);

        View accentBar = new View();
        view.addView(accentBar, new LayoutParams(
                LayoutParams.MATCH_PARENT, ACCENT_HEIGHT));

        FrameLayout previewSlot = new FrameLayout();
        LayoutParams previewSlotParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, 0, 1.0F);
        view.addView(previewSlot, previewSlotParams);

        EntityPreviewWidget preview = new EntityPreviewWidget(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        preview.setChromeVisible(false);
        preview.setInteractive(false);
        preview.setBackgroundColor(0);
        previewSlot.addView(preview.getView(), LayoutParams.matchParent());

        TextView placeholderView = new TextView(I18n.get("mobends.gui.preview.unavailable"));
        placeholderView.setTextColor(MoBendsTheme.TEXT_HINT);
        placeholderView.setTextSize(NAME_TEXT_SIZE);
        placeholderView.setGravity(LayoutParams.GRAVITY_CENTER);
        placeholderView.setVisibility(View.GONE);
        previewSlot.addView(placeholderView, LayoutParams.matchParent());

        TextView nameView = new TextView(bender.getLocalizedName());
        nameView.setTextSize(NAME_TEXT_SIZE);
        nameView.setGravity(LayoutParams.GRAVITY_CENTER);
        nameView.setMaxLines(2);
        view.addView(nameView, new LayoutParams(
                LayoutParams.MATCH_PARENT, NAME_HEIGHT));

        Toggle toggle = new Toggle(bender.isAnimated());
        toggle.setToggleSize(TOGGLE_TRACK_WIDTH, TOGGLE_TRACK_HEIGHT);
        toggle.setTooltip(I18n.get("mobends.gui.animations.toggle"));
        view.addView(toggle, new LayoutParams(
                LayoutParams.WRAP_CONTENT, TOGGLE_HEIGHT));

        MobCard card = new MobCard(bender, view, preview, accentBar, nameView, placeholderView);

        toggle.setOnCheckedChangeListener(checked -> setBenderAnimated(card, checked));
        view.setOnClickListener(toggle::toggle);
        view.setTicker(() -> tickCard(card));

        applyEnabledStyle(card, bender.isAnimated());

        return card;
    }

    private void setBenderAnimated(MobCard card, boolean animated)
    {
        goblinbob.mobends.core.bender.EntityBenderRegistry.instance
                .setAnimateForKey(card.bender.getKey(), animated);
        CoreClientConfig.getInstance().setEntityEnabled(card.bender.getKey(), animated);
        applyEnabledStyle(card, animated);
    }

    private void applyEnabledStyle(MobCard card, boolean animated)
    {
        card.accentBar.setBackgroundColor(animated ? MoBendsTheme.TOGGLE_ON : MoBendsTheme.TOGGLE_OFF);
        card.nameView.setTextColor(animated ? MoBendsTheme.TEXT_PRIMARY : MoBendsTheme.TEXT_DISABLED);
    }

    private void tickCard(MobCard card)
    {
        boolean onScreen = isCardOnScreen(card);

        if (onScreen && !card.previewInitialized)
        {
            card.previewInitialized = true;
            card.preview.setBender(card.bender);
            card.preview.fitToSize(PREVIEW_FIT_PIXELS, PREVIEW_MIN_FIT_SCALE, PREVIEW_MAX_FIT_SCALE);
            applyAnimationTo(card);

            if (!card.preview.hasEntity())
            {
                card.placeholderView.setVisibility(View.VISIBLE);
            }
        }

        card.preview.getView().setVisibility(onScreen ? View.VISIBLE : View.INVISIBLE);

        if (onScreen && shouldSpin(card))
        {
            spinPreview(card);
        }
        else
        {
            card.lastSpinNanos = -1L;
        }
    }

    private boolean shouldSpin(MobCard card)
    {
        return switch (spinMode)
        {
            case ALWAYS -> true;
            case HOVER -> card.view.isHovered();
            case OFF -> false;
        };
    }

    private void spinPreview(MobCard card)
    {
        long now = System.nanoTime();

        if (card.lastSpinNanos < 0L)
        {
            card.lastSpinNanos = now;
            return;
        }

        float deltaSec = Math.min((now - card.lastSpinNanos) / 1_000_000_000.0F, 0.1F);
        card.lastSpinNanos = now;

        EntityPreviewWidget preview = card.preview;
        preview.setRotation(preview.getRotationX(),
                preview.getRotationY() + deltaSec * SPIN_DEGREES_PER_SECOND);
    }

    private boolean isCardOnScreen(MobCard card)
    {
        int cardTop = card.view.getTop();
        int cardBottom = cardTop + card.view.getHeight();
        int viewportTop = scrollView.getTop();
        int viewportBottom = viewportTop + scrollView.getHeight();

        return cardBottom > viewportTop && cardTop < viewportBottom;
    }

    private static final class MobCard
    {
        final EntityBender<?> bender;
        final TileView view;
        final EntityPreviewWidget preview;
        final View accentBar;
        final TextView nameView;
        final TextView placeholderView;

        boolean previewInitialized;
        long lastSpinNanos = -1L;

        MobCard(EntityBender<?> bender, TileView view, EntityPreviewWidget preview,
                View accentBar, TextView nameView, TextView placeholderView)
        {
            this.bender = bender;
            this.view = view;
            this.preview = preview;
            this.accentBar = accentBar;
            this.nameView = nameView;
            this.placeholderView = placeholderView;
        }
    }
}
