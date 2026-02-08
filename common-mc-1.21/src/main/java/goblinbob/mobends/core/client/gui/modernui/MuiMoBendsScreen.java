package goblinbob.mobends.core.client.gui.modernui;

import goblinbob.mobends.api.gui.modernui.ILayoutParams;
import goblinbob.mobends.api.gui.modernui.IMuiScreenBuilder;
import goblinbob.mobends.api.gui.modernui.IViewFactory;
import goblinbob.mobends.api.gui.modernui.view.*;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.gui.modernui.theme.MoBendsTheme;
import goblinbob.mobends.core.client.gui.modernui.widget.MuiBenderListView;
import goblinbob.mobends.core.client.gui.modernui.widget.MuiEntityPreview;
import goblinbob.mobends.core.client.gui.modernui.widget.MuiPackListView;
import goblinbob.mobends.core.client.gui.modernui.widget.MuiTabBar;
import goblinbob.mobends.core.pack.IBendsPack;
import goblinbob.mobends.core.network.NetworkConfiguration;
import net.minecraft.client.resources.language.I18n;

/**
 * Modern UI implementation of the MoBends settings screen.
 * Uses the abstraction layer to work across both Modern UI versions.
 */
public class MuiMoBendsScreen implements IMuiScreenBuilder
{
    // Tab indices
    private static final int TAB_SETTINGS = 0;
    private static final int TAB_PACKS = 1;
    private static final int TAB_CUSTOMIZE = 2;

    // Colors matching vanilla GuiMoBendsScreen
    private static final int COLOR_SETTINGS = 0xFFDA3A00;
    private static final int COLOR_PACKS = 0xFF4577DE;
    private static final int COLOR_CUSTOMIZE = 0xFF26DAA3;

    // State
    private MuiTabBar tabBar;
    private MuiBenderListView benderList;
    private MuiEntityPreview entityPreview;
    private MuiPackListView packList;
    private IMuiFrameLayout contentFrame;
    private IMuiView settingsContent;
    private IMuiView packsContent;
    private IMuiView customizeContent;
    private IMuiTextField searchField;
    private IMuiTextField packSearchField;
    private final EntityBenderRegistry.Filter filter = new EntityBenderRegistry.Filter();

    @Override
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
    public MuiEntityPreview getEntityPreview()
    {
        return entityPreview;
    }

    @Override
    public IMuiView buildContent(IViewFactory factory)
    {
        // Create root layout with FrameLayout.LayoutParams for proper positioning in the fragment container
        IMuiLinearLayout root = factory.createLinearLayout(IViewFactory.VERTICAL);
        // Use FrameLayout params since the fragment container is a FrameLayout
        root.setLayoutParams(factory.createFrameLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.GRAVITY_CENTER
        ));
        root.setBackgroundColor(MoBendsTheme.BG_PANEL);

        // ==================== Header with Title ====================

