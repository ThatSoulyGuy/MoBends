package goblinbob.mobends.core.client.gui.widget;

import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import goblinbob.mobends.core.client.gui.view.*;
import goblinbob.mobends.core.util.ResourceLocationFactory;

import java.util.Arrays;
import java.util.List;

public final class UIGalleryWidget
{
    private static final String LOREM =
            "Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt";

    private UIGalleryWidget() {}

    public static View build()
    {
        ScrollView root = new ScrollView();
        root.setLayoutParams(LayoutParams.matchParent());
        root.setBackgroundColor(MoBendsTheme.BG_CONTENT);

        LinearLayout col = new LinearLayout(LinearLayout.VERTICAL);
        col.setLayoutParams(wrapColumn());
        col.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING, MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        header(col, "TextView");
        row(col, text("Plain text", MoBendsTheme.TEXT_PRIMARY, 14, false));
        row(col, text("Bold text", MoBendsTheme.TEXT_PRIMARY, 14, true));
        TextView italic = text("Italic text (slanted)", MoBendsTheme.TEXT_PRIMARY, 14, false);
        italic.setItalic(true);
        row(col, italic);
        TextView boldItalic = text("Bold + italic", MoBendsTheme.TEXT_PRIMARY, 14, true);
        boldItalic.setItalic(true);
        row(col, boldItalic);
        row(col, text("Colored + size 20", MoBendsTheme.COLOR_SETTINGS, 20, false));
        TextView centered = text("Gravity center", MoBendsTheme.TEXT_PRIMARY, 14, false);
        centered.setGravity(LayoutParams.GRAVITY_CENTER);
        centered.setBackgroundColor(MoBendsTheme.BG_LIST);
        rowH(col, centered, 18);
        TextView maxLines = text("maxLines(2) wraps then clips: " + LOREM + " " + LOREM,
                MoBendsTheme.TEXT_SECONDARY, 12, false);
        maxLines.setMaxLines(2);
        row(col, maxLines);

        header(col, "Button");
        final TextView clickStatus = text("Clicked: 0", MoBendsTheme.TEXT_SECONDARY, 12, false);
        final int[] count = {0};
        Button clickBtn = new Button("Click me");
        clickBtn.setOnClickListener(() -> clickStatus.setText("Clicked: " + (++count[0])));
        rowH(col, clickBtn, MoBendsTheme.BUTTON_HEIGHT);
        row(col, clickStatus);
        Button colorBtn = new Button("Custom background color");
        colorBtn.setBackgroundColor(0xFF7A4FC0);
        rowH(col, colorBtn, MoBendsTheme.BUTTON_HEIGHT);
        Button sizeBtn = new Button("Larger text (setTextSize 20)");
        sizeBtn.setTextSize(20);
        rowH(col, sizeBtn, 40);
        Button iconBtn = new Button("Button with icon");
        iconBtn.setIcon(ResourceLocationFactory.create("mobends", "textures/gui/icons.png"));
        rowH(col, iconBtn, MoBendsTheme.BUTTON_HEIGHT);
        Button disabledBtn = new Button("Disabled button (dimmed + bordered)");
        disabledBtn.setEnabled(false);
        rowH(col, disabledBtn, MoBendsTheme.BUTTON_HEIGHT);

        header(col, "Toggle");
        final TextView toggleStatus = text("Toggle: OFF", MoBendsTheme.TEXT_SECONDARY, 12, false);
        Toggle toggle = new Toggle(false);
        toggle.setText("Enable feature");
        toggle.setOnCheckedChangeListener(v -> toggleStatus.setText("Toggle: " + (v ? "ON" : "OFF")));
        rowH(col, toggle, 20);
        row(col, toggleStatus);

        header(col, "TextField");
        final TextView echo = text("You typed: ", MoBendsTheme.TEXT_SECONDARY, 12, false);
        TextField field = new TextField("Type here (max 20 chars)...");
        field.setMaxLength(20);
        field.setOnTextChangedListener(s -> echo.setText("You typed: " + s));
        rowH(col, field, MoBendsTheme.BUTTON_HEIGHT);
        row(col, echo);

        header(col, "LinearLayout — horizontal weights 1 : 2 : 1");
        LinearLayout hrow = new LinearLayout(LinearLayout.HORIZONTAL);
        hrow.setSpacing(MoBendsTheme.SPACING);
        weighted(hrow, 0xFFE0563B, 1f);
        weighted(hrow, 0xFF3BA0E0, 2f);
        weighted(hrow, 0xFF43D9AD, 1f);
        rowH(col, hrow, 24);

        header(col, "FrameLayout — overlapping children");
        FrameLayout frame = new FrameLayout();
        View frameBg = new View();
        frameBg.setBackgroundColor(0xFF333845);
        frame.addView(frameBg, LayoutParams.matchParent());
        TextView overlay = text("centered on top", MoBendsTheme.COLOR_CUSTOMIZE, 14, true);
        frame.addView(overlay, new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).setGravity(LayoutParams.GRAVITY_CENTER));
        rowH(col, frame, 36);

