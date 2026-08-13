package goblinbob.mobends.core.supporters;

import goblinbob.mobends.core.util.Color;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class SupporterContent
{
    public static Set<Map.Entry<String, AccessoryDetails>> getAccessories()
    {
        return Collections.emptySet();
    }

    public static Map<String, AccessorySettings> getAccessorySettingsMapFor(Player player)
    {
        return Collections.emptyMap();
    }

    public static Color getTrailColorFor(Player player)
    {
        return new Color(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
