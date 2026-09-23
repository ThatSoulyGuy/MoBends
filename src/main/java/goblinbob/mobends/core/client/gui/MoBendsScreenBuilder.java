package goblinbob.mobends.core.client.gui;

import goblinbob.mobends.core.client.gui.vanilla.*;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import goblinbob.mobends.core.client.gui.widget.EntityPreviewWidget;
import goblinbob.mobends.core.client.gui.widget.MobPreviewGridWidget;
import goblinbob.mobends.core.client.gui.widget.PackListWidget;
import goblinbob.mobends.core.client.gui.widget.TabBarWidget;
import goblinbob.mobends.core.client.gui.widget.UIGalleryWidget;
import goblinbob.mobends.api.platform.PlatformServices;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.pack.IBendsPack;
import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.util.CustomWeapons;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import goblinbob.mobends.standard.main.ConfigOptions;
import net.minecraft.client.resources.language.I18n;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MoBendsScreenBuilder
{
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    private static final int TAB_SETTINGS = 0;
    private static final int TAB_PACKS = 1;
    private static final int TAB_CUSTOMIZE = 2;

    private static final int SUB_CHOOSER = 0;
    private static final int SUB_ANIMATIONS = 1;
    private static final int SUB_CONFIG = 2;
    private static final int SUB_WEAPONS = 3;

    private static final int COGWHEEL_TEXTURE_SIZE = 512;
    private static final long CHOOSER_CYCLE_MS = 2500L;
    private static final float CHOOSER_PREVIEW_SCALE = 3.0F;

    private static final int CONFIG_ROW_HEIGHT = 46;
    private static final int CONFIG_TOGGLE_WIDTH = 40;
    private static final int CONFIG_TOGGLE_HEIGHT = 20;

    private static final int WEAPONS_BUTTON_WIDTH = 70;
    private static final int CONFIG_SEARCH_FIELD_WIDTH = 140;
    private static final int WEAPON_ADD_WIDTH = 50;
    private static final int WEAPON_REMOVE_WIDTH = 60;
    private static final int WEAPON_ROW_HEIGHT = 24;
    private static final int WEAPON_ROW_BUTTON_HEIGHT = 16;

    private static final int SEARCH_FIELD_WIDTH = 90;
    private static final int CHIP_WIDTH = 54;
    private static final int CHIP_TEXT_SIZE = 10;

    private static final int COLOR_SETTINGS = MoBendsTheme.COLOR_SETTINGS;
    private static final int COLOR_PACKS = MoBendsTheme.COLOR_PACKS;
    private static final int COLOR_CUSTOMIZE = MoBendsTheme.COLOR_CUSTOMIZE;

    private TabBarWidget tabBar;
    private MobPreviewGridWidget mobGrid;
    private final Map<String, VanillaButton> animationChips = new LinkedHashMap<>();
    private PackListWidget packList;
    private VanillaFrameLayout contentFrame;
    private VanillaView settingsContent;
    private VanillaView packsContent;
    private VanillaView customizeContent;
    private VanillaView galleryContent;
    private int galleryTabIndex = -1;
    private VanillaTextField searchField;
    private VanillaTextField packSearchField;
    private VanillaFrameLayout settingsFrame;
    private VanillaView settingsChooser;
    private VanillaView animationsContent;
    private VanillaView configContent;
    private VanillaView weaponsContent;
    private VanillaViewFactory weaponFactory;
    private VanillaLinearLayout weaponList;
    private VanillaTextField weaponField;
    private VanillaTextField configSearchField;
    private final java.util.List<ConfigRow> configRows = new java.util.ArrayList<>();
    private EntityPreviewWidget chooserPreview;
    private int chooserBenderIndex;
    private boolean chooserStarted;
    private long chooserLastCycle;
    private boolean openConfigOnBuild;
    private final EntityBenderRegistry.Filter filter = new EntityBenderRegistry.Filter();

    public String getTitle()
    {
        return I18n.get("mobends.gui.title");
    }

    public void dispose()
    {
        if (mobGrid != null)
        {
            mobGrid.dispose();
        }
        if (chooserPreview != null)
        {
            chooserPreview.getRenderer().dispose();
        }
    }

    public VanillaView buildContent(VanillaViewFactory factory)
    {
        VanillaLinearLayout root = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        root.setLayoutParams(factory.createFrameLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.GRAVITY_CENTER
        ));
        root.setBackgroundColor(MoBendsTheme.BG_PANEL);

        VanillaLinearLayout header = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        header.setLayoutParams(factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                MoBendsTheme.HEADER_HEIGHT
        ));
        header.setBackgroundColor(MoBendsTheme.BG_HEADER);
        header.setGravity(VanillaLinearLayout.GRAVITY_CENTER);
        header.setPadding(0, MoBendsTheme.PADDING, 0, 0);

        VanillaTextView title = factory.createTextView(I18n.get("mobends.gui.title"));
        title.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        title.setTextSize(15);
        title.setBold(true);
        title.setGravity(VanillaLinearLayout.GRAVITY_CENTER);
        header.addView(title, factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.WRAP_CONTENT
        ));

        root.addView(header, factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                MoBendsTheme.HEADER_HEIGHT
        ));

        tabBar = new TabBarWidget(factory);
        tabBar.addTab("mobends.gui.section.settings", COLOR_SETTINGS);
        if (NetworkConfiguration.instance.areBendsPacksAllowed())
        {
            tabBar.addTab("mobends.gui.section.packs", COLOR_PACKS);
        }
        tabBar.addTab("mobends.gui.section.customize", COLOR_CUSTOMIZE);
        boolean devMode = isDevEnvironment();
        if (devMode)
        {
            tabBar.addTab("UI Test", MoBendsTheme.ACCENT_ERROR);
            galleryTabIndex = NetworkConfiguration.instance.areBendsPacksAllowed() ? 3 : 2;
        }
        tabBar.setOnTabChanged(this::onTabChanged);

        VanillaLayoutParams tabParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                MoBendsTheme.TAB_HEIGHT
        );
        root.addView(tabBar.getView(), tabParams);

        contentFrame = factory.createFrameLayout();
        contentFrame.setLayoutParams(factory.createMatchParent());
        contentFrame.setBackgroundColor(MoBendsTheme.BG_CONTENT);

        settingsContent = buildSettingsTab(factory);
        packsContent = withWipOverlay(factory, buildPacksContent(factory));
        customizeContent = withWipOverlay(factory, buildCustomizeContent(factory));

        contentFrame.addView(settingsContent, factory.createMatchParent());
        contentFrame.addView(packsContent, factory.createMatchParent());
        contentFrame.addView(customizeContent, factory.createMatchParent());
        if (devMode)
        {
            galleryContent = UIGalleryWidget.build(factory);
            contentFrame.addView(galleryContent, factory.createMatchParent());
        }

        showTab(TAB_SETTINGS);

        if (openConfigOnBuild)
        {
            showSettingsSubView(SUB_CONFIG);
        }

        root.addView(contentFrame, factory.createMatchParent());

        return root;
    }

    public void setOpenConfigOnBuild(boolean openConfigOnBuild)
    {
        this.openConfigOnBuild = openConfigOnBuild;
    }

    private VanillaView buildAnimationsContent(VanillaViewFactory factory)
    {
        VanillaLinearLayout layout = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        layout.setLayoutParams(factory.createMatchParent());
        layout.setPadding(MoBendsTheme.PADDING, 0, MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        mobGrid = new MobPreviewGridWidget(factory);
        mobGrid.populateFromRegistry(filter);

        VanillaLinearLayout toolbar = factory.createLinearLayout(VanillaViewFactory.HORIZONTAL);
        toolbar.setGravity(VanillaLinearLayout.GRAVITY_CENTER_VERTICAL);

        searchField = factory.createTextField(I18n.get("mobends.gui.search"));
        searchField.setOnTextChangedListener(this::onSearchTextChanged);
        VanillaLayoutParams searchParams = factory.createLayoutParams(
                SEARCH_FIELD_WIDTH,
                MoBendsTheme.BUTTON_HEIGHT
        );
        searchParams.setMargins(0, 0, MoBendsTheme.PADDING, 0);
        toolbar.addView(searchField, searchParams);

        VanillaTextView hint = factory.createTextView(I18n.get("mobends.gui.animations.hint"));
        hint.setTextColor(MoBendsTheme.TEXT_HINT);
        hint.setTextSize(10);
        toolbar.addView(hint, factory.createLayoutParams(
                0, VanillaLayoutParams.WRAP_CONTENT, 1.0f));

        VanillaLayoutParams toolbarParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                MoBendsTheme.BUTTON_HEIGHT
        );
        toolbarParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        layout.addView(toolbar, toolbarParams);

        VanillaLayoutParams chipParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.WRAP_CONTENT
        );
        chipParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        layout.addView(buildAnimationChips(), chipParams);

        layout.addView(mobGrid.getView(), factory.createMatchParent());

        return layout;
    }

    private VanillaView buildAnimationChips()
    {
        VanillaGridLayout chipGrid = new VanillaGridLayout();
        chipGrid.setCellSize(CHIP_WIDTH, MoBendsTheme.BUTTON_HEIGHT);
        chipGrid.setSpacing(MoBendsTheme.SPACING, MoBendsTheme.SPACING);

        animationChips.clear();

        for (String animation : mobGrid.getAvailableAnimations())
        {
            VanillaButton chip = new VanillaButton(getAnimationLabel(animation));
            chip.setTextSize(CHIP_TEXT_SIZE);
            chip.setOnClickListener(() -> onAnimationSelected(animation));
            animationChips.put(animation, chip);

            chipGrid.addView(chip);
        }

        applyAnimationChipStyles();

        return chipGrid;
    }

    private static String getAnimationLabel(String animation)
    {
        String key = "mobends.animation." + animation;
        return I18n.exists(key) ? I18n.get(key) : animation;
    }

    private void applyAnimationChipStyles()
    {
        String selected = mobGrid.getAnimationType();

        for (Map.Entry<String, VanillaButton> entry : animationChips.entrySet())
        {
            boolean active = entry.getKey().equals(selected);
            VanillaButton chip = entry.getValue();

            chip.setBackgroundColor(active ? MoBendsTheme.TOGGLE_ON : MoBendsTheme.BG_BUTTON);
            chip.setTextColor(active ? MoBendsTheme.BG_HEADER : MoBendsTheme.TEXT_PRIMARY);
            chip.setTextShadow(!active);
        }
    }

    private VanillaView buildSettingsTab(VanillaViewFactory factory)
    {
        settingsFrame = factory.createFrameLayout();
        settingsFrame.setLayoutParams(factory.createMatchParent());

        settingsChooser = buildSettingsChooser(factory);

        VanillaView animations = buildAnimationsContent(factory);
        animationsContent = withBackHeader(factory, animations, buildSpinDropDown());

        configContent = withBackHeader(factory, buildConfigContent(factory), buildWeaponsButton(factory));

        weaponsContent = withBackHeader(factory, buildWeaponsContent(factory), null, SUB_CONFIG);

        settingsFrame.addView(settingsChooser, factory.createMatchParent());
        settingsFrame.addView(animationsContent, factory.createMatchParent());
        settingsFrame.addView(configContent, factory.createMatchParent());
        settingsFrame.addView(weaponsContent, factory.createMatchParent());

        showSettingsSubView(SUB_CHOOSER);

        return settingsFrame;
    }

    private VanillaView buildSettingsChooser(VanillaViewFactory factory)
    {
        VanillaLinearLayout layout = factory.createLinearLayout(VanillaViewFactory.HORIZONTAL);
        layout.setLayoutParams(factory.createMatchParent());
        layout.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                         MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        chooserPreview = new EntityPreviewWidget(factory, 0, 0);
        chooserPreview.setChromeVisible(false);
        chooserPreview.setInteractive(false);
        chooserPreview.getView().setBackgroundColor(0);
        chooserPreview.setScaleMultiplier(CHOOSER_PREVIEW_SCALE);
        applyChooserBender();

        VanillaTileView animationsTile = buildTile(factory, chooserPreview.getView(),
                I18n.get("mobends.gui.settings.animations"),
                () -> showSettingsSubView(SUB_ANIMATIONS));
        animationsTile.setTicker(this::tickChooserPreview);

        VanillaIconView cogwheel = new VanillaIconView(
                ResourceLocationFactory.create("mobends", "textures/gui/cogwheel.png"),
                COGWHEEL_TEXTURE_SIZE);
        cogwheel.setIconSize(96);
        cogwheel.setSpinning(true);

        VanillaTileView configTile = buildTile(factory, cogwheel,
                I18n.get("mobends.gui.settings.config"),
                () -> showSettingsSubView(SUB_CONFIG));
        cogwheel.setHoverSupplier(configTile::isHovered);

        VanillaLayoutParams leftParams = factory.createLayoutParams(
                0, VanillaLayoutParams.MATCH_PARENT, 1.0f);
        leftParams.setMargins(0, 0, MoBendsTheme.PADDING_LARGE * 2, 0);
        layout.addView(animationsTile, leftParams);
        layout.addView(configTile, factory.createLayoutParams(
                0, VanillaLayoutParams.MATCH_PARENT, 1.0f));

        return layout;
    }

    private VanillaTileView buildTile(VanillaViewFactory factory, VanillaView content, String label, Runnable onClick)
    {
        VanillaTileView tile = new VanillaTileView();
        tile.setOrientation(VanillaViewFactory.VERTICAL);
        tile.setGravity(VanillaLinearLayout.GRAVITY_CENTER);
        tile.setPadding(MoBendsTheme.SPACING, MoBendsTheme.SPACING,
                       MoBendsTheme.SPACING, MoBendsTheme.SPACING);
        tile.setOnClickListener(onClick);

        tile.addView(content, factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT, 0, 1.0f));

        VanillaTextView labelView = factory.createTextView(label);
        labelView.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        labelView.setTextSize(12);
        labelView.setBold(true);
        labelView.setGravity(VanillaLinearLayout.GRAVITY_CENTER);
        tile.addView(labelView, factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT));

        return tile;
    }

    private void tickChooserPreview()
    {
        long now = System.currentTimeMillis();

        if (chooserLastCycle == 0L)
        {
            chooserLastCycle = now;
            return;
        }

        if (now - chooserLastCycle < CHOOSER_CYCLE_MS) return;

        chooserLastCycle = now;
        chooserBenderIndex++;
        applyChooserBender();
    }

    private void applyChooserBender()
    {
        if (chooserPreview == null) return;

        java.util.List<EntityBender<?>> benders =
                new java.util.ArrayList<>(EntityBenderRegistry.instance.getRegistered());
        if (benders.isEmpty()) return;

        if (!chooserStarted)
        {
            chooserStarted = true;
            chooserBenderIndex = (int) (Math.random() * benders.size());
        }

        chooserPreview.setBender(benders.get(Math.floorMod(chooserBenderIndex, benders.size())));
    }

    private VanillaView buildConfigContent(VanillaViewFactory factory)
    {
        configRows.clear();

        VanillaLinearLayout layout = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        layout.setLayoutParams(factory.createMatchParent());

        VanillaLinearLayout toolbar = factory.createLinearLayout(VanillaViewFactory.HORIZONTAL);
        toolbar.setGravity(VanillaLinearLayout.GRAVITY_CENTER_VERTICAL);

        configSearchField = factory.createTextField(I18n.get("mobends.gui.search"));
        configSearchField.setOnTextChangedListener(this::onConfigSearchTextChanged);
        VanillaLayoutParams searchParams = factory.createLayoutParams(
                CONFIG_SEARCH_FIELD_WIDTH,
                MoBendsTheme.BUTTON_HEIGHT
        );
        searchParams.setMargins(0, 0, MoBendsTheme.PADDING, 0);
        toolbar.addView(configSearchField, searchParams);

        VanillaTextView hint = factory.createTextView(I18n.get("mobends.gui.config.search.hint"));
        hint.setTextColor(MoBendsTheme.TEXT_HINT);
        hint.setTextSize(10);
        toolbar.addView(hint, factory.createLayoutParams(
                0, VanillaLayoutParams.WRAP_CONTENT, 1.0f));

        VanillaLayoutParams toolbarParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                MoBendsTheme.BUTTON_HEIGHT
        );
        toolbarParams.setMargins(MoBendsTheme.PADDING, 0, MoBendsTheme.PADDING, MoBendsTheme.SPACING);
        layout.addView(toolbar, toolbarParams);

        VanillaScrollView scrollView = factory.createScrollView();
        scrollView.setLayoutParams(factory.createMatchParent());

        VanillaLinearLayout list = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        list.setLayoutParams(factory.createMatchParent());
        list.setPadding(MoBendsTheme.PADDING, 0, MoBendsTheme.PADDING, 0);

        if (goblinbob.mobends.compat.BetterCombatCompat.isModLoaded())
        {
            VanillaLayoutParams params = factory.createLayoutParams(
                    VanillaLayoutParams.MATCH_PARENT,
                    CONFIG_ROW_HEIGHT);
            params.setMargins(0, 0, 0, MoBendsTheme.SPACING);
            VanillaDropDown betterCombat = buildBetterCombatDropDown();
            list.addView(betterCombat, params);
            configRows.add(new ConfigRow(betterCombat,
                    I18n.get("mobends.gui.config.better_combat_animations") + " "
                            + I18n.get("mobends.gui.config.better_combat_animations.desc")));
        }

        for (ConfigOptions.Option option : ConfigOptions.all())
        {
            VanillaToggle toggle = factory.createToggle(option.get());
            toggle.setText(I18n.get(option.getTranslationKey()));
            toggle.setBackgroundColor(MoBendsTheme.BG_LIST);
            toggle.setPadding(MoBendsTheme.PADDING_LARGE, 0, MoBendsTheme.PADDING_LARGE, 0);
            toggle.setToggleSize(CONFIG_TOGGLE_WIDTH, CONFIG_TOGGLE_HEIGHT);
            toggle.setTooltip(I18n.get(option.getDescriptionKey()));
            toggle.setOnCheckedChangeListener(option::set);

            VanillaLayoutParams params = factory.createLayoutParams(
                    VanillaLayoutParams.MATCH_PARENT,
                    CONFIG_ROW_HEIGHT);
            params.setMargins(0, 0, 0, MoBendsTheme.SPACING);
            list.addView(toggle, params);
            configRows.add(new ConfigRow(toggle,
                    I18n.get(option.getTranslationKey()) + " " + I18n.get(option.getDescriptionKey())));
        }

        scrollView.addView(list, factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT));

        layout.addView(scrollView, factory.createMatchParent());

        return layout;
    }

    private void onConfigSearchTextChanged(String query)
    {
        final String needle = query == null ? "" : query.trim().toLowerCase(java.util.Locale.ROOT);

        for (ConfigRow row : configRows)
        {
            final boolean matches = needle.isEmpty() || row.searchText.contains(needle);
            row.view.setVisibility(matches ? VanillaView.VISIBLE : VanillaView.GONE);
        }
    }

    private static final class ConfigRow
    {
        private final VanillaView view;
        private final String searchText;

        private ConfigRow(VanillaView view, String searchText)
        {
            this.view = view;
            this.searchText = searchText.toLowerCase(java.util.Locale.ROOT);
        }
    }

    private VanillaView withBackHeader(VanillaViewFactory factory, VanillaView content)
    {
        return withBackHeader(factory, content, null);
    }

    private VanillaView withBackHeader(VanillaViewFactory factory, VanillaView content,
                                       @Nullable VanillaView trailingControl)
    {
        return withBackHeader(factory, content, trailingControl, SUB_CHOOSER);
    }

    private VanillaView withBackHeader(VanillaViewFactory factory, VanillaView content,
                                       @Nullable VanillaView trailingControl, int backTarget)
    {
        VanillaLinearLayout layout = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        layout.setLayoutParams(factory.createMatchParent());

        VanillaLinearLayout header = factory.createLinearLayout(VanillaViewFactory.HORIZONTAL);
        header.setGravity(VanillaLinearLayout.GRAVITY_CENTER_VERTICAL);

        VanillaButton backButton = factory.createButton(I18n.get("mobends.gui.back"));
        backButton.setOnClickListener(() -> showSettingsSubView(backTarget));

        VanillaLayoutParams backParams = factory.createLayoutParams(60, MoBendsTheme.BUTTON_HEIGHT);
        backParams.setMargins(0, 0, MoBendsTheme.PADDING, 0);
        header.addView(backButton, backParams);

        if (trailingControl != null)
        {
            header.addView(trailingControl, factory.createLayoutParams(
                    VanillaLayoutParams.WRAP_CONTENT, MoBendsTheme.BUTTON_HEIGHT));
        }

        VanillaLayoutParams headerParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT, MoBendsTheme.BUTTON_HEIGHT);
        headerParams.setMargins(MoBendsTheme.PADDING, MoBendsTheme.PADDING, 0, MoBendsTheme.SPACING);
        layout.addView(header, headerParams);

        layout.addView(content, factory.createMatchParent());

        return layout;
    }

    private VanillaButton buildWeaponsButton(VanillaViewFactory factory)
    {
        VanillaButton button = factory.createButton(I18n.get("mobends.gui.weapons"));
        button.setMinimumWidth(WEAPONS_BUTTON_WIDTH);
        button.setOnClickListener(() -> showSettingsSubView(SUB_WEAPONS));
        return button;
    }

    private VanillaView buildWeaponsContent(VanillaViewFactory factory)
    {
        weaponFactory = factory;

        VanillaLinearLayout layout = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        layout.setLayoutParams(factory.createMatchParent());
        layout.setPadding(MoBendsTheme.PADDING, 0, MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        VanillaLinearLayout toolbar = factory.createLinearLayout(VanillaViewFactory.HORIZONTAL);
        toolbar.setGravity(VanillaLinearLayout.GRAVITY_CENTER_VERTICAL);

        weaponField = factory.createTextField("");
        weaponField.setOnSubmitListener(this::addWeaponFromField);
        VanillaLayoutParams fieldParams = factory.createLayoutParams(
                0, MoBendsTheme.BUTTON_HEIGHT, 1.0f);
        fieldParams.setMargins(0, 0, MoBendsTheme.PADDING, 0);
        toolbar.addView(weaponField, fieldParams);

        VanillaButton addButton = factory.createButton(I18n.get("mobends.gui.weapons.add"));
        addButton.setOnClickListener(this::addWeaponFromField);
        toolbar.addView(addButton, factory.createLayoutParams(WEAPON_ADD_WIDTH, MoBendsTheme.BUTTON_HEIGHT));

        VanillaLayoutParams toolbarParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                MoBendsTheme.BUTTON_HEIGHT
        );
        toolbarParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        layout.addView(toolbar, toolbarParams);

        VanillaTextView hint = factory.createTextView(I18n.get("mobends.gui.weapons.hint"));
        hint.setTextColor(MoBendsTheme.TEXT_HINT);
        hint.setTextSize(10);
        VanillaLayoutParams hintParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT);
        hintParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        layout.addView(hint, hintParams);

        VanillaScrollView scrollView = factory.createScrollView();
        weaponList = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        weaponList.setLayoutParams(factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT));
        scrollView.addView(weaponList, factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT));
        layout.addView(scrollView, factory.createMatchParent());

        refreshWeaponList();

        return layout;
    }

    private void addWeaponFromField()
    {
        if (weaponField == null) return;

        String itemId = CustomWeapons.normalize(weaponField.getText());
        if (itemId == null) return;

        if (CoreClientConfig.getInstance().addCustomWeapon(itemId))
        {
            CustomWeapons.invalidate();
            weaponField.setText("");
            net.minecraft.client.Minecraft.getInstance().tell(this::refreshWeaponList);
        }
    }

    private void removeWeapon(String itemId)
    {
        if (CoreClientConfig.getInstance().removeCustomWeapon(itemId))
        {
            CustomWeapons.invalidate();
            net.minecraft.client.Minecraft.getInstance().tell(this::refreshWeaponList);
        }
    }

    private void refreshWeaponList()
    {
        if (weaponList == null || weaponFactory == null) return;

        weaponList.removeAllViews();

        List<String> weapons = CoreClientConfig.getInstance().getCustomWeapons();

        if (weapons.isEmpty())
        {
            VanillaTextView empty = weaponFactory.createTextView(I18n.get("mobends.gui.weapons.empty"));
            empty.setTextColor(MoBendsTheme.TEXT_SECONDARY);
            empty.setTextSize(10);
            empty.setPadding(MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING,
                             MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING);
            weaponList.addView(empty, weaponFactory.createLayoutParams(
                    VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT));
            return;
        }

        for (String weapon : weapons)
        {
            VanillaLinearLayout row = weaponFactory.createLinearLayout(VanillaViewFactory.HORIZONTAL);
            row.setGravity(VanillaLinearLayout.GRAVITY_CENTER_VERTICAL);
            row.setBackgroundColor(MoBendsTheme.BG_LIST);
            row.setPadding(MoBendsTheme.PADDING_LARGE, 0, MoBendsTheme.PADDING, 0);

            VanillaTextView label = weaponFactory.createTextView(weapon);
            label.setTextColor(MoBendsTheme.TEXT_PRIMARY);
            label.setTextSize(11);
            label.setMaxLines(1);
            row.addView(label, weaponFactory.createLayoutParams(
                    0, VanillaLayoutParams.WRAP_CONTENT, 1.0f));

            VanillaButton remove = weaponFactory.createButton(I18n.get("mobends.gui.weapons.remove"));
            remove.setTextSize(10);
            remove.setOnClickListener(() -> removeWeapon(weapon));
            row.addView(remove, weaponFactory.createLayoutParams(WEAPON_REMOVE_WIDTH, WEAPON_ROW_BUTTON_HEIGHT));

            VanillaLayoutParams rowParams = weaponFactory.createLayoutParams(
                    VanillaLayoutParams.MATCH_PARENT, WEAPON_ROW_HEIGHT);
            rowParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
            weaponList.addView(row, rowParams);
        }
    }

    private VanillaDropDown buildSpinDropDown()
    {
        VanillaDropDown dropDown = new VanillaDropDown(I18n.get("mobends.gui.animations.spin"));
        dropDown.addOption(I18n.get("mobends.gui.animations.spin.off"));
        dropDown.addOption(I18n.get("mobends.gui.animations.spin.hover"));
        dropDown.addOption(I18n.get("mobends.gui.animations.spin.always"));

        MobPreviewGridWidget.SpinMode mode = readSpinMode();
        mobGrid.setSpinMode(mode);
        dropDown.setSelectedIndex(mode.ordinal());

        dropDown.setOnSelectionChanged(index -> {
            MobPreviewGridWidget.SpinMode selected = MobPreviewGridWidget.SpinMode.values()[index];
            mobGrid.setSpinMode(selected);
            CoreClientConfig.getInstance().setPreviewSpinMode(selected.name());
        });

        return dropDown;
    }

    private VanillaDropDown buildBetterCombatDropDown()
    {
        final goblinbob.mobends.compat.BetterCombatCompat.Animations[] modes =
                goblinbob.mobends.compat.BetterCombatCompat.Animations.values();

        VanillaDropDown dropDown = new VanillaDropDown("");
        dropDown.setBackgroundColor(MoBendsTheme.BG_LIST);

        for (goblinbob.mobends.compat.BetterCombatCompat.Animations mode : modes)
        {
            dropDown.addOption(I18n.get(betterCombatModeKey(mode)),
                    I18n.get(betterCombatModeKey(mode) + ".desc"));
        }

        goblinbob.mobends.compat.BetterCombatCompat.Animations current =
                goblinbob.mobends.compat.BetterCombatCompat.getAnimations();
        dropDown.setSelectedIndex(current.ordinal());
        dropDown.setLabel(betterCombatLabel(current));

        dropDown.setOnSelectionChanged(index -> {
            goblinbob.mobends.compat.BetterCombatCompat.Animations selected = modes[index];
            goblinbob.mobends.compat.BetterCombatCompat.setAnimations(selected);
            dropDown.setLabel(betterCombatLabel(selected));
        });

        return dropDown;
    }

    private static String betterCombatLabel(goblinbob.mobends.compat.BetterCombatCompat.Animations mode)
    {
        return I18n.get("mobends.gui.config.better_combat_animations")
                + ": " + I18n.get(betterCombatModeKey(mode));
    }

    private static String betterCombatModeKey(goblinbob.mobends.compat.BetterCombatCompat.Animations mode)
    {
        return "mobends.gui.config.better_combat_animations." + mode.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static MobPreviewGridWidget.SpinMode readSpinMode()
    {
        String stored = CoreClientConfig.getInstance().getPreviewSpinMode();

        try
        {
            return MobPreviewGridWidget.SpinMode.valueOf(stored);
        }
        catch (IllegalArgumentException | NullPointerException e)
        {
            return MobPreviewGridWidget.SpinMode.HOVER;
        }
    }

    private void showSettingsSubView(int subView)
    {
        showOrHideTab(settingsChooser, subView == SUB_CHOOSER);
        showOrHideTab(animationsContent, subView == SUB_ANIMATIONS);
        showOrHideTab(configContent, subView == SUB_CONFIG);
        showOrHideTab(weaponsContent, subView == SUB_WEAPONS);

        if (subView == SUB_WEAPONS)
        {
            refreshWeaponList();
        }

        if (subView != SUB_CONFIG && configSearchField != null && !configSearchField.getText().isEmpty())
        {
            configSearchField.setText("");
            onConfigSearchTextChanged("");
        }
    }

    public void openConfig()
    {
        if (tabBar != null)
        {
            tabBar.selectTab(TAB_SETTINGS);
        }
        showTab(TAB_SETTINGS);
        showSettingsSubView(SUB_CONFIG);
    }

    private VanillaView buildPacksContent(VanillaViewFactory factory)
    {
        VanillaLinearLayout layout = factory.createLinearLayout(VanillaViewFactory.HORIZONTAL);
        layout.setLayoutParams(factory.createMatchParent());
        layout.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                         MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        VanillaLinearLayout leftPanel = factory.createLinearLayout(VanillaViewFactory.VERTICAL);

        packSearchField = factory.createTextField(I18n.get("mobends.gui.search"));
        packSearchField.setOnTextChangedListener(this::onPackSearchTextChanged);
        VanillaLayoutParams searchParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                MoBendsTheme.BUTTON_HEIGHT
        );
        searchParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        leftPanel.addView(packSearchField, searchParams);

        packList = new PackListWidget(factory);
        packList.setOnPackSelected(this::onPackSelected);
        packList.populateFromManager();

        leftPanel.addView(packList.getView(), factory.createMatchParent());

        int detailsWidth = 160;

        VanillaLinearLayout detailsPanel = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        detailsPanel.setBackgroundColor(MoBendsTheme.BG_LIST);
        detailsPanel.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                               MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        VanillaTextView detailsHeader = factory.createTextView(I18n.get("mobends.gui.packs.details"));
        detailsHeader.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        detailsHeader.setTextSize(14);
        detailsHeader.setBold(true);
        detailsPanel.addView(detailsHeader, factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.WRAP_CONTENT
        ));

        VanillaTextView detailsPlaceholder = factory.createTextView(I18n.get("mobends.gui.packs.select_pack"));
        detailsPlaceholder.setTextColor(MoBendsTheme.TEXT_HINT);
        detailsPlaceholder.setTextSize(12);
        VanillaLayoutParams placeholderParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.WRAP_CONTENT
        );
        placeholderParams.setMargins(0, MoBendsTheme.SPACING, 0, 0);
        detailsPanel.addView(detailsPlaceholder, placeholderParams);

        VanillaLayoutParams detailsParams = factory.createLayoutParams(
                detailsWidth,
                VanillaLayoutParams.MATCH_PARENT
        );
        detailsParams.setMargins(MoBendsTheme.SPACING, 0, 0, 0);

        VanillaLayoutParams leftPanelParams = factory.createLayoutParams(0, VanillaLayoutParams.MATCH_PARENT, 1.0f);
        layout.addView(leftPanel, leftPanelParams);
        layout.addView(detailsPanel, detailsParams);

        return layout;
    }

    private VanillaView buildCustomizeContent(VanillaViewFactory factory)
    {
        VanillaLinearLayout layout = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        layout.setLayoutParams(factory.createMatchParent());
        layout.setGravity(VanillaLinearLayout.GRAVITY_CENTER);
        layout.setPadding(MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING_LARGE,
                         MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING_LARGE);

        final IAnimationEditor editor = AnimationEditorRegistry.INSTANCE.getPrimaryEditor();

        if (editor == null)
        {
            return layout;
        }

        VanillaButton openEditor = factory.createButton(I18n.get("mobends.gui.customize.open_editor"));
        openEditor.setOnClickListener(() -> {
            try
            {
                editor.openEditorGui();
            }
            catch (Exception e)
            {
                LOGGER.error("The registered animation editor failed to open", e);
            }
        });

        layout.addView(openEditor, factory.createLayoutParams(
                160,
                MoBendsTheme.BUTTON_HEIGHT
        ));

        return layout;
    }

    private VanillaView withWipOverlay(VanillaViewFactory factory, VanillaView content)
    {
        VanillaFrameLayout frame = factory.createFrameLayout();
        frame.setLayoutParams(factory.createMatchParent());
        frame.addView(content, factory.createMatchParent());

        VanillaFrameLayout overlay = new VanillaFrameLayout()
        {
            @Override
            public boolean handleClick(double mouseX, double mouseY, int button)
            {
                return visibility == VISIBLE && isInBounds(mouseX, mouseY);
            }

            @Override
            public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollY)
            {
                return visibility == VISIBLE && isInBounds(mouseX, mouseY);
            }

            @Override
            public boolean handleMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
            {
                return visibility == VISIBLE && isInBounds(mouseX, mouseY);
            }
        };
        overlay.setBackgroundColor(MoBendsTheme.BG_WIP_OVERLAY);

        VanillaTextView label = factory.createTextView(I18n.get("mobends.gui.wip"));
        label.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        label.setTextSize(42);
        label.setBold(true);
        label.setGravity(VanillaLinearLayout.GRAVITY_CENTER);
        overlay.addView(label, factory.createFrameLayoutParams(
                VanillaLayoutParams.WRAP_CONTENT,
                VanillaLayoutParams.WRAP_CONTENT,
                VanillaLayoutParams.GRAVITY_CENTER
        ));

        frame.addView(overlay, factory.createMatchParent());
        return frame;
    }

    private void onTabChanged(int tabIndex)
    {
        showTab(tabIndex);

        if (tabIndex == TAB_SETTINGS)
        {
            showSettingsSubView(SUB_CHOOSER);
        }

        if (searchField != null)
        {
            searchField.setText("");
        }
    }

    private void onSearchTextChanged(String query)
    {
        if (mobGrid != null)
        {
            mobGrid.filter(query);
        }
    }

    private void showTab(int tabIndex)
    {
        boolean packsAllowed = NetworkConfiguration.instance.areBendsPacksAllowed();

        int settingsIdx = TAB_SETTINGS;
        int packsIdx = packsAllowed ? TAB_PACKS : -1;
        int customizeIdx = packsAllowed ? TAB_CUSTOMIZE : TAB_PACKS;

        showOrHideTab(settingsContent, tabIndex == settingsIdx);
        showOrHideTab(packsContent, tabIndex == packsIdx);
        showOrHideTab(customizeContent, tabIndex == customizeIdx);
        if (galleryContent != null)
        {
            showOrHideTab(galleryContent, tabIndex == galleryTabIndex);
        }
    }

    private static boolean isDevEnvironment()
    {
        try
        {
            return PlatformServices.get() != null && PlatformServices.get().isDevelopmentEnvironment();
        }
        catch (Throwable t)
        {
            return false;
        }
    }

    private void showOrHideTab(VanillaView content, boolean show)
    {
        if (show)
        {
            content.setAlpha(0f);
            content.setVisibility(VanillaView.VISIBLE);
            content.animateAlpha(1f, 150);
        }
        else
        {
            content.setVisibility(VanillaView.GONE);
        }
    }

    private void onAnimationSelected(String animationType)
    {
        if (mobGrid == null) return;

        mobGrid.setAnimationType(animationType);
        applyAnimationChipStyles();
    }

    private void onPackSearchTextChanged(String query)
    {
        if (packList != null)
        {
            packList.filter(query);
        }
    }

    private void onPackSelected(IBendsPack pack)
    {
    }
}
