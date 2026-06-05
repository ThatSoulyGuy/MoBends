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
import net.minecraft.client.resources.language.I18n;

/**
 * UI implementation of the MoBends settings screen.
 * Uses the abstraction layer to work across UI backend versions.
 */
public class MoBendsScreenBuilder
{
    // Tab indices
    private static final int TAB_SETTINGS = 0;
    private static final int TAB_PACKS = 1;
    private static final int TAB_CUSTOMIZE = 2;

    // Tab accent colors from theme
    private static final int COLOR_SETTINGS = MoBendsTheme.COLOR_SETTINGS;
    private static final int COLOR_PACKS = MoBendsTheme.COLOR_PACKS;
    private static final int COLOR_CUSTOMIZE = MoBendsTheme.COLOR_CUSTOMIZE;

    // State
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
    private final EntityBenderRegistry.Filter filter = new EntityBenderRegistry.Filter();

    public String getTitle()
    {
        return I18n.get("mobends.gui.title");
    }

    /**
     * Gets the entity preview widget for external rendering.
     * Used by platform-specific code to render entities in Minecraft's render cycle.
     *
     * @return The entity preview widget, or null if not yet created
     */
    public EntityPreviewWidget getEntityPreview()
    {
        return entityPreview;
    }

    public VanillaView buildContent(VanillaViewFactory factory)
    {
        // Create root layout with FrameLayout.LayoutParams for proper positioning in the fragment container
        VanillaLinearLayout root = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        // Use FrameLayout params since the fragment container is a FrameLayout
        root.setLayoutParams(factory.createFrameLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.GRAVITY_CENTER
        ));
        root.setBackgroundColor(MoBendsTheme.BG_PANEL);

        // ==================== Header with Title ====================

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

