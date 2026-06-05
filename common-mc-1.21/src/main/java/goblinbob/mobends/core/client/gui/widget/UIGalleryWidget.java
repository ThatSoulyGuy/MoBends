package goblinbob.mobends.core.client.gui.widget;

import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import goblinbob.mobends.core.client.gui.vanilla.*;
import goblinbob.mobends.core.util.ResourceLocationFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Developer "kitchen-sink" gallery: instantiates every UI element with a few variations
 * and live interactions so each widget can be eyeballed for correctness. Items that are
 * known to be unimplemented stubs are labelled "(KNOWN: ...)" so expected-vs-actual is clear.
 *
 * Shown only on the dev-only "UI Test" tab (see MoBendsScreenBuilder). Self-contained and
 * safe to delete wholesale once the UI elements are verified/fixed.
 */
public final class UIGalleryWidget
{
    private static final String LOREM =
            "Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt";

    private UIGalleryWidget() {}

    public static VanillaView build(VanillaViewFactory factory)
    {
        VanillaScrollView root = factory.createScrollView();
        root.setLayoutParams(factory.createMatchParent());
        root.setBackgroundColor(MoBendsTheme.BG_CONTENT);

        VanillaLinearLayout col = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        col.setLayoutParams(wrapColumn(factory));
        col.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING, MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        // ==================== TextView ====================
        header(factory, col, "TextView");
        row(factory, col, text(factory, "Plain text", MoBendsTheme.TEXT_PRIMARY, 14, false));
        row(factory, col, text(factory, "Bold text", MoBendsTheme.TEXT_PRIMARY, 14, true));
        VanillaTextView italic = text(factory, "Italic text (slanted)", MoBendsTheme.TEXT_PRIMARY, 14, false);
        italic.setItalic(true);
        row(factory, col, italic);
        VanillaTextView boldItalic = text(factory, "Bold + italic", MoBendsTheme.TEXT_PRIMARY, 14, true);
        boldItalic.setItalic(true);
        row(factory, col, boldItalic);
        row(factory, col, text(factory, "Colored + size 20", MoBendsTheme.COLOR_SETTINGS, 20, false));
        VanillaTextView centered = text(factory, "Gravity center", MoBendsTheme.TEXT_PRIMARY, 14, false);
        centered.setGravity(VanillaLinearLayout.GRAVITY_CENTER);
        centered.setBackgroundColor(MoBendsTheme.BG_LIST);
        rowH(factory, col, centered, 18);
        VanillaTextView maxLines = text(factory, "maxLines(2) wraps then clips: " + LOREM + " " + LOREM,
                MoBendsTheme.TEXT_SECONDARY, 12, false);
        maxLines.setMaxLines(2);
        row(factory, col, maxLines);

        // ==================== Button ====================
        header(factory, col, "Button");
        final VanillaTextView clickStatus = text(factory, "Clicked: 0", MoBendsTheme.TEXT_SECONDARY, 12, false);
        final int[] count = {0};
        VanillaButton clickBtn = factory.createButton("Click me");
        clickBtn.setOnClickListener(() -> clickStatus.setText("Clicked: " + (++count[0])));
        rowH(factory, col, clickBtn, MoBendsTheme.BUTTON_HEIGHT);
        row(factory, col, clickStatus);
        VanillaButton colorBtn = factory.createButton("Custom background color");
        colorBtn.setBackgroundColor(0xFF7A4FC0);
        rowH(factory, col, colorBtn, MoBendsTheme.BUTTON_HEIGHT);
        VanillaButton sizeBtn = factory.createButton("Larger text (setTextSize 20)");
        sizeBtn.setTextSize(20);
        rowH(factory, col, sizeBtn, 40);
        VanillaButton iconBtn = factory.createButton("Button with icon");
        iconBtn.setIcon(ResourceLocationFactory.create("mobends", "textures/gui/icons.png"));
        rowH(factory, col, iconBtn, MoBendsTheme.BUTTON_HEIGHT);
        VanillaButton disabledBtn = factory.createButton("Disabled button (dimmed + bordered)");
        disabledBtn.setEnabled(false);
        rowH(factory, col, disabledBtn, MoBendsTheme.BUTTON_HEIGHT);

        // ==================== Toggle ====================
        header(factory, col, "Toggle");
        final VanillaTextView toggleStatus = text(factory, "Toggle: OFF", MoBendsTheme.TEXT_SECONDARY, 12, false);
        VanillaToggle toggle = factory.createToggle(false);
        toggle.setText("Enable feature");
        toggle.setOnCheckedChangeListener(v -> toggleStatus.setText("Toggle: " + (v ? "ON" : "OFF")));
        rowH(factory, col, toggle, 20);
        row(factory, col, toggleStatus);

        // ==================== TextField ====================
        header(factory, col, "TextField");
        final VanillaTextView echo = text(factory, "You typed: ", MoBendsTheme.TEXT_SECONDARY, 12, false);
        VanillaTextField field = factory.createTextField("Type here (max 20 chars)...");
        field.setMaxLength(20);
        field.setOnTextChangedListener(s -> echo.setText("You typed: " + s));
        rowH(factory, col, field, MoBendsTheme.BUTTON_HEIGHT);
        row(factory, col, echo);

        // ==================== LinearLayout (weights) ====================
        header(factory, col, "LinearLayout — horizontal weights 1 : 2 : 1");
        VanillaLinearLayout hrow = factory.createLinearLayout(VanillaViewFactory.HORIZONTAL);
        hrow.setSpacing(MoBendsTheme.SPACING);
        weighted(factory, hrow, 0xFFE0563B, 1f);
        weighted(factory, hrow, 0xFF3BA0E0, 2f);
        weighted(factory, hrow, 0xFF43D9AD, 1f);
        rowH(factory, col, hrow, 24);

        // ==================== FrameLayout (overlap) ====================
        header(factory, col, "FrameLayout — overlapping children");
        VanillaFrameLayout frame = factory.createFrameLayout();
        VanillaView frameBg = factory.createView();
        frameBg.setBackgroundColor(0xFF333845);
        frame.addView(frameBg, factory.createMatchParent());
        VanillaTextView overlay = text(factory, "centered on top", MoBendsTheme.COLOR_CUSTOMIZE, 14, true);
        frame.addView(overlay, factory.createFrameLayoutParams(
                VanillaLayoutParams.WRAP_CONTENT, VanillaLayoutParams.WRAP_CONTENT, VanillaLayoutParams.GRAVITY_CENTER));
        rowH(factory, col, frame, 36);

        // ==================== ScrollView (nested) ====================
        header(factory, col, "ScrollView — mouse-wheel scrolls; bordered box");
        VanillaScrollView inner = factory.createScrollView();
        inner.setBackgroundColor(MoBendsTheme.BG_LIST);
        VanillaLinearLayout innerCol = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        innerCol.setLayoutParams(wrapColumn(factory));
        for (int i = 1; i <= 12; i++)
        {
            VanillaTextView r = text(factory, "scrollable row " + i, MoBendsTheme.TEXT_PRIMARY, 12, false);
            r.setPadding(4, 3, 4, 3);
            innerCol.addView(r, factory.createLayoutParams(VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT));
        }
        inner.addView(innerCol, wrapColumn(factory));
        rowH(factory, col, inner, 60);
        VanillaButton smooth = factory.createButton("smoothScrollTo(bottom) — animated");
        smooth.setOnClickListener(() -> inner.smoothScrollTo(9999));
        rowH(factory, col, smooth, MoBendsTheme.BUTTON_HEIGHT);
        VanillaButton smoothTop = factory.createButton("smoothScrollTo(top) — animated");
        smoothTop.setOnClickListener(() -> inner.smoothScrollTo(0));
        rowH(factory, col, smoothTop, MoBendsTheme.BUTTON_HEIGHT);

        // ==================== ListView ====================
        header(factory, col, "ListView — simple adapter with dividers");
        final VanillaTextView listStatus = text(factory, "Selected: (none)", MoBendsTheme.TEXT_SECONDARY, 12, false);
        VanillaListView list = factory.createListView();
        List<String> items = Arrays.asList(
                "Apple", "Banana", "Cherry", "Date", "Elderberry", "Fig", "Grape", "Honeydew");
        list.setSimpleAdapter(items, (i, s) -> listStatus.setText("Selected: [" + i + "] " + s));
        list.setDividers(true, 0xFF55607A, 1);
        rowH(factory, col, list, 80);
        row(factory, col, listStatus);

        // ==================== Alpha / animateAlpha ====================
        header(factory, col, "Alpha / animateAlpha (animated fade)");
        VanillaTextView alphaBox = text(factory, "I can fade", MoBendsTheme.TEXT_PRIMARY, 14, true);
        alphaBox.setBackgroundColor(0xFF505870);
        rowH(factory, col, alphaBox, 20);
        VanillaLinearLayout alphaBtns = factory.createLinearLayout(VanillaViewFactory.HORIZONTAL);
        alphaBtns.setSpacing(MoBendsTheme.SPACING);
        VanillaButton fade = factory.createButton("animateAlpha 0.2");
        fade.setOnClickListener(() -> alphaBox.animateAlpha(0.2f, 400));
        VanillaButton unfade = factory.createButton("setAlpha 1.0");
        unfade.setOnClickListener(() -> alphaBox.setAlpha(1f));
        alphaBtns.addView(fade, factory.createLayoutParams(0, MoBendsTheme.BUTTON_HEIGHT, 1f));
        alphaBtns.addView(unfade, factory.createLayoutParams(0, MoBendsTheme.BUTTON_HEIGHT, 1f));
        rowH(factory, col, alphaBtns, MoBendsTheme.BUTTON_HEIGHT);

        // ==================== Visibility ====================
        header(factory, col, "Visibility — GONE / VISIBLE");
        VanillaTextView toggleMe = text(factory, "Now you see me", MoBendsTheme.TEXT_PRIMARY, 14, false);
        toggleMe.setBackgroundColor(0xFF425C42);
        rowH(factory, col, toggleMe, 18);
        VanillaButton visBtn = factory.createButton("Toggle visibility");
        visBtn.setOnClickListener(() -> toggleMe.setVisibility(
                toggleMe.getVisibility() == VanillaView.VISIBLE ? VanillaView.GONE : VanillaView.VISIBLE));
        rowH(factory, col, visBtn, MoBendsTheme.BUTTON_HEIGHT);

        root.addView(col, wrapColumn(factory));
        return root;
    }

