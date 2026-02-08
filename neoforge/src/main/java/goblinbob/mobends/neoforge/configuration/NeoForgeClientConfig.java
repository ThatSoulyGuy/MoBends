package goblinbob.mobends.neoforge.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NeoForge-specific client configuration that persists settings to disk.
 */
public class NeoForgeClientConfig extends CoreClientConfig
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "mobends-client.json";

    private static NeoForgeClientConfig instance;

    private File configFile;
    private ConfigData data;

    public static NeoForgeClientConfig getInstance()
    {
        if (instance == null)
        {
            instance = new NeoForgeClientConfig();
        }
        return instance;
    }

    private NeoForgeClientConfig()
    {
        this.data = new ConfigData();
    }

    /**
     * Initialize the config with the game directory.
     * Must be called after Minecraft is initialized.
     */
    public void initialize()
    {
        File gameDir = Minecraft.getInstance().gameDirectory;
        File configDir = new File(gameDir, "config");
        configDir.mkdirs();
        this.configFile = new File(configDir, CONFIG_FILE_NAME);

        load();
    }

    private void load()
    {
        if (configFile == null || !configFile.exists())
        {
            LOGGER.info("MoBends config file not found, using defaults");
            return;
        }

        try (FileReader reader = new FileReader(configFile))
        {
            Type type = new TypeToken<ConfigData>(){}.getType();
            ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
            if (loaded != null)
            {
                this.data = loaded;
                LOGGER.info("Loaded MoBends config with {} applied packs", data.appliedPacks.size());
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to load MoBends config", e);
        }
    }

    private void save()
    {
        if (configFile == null)
        {
            LOGGER.warn("Config file not initialized, cannot save");
            return;
        }

        try (FileWriter writer = new FileWriter(configFile))
        {
            GSON.toJson(data, writer);
            LOGGER.debug("Saved MoBends config with {} applied packs", data.appliedPacks.size());
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to save MoBends config", e);
        }
    }

    @Override
    public List<String> getAppliedPacks()
    {
        return new ArrayList<>(data.appliedPacks);
    }

    @Override
    public void setAppliedPacks(List<String> packs)
    {
        data.appliedPacks = new ArrayList<>(packs);
        LOGGER.info("Setting applied packs: {}", packs);
        save();
    }

    @Override
    public boolean isEnabled()
    {
        return data.enabled;
    }

    public void setEnabled(boolean enabled)
    {
        data.enabled = enabled;
        save();
    }

    @Override
    public boolean isEntityEnabled(String entity)
    {
        return data.enabledEntities.getOrDefault(entity, true);
    }

    public void setEntityEnabled(String entity, boolean enabled)
    {
        data.enabledEntities.put(entity, enabled);
        save();
    }

    /**
     * Internal data structure for JSON serialization.
     */
    private static class ConfigData
    {
        boolean enabled = true;
        List<String> appliedPacks = new ArrayList<>();
        Map<String, Boolean> enabledEntities = new HashMap<>();
    }
}
