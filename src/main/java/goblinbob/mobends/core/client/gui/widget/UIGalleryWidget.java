package goblinbob.mobends.core.client.gui.widget;

import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import goblinbob.mobends.core.client.gui.vanilla.*;
import goblinbob.mobends.core.util.ResourceLocationFactory;

import java.util.Arrays;
import java.util.List;

public final class UIGalleryWidget
{
    private static final String LOREM =
            "Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt";

    private UIGalleryWidget() {}

    public static VanillaView build()
    {
        VanillaScrollView root = new VanillaScrollView();
        root.setLayoutParams(VanillaLayoutParams.matchParent());
        root.setBackgroundColor(MoBendsTheme.BG_CONTENT);

        VanillaLinearLayout col = new VanillaLinearLayout(VanillaLinearLayout.VERTICAL);
        col.setLayoutParams(wrapColumn());
        col.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING, MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        header(col, "TextView");
        row(col, text("Plain text", MoBendsTheme.TEXT_PRIMARY, 14, false));
        row(col, text("Bold text", MoBendsTheme.TEXT_PRIMARY, 14, true));
        VanillaTextView italic = text("Italic text (slanted)", MoBendsTheme.TEXT_PRIMARY, 14, false);
        italic.setItalic(true);
        row(col, italic);
        VanillaTextView boldItalic = text("Bold + italic", MoBendsTheme.TEXT_PRIMARY, 14, true);
        boldItalic.setItalic(true);
        row(col, boldItalic);
        row(col, text("Colored + size 20", MoBendsTheme.COLOR_SETTINGS, 20, false));
        VanillaTextView centered = text("Gravity center", MoBendsTheme.TEXT_PRIMARY, 14, false);
        centered.setGravity(VanillaLayoutParams.GRAVITY_CENTER);
        centered.setBackgroundColor(MoBendsTheme.BG_LIST);
        rowH(col, centered, 18);
        VanillaTextView maxLines = text("maxLines(2) wraps then clips: " + LOREM + " " + LOREM,
                MoBendsTheme.TEXT_SECONDARY, 12, false);
        maxLines.setMaxLines(2);
        row(col, maxLines);

        header(col, "Button");
        final VanillaTextView clickStatus = text("Clicked: 0", MoBendsTheme.TEXT_SECONDARY, 12, false);
        final int[] count = {0};
        VanillaButton clickBtn = new VanillaButton("Click me");
        clickBtn.setOnClickListener(() -> clickStatus.setText("Clicked: " + (++count[0])));
        rowH(col, clickBtn, MoBendsTheme.BUTTON_HEIGHT);
        row(col, clickStatus);
        VanillaButton colorBtn = new VanillaButton("Custom background color");
        colorBtn.setBackgroundColor(0xFF7A4FC0);
        rowH(col, colorBtn, MoBendsTheme.BUTTON_HEIGHT);
        VanillaButton sizeBtn = new VanillaButton("Larger text (setTextSize 20)");
        sizeBtn.setTextSize(20);
        rowH(col, sizeBtn, 40);
        VanillaButton iconBtn = new VanillaButton("Button with icon");
        iconBtn.setIcon(ResourceLocationFactory.create("mobends", "textures/gui/icons.png"));
        rowH(col, iconBtn, MoBendsTheme.BUTTON_HEIGHT);
        VanillaButton disabledBtn = new VanillaButton("Disabled button (dimmed + bordered)");
        disabledBtn.setEnabled(false);
        rowH(col, disabledBtn, MoBendsTheme.BUTTON_HEIGHT);

        header(col, "Toggle");
        final VanillaTextView toggleStatus = text("Toggle: OFF", MoBendsTheme.TEXT_SECONDARY, 12, false);
        VanillaToggle toggle = new VanillaToggle(false);
        toggle.setText("Enable feature");
        toggle.setOnCheckedChangeListener(v -> toggleStatus.setText("Toggle: " + (v ? "ON" : "OFF")));
        rowH(col, toggle, 20);
        row(col, toggleStatus);

        header(col, "TextField");
        final VanillaTextView echo = text("You typed: ", MoBendsTheme.TEXT_SECONDARY, 12, false);
        VanillaTextField field = new VanillaTextField("Type here (max 20 chars)...");
        field.setMaxLength(20);
        field.setOnTextChangedListener(s -> echo.setText("You typed: " + s));
        rowH(col, field, MoBendsTheme.BUTTON_HEIGHT);
        row(col, echo);

        header(col, "LinearLayout — horizontal weights 1 : 2 : 1");
        VanillaLinearLayout hrow = new VanillaLinearLayout(VanillaLinearLayout.HORIZONTAL);
        hrow.setSpacing(MoBendsTheme.SPACING);
        weighted(hrow, 0xFFE0563B, 1f);
        weighted(hrow, 0xFF3BA0E0, 2f);
        weighted(hrow, 0xFF43D9AD, 1f);
        rowH(col, hrow, 24);

        header(col, "FrameLayout — overlapping children");
        VanillaFrameLayout frame = new VanillaFrameLayout();
        VanillaView frameBg = new VanillaView();
        frameBg.setBackgroundColor(0xFF333845);
        frame.addView(frameBg, VanillaLayoutParams.matchParent());
        VanillaTextView overlay = text("centered on top", MoBendsTheme.COLOR_CUSTOMIZE, 14, true);
        frame.addView(overlay, new VanillaLayoutParams(
                VanillaLayoutParams.WRAP_CONTENT, VanillaLayoutParams.WRAP_CONTENT).setGravity(VanillaLayoutParams.GRAVITY_CENTER));
        rowH(col, frame, 36);

        header(col, "ScrollView — mouse-wheel scrolls; bordered box");
        VanillaScrollView inner = new VanillaScrollView();
        inner.setBackgroundColor(MoBendsTheme.BG_LIST);
        VanillaLinearLayout innerCol = new VanillaLinearLayout(VanillaLinearLayout.VERTICAL);
        innerCol.setLayoutParams(wrapColumn());
        for (int i = 1; i <= 12; i++)
        {
            VanillaTextView r = text("scrollable row " + i, MoBendsTheme.TEXT_PRIMARY, 12, false);
            r.setPadding(4, 3, 4, 3);
            innerCol.addView(r, new VanillaLayoutParams(VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT));
        }
        inner.addView(innerCol, wrapColumn());
        rowH(col, inner, 60);
        VanillaButton smooth = new VanillaButton("smoothScrollTo(bottom) — animated");
        smooth.setOnClickListener(() -> inner.smoothScrollTo(9999));
        rowH(col, smooth, MoBendsTheme.BUTTON_HEIGHT);
        VanillaButton smoothTop = new VanillaButton("smoothScrollTo(top) — animated");
        smoothTop.setOnClickListener(() -> inner.smoothScrollTo(0));
        rowH(col, smoothTop, MoBendsTheme.BUTTON_HEIGHT);

        header(col, "ListView — simple adapter with dividers");
        final VanillaTextView listStatus = text("Selected: (none)", MoBendsTheme.TEXT_SECONDARY, 12, false);
        VanillaListView list = new VanillaListView();
        List<String> items = Arrays.asList(
                "Apple", "Banana", "Cherry", "Date", "Elderberry", "Fig", "Grape", "Honeydew");
        list.setSimpleAdapter(items, (i, s) -> listStatus.setText("Selected: [" + i + "] " + s));
        list.setDividers(true, 0xFF55607A, 1);
        rowH(col, list, 80);
        row(col, listStatus);

        header(col, "Alpha / animateAlpha (animated fade)");
        VanillaTextView alphaBox = text("I can fade", MoBendsTheme.TEXT_PRIMARY, 14, true);
        alphaBox.setBackgroundColor(0xFF505870);
        rowH(col, alphaBox, 20);
        VanillaLinearLayout alphaBtns = new VanillaLinearLayout(VanillaLinearLayout.HORIZONTAL);
        alphaBtns.setSpacing(MoBendsTheme.SPACING);
        VanillaButton fade = new VanillaButton("animateAlpha 0.2");
        fade.setOnClickListener(() -> alphaBox.animateAlpha(0.2f, 400));
        VanillaButton unfade = new VanillaButton("setAlpha 1.0");
        unfade.setOnClickListener(() -> alphaBox.setAlpha(1f));
        alphaBtns.addView(fade, new VanillaLayoutParams(0, MoBendsTheme.BUTTON_HEIGHT, 1f));
        alphaBtns.addView(unfade, new VanillaLayoutParams(0, MoBendsTheme.BUTTON_HEIGHT, 1f));
        rowH(col, alphaBtns, MoBendsTheme.BUTTON_HEIGHT);

        header(col, "Visibility — GONE / VISIBLE");
        VanillaTextView toggleMe = text("Now you see me", MoBendsTheme.TEXT_PRIMARY, 14, false);
        toggleMe.setBackgroundColor(0xFF425C42);
        rowH(col, toggleMe, 18);
        VanillaButton visBtn = new VanillaButton("Toggle visibility");
        visBtn.setOnClickListener(() -> toggleMe.setVisibility(
                toggleMe.getVisibility() == VanillaView.VISIBLE ? VanillaView.GONE : VanillaView.VISIBLE));
        rowH(col, visBtn, MoBendsTheme.BUTTON_HEIGHT);

        root.addView(col, wrapColumn());
        return root;
    }