        header(col, "ScrollView — mouse-wheel scrolls; bordered box");
        ScrollView inner = new ScrollView();
        inner.setBackgroundColor(MoBendsTheme.BG_LIST);
        LinearLayout innerCol = new LinearLayout(LinearLayout.VERTICAL);
        innerCol.setLayoutParams(wrapColumn());
        for (int i = 1; i <= 12; i++)
        {
            TextView r = text("scrollable row " + i, MoBendsTheme.TEXT_PRIMARY, 12, false);
            r.setPadding(4, 3, 4, 3);
            innerCol.addView(r, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        inner.addView(innerCol, wrapColumn());
        rowH(col, inner, 60);
        Button smooth = new Button("smoothScrollTo(bottom) — animated");
        smooth.setOnClickListener(() -> inner.smoothScrollTo(9999));
        rowH(col, smooth, MoBendsTheme.BUTTON_HEIGHT);
        Button smoothTop = new Button("smoothScrollTo(top) — animated");
        smoothTop.setOnClickListener(() -> inner.smoothScrollTo(0));
        rowH(col, smoothTop, MoBendsTheme.BUTTON_HEIGHT);

        header(col, "ListView — simple adapter with dividers");
        final TextView listStatus = text("Selected: (none)", MoBendsTheme.TEXT_SECONDARY, 12, false);
        ListView list = new ListView();
        List<String> items = Arrays.asList(
                "Apple", "Banana", "Cherry", "Date", "Elderberry", "Fig", "Grape", "Honeydew");
        list.setSimpleAdapter(items, (i, s) -> listStatus.setText("Selected: [" + i + "] " + s));
        list.setDividers(true, 0xFF55607A, 1);
        rowH(col, list, 80);
        row(col, listStatus);

        header(col, "Alpha / animateAlpha (animated fade)");
        TextView alphaBox = text("I can fade", MoBendsTheme.TEXT_PRIMARY, 14, true);
        alphaBox.setBackgroundColor(0xFF505870);
        rowH(col, alphaBox, 20);
        LinearLayout alphaBtns = new LinearLayout(LinearLayout.HORIZONTAL);
        alphaBtns.setSpacing(MoBendsTheme.SPACING);
        Button fade = new Button("animateAlpha 0.2");
        fade.setOnClickListener(() -> alphaBox.animateAlpha(0.2f, 400));
        Button unfade = new Button("setAlpha 1.0");
        unfade.setOnClickListener(() -> alphaBox.setAlpha(1f));
        alphaBtns.addView(fade, new LayoutParams(0, MoBendsTheme.BUTTON_HEIGHT, 1f));
        alphaBtns.addView(unfade, new LayoutParams(0, MoBendsTheme.BUTTON_HEIGHT, 1f));
        rowH(col, alphaBtns, MoBendsTheme.BUTTON_HEIGHT);

        header(col, "Visibility — GONE / VISIBLE");
        TextView toggleMe = text("Now you see me", MoBendsTheme.TEXT_PRIMARY, 14, false);
        toggleMe.setBackgroundColor(0xFF425C42);
        rowH(col, toggleMe, 18);
        Button visBtn = new Button("Toggle visibility");
        visBtn.setOnClickListener(() -> toggleMe.setVisibility(
                toggleMe.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
        rowH(col, visBtn, MoBendsTheme.BUTTON_HEIGHT);

        root.addView(col, wrapColumn());
        return root;
    }

    private static LayoutParams wrapColumn()
    {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    private static TextView text(String s, int color, int size, boolean bold)
    {
        TextView t = new TextView(s);
        t.setTextColor(color);
        t.setTextSize(size);
        t.setBold(bold);
        return t;
    }

    private static void header(LinearLayout col, String s)
    {
        TextView h = new TextView(s);
        h.setTextColor(MoBendsTheme.COLOR_PACKS);
        h.setTextSize(13);
        h.setBold(true);
        LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        p.setMargins(0, MoBendsTheme.PADDING, 0, MoBendsTheme.SPACING);
        col.addView(h, p);
    }

    private static void row(LinearLayout col, View v)
    {
        rowH(col, v, LayoutParams.WRAP_CONTENT);
    }

    private static void rowH(LinearLayout col, View v, int height)
    {
        LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, height);
        p.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        col.addView(v, p);
    }

    private static void weighted(LinearLayout row, int color, float weight)
    {
        View box = new View();
        box.setBackgroundColor(color);
        row.addView(box, new LayoutParams(0, LayoutParams.MATCH_PARENT, weight));
    }
}
