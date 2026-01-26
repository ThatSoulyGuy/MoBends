package goblinbob.mobends.api.gui;

/**
 * Platform-agnostic screen helper for methods with different signatures.
 *
 * renderBackground:
 * - MC 1.21.1: renderBackground(GuiGraphics, int mouseX, int mouseY, float partialTicks)
 * - MC 1.20.1: renderBackground(GuiGraphics)
 *
 * mouseScrolled:
 * - MC 1.21.1: mouseScrolled(double, double, double scrollX, double scrollY) - 4 params
 * - MC 1.20.1: mouseScrolled(double, double, double scrollY) - 3 params
 */
public interface IScreenHelper
{
    /**
     * Renders the screen background.
     *
     * @param screen The screen instance
     * @param guiGraphics The GuiGraphics instance
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @param partialTicks Partial ticks
     */
    void renderBackground(Object screen, Object guiGraphics, int mouseX, int mouseY, float partialTicks);

    /**
     * Calls super.mouseScrolled with the appropriate signature.
     *
     * @param screen The screen instance
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @param scrollX Scroll X amount (ignored in 1.20.1)
     * @param scrollY Scroll Y amount
     * @return Result from super.mouseScrolled
     */
    boolean superMouseScrolled(Object screen, double mouseX, double mouseY, double scrollX, double scrollY);

    /**
     * Singleton holder for the platform-specific implementation.
     */
    class Holder
    {
        private static IScreenHelper helper;

        public static void setHelper(IScreenHelper helper)
        {
            Holder.helper = helper;
        }

        public static IScreenHelper getHelper()
        {
            return helper;
        }
    }
}