    private static VanillaLayoutParams wrapColumn()
    {
        return new VanillaLayoutParams(VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT);
    }

    private static VanillaTextView text(String s, int color, int size, boolean bold)
    {
        VanillaTextView t = new VanillaTextView(s);
        t.setTextColor(color);
        t.setTextSize(size);
        t.setBold(bold);
        return t;
    }

    private static void header(VanillaLinearLayout col, String s)
    {
        VanillaTextView h = new VanillaTextView(s);
        h.setTextColor(MoBendsTheme.COLOR_PACKS);
        h.setTextSize(13);
        h.setBold(true);
        VanillaLayoutParams p = new VanillaLayoutParams(VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT);
        p.setMargins(0, MoBendsTheme.PADDING, 0, MoBendsTheme.SPACING);
        col.addView(h, p);
    }

    private static void row(VanillaLinearLayout col, VanillaView v)
    {
        rowH(col, v, VanillaLayoutParams.WRAP_CONTENT);
    }

    private static void rowH(VanillaLinearLayout col, VanillaView v, int height)
    {
        VanillaLayoutParams p = new VanillaLayoutParams(VanillaLayoutParams.MATCH_PARENT, height);
        p.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        col.addView(v, p);
    }

    private static void weighted(VanillaLinearLayout row, int color, float weight)
    {
        VanillaView box = new VanillaView();
        box.setBackgroundColor(color);
        row.addView(box, new VanillaLayoutParams(0, VanillaLayoutParams.MATCH_PARENT, weight));
    }
}
