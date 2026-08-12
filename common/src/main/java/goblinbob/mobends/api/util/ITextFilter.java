package goblinbob.mobends.api.util;

public interface ITextFilter
{
    String filterText(String text);

    boolean isAllowedChatCharacter(char c);

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
