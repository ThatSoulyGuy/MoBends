package goblinbob.mobends.core.util;

import goblinbob.mobends.core.configuration.CoreClientConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class CustomWeapons
{
    private static final String DEFAULT_NAMESPACE = "minecraft";

    private static List<Pattern> patterns;
    private static final Map<Item, Boolean> cache = new HashMap<>();

    private CustomWeapons()
    {
    }

    public static boolean matches(Item item)
    {
        if (item == null)
        {
            return false;
        }

        final List<Pattern> compiled = compiledPatterns();
        if (compiled.isEmpty())
        {
            return false;
        }

        Boolean cached = cache.get(item);
        if (cached != null)
        {
            return cached;
        }

        final ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        boolean result = false;

        if (key != null)
        {
            final String id = key.toString();
            for (Pattern pattern : compiled)
            {
                if (pattern.matcher(id).matches())
                {
                    result = true;
                    break;
                }
            }
        }

        cache.put(item, result);
        return result;
    }

    public static void invalidate()
    {
        patterns = null;
        cache.clear();
    }

    @Nullable
    public static String normalize(@Nullable String input)
    {
        if (input == null)
        {
            return null;
        }

        String id = input.trim().toLowerCase(Locale.ROOT);
        if (id.isEmpty())
        {
            return null;
        }

        if (!id.contains(":"))
        {
            id = DEFAULT_NAMESPACE + ":" + id;
        }

        for (int i = 0; i < id.length(); ++i)
        {
            final char c = id.charAt(i);
            final boolean allowed = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '_' || c == '-' || c == '.' || c == '/' || c == ':' || c == '*';
            if (!allowed)
            {
                return null;
            }
        }

        return id;
    }

    private static List<Pattern> compiledPatterns()
    {
        if (patterns == null)
        {
            final List<Pattern> compiled = new ArrayList<>();
            for (String entry : CoreClientConfig.getInstance().getCustomWeapons())
            {
                final String id = normalize(entry);
                if (id != null)
                {
                    compiled.add(toPattern(id));
                }
            }
            patterns = compiled;
        }
        return patterns;
    }

    private static Pattern toPattern(String id)
    {
        final StringBuilder regex = new StringBuilder();
        int start = 0;

        for (int i = 0; i <= id.length(); ++i)
        {
            if (i == id.length() || id.charAt(i) == '*')
            {
                if (i > start)
                {
                    regex.append(Pattern.quote(id.substring(start, i)));
                }
                if (i < id.length())
                {
                    regex.append(".*");
                }
                start = i + 1;
            }
        }

        return Pattern.compile(regex.toString());
    }
}
