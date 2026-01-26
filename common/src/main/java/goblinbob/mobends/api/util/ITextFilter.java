package goblinbob.mobends.api.util;

/**
 * Platform-agnostic text filtering interface.
 *
 * In MC 1.21.1: StringUtil.filterText() and StringUtil.isAllowedChatCharacter()
 * In MC 1.20.1: SharedConstants.filterText() and SharedConstants.isAllowedChatCharacter()
 */
public interface ITextFilter
{
    /**
     * Filters a string to only contain allowed characters.
     *
     * @param text The text to filter
     * @return The filtered text
     */
    String filterText(String text);

    /**
     * Checks if a character is allowed in chat.
     *
     * @param c The character to check
     * @return True if the character is allowed
     */
    boolean isAllowedChatCharacter(char c);

    /**
     * Singleton holder for the platform-specific implementation.
     */
    class Holder
    {
        private static ITextFilter filter;

        public static void setFilter(ITextFilter filter)
        {
            Holder.filter = filter;
        }

        public static ITextFilter getFilter()
        {
            return filter;
        }
    }
}
