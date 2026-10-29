package goblinbob.mobends.core.client.gui.widget;

import goblinbob.mobends.compat.BetterCombatCompat;
import goblinbob.mobends.compat.PlayerAnimationLibCompat;
import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import goblinbob.mobends.core.client.gui.view.*;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.util.CustomWeapons;
import goblinbob.mobends.standard.main.ConfigOptions;
import goblinbob.mobends.standard.main.ConfigOptions.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigPanelWidget
{
    private static final String ALL_KEY = "mobends.gui.config.category.all";
    private static final String COMBAT_KEY = "mobends.gui.config.better_combat_animations";
    private static final String MOB_COMBAT_KEY = "mobends.gui.config.mob_better_combat_animations";
    private static final BetterCombatCompat.Animations COMBAT_DEFAULT = BetterCombatCompat.Animations.BETTER_COMBAT_AND_MOBENDS;

    private static final int SIDEBAR_MIN_WIDTH = 96;
    private static final int SIDEBAR_MAX_WIDTH = 160;
    private static final int NAV_HEIGHT = 16;
    private static final int TOGGLE_WIDTH = 24;
    private static final int TOGGLE_HEIGHT = 12;
    private static final int DROPDOWN_HEIGHT = 16;
    private static final int DROPDOWN_CHROME = 20;
    private static final int RESET_SIZE = 12;
    private static final int INDENT_WIDTH = 10;
    private static final int RESET_ALL_HEIGHT = 16;
    private static final int WEAPON_ADD_WIDTH = 50;
    private static final int WEAPON_REMOVE_WIDTH = 60;
    private static final int WEAPON_ROW_HEIGHT = 24;
    private static final int WEAPON_ROW_BUTTON_HEIGHT = 16;

    private static final String[] RESET_ICON = {
            "..#....",
            ".##....",
            "######.",
            ".##...#",
            "..#...#",
            "......#",
            ".#####."
    };

    private record Setting(Category category, View row, View resetButton, String searchText,
                           BooleanSupplier changed, Runnable reset, Runnable sync)
    {
    }

    private record NavItem(HoverLayout layout, TextView label, TextView count, BooleanSupplier active)
    {
        private void update()
        {
            final boolean on = active.getAsBoolean();
            layout.setBackgroundColor(on ? MoBendsTheme.BG_TAB_ACTIVE : 0);
            label.setTextColor(on ? MoBendsTheme.COLOR_SETTINGS : MoBendsTheme.TEXT_PRIMARY);
        }
    }

    private final LinearLayout root = new LinearLayout(LinearLayout.HORIZONTAL);
    private final List<Setting> settings = new ArrayList<>();
    private final Map<Category, View> sectionHeaders = new EnumMap<>(Category.class);
    private final List<NavItem> navItems = new ArrayList<>();
    private final TextField searchField = new TextField(I18n.get("mobends.gui.search"));
    private final TextView title = new TextView("");
    private final Button resetAllButton = new Button(I18n.get("mobends.gui.config.reset_all"));
    private final ScrollView settingsScroll = new ScrollView();
    private final TextView noResults = new TextView(I18n.get("mobends.gui.config.no_results"));
    private final LinearLayout weaponsView = new LinearLayout(LinearLayout.VERTICAL);
    private final LinearLayout weaponList = new LinearLayout(LinearLayout.VERTICAL);
    private final TextField weaponField = new TextField("");
    private NavItem weaponsNav;

    @Nullable
    private Category selectedCategory;
    private boolean weaponsSelected;
    private String query = "";

    public ConfigPanelWidget(Runnable onBack)
    {
        root.setLayoutParams(LayoutParams.matchParent());

        final LinearLayout list = buildSettingsList();

        root.addView(buildSidebar(onBack), new LayoutParams(sidebarWidth(), LayoutParams.MATCH_PARENT));

        final View divider = new View();
        divider.setBackgroundColor(MoBendsTheme.BORDER);
        root.addView(divider, new LayoutParams(1, LayoutParams.MATCH_PARENT));

        root.addView(buildMain(list), new LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f));

        refreshWeaponList();
        refresh();
    }

    public View getView()
    {
        return root;
    }

    public void onShown()
    {
        refreshWeaponList();
        refresh();
    }

    public void clearSearch()
    {
        if (query.isEmpty()) return;

        query = "";
        searchField.setText("");
        refresh();
    }

    private boolean isSearching()
    {
        return !query.trim().isEmpty();
    }

    private int sidebarWidth()
    {
        final Font font = Minecraft.getInstance().font;
        int widest = Math.max(font.width(I18n.get(ALL_KEY)), font.width(I18n.get("mobends.gui.weapons")));

        for (Category category : Category.values())
        {
            widest = Math.max(widest, font.width(I18n.get(category.getTranslationKey())));
        }

        return Math.min(SIDEBAR_MAX_WIDTH, Math.max(SIDEBAR_MIN_WIDTH, widest + font.width("00") + MoBendsTheme.PADDING * 4));
    }

    private View buildSidebar(Runnable onBack)
    {
        final LinearLayout sidebar = new LinearLayout(LinearLayout.VERTICAL);
        sidebar.setBackgroundColor(MoBendsTheme.BG_PANEL);
        sidebar.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING, MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        searchField.setOnTextChangedListener(text -> {
            query = text == null ? "" : text;
            settingsScroll.scrollTo(0);
            refresh();
        });
        final LayoutParams searchParams = new LayoutParams(LayoutParams.MATCH_PARENT, MoBendsTheme.BUTTON_HEIGHT);
        searchParams.setMargins(0, 0, 0, MoBendsTheme.PADDING);
        sidebar.addView(searchField, searchParams);

        addNav(sidebar, I18n.get(ALL_KEY), settings.size(),
                () -> !isSearching() && !weaponsSelected && selectedCategory == null,
                () -> select(null, false));

        for (Category category : Category.values())
        {
            final int count = (int) settings.stream().filter(setting -> setting.category() == category).count();
            addNav(sidebar, I18n.get(category.getTranslationKey()), count,
                    () -> !isSearching() && !weaponsSelected && selectedCategory == category,
                    () -> select(category, false));
        }

        final View divider = new View();
        divider.setBackgroundColor(MoBendsTheme.BORDER);
        final LayoutParams dividerParams = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
        dividerParams.setMargins(MoBendsTheme.PADDING_SMALL, MoBendsTheme.SPACING,
                MoBendsTheme.PADDING_SMALL, MoBendsTheme.SPACING);
        sidebar.addView(divider, dividerParams);

        weaponsNav = addNav(sidebar, I18n.get("mobends.gui.weapons"), 0,
                () -> !isSearching() && weaponsSelected,
                () -> select(null, true));

        sidebar.addView(new View(), new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f));

        final Button back = new Button(I18n.get("mobends.gui.back"));
        back.setOnClickListener(onBack);
        sidebar.addView(back, new LayoutParams(LayoutParams.MATCH_PARENT, MoBendsTheme.BUTTON_HEIGHT));

        return sidebar;
    }

    private NavItem addNav(LinearLayout sidebar, String text, int count, BooleanSupplier active, Runnable onClick)
    {
        final HoverLayout layout = new HoverLayout(LinearLayout.HORIZONTAL);
        layout.setGravity(LayoutParams.GRAVITY_CENTER_VERTICAL);
        layout.setPadding(MoBendsTheme.PADDING, 0, MoBendsTheme.PADDING, 0);
        layout.setOnClickListener(onClick);

        final TextView label = new TextView(text);
        label.setMaxLines(1);
        layout.addView(label, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));

        final TextView countView = new TextView(String.valueOf(count));
        countView.setTextSize(10);
        countView.setTextColor(MoBendsTheme.TEXT_SECONDARY);
        layout.addView(countView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, NAV_HEIGHT);
        params.setMargins(0, 0, 0, 1);
        sidebar.addView(layout, params);

        final NavItem item = new NavItem(layout, label, countView, active);
        navItems.add(item);
        return item;
    }

    private void select(@Nullable Category category, boolean weapons)
    {
        selectedCategory = category;
        weaponsSelected = weapons;
        query = "";
        searchField.setText("");
        settingsScroll.scrollTo(0);
        refresh();
    }

    private View buildMain(LinearLayout list)
    {
        final LinearLayout main = new LinearLayout(LinearLayout.VERTICAL);

        final LinearLayout header = new LinearLayout(LinearLayout.HORIZONTAL);
        header.setGravity(LayoutParams.GRAVITY_CENTER_VERTICAL);

        title.setTextSize(14);
        title.setBold(true);
        title.setMaxLines(1);
        header.addView(title, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));

        resetAllButton.setTextSize(10);
        resetAllButton.setOnClickListener(this::resetVisible);
        header.addView(resetAllButton, new LayoutParams(LayoutParams.WRAP_CONTENT, RESET_ALL_HEIGHT));

        final LayoutParams headerParams = new LayoutParams(LayoutParams.MATCH_PARENT, MoBendsTheme.BUTTON_HEIGHT);
        headerParams.setMargins(MoBendsTheme.PADDING, MoBendsTheme.PADDING, MoBendsTheme.PADDING, MoBendsTheme.SPACING);
        main.addView(header, headerParams);

        settingsScroll.addView(list, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        noResults.setTextColor(MoBendsTheme.TEXT_SECONDARY);
        noResults.setGravity(LayoutParams.GRAVITY_CENTER_HORIZONTAL);

        final FrameLayout body = new FrameLayout();
        body.addView(settingsScroll, LayoutParams.matchParent());
        body.addView(buildWeaponsView(), LayoutParams.matchParent());
        final LayoutParams noResultsParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        noResultsParams.setMargins(0, MoBendsTheme.PADDING_LARGE * 2, 0, 0);
        body.addView(noResults, noResultsParams);

        final LayoutParams bodyParams = LayoutParams.matchParent();
        bodyParams.setMargins(MoBendsTheme.PADDING, 0, MoBendsTheme.PADDING, MoBendsTheme.PADDING);
        main.addView(body, bodyParams);

        return main;
    }

    private LinearLayout buildSettingsList()
    {
        final LinearLayout list = new LinearLayout(LinearLayout.VERTICAL);
        list.setPadding(MoBendsTheme.PADDING, 0, MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        for (Category category : Category.values())
        {
            final View header = buildSectionHeader(I18n.get(category.getTranslationKey()));
            final LayoutParams headerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            headerParams.setMargins(MoBendsTheme.PADDING_SMALL, MoBendsTheme.PADDING + 1, 0, MoBendsTheme.SPACING);
            list.addView(header, headerParams);
            sectionHeaders.put(category, header);

            for (ConfigOptions.Option option : ConfigOptions.all())
            {
                if (option.getCategory() == category)
                {
                    addOptionRow(list, option);
                }
            }

            if (category == Category.COMBAT && BetterCombatCompat.isModLoaded())
            {
                addModeRow(list, COMBAT_KEY, BetterCombatCompat.Animations.values(),
                        BetterCombatCompat::getAnimations, BetterCombatCompat::setAnimations);
            }

            if (category == Category.COMBAT && PlayerAnimationLibCompat.isMobAnimatorLoaded())
            {
                addModeRow(list, MOB_COMBAT_KEY, BetterCombatCompat.MOB_ANIMATIONS,
                        BetterCombatCompat::getMobAnimations, BetterCombatCompat::setMobAnimations);
            }
        }

        return list;
    }

    private static View buildSectionHeader(String name)
    {
        final LinearLayout header = new LinearLayout(LinearLayout.HORIZONTAL);
        header.setGravity(LayoutParams.GRAVITY_CENTER_VERTICAL);

        final TextView label = new TextView(name);
        label.setTextColor(MoBendsTheme.COLOR_SETTINGS);
        label.setTextSize(11);
        label.setBold(true);
        header.addView(label, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        final View line = new View();
        line.setBackgroundColor(MoBendsTheme.BORDER);
        final LayoutParams lineParams = new LayoutParams(0, 1, 1.0f);
        lineParams.setMargins(MoBendsTheme.PADDING, 0, 0, 0);
        header.addView(line, lineParams);

        return header;
    }

    private static HoverLayout newRow()
    {
        final HoverLayout row = new HoverLayout(LinearLayout.HORIZONTAL);
        row.setGravity(LayoutParams.GRAVITY_CENTER_VERTICAL);
        row.setBackgroundColor(MoBendsTheme.BG_LIST);
        row.setPadding(MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING, MoBendsTheme.PADDING, MoBendsTheme.PADDING);
        return row;
    }

    private static void addTextColumn(HoverLayout row, TextView label, TextView description)
    {
        final LinearLayout column = new LinearLayout(LinearLayout.VERTICAL);

        label.setMaxLines(1);
        column.addView(label, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        description.setTextSize(10);
        description.setTextColor(MoBendsTheme.TEXT_SECONDARY);
        final LayoutParams descriptionParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        descriptionParams.setMargins(0, MoBendsTheme.PADDING_SMALL, 0, 0);
        column.addView(description, descriptionParams);

        final LayoutParams columnParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        columnParams.setMargins(0, 0, MoBendsTheme.PADDING, 0);
        row.addView(column, columnParams);
    }

    private void addOptionRow(LinearLayout list, ConfigOptions.Option option)
    {
        final ConfigOptions.Option parent = option.getParent();
        final HoverLayout row = newRow();
        final TextView label = new TextView(I18n.get(option.getTranslationKey()));
        final TextView description = new TextView(I18n.get(option.getDescriptionKey()));

        if (parent != null)
        {
            final LayoutParams indentParams = new LayoutParams(INDENT_WIDTH, LayoutParams.MATCH_PARENT);
            indentParams.setMargins(0, 0, MoBendsTheme.SPACING, 0);
            row.addView(buildIndent(), indentParams);
        }

        addTextColumn(row, label, description);

        final Toggle toggle = new Toggle(option.get());
        toggle.setToggleSize(TOGGLE_WIDTH, TOGGLE_HEIGHT);
        toggle.setOnCheckedChangeListener(value -> {
            option.set(value);
            refresh();
        });

        final String parentLabel = parent == null ? "" : I18n.get(parent.getTranslationKey()) + " ";

        addSetting(list, row, toggle, new LayoutParams(TOGGLE_WIDTH + 4, TOGGLE_HEIGHT + 4),
                new Setting(option.getCategory(), row, null,
                        parentLabel + label.getText() + " " + description.getText(),
                        () -> option.get() != option.getDefault(),
                        () -> option.set(option.getDefault()),
                        () -> {
                            final boolean active = parent == null || parent.get();
                            toggle.setChecked(option.get());
                            toggle.setEnabled(active);
                            row.setOnClickListener(active ? toggle::toggle : null);
                            label.setTextColor(active ? MoBendsTheme.TEXT_PRIMARY : MoBendsTheme.TEXT_DISABLED);
                            description.setTextColor(active ? MoBendsTheme.TEXT_SECONDARY : MoBendsTheme.TEXT_DISABLED);
                        }));
    }

    private void addModeRow(LinearLayout list, String key, BetterCombatCompat.Animations[] modes,
                            Supplier<BetterCombatCompat.Animations> getter,
                            Consumer<BetterCombatCompat.Animations> setter)
    {
        final HoverLayout row = newRow();
        final TextView label = new TextView(I18n.get(key));
        final TextView description = new TextView("");
        addTextColumn(row, label, description);

        final Font font = Minecraft.getInstance().font;
        final DropDown dropDown = new DropDown("");
        int widest = 0;

        for (BetterCombatCompat.Animations mode : modes)
        {
            final String name = I18n.get(modeKey(key, mode));
            dropDown.addOption(name, I18n.get(modeKey(key, mode) + ".desc"));
            widest = Math.max(widest, font.width(name));
        }

        dropDown.setMinimumWidth(widest + DROPDOWN_CHROME);
        dropDown.setOnSelectionChanged(index -> {
            setter.accept(modes[index]);
            refresh();
        });

        addSetting(list, row, dropDown, new LayoutParams(LayoutParams.WRAP_CONTENT, DROPDOWN_HEIGHT),
                new Setting(Category.COMBAT, row, null,
                        label.getText() + " " + I18n.get(key + ".desc"),
                        () -> getter.get() != COMBAT_DEFAULT,
                        () -> setter.accept(COMBAT_DEFAULT),
                        () -> {
                            final BetterCombatCompat.Animations current = getter.get();
                            dropDown.setSelectedIndex(Arrays.asList(modes).indexOf(current));
                            dropDown.setLabel(I18n.get(modeKey(key, current)));
                            description.setText(I18n.get(modeKey(key, current) + ".desc"));
                        }));
    }

    private static String modeKey(String key, BetterCombatCompat.Animations mode)
    {
        return key + "." + mode.name().toLowerCase(Locale.ROOT);
    }

    private void addSetting(LinearLayout list, HoverLayout row, View control, LayoutParams controlParams, Setting setting)
    {
        final View resetButton = buildResetButton(() -> {
            setting.reset().run();
            refresh();
        });

        final LayoutParams resetParams = new LayoutParams(RESET_SIZE, RESET_SIZE);
        resetParams.setMargins(0, 0, MoBendsTheme.SPACING, 0);
        row.addView(resetButton, resetParams);
        row.addView(control, controlParams);

        final LayoutParams rowParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, MoBendsTheme.PADDING_SMALL);
        list.addView(row, rowParams);

        settings.add(new Setting(setting.category(), row, resetButton, setting.searchText().toLowerCase(Locale.ROOT),
                setting.changed(), setting.reset(), setting.sync()));
    }

    private static View buildIndent()
    {
        return new View()
        {
            @Override
            public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
            {
                if (visibility != VISIBLE) return;

                final int lineX = x + 3;
                final int middle = y + measuredHeight / 2;
                guiGraphics.fill(lineX, y - MoBendsTheme.PADDING, lineX + 1, middle + 1, MoBendsTheme.SCROLLBAR_THUMB);
                guiGraphics.fill(lineX, middle, x + measuredWidth, middle + 1, MoBendsTheme.SCROLLBAR_THUMB);
            }
        };
    }

    private static View buildResetButton(Runnable onReset)
    {
        final String tooltip = I18n.get("mobends.gui.config.reset");

        final View button = new View()
        {
            @Override
            public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
            {
                if (visibility != VISIBLE) return;

                if (isInBounds(mouseX, mouseY))
                {
                    guiGraphics.fill(x, y, x + measuredWidth, y + measuredHeight, MoBendsTheme.BG_BUTTON_HOVER);
                    GuiTooltip.request(tooltip);
                }

                final int left = x + (measuredWidth - RESET_ICON[0].length()) / 2;
                final int top = y + (measuredHeight - RESET_ICON.length) / 2;

                for (int row = 0; row < RESET_ICON.length; row++)
                {
                    for (int column = 0; column < RESET_ICON[row].length(); column++)
                    {
                        if (RESET_ICON[row].charAt(column) == '#')
                        {
                            guiGraphics.fill(left + column, top + row, left + column + 1, top + row + 1,
                                    MoBendsTheme.COLOR_SETTINGS);
                        }
                    }
                }
            }
        };
        button.setOnClickListener(onReset);
        return button;
    }

    private void resetVisible()
    {
        for (Setting setting : settings)
        {
            if (setting.row().getVisibility() == View.VISIBLE && setting.changed().getAsBoolean())
            {
                setting.reset().run();
            }
        }

        refresh();
    }

    private void refresh()
    {
        final String needle = query.trim().toLowerCase(Locale.ROOT);
        final boolean searching = !needle.isEmpty();
        final boolean showWeapons = weaponsSelected && !searching;
        final Set<Category> shownCategories = EnumSet.noneOf(Category.class);
        boolean anyChanged = false;

        for (Setting setting : settings)
        {
            setting.sync().run();

            final boolean changed = setting.changed().getAsBoolean();
            setting.resetButton().setVisibility(changed ? View.VISIBLE : View.INVISIBLE);

            final boolean shown = !showWeapons && (searching
                    ? setting.searchText().contains(needle)
                    : selectedCategory == null || selectedCategory == setting.category());
            setting.row().setVisibility(shown ? View.VISIBLE : View.GONE);

            if (shown)
            {
                shownCategories.add(setting.category());
                anyChanged |= changed;
            }
        }

        final boolean showHeaders = searching || selectedCategory == null;
        sectionHeaders.forEach((category, header) -> header.setVisibility(
                showHeaders && shownCategories.contains(category) ? View.VISIBLE : View.GONE));

        settingsScroll.setVisibility(showWeapons ? View.GONE : View.VISIBLE);
        weaponsView.setVisibility(showWeapons ? View.VISIBLE : View.GONE);
        noResults.setVisibility(!showWeapons && shownCategories.isEmpty() ? View.VISIBLE : View.GONE);
        resetAllButton.setVisibility(showWeapons ? View.GONE : View.VISIBLE);
        resetAllButton.setEnabled(anyChanged);

        if (searching)
        {
            title.setText(I18n.get("mobends.gui.config.search_results"));
        }
        else if (showWeapons)
        {
            title.setText(I18n.get("mobends.gui.weapons"));
        }
        else
        {
            title.setText(I18n.get(selectedCategory == null ? ALL_KEY : selectedCategory.getTranslationKey()));
        }

        navItems.forEach(NavItem::update);
    }

    private View buildWeaponsView()
    {
        weaponsView.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING, MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        final LinearLayout toolbar = new LinearLayout(LinearLayout.HORIZONTAL);
        toolbar.setGravity(LayoutParams.GRAVITY_CENTER_VERTICAL);

        weaponField.setOnSubmitListener(this::addWeaponFromField);
        final LayoutParams fieldParams = new LayoutParams(0, MoBendsTheme.BUTTON_HEIGHT, 1.0f);
        fieldParams.setMargins(0, 0, MoBendsTheme.PADDING, 0);
        toolbar.addView(weaponField, fieldParams);

        final Button addButton = new Button(I18n.get("mobends.gui.weapons.add"));
        addButton.setOnClickListener(this::addWeaponFromField);
        toolbar.addView(addButton, new LayoutParams(WEAPON_ADD_WIDTH, MoBendsTheme.BUTTON_HEIGHT));

        final LayoutParams toolbarParams = new LayoutParams(LayoutParams.MATCH_PARENT, MoBendsTheme.BUTTON_HEIGHT);
        toolbarParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        weaponsView.addView(toolbar, toolbarParams);

        final TextView hint = new TextView(I18n.get("mobends.gui.weapons.hint"));
        hint.setTextColor(MoBendsTheme.TEXT_SECONDARY);
        hint.setTextSize(10);
        final LayoutParams hintParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        hintParams.setMargins(0, 0, 0, MoBendsTheme.PADDING);
        weaponsView.addView(hint, hintParams);

        final ScrollView scrollView = new ScrollView();
        weaponList.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        scrollView.addView(weaponList, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        weaponsView.addView(scrollView, LayoutParams.matchParent());

        return weaponsView;
    }

    private void addWeaponFromField()
    {
        final String itemId = CustomWeapons.normalize(weaponField.getText());
        if (itemId == null) return;

        if (CoreClientConfig.getInstance().addCustomWeapon(itemId))
        {
            CustomWeapons.invalidate();
            weaponField.setText("");
            Minecraft.getInstance().tell(this::refreshWeaponList);
        }
    }

    private void removeWeapon(String itemId)
    {
        if (CoreClientConfig.getInstance().removeCustomWeapon(itemId))
        {
            CustomWeapons.invalidate();
            Minecraft.getInstance().tell(this::refreshWeaponList);
        }
    }

    private void refreshWeaponList()
    {
        weaponList.removeAllViews();

        final List<String> weapons = CoreClientConfig.getInstance().getCustomWeapons();
        weaponsNav.count().setText(String.valueOf(weapons.size()));

        if (weapons.isEmpty())
        {
            final TextView empty = new TextView(I18n.get("mobends.gui.weapons.empty"));
            empty.setTextColor(MoBendsTheme.TEXT_SECONDARY);
            empty.setTextSize(10);
            empty.setPadding(MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING,
                    MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING);
            weaponList.addView(empty, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            return;
        }

        for (String weapon : weapons)
        {
            final LinearLayout row = new LinearLayout(LinearLayout.HORIZONTAL);
            row.setGravity(LayoutParams.GRAVITY_CENTER_VERTICAL);
            row.setBackgroundColor(MoBendsTheme.BG_LIST);
            row.setPadding(MoBendsTheme.PADDING_LARGE, 0, MoBendsTheme.PADDING, 0);

            final TextView label = new TextView(weapon);
            label.setTextColor(MoBendsTheme.TEXT_PRIMARY);
            label.setTextSize(11);
            label.setMaxLines(1);
            row.addView(label, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));

            final Button remove = new Button(I18n.get("mobends.gui.weapons.remove"));
            remove.setTextSize(10);
            remove.setOnClickListener(() -> removeWeapon(weapon));
            row.addView(remove, new LayoutParams(WEAPON_REMOVE_WIDTH, WEAPON_ROW_BUTTON_HEIGHT));

            final LayoutParams rowParams = new LayoutParams(LayoutParams.MATCH_PARENT, WEAPON_ROW_HEIGHT);
            rowParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
            weaponList.addView(row, rowParams);
        }
    }
}
