package goblinbob.mobends.core.client.gui.theme;

/**
 * Theme constants for MoBends UI screens.
 * Modern dark theme with accent color highlights.
 */
public final class MoBendsTheme
{
    private MoBendsTheme()
    {
        // Constants class
    }

    // ==================== Panel Colors ====================

    /**
     * Main panel background color — dark but not pitch black.
     */
    public static final int BG_PANEL = 0xF0181A20;

    /**
     * Content area background — slightly lighter than panel.
     */
    public static final int BG_CONTENT = 0xF01E2028;

    /**
     * List background color.
     */
    public static final int BG_LIST = 0xF0161820;

    /**
     * Header/title bar background.
     */
    public static final int BG_HEADER = 0xFF12141A;

    // ==================== Button Colors ====================

    /**
     * Normal button background.
     */
    public static final int BG_BUTTON = 0xFF2A2D38;

    /**
     * Button hover state.
     */
    public static final int BG_BUTTON_HOVER = 0xFF363A48;

    /**
     * Button pressed/active state.
     */
    public static final int BG_BUTTON_PRESSED = 0xFF424858;

    /**
     * Disabled button background.
     */
    public static final int BG_BUTTON_DISABLED = 0xFF1E2028;

    // ==================== Toggle Colors ====================

    /**
     * Toggle on state color — vibrant green.
     */
    public static final int TOGGLE_ON = 0xFF43D9AD;

    /**
     * Toggle off state color.
     */
    public static final int TOGGLE_OFF = 0xFF404458;

    // ==================== List Item Colors ====================

    /**
     * Normal list item background — transparent.
     */
    public static final int BG_LIST_ITEM = 0x00000000;

    /**
     * Selected list item background.
     */
    public static final int BG_LIST_ITEM_SELECTED = 0x302A6FFF;

    /**
     * Hovered list item background.
     */
    public static final int BG_LIST_ITEM_HOVER = 0x18FFFFFF;

    // ==================== Tab Colors ====================

    /**
     * Settings tab accent color — warm orange.
     */
    public static final int COLOR_SETTINGS = 0xFFFF8C42;

    /**
     * Packs tab accent color — cool blue.
     */
    public static final int COLOR_PACKS = 0xFF5B9BFF;

    /**
     * Customize tab accent color — teal/mint.
     */
    public static final int COLOR_CUSTOMIZE = 0xFF43D9AD;

    /**
     * Active tab background.
     */
    public static final int BG_TAB_ACTIVE = 0xFF252830;

    /**
     * Inactive tab background.
     */
    public static final int BG_TAB_INACTIVE = 0xFF181A22;

    // ==================== Text Colors ====================

    /**
     * Primary text color — soft white.
     */
    public static final int TEXT_PRIMARY = 0xFFE8ECF4;

    /**
     * Secondary text color — muted.
     */
    public static final int TEXT_SECONDARY = 0xFF9098B0;

    /**
     * Disabled text color.
     */
    public static final int TEXT_DISABLED = 0xFF505468;

    /**
     * Hint/placeholder text color.
     */
    public static final int TEXT_HINT = 0xFF686E88;

    // ==================== Accent Colors ====================

    /**
     * Primary accent color — bright blue.
     */
    public static final int ACCENT_PRIMARY = 0xFF5B9BFF;

    /**
     * Error/warning color — soft red.
     */
    public static final int ACCENT_ERROR = 0xFFFF6B6B;

    /**
     * Info color — blue.
     */
    public static final int ACCENT_INFO = 0xFF5B9BFF;

    // ==================== Scroll Bar Colors ====================

    /**
     * Scroll bar track color.
     */
    public static final int SCROLLBAR_TRACK = 0xFF141620;

    /**
     * Scroll bar thumb color.
     */
    public static final int SCROLLBAR_THUMB = 0xFF3A3E50;

    /**
     * Scroll bar thumb hover color.
     */
    public static final int SCROLLBAR_THUMB_HOVER = 0xFF4A5068;

    // ==================== Border Colors ====================

    /**
     * Default border color.
     */
    public static final int BORDER = 0xFF2A2E3C;

    /**
     * Focused element border.
     */
    public static final int BORDER_FOCUSED = 0xFF5B9BFF;

    // ==================== Spacing Constants ====================

    /**
     * Standard padding in dp.
     */
    public static final int PADDING = 5;

    /**
     * Large padding in dp.
     */
    public static final int PADDING_LARGE = 10;

    /**
     * Small padding in dp.
     */
    public static final int PADDING_SMALL = 2;

    /**
     * Standard spacing between elements in dp.
     */
    public static final int SPACING = 3;

    /**
     * Corner radius for rounded elements in dp.
     */
    public static final float CORNER_RADIUS = 5f;

    /**
     * Header height in dp.
     */
    public static final int HEADER_HEIGHT = 28;

    /**
     * Tab height in dp.
     */
    public static final int TAB_HEIGHT = 24;

    /**
     * Button height in dp.
     */
    public static final int BUTTON_HEIGHT = 20;

    /**
     * List item height in dp.
     */
    public static final int LIST_ITEM_HEIGHT = 26;
}
