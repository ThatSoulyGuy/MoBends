package goblinbob.mobends.core.util;

public class WildcardPattern
{
    private String pattern;

    public WildcardPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public boolean matches(String check)
    {
        final boolean startsWithWildcard =  pattern.startsWith("*");
        final boolean endsWithWildcard =  pattern.endsWith("*");

        return pattern.equals("*") ||
                startsWithWildcard && endsWithWildcard && check.contains(pattern.substring(1, pattern.length() - 1)) ||
                startsWithWildcard && check.endsWith(pattern.substring(1)) ||
                endsWithWildcard && check.startsWith(pattern.substring(0, pattern.length() - 1)) ||
                check.equals(pattern);
    }
}