        // ==================== Tab Bar ====================

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
            // Dev-only "kitchen-sink" tab for visually verifying every UI element.
            tabBar.addTab("UI Test", MoBendsTheme.ACCENT_ERROR);
            galleryTabIndex = NetworkConfiguration.instance.areBendsPacksAllowed() ? 3 : 2;
        }
        tabBar.setOnTabChanged(this::onTabChanged);

        VanillaLayoutParams tabParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                MoBendsTheme.TAB_HEIGHT
        );
        root.addView(tabBar.getView(), tabParams);

        // ==================== Content Frame (for tab switching) ====================

        contentFrame = factory.createFrameLayout();
        contentFrame.setLayoutParams(factory.createMatchParent());
        contentFrame.setBackgroundColor(MoBendsTheme.BG_CONTENT);

        // Build tab contents
        settingsContent = buildSettingsContent(factory);
        packsContent = buildPacksContent(factory);
        customizeContent = buildCustomizeContent(factory);

        // Add all contents to frame (only one visible at a time)
        contentFrame.addView(settingsContent, factory.createMatchParent());
        contentFrame.addView(packsContent, factory.createMatchParent());
        contentFrame.addView(customizeContent, factory.createMatchParent());
        if (devMode)
        {
            galleryContent = UIGalleryWidget.build(factory);
            contentFrame.addView(galleryContent, factory.createMatchParent());
        }

        // Initially show settings tab
        showTab(TAB_SETTINGS);

        root.addView(contentFrame, factory.createMatchParent());

        return root;
    }

    // ==================== Tab Content Builders ====================

    private VanillaView buildSettingsContent(VanillaViewFactory factory)
    {
        // Horizontal layout: left (list) | right (preview)
        VanillaLinearLayout layout = factory.createLinearLayout(VanillaViewFactory.HORIZONTAL);
        layout.setLayoutParams(factory.createMatchParent());
        layout.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                         MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        // ==================== Left Panel (Search + Bender List) ====================

        VanillaLinearLayout leftPanel = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        VanillaLayoutParams leftParams = factory.createLayoutParams(0, VanillaLayoutParams.MATCH_PARENT);
        // Weight simulation - will take remaining space
        leftPanel.setLayoutParams(leftParams);

        // Search field
        searchField = factory.createTextField(I18n.get("mobends.gui.search"));
        searchField.setOnTextChangedListener(this::onSearchTextChanged);
        VanillaLayoutParams searchParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                MoBendsTheme.BUTTON_HEIGHT
        );
        searchParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        leftPanel.addView(searchField, searchParams);

        // Bender list
        benderList = new BenderListWidget(factory);
        benderList.setOnBenderSelected(this::onBenderSelected);
        benderList.setOnAnimationSelected(this::onAnimationSelected);
        benderList.populateFromRegistry(filter);

        leftPanel.addView(benderList.getView(), factory.createMatchParent());

        // ==================== Right Panel (Entity Preview) ====================

        // Fixed width for preview panel
        int previewWidth = 150;

        entityPreview = new EntityPreviewWidget(factory, previewWidth, 0);
        VanillaLayoutParams previewParams = factory.createLayoutParams(
                previewWidth,
                VanillaLayoutParams.MATCH_PARENT
        );
        previewParams.setMargins(MoBendsTheme.SPACING, 0, 0, 0);

        // Add panels to layout using weight-based sizing
        // Left panel takes remaining space (weight = 1)
        VanillaLayoutParams leftPanelParams = factory.createLayoutParams(0, VanillaLayoutParams.MATCH_PARENT, 1.0f);
        layout.addView(leftPanel, leftPanelParams);
        // Right panel has fixed width
        layout.addView(entityPreview.getView(), previewParams);

        return layout;
    }

    private VanillaView buildPacksContent(VanillaViewFactory factory)
    {
        // Horizontal layout: left (pack list) | right (pack details)
        VanillaLinearLayout layout = factory.createLinearLayout(VanillaViewFactory.HORIZONTAL);
        layout.setLayoutParams(factory.createMatchParent());
        layout.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                         MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        // ==================== Left Panel (Search + Pack List) ====================

        VanillaLinearLayout leftPanel = factory.createLinearLayout(VanillaViewFactory.VERTICAL);

        // Search field for packs
        packSearchField = factory.createTextField(I18n.get("mobends.gui.search"));
        packSearchField.setOnTextChangedListener(this::onPackSearchTextChanged);
        VanillaLayoutParams searchParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                MoBendsTheme.BUTTON_HEIGHT
        );
        searchParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        leftPanel.addView(packSearchField, searchParams);

        // Pack list
        packList = new PackListWidget(factory);
        packList.setOnPackSelected(this::onPackSelected);
        packList.populateFromManager();

        leftPanel.addView(packList.getView(), factory.createMatchParent());

        // ==================== Right Panel (Pack Details) ====================

        int detailsWidth = 160;

        VanillaLinearLayout detailsPanel = factory.createLinearLayout(VanillaViewFactory.VERTICAL);
        detailsPanel.setBackgroundColor(MoBendsTheme.BG_LIST);
        detailsPanel.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                               MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        // Details header
        VanillaTextView detailsHeader = factory.createTextView(I18n.get("mobends.gui.packs.details"));
        detailsHeader.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        detailsHeader.setTextSize(14);
        detailsHeader.setBold(true);
        detailsPanel.addView(detailsHeader, factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.WRAP_CONTENT
        ));

        // Placeholder for details content
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

        // Add panels to layout using weight-based sizing
        // Left panel takes remaining space (weight = 1)
        VanillaLayoutParams leftPanelParams = factory.createLayoutParams(0, VanillaLayoutParams.MATCH_PARENT, 1.0f);
        layout.addView(leftPanel, leftPanelParams);
        // Right panel has fixed width
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

        // Info text
        VanillaTextView info = factory.createTextView(I18n.get("mobends.gui.customize.editor_info"));
        info.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        info.setTextSize(14);
        info.setGravity(VanillaLinearLayout.GRAVITY_CENTER);
        layout.addView(info, factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                VanillaLayoutParams.WRAP_CONTENT
        ));

        // Spacer
        VanillaView spacer = factory.createView();
        VanillaLayoutParams spacerParams = factory.createLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                MoBendsTheme.SPACING
        );
        layout.addView(spacer, spacerParams);

        // Editor status
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

    // ==================== Event Handlers ====================

    private void onTabChanged(int tabIndex)
    {
        showTab(tabIndex);

        // Reset search when changing tabs
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

        // Determine actual tab based on whether packs are allowed
        int settingsIdx = TAB_SETTINGS;
        int packsIdx = packsAllowed ? TAB_PACKS : -1;
        int customizeIdx = packsAllowed ? TAB_CUSTOMIZE : TAB_PACKS;

        // Update visibility with fade-in for newly shown tab
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
        // TODO: Update details panel with pack info
        // For now, just log the selection
    }
}