        IMuiLinearLayout header = factory.createLinearLayout(IViewFactory.VERTICAL);
        header.setLayoutParams(factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                MoBendsTheme.HEADER_HEIGHT
        ));
        header.setBackgroundColor(MoBendsTheme.BG_HEADER);
        header.setGravity(IMuiLinearLayout.GRAVITY_CENTER);
        header.setPadding(0, MoBendsTheme.PADDING, 0, 0);

        IMuiTextView title = factory.createTextView(I18n.get("mobends.gui.title"));
        title.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        title.setTextSize(18);
        title.setBold(true);
        title.setGravity(IMuiLinearLayout.GRAVITY_CENTER);
        header.addView(title, factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.WRAP_CONTENT
        ));

        root.addView(header, factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                MoBendsTheme.HEADER_HEIGHT
        ));

        // ==================== Tab Bar ====================

        tabBar = new MuiTabBar(factory);
        tabBar.addTab("mobends.gui.section.settings", COLOR_SETTINGS);
        if (NetworkConfiguration.instance.areBendsPacksAllowed())
        {
            tabBar.addTab("mobends.gui.section.packs", COLOR_PACKS);
        }
        tabBar.addTab("mobends.gui.section.customize", COLOR_CUSTOMIZE);
        tabBar.setOnTabChanged(this::onTabChanged);

        ILayoutParams tabParams = factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
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

        // Initially show settings tab
        showTab(TAB_SETTINGS);

        root.addView(contentFrame, factory.createMatchParent());

        return root;
    }

    // ==================== Tab Content Builders ====================

    private IMuiView buildSettingsContent(IViewFactory factory)
    {
        // Horizontal layout: left (list) | right (preview)
        IMuiLinearLayout layout = factory.createLinearLayout(IViewFactory.HORIZONTAL);
        layout.setLayoutParams(factory.createMatchParent());
        layout.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                         MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        // ==================== Left Panel (Search + Bender List) ====================

        IMuiLinearLayout leftPanel = factory.createLinearLayout(IViewFactory.VERTICAL);
        ILayoutParams leftParams = factory.createLayoutParams(0, ILayoutParams.MATCH_PARENT);
        // Weight simulation - will take remaining space
        leftPanel.setLayoutParams(leftParams);

        // Search field
        searchField = factory.createTextField(I18n.get("mobends.gui.search"));
        searchField.setOnTextChangedListener(this::onSearchTextChanged);
        ILayoutParams searchParams = factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                MoBendsTheme.BUTTON_HEIGHT
        );
        searchParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        leftPanel.addView(searchField, searchParams);

        // Bender list
        benderList = new MuiBenderListView(factory);
        benderList.setOnBenderSelected(this::onBenderSelected);
        benderList.setOnAnimationSelected(this::onAnimationSelected);
        benderList.populateFromRegistry(filter);

        leftPanel.addView(benderList.getView(), factory.createMatchParent());

        // ==================== Right Panel (Entity Preview) ====================

        // Fixed width for preview panel
        int previewWidth = 180;

        entityPreview = new MuiEntityPreview(factory, previewWidth, 0);
        ILayoutParams previewParams = factory.createLayoutParams(
                previewWidth,
                ILayoutParams.MATCH_PARENT
        );
        previewParams.setMargins(MoBendsTheme.SPACING, 0, 0, 0);

        // Add panels to layout using weight-based sizing
        // Left panel takes remaining space (weight = 1)
        ILayoutParams leftPanelParams = factory.createLayoutParams(0, ILayoutParams.MATCH_PARENT, 1.0f);
        layout.addView(leftPanel, leftPanelParams);
        // Right panel has fixed width
        layout.addView(entityPreview.getView(), previewParams);

        return layout;
    }

    private IMuiView buildPacksContent(IViewFactory factory)
    {
        // Horizontal layout: left (pack list) | right (pack details)
        IMuiLinearLayout layout = factory.createLinearLayout(IViewFactory.HORIZONTAL);
        layout.setLayoutParams(factory.createMatchParent());
        layout.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                         MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        // ==================== Left Panel (Search + Pack List) ====================

        IMuiLinearLayout leftPanel = factory.createLinearLayout(IViewFactory.VERTICAL);

        // Search field for packs
        packSearchField = factory.createTextField(I18n.get("mobends.gui.search"));
        packSearchField.setOnTextChangedListener(this::onPackSearchTextChanged);
        ILayoutParams searchParams = factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                MoBendsTheme.BUTTON_HEIGHT
        );
        searchParams.setMargins(0, 0, 0, MoBendsTheme.SPACING);
        leftPanel.addView(packSearchField, searchParams);

        // Pack list
        packList = new MuiPackListView(factory);
        packList.setOnPackSelected(this::onPackSelected);
        packList.populateFromManager();

        leftPanel.addView(packList.getView(), factory.createMatchParent());

        // ==================== Right Panel (Pack Details) ====================

        int detailsWidth = 200;

        IMuiLinearLayout detailsPanel = factory.createLinearLayout(IViewFactory.VERTICAL);
        detailsPanel.setBackgroundColor(MoBendsTheme.BG_LIST);
        detailsPanel.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                               MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        // Details header
        IMuiTextView detailsHeader = factory.createTextView(I18n.get("mobends.gui.packs.details"));
        detailsHeader.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        detailsHeader.setTextSize(14);
        detailsHeader.setBold(true);
        detailsPanel.addView(detailsHeader, factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.WRAP_CONTENT
        ));

        // Placeholder for details content
        IMuiTextView detailsPlaceholder = factory.createTextView(I18n.get("mobends.gui.packs.select_pack"));
        detailsPlaceholder.setTextColor(MoBendsTheme.TEXT_HINT);
        detailsPlaceholder.setTextSize(12);
        ILayoutParams placeholderParams = factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.WRAP_CONTENT
        );
        placeholderParams.setMargins(0, MoBendsTheme.SPACING, 0, 0);
        detailsPanel.addView(detailsPlaceholder, placeholderParams);

        ILayoutParams detailsParams = factory.createLayoutParams(
                detailsWidth,
                ILayoutParams.MATCH_PARENT
        );
        detailsParams.setMargins(MoBendsTheme.SPACING, 0, 0, 0);

        // Add panels to layout using weight-based sizing
        // Left panel takes remaining space (weight = 1)
        ILayoutParams leftPanelParams = factory.createLayoutParams(0, ILayoutParams.MATCH_PARENT, 1.0f);
        layout.addView(leftPanel, leftPanelParams);
        // Right panel has fixed width
        layout.addView(detailsPanel, detailsParams);

        return layout;
    }

    private IMuiView buildCustomizeContent(IViewFactory factory)
    {
        IMuiLinearLayout layout = factory.createLinearLayout(IViewFactory.VERTICAL);
        layout.setLayoutParams(factory.createMatchParent());
        layout.setGravity(IMuiLinearLayout.GRAVITY_CENTER);
        layout.setPadding(MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING_LARGE,
                         MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING_LARGE);

        // Info text
        IMuiTextView info = factory.createTextView(I18n.get("mobends.gui.customize.editor_info"));
        info.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        info.setTextSize(14);
        info.setGravity(IMuiLinearLayout.GRAVITY_CENTER);
        layout.addView(info, factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.WRAP_CONTENT
        ));

        // Spacer
        IMuiView spacer = factory.createView();
        ILayoutParams spacerParams = factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                MoBendsTheme.SPACING
        );
        layout.addView(spacer, spacerParams);

        // Editor status
        IMuiTextView status = factory.createTextView(I18n.get("mobends.gui.customize.no_editor"));
        status.setTextColor(MoBendsTheme.ACCENT_ERROR);
        status.setTextSize(12);
        status.setGravity(IMuiLinearLayout.GRAVITY_CENTER);
        layout.addView(status, factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.WRAP_CONTENT
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

        // Update visibility
        settingsContent.setVisibility(tabIndex == settingsIdx ? IMuiView.VISIBLE : IMuiView.GONE);
        packsContent.setVisibility(tabIndex == packsIdx ? IMuiView.VISIBLE : IMuiView.GONE);
        customizeContent.setVisibility(tabIndex == customizeIdx ? IMuiView.VISIBLE : IMuiView.GONE);
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
