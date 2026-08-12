package goblinbob.mobends.platform;

import goblinbob.mobends.api.resource.ILocalization;
import net.minecraft.client.resources.language.I18n;

public class McLocalization implements ILocalization
{
    @Override
    public String get(String key)
    {
        return I18n.get(key);
    }

    @Override
    public String get(String key, Object... args)
    {
        return I18n.get(key, args);
    }

    @Override
    public boolean has(String key)
    {
        return I18n.exists(key);
    }
}