    // ==================== Helpers ====================

    private static VanillaLayoutParams wrapColumn(VanillaViewFactory factory)
    {
        return factory.createLayoutParams(VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT);
    }

    private static VanillaTextView text(VanillaViewFactory factory, String s, int color, int size, boolean bold)
    {
        VanillaTextView t = factory.createTextView(s);
        t.setTextColor(color);
        t.setTextSize(size);
        t.setBold(bold);
        return t;
    }

    private static void header(VanillaViewFactory factory, VanillaLinearLayout col, String s)
    {
        VanillaTextView h = factory.createTextView(s);
        h.setTextColor(MoBendsTheme.COLOR_PACKS);
        h.setTextSize(13);
        h.setBold(true);
        VanillaLayoutParams p = factory.createLayoutParams(VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT);
        p.setMargins(0, MoBendsTheme.PADDING, 0, MoBendsTheme.SPACING);
        col.addView(h, p);
    }

    private static void row(VanillaViewFactory factory, VanillaLinearLayout col, VanillaView v)
    {
        rowH(factory, col, v, VanillaLayoutParams.WRAP_CONTENT);
    }

    private static void rowH(VanillaViewFactory factory, VanillaLinearLayout col, VanillaView v, int height)
    {
        VanillaLayoutParams p = factory.createLayoutParams(VanillaLayoutParams.MATCH_PARENT, height);
        p.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        col.addView(v, p);
    }

    private static void weighted(VanillaViewFactory factory, VanillaLinearLayout row, int color, float weight)
    {
        VanillaView box = factory.createView();
        box.setBackgroundColor(color);
        row.addView(box, factory.createLayoutParams(0, VanillaLayoutParams.MATCH_PARENT, weight));
    }
}
