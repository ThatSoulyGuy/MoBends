package goblinbob.mobends.core.supporters;

import goblinbob.mobends.core.util.Color;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Stub implementation of SupporterContent for common-mc.
 * Platform-specific implementations can override this.
 */
public class SupporterContent
{
    /**
     * Gets all registered accessories.
     * @return Empty set in stub implementation
     */
    public static Set<Map.Entry<String, AccessoryDetails>> getAccessories()
    {
        return Collections.emptySet();
    }

    /**
     * Gets accessory settings for a player.
     * @param player The player
     * @return Empty map in stub implementation
     */
    public static Map<String, AccessorySettings> getAccessorySettingsMapFor(Player player)
    {
        return Collections.emptyMap();
    }

    /**
     * Gets trail color for a player.
     * @param player The player
     * @return Default white color
     */
    public static Color getTrailColorFor(Player player)
    {
        return new Color(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
