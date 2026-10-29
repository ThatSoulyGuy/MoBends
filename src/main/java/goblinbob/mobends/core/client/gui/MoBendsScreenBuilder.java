package goblinbob.mobends.core.client.gui;

import goblinbob.mobends.core.client.gui.view.*;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import goblinbob.mobends.core.client.gui.widget.ConfigPanelWidget;
import goblinbob.mobends.core.client.gui.widget.EntityPreviewWidget;
import goblinbob.mobends.core.client.gui.widget.MobPreviewGridWidget;
import goblinbob.mobends.core.client.gui.widget.PackListWidget;
import goblinbob.mobends.core.client.gui.widget.TabBarWidget;
import goblinbob.mobends.core.client.gui.widget.UIGalleryWidget;
import goblinbob.mobends.api.platform.PlatformServices;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.pack.IBendsPack;
import goblinbob.mobends.core.network.SharedNetworkConfiguration;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import net.minecraft.client.resources.language.I18n;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
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

    private static final int COGWHEEL_TEXTURE_SIZE = 512;
    private static final long CHOOSER_CYCLE_MS = 2500L;
    private static final float CHOOSER_PREVIEW_SCALE = 3.0F;

    private static final int SEARCH_FIELD_WIDTH = 90;
    private static final int CHIP_WIDTH = 54;
    private static final int CHIP_TEXT_SIZE = 10;

    private static final int COLOR_SETTINGS = MoBendsTheme.COLOR_SETTINGS;
    private static final int COLOR_PACKS = MoBendsTheme.COLOR_PACKS;
    private static final int COLOR_CUSTOMIZE = MoBendsTheme.COLOR_CUSTOMIZE;

    private TabBarWidget tabBar;
    private MobPreviewGridWidget mobGrid;
    private final Map<String, Button> animationChips = new LinkedHashMap<>();
    private PackListWidget packList;
    private FrameLayout contentFrame;
    private View settingsContent;
    private View packsContent;
    private View customizeContent;
    private View galleryContent;
    private int galleryTabIndex = -1;
    private TextField searchField;
    private TextField packSearchField;
    private FrameLayout settingsFrame;
    private View settingsChooser;
    private View animationsContent;
    private View configContent;
    private ConfigPanelWidget configPanel;
    private EntityPreviewWidget chooserPreview;
    private int chooserBenderIndex;
    private boolean chooserStarted;
    private long chooserLastCycle;
    private boolean openConfigOnBuild;

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

    public View buildContent()
    {
        LinearLayout root = new LinearLayout(LinearLayout.VERTICAL);
        root.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ).setGravity(LayoutParams.GRAVITY_CENTER));
        root.setBackgroundColor(MoBendsTheme.BG_PANEL);

        LinearLayout header = new LinearLayout(LinearLayout.VERTICAL);
        header.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                MoBendsTheme.HEADER_HEIGHT
        ));
        header.setBackgroundColor(MoBendsTheme.BG_HEADER);
        header.setGravity(LayoutParams.GRAVITY_CENTER);
        header.setPadding(0, MoBendsTheme.PADDING, 0, 0);

        TextView title = new TextView(I18n.get("mobends.gui.title"));
        title.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        title.setTextSize(15);
        title.setBold(true);
        title.setGravity(LayoutParams.GRAVITY_CENTER);
        header.addView(title, new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));

        root.addView(header, new LayoutParams(
                LayoutParams.MATCH_PARENT,
                MoBendsTheme.HEADER_HEIGHT
        ));

        tabBar = new TabBarWidget();
        tabBar.addTab("mobends.gui.section.settings", COLOR_SETTINGS);
        if (SharedNetworkConfiguration.INSTANCE.areBendsPacksAllowed())
        {
            tabBar.addTab("mobends.gui.section.packs", COLOR_PACKS);
        }
        tabBar.addTab("mobends.gui.section.customize", COLOR_CUSTOMIZE);
        boolean devMode = isDevEnvironment();
        if (devMode)
        {
            tabBar.addTab("UI Test", MoBendsTheme.ACCENT_ERROR);
            galleryTabIndex = SharedNetworkConfiguration.INSTANCE.areBendsPacksAllowed() ? 3 : 2;
        }
        tabBar.setOnTabChanged(this::onTabChanged);

        LayoutParams tabParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                MoBendsTheme.TAB_HEIGHT
        );
        root.addView(tabBar.getView(), tabParams);

        contentFrame = new FrameLayout();
        contentFrame.setLayoutParams(LayoutParams.matchParent());
        contentFrame.setBackgroundColor(MoBendsTheme.BG_CONTENT);

        settingsContent = buildSettingsTab();
        packsContent = withWipOverlay(buildPacksContent());
        customizeContent = withWipOverlay(buildCustomizeContent());

        contentFrame.addView(settingsContent, LayoutParams.matchParent());
        contentFrame.addView(packsContent, LayoutParams.matchParent());
        contentFrame.addView(customizeContent, LayoutParams.matchParent());
        if (devMode)
        {
            galleryContent = UIGalleryWidget.build();
            contentFrame.addView(galleryContent, LayoutParams.matchParent());
        }

        showTab(TAB_SETTINGS);

        if (openConfigOnBuild)
        {
            showSettingsSubView(SUB_CONFIG);
        }

        root.addView(contentFrame, LayoutParams.matchParent());

        return root;
    }

    public void setOpenConfigOnBuild(boolean openConfigOnBuild)
    {
        this.openConfigOnBuild = openConfigOnBuild;
    }

    private View buildAnimationsContent()
    {
        LinearLayout layout = new LinearLayout(LinearLayout.VERTICAL);
        layout.setLayoutParams(LayoutParams.matchParent());
        layout.setPadding(MoBendsTheme.PADDING, 0, MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        mobGrid = new MobPreviewGridWidget();
        mobGrid.populateFromRegistry();

        LinearLayout toolbar = new LinearLayout(LinearLayout.HORIZONTAL);
        toolbar.setGravity(LayoutParams.GRAVITY_CENTER_VERTICAL);

        searchField = new TextField(I18n.get("mobends.gui.search"));
        searchField.setOnTextChangedListener(this::onSearchTextChanged);
        LayoutParams searchParams = new LayoutParams(
                SEARCH_FIELD_WIDTH,
                MoBendsTheme.BUTTON_HEIGHT
        );
        searchParams.setMargins(0, 0, MoBendsTheme.PADDING, 0);
        toolbar.addView(searchField, searchParams);

        TextView hint = new TextView(I18n.get("mobends.gui.animations.hint"));
        hint.setTextColor(MoBendsTheme.TEXT_HINT);
        hint.setTextSize(10);
        toolbar.addView(hint, new LayoutParams(
                0, LayoutParams.WRAP_CONTENT, 1.0f));

        LayoutParams toolbarParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                MoBendsTheme.BUTTON_HEIGHT
        );
        toolbarParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        layout.addView(toolbar, toolbarParams);

        LayoutParams chipParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        chipParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        layout.addView(buildAnimationChips(), chipParams);

        layout.addView(mobGrid.getView(), LayoutParams.matchParent());

        return layout;
    }

    private View buildAnimationChips()
    {
        GridLayout chipGrid = new GridLayout();
        chipGrid.setCellSize(CHIP_WIDTH, MoBendsTheme.BUTTON_HEIGHT);
        chipGrid.setSpacing(MoBendsTheme.SPACING, MoBendsTheme.SPACING);

        animationChips.clear();

        for (String animation : mobGrid.getAvailableAnimations())
        {
            Button chip = new Button(getAnimationLabel(animation));
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

        for (Map.Entry<String, Button> entry : animationChips.entrySet())
        {
            boolean active = entry.getKey().equals(selected);
            Button chip = entry.getValue();

            chip.setBackgroundColor(active ? MoBendsTheme.TOGGLE_ON : MoBendsTheme.BG_BUTTON);
            chip.setTextColor(active ? MoBendsTheme.BG_HEADER : MoBendsTheme.TEXT_PRIMARY);
            chip.setTextShadow(!active);
        }
    }

    private View buildSettingsTab()
    {
        settingsFrame = new FrameLayout();
        settingsFrame.setLayoutParams(LayoutParams.matchParent());

        settingsChooser = buildSettingsChooser();

        View animations = buildAnimationsContent();
        animationsContent = withBackHeader(animations, buildSpinDropDown());

        configPanel = new ConfigPanelWidget(() -> showSettingsSubView(SUB_CHOOSER));
        configContent = configPanel.getView();

        settingsFrame.addView(settingsChooser, LayoutParams.matchParent());
        settingsFrame.addView(animationsContent, LayoutParams.matchParent());
        settingsFrame.addView(configContent, LayoutParams.matchParent());

        showSettingsSubView(SUB_CHOOSER);

        return settingsFrame;
    }

    private View buildSettingsChooser()
    {
        LinearLayout layout = new LinearLayout(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(LayoutParams.matchParent());
        layout.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                         MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        chooserPreview = new EntityPreviewWidget(0, 0);
        chooserPreview.setChromeVisible(false);
        chooserPreview.setInteractive(false);
        chooserPreview.getView().setBackgroundColor(0);
        chooserPreview.setScaleMultiplier(CHOOSER_PREVIEW_SCALE);
        applyChooserBender();

        TileView animationsTile = buildTile(chooserPreview.getView(),
                I18n.get("mobends.gui.settings.animations"),
                () -> showSettingsSubView(SUB_ANIMATIONS));
        animationsTile.setTicker(this::tickChooserPreview);

        IconView cogwheel = new IconView(
                ResourceLocationFactory.create("mobends", "textures/gui/cogwheel.png"),
                COGWHEEL_TEXTURE_SIZE);
        cogwheel.setIconSize(96);
        cogwheel.setSpinning(true);

        TileView configTile = buildTile(cogwheel,
                I18n.get("mobends.gui.settings.config"),
                () -> showSettingsSubView(SUB_CONFIG));
        cogwheel.setHoverSupplier(configTile::isHovered);

        LayoutParams leftParams = new LayoutParams(
                0, LayoutParams.MATCH_PARENT, 1.0f);
        leftParams.setMargins(0, 0, MoBendsTheme.PADDING_LARGE * 2, 0);
        layout.addView(animationsTile, leftParams);
        layout.addView(configTile, new LayoutParams(
                0, LayoutParams.MATCH_PARENT, 1.0f));

        return layout;
    }

    private TileView buildTile(View content, String label, Runnable onClick)
    {
        TileView tile = new TileView();
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(LayoutParams.GRAVITY_CENTER);
        tile.setPadding(MoBendsTheme.SPACING, MoBendsTheme.SPACING,
                       MoBendsTheme.SPACING, MoBendsTheme.SPACING);
        tile.setOnClickListener(onClick);

        tile.addView(content, new LayoutParams(
                LayoutParams.MATCH_PARENT, 0, 1.0f));

        TextView labelView = new TextView(label);
        labelView.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        labelView.setTextSize(12);
        labelView.setBold(true);
        labelView.setGravity(LayoutParams.GRAVITY_CENTER);
        tile.addView(labelView, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

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

    private View withBackHeader(View content,
                                @Nullable View trailingControl)
    {
        LinearLayout layout = new LinearLayout(LinearLayout.VERTICAL);
        layout.setLayoutParams(LayoutParams.matchParent());

        LinearLayout header = new LinearLayout(LinearLayout.HORIZONTAL);
        header.setGravity(LayoutParams.GRAVITY_CENTER_VERTICAL);

        Button backButton = new Button(I18n.get("mobends.gui.back"));
        backButton.setOnClickListener(() -> showSettingsSubView(SUB_CHOOSER));

        LayoutParams backParams = new LayoutParams(60, MoBendsTheme.BUTTON_HEIGHT);
        backParams.setMargins(0, 0, MoBendsTheme.PADDING, 0);
        header.addView(backButton, backParams);

        if (trailingControl != null)
        {
            header.addView(trailingControl, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, MoBendsTheme.BUTTON_HEIGHT));
        }

        LayoutParams headerParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, MoBendsTheme.BUTTON_HEIGHT);
        headerParams.setMargins(MoBendsTheme.PADDING, MoBendsTheme.PADDING, 0, MoBendsTheme.SPACING);
        layout.addView(header, headerParams);

        layout.addView(content, LayoutParams.matchParent());

        return layout;
    }

    private DropDown buildSpinDropDown()
    {
        DropDown dropDown = new DropDown(I18n.get("mobends.gui.animations.spin"));
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

        if (subView == SUB_CONFIG)
        {
            configPanel.onShown();
        }
        else
        {
            configPanel.clearSearch();
        }
    }

    private View buildPacksContent()
    {
        LinearLayout layout = new LinearLayout(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(LayoutParams.matchParent());
        layout.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                         MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        LinearLayout leftPanel = new LinearLayout(LinearLayout.VERTICAL);

        packSearchField = new TextField(I18n.get("mobends.gui.search"));
        packSearchField.setOnTextChangedListener(this::onPackSearchTextChanged);
        LayoutParams searchParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                MoBendsTheme.BUTTON_HEIGHT
        );
        searchParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        leftPanel.addView(packSearchField, searchParams);

        packList = new PackListWidget();
        packList.setOnPackSelected(this::onPackSelected);
        packList.populateFromManager();

        leftPanel.addView(packList.getView(), LayoutParams.matchParent());

        int detailsWidth = 160;

        LinearLayout detailsPanel = new LinearLayout(LinearLayout.VERTICAL);
        detailsPanel.setBackgroundColor(MoBendsTheme.BG_LIST);
        detailsPanel.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                               MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        TextView detailsHeader = new TextView(I18n.get("mobends.gui.packs.details"));
        detailsHeader.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        detailsHeader.setTextSize(14);
        detailsHeader.setBold(true);
        detailsPanel.addView(detailsHeader, new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));

        TextView detailsPlaceholder = new TextView(I18n.get("mobends.gui.packs.select_pack"));
        detailsPlaceholder.setTextColor(MoBendsTheme.TEXT_HINT);
        detailsPlaceholder.setTextSize(12);
        LayoutParams placeholderParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        placeholderParams.setMargins(0, MoBendsTheme.SPACING, 0, 0);
        detailsPanel.addView(detailsPlaceholder, placeholderParams);

        LayoutParams detailsParams = new LayoutParams(
                detailsWidth,
                LayoutParams.MATCH_PARENT
        );
        detailsParams.setMargins(MoBendsTheme.SPACING, 0, 0, 0);

        LayoutParams leftPanelParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        layout.addView(leftPanel, leftPanelParams);
        layout.addView(detailsPanel, detailsParams);

        return layout;
    }

    private View buildCustomizeContent()
    {
        LinearLayout layout = new LinearLayout(LinearLayout.VERTICAL);
        layout.setLayoutParams(LayoutParams.matchParent());
        layout.setGravity(LayoutParams.GRAVITY_CENTER);
        layout.setPadding(MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING_LARGE,
                         MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING_LARGE);

        final IAnimationEditor editor = AnimationEditorRegistry.INSTANCE.getPrimaryEditor();

        if (editor == null)
        {
            return layout;
        }

        Button openEditor = new Button(I18n.get("mobends.gui.customize.open_editor"));
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

        layout.addView(openEditor, new LayoutParams(
                160,
                MoBendsTheme.BUTTON_HEIGHT
        ));

        return layout;
    }

    private View withWipOverlay(View content)
    {
        FrameLayout frame = new FrameLayout();
        frame.setLayoutParams(LayoutParams.matchParent());
        frame.addView(content, LayoutParams.matchParent());

        FrameLayout overlay = new FrameLayout()
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

        TextView label = new TextView(I18n.get("mobends.gui.wip"));
        label.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        label.setTextSize(42);
        label.setBold(true);
        label.setGravity(LayoutParams.GRAVITY_CENTER);
        overlay.addView(label, new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        ).setGravity(LayoutParams.GRAVITY_CENTER));

        frame.addView(overlay, LayoutParams.matchParent());
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
        boolean packsAllowed = SharedNetworkConfiguration.INSTANCE.areBendsPacksAllowed();

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

    private void showOrHideTab(View content, boolean show)
    {
        if (show)
        {
            content.setAlpha(0f);
            content.setVisibility(View.VISIBLE);
            content.animateAlpha(1f, 150);
        }
        else
        {
            content.setVisibility(View.GONE);
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
