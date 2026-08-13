package goblinbob.mobends.core.client.gui;

import goblinbob.mobends.core.client.gui.vanilla.*;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import goblinbob.mobends.core.client.gui.widget.BenderListWidget;
import goblinbob.mobends.core.client.gui.widget.EntityPreviewWidget;
import goblinbob.mobends.core.client.gui.widget.PackListWidget;
import goblinbob.mobends.core.client.gui.widget.TabBarWidget;
import goblinbob.mobends.core.client.gui.widget.UIGalleryWidget;
import goblinbob.mobends.api.platform.PlatformServices;
import goblinbob.mobends.core.pack.IBendsPack;
import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import goblinbob.mobends.standard.main.ConfigOptions;
import net.minecraft.client.resources.language.I18n;

public class MoBendsScreenBuilder
{
    private static final int TAB_SETTINGS = 0;
    private static final int TAB_PACKS = 1;
    private static final int TAB_CUSTOMIZE = 2;

    private static final int SUB_CHOOSER = 0;
    private static final int SUB_ANIMATIONS = 1;
    private static final int SUB_CONFIG = 2;

    private static final int COGWHEEL_TEXTURE_SIZE = 512;
    private static final long CHOOSER_CYCLE_MS = 2500L;
    private static final float CHOOSER_PREVIEW_SCALE = 3.0F;

    private static final int CONFIG_ROW_HEIGHT = 46;
    private static final int CONFIG_TOGGLE_WIDTH = 40;
    private static final int CONFIG_TOGGLE_HEIGHT = 20;

    private static final int COLOR_SETTINGS = MoBendsTheme.COLOR_SETTINGS;
    private static final int COLOR_PACKS = MoBendsTheme.COLOR_PACKS;
    private static final int COLOR_CUSTOMIZE = MoBendsTheme.COLOR_CUSTOMIZE;

    private TabBarWidget tabBar;
    private BenderListWidget benderList;
    private EntityPreviewWidget entityPreview;
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

    public EntityPreviewWidget getEntityPreview()
    {
        return entityPreview;
    }

    public void dispose()
    {
        if (entityPreview != null)
        {
            entityPreview.getRenderer().dispose();
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
        packsContent = buildPacksContent(factory);
        customizeContent = buildCustomizeContent(factory);

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
        VanillaLinearLayout layout = factory.createLinearLayout(VanillaViewFactory.HORIZONTAL);
        layout.setLayoutParams(factory.createMatchParent());
        layout.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                         MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        VanillaLinearLayout leftPanel = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        VanillaLayoutParams leftParams = factory.createLayoutParams(0, VanillaLayoutParams.MATCH_PARENT);
        leftPanel.setLayoutParams(leftParams);

        searchField = factory.createTextField(I18n.get("mobends.gui.search"));
        searchField.setOnTextChangedListener(this::onSearchTextChanged);
        VanillaLayoutParams searchParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                MoBendsTheme.BUTTON_HEIGHT
        );
        searchParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        leftPanel.addView(searchField, searchParams);

        benderList = new BenderListWidget(factory);
        benderList.setOnBenderSelected(this::onBenderSelected);
        benderList.setOnAnimationSelected(this::onAnimationSelected);
        benderList.populateFromRegistry(filter);

        leftPanel.addView(benderList.getView(), factory.createMatchParent());

        int previewWidth = 150;

        entityPreview = new EntityPreviewWidget(factory, previewWidth, 0);
        VanillaLayoutParams previewParams = factory.createLayoutParams(
                previewWidth,
                VanillaLayoutParams.MATCH_PARENT
        );
        previewParams.setMargins(MoBendsTheme.SPACING, 0, 0, 0);

        VanillaLayoutParams leftPanelParams = factory.createLayoutParams(0, VanillaLayoutParams.MATCH_PARENT, 1.0f);
        layout.addView(leftPanel, leftPanelParams);
        layout.addView(entityPreview.getView(), previewParams);

        return layout;
    }

    private VanillaView buildSettingsTab(VanillaViewFactory factory)
    {
        settingsFrame = factory.createFrameLayout();
        settingsFrame.setLayoutParams(factory.createMatchParent());

        settingsChooser = buildSettingsChooser(factory);
        animationsContent = withBackHeader(factory, buildAnimationsContent(factory));
        configContent = withBackHeader(factory, buildConfigContent(factory));

        settingsFrame.addView(settingsChooser, factory.createMatchParent());
        settingsFrame.addView(animationsContent, factory.createMatchParent());
        settingsFrame.addView(configContent, factory.createMatchParent());

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
        VanillaScrollView scrollView = factory.createScrollView();
        scrollView.setLayoutParams(factory.createMatchParent());

        VanillaLinearLayout list = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        list.setLayoutParams(factory.createMatchParent());
        list.setPadding(MoBendsTheme.PADDING, 0, MoBendsTheme.PADDING, MoBendsTheme.PADDING);

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
        }

        scrollView.addView(list, factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT));

        return scrollView;
    }

    private VanillaView withBackHeader(VanillaViewFactory factory, VanillaView content)
    {
        VanillaLinearLayout layout = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        layout.setLayoutParams(factory.createMatchParent());

        VanillaButton backButton = factory.createButton(I18n.get("mobends.gui.back"));
        backButton.setOnClickListener(() -> showSettingsSubView(SUB_CHOOSER));

        VanillaLayoutParams backParams = factory.createLayoutParams(60, MoBendsTheme.BUTTON_HEIGHT);
        backParams.setMargins(MoBendsTheme.PADDING, MoBendsTheme.PADDING, 0, MoBendsTheme.SPACING);
        layout.addView(backButton, backParams);

        layout.addView(content, factory.createMatchParent());

        return layout;
    }

    private void showSettingsSubView(int subView)
    {
        showOrHideTab(settingsChooser, subView == SUB_CHOOSER);
        showOrHideTab(animationsContent, subView == SUB_ANIMATIONS);
        showOrHideTab(configContent, subView == SUB_CONFIG);
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

        VanillaTextView info = factory.createTextView(I18n.get("mobends.gui.customize.editor_info"));
        info.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        info.setTextSize(14);
        info.setGravity(VanillaLinearLayout.GRAVITY_CENTER);
        layout.addView(info, factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.WRAP_CONTENT
        ));

        VanillaView spacer = factory.createView();
        VanillaLayoutParams spacerParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                MoBendsTheme.SPACING
        );
        layout.addView(spacer, spacerParams);

        VanillaTextView status = factory.createTextView(I18n.get("mobends.gui.customize.no_editor"));
        status.setTextColor(MoBendsTheme.ACCENT_ERROR);
        status.setTextSize(12);
        status.setGravity(VanillaLinearLayout.GRAVITY_CENTER);
        layout.addView(status, factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.WRAP_CONTENT
        ));

        return layout;
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
        if (benderList != null)
        {
            benderList.filter(query);
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

    private void onBenderSelected(EntityBender<?> bender)
    {
        if (entityPreview != null)
        {
            entityPreview.setBender(bender);
        }
    }

    private void onAnimationSelected(String animationType)
    {
        if (entityPreview != null)
        {
            entityPreview.setAnimationModeByName(animationType);
        }
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
