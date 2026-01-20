package goblinbob.mobends.core.supporters;

import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.connection.ConnectionManager;
import goblinbob.mobends.core.connection.PlayerSettingsResponse;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.module.IModule;
import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.core.util.IColorRead;
import net.minecraft.world.entity.LivingEntity;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static goblinbob.mobends.core.util.ConnectionHelper.sendGetRequest;

public class SupporterContent
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static SupporterContent INSTANCE;
    private static final Color DEFAULT_TRAIL_COLOR = new Color(Color.WHITE);

    private final String apiUrl;
    private final Map<String, AccessoryDetails> accessoryDetailsMap = new HashMap<>();
    private final Map<String, PlayerSettingsResponse> accessorySettingsPerPlayer = new HashMap<>();

    private SupporterContent()
    {
        this.apiUrl = EnvironmentModule.getConfig().getApiUrl();
    }

    public static void fetchAccessoryDetails()
    {
        try
        {
            AccessoryDetailsResponse response = sendGetRequest(new URL(INSTANCE.apiUrl + "/api/accessory/details"), new HashMap<>(), AccessoryDetailsResponse.class);

            Map<String, AccessoryDetails> details = Objects.requireNonNull(response).getDetails();
            INSTANCE.accessoryDetailsMap.clear();
            INSTANCE.accessoryDetailsMap.putAll(details);
        }
        catch (HttpHostConnectException e)
        {
            // No internet, do nothing.
        }
        catch (JsonParseException e)
        {
            LOGGER.warn("Failed to parse accessory details map.");
            e.printStackTrace();
        }
        catch (NullPointerException | IOException | URISyntaxException e)
        {
            LOGGER.warn("Failed to get accessory details map.");
            e.printStackTrace();
        }
    }

    private void clearCache()
    {
        synchronized (accessorySettingsPerPlayer)
        {
            this.accessorySettingsPerPlayer.clear();
        }
    }

    public static void registerPlayerAccessorySettings(String playerName, PlayerSettingsResponse settings)
    {
        synchronized (INSTANCE.accessorySettingsPerPlayer)
        {
            INSTANCE.accessorySettingsPerPlayer.put(playerName, settings);
        }
    }

    public static Set<Map.Entry<String, AccessoryDetails>> getAccessories()
    {
        return Collections.unmodifiableSet(INSTANCE.accessoryDetailsMap.entrySet());
    }

    public static Map<String, AccessorySettings> getAccessorySettingsMapFor(LivingEntity entity)
    {
        final String name = entity.getName().getString();

        PlayerSettingsResponse settings;

        synchronized (INSTANCE.accessorySettingsPerPlayer)
        {
            settings = INSTANCE.accessorySettingsPerPlayer.get(name);
        }

        if (settings == null)
        {
            ConnectionManager.INSTANCE.fetchSettingsForPlayer(name);
            return Collections.emptyMap();
        }

        return settings.getSettings();
    }

    public static IColorRead getTrailColorFor(LivingEntity entity)
    {
        Map<String, AccessorySettings> settingsMap = getAccessorySettingsMapFor(entity);
        AccessorySettings swordTrailSettings = settingsMap.get("sword_trail");

        if (swordTrailSettings == null)
        {
            return DEFAULT_TRAIL_COLOR;
        }

        return swordTrailSettings.getColor();
    }

    public static class Factory implements IModule
    {
        @Override
        public void init()
        {
            // Doing it here so that we know the module's been registered.
            // Otherwise, this module wouldn't get refreshed and we wouldn't know.
            INSTANCE = new SupporterContent();
            fetchAccessoryDetails();
        }

        @Override
        public void onRefresh()
        {
            fetchAccessoryDetails();
            INSTANCE.clearCache();
        }
    }
}
