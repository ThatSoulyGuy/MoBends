package goblinbob.mobends.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side configuration with JSON persistence.
 */
public class CoreClientConfig
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "mobends-client.json";

    private static CoreClientConfig instance;

    private File configFile;
    private ConfigData data = new ConfigData();
    private boolean initialized = false;

    public static CoreClientConfig getInstance()
    {
        if (instance == null)
        {
            instance = new CoreClientConfig();
        }
        return instance;
    }

    /**
     * Initialize config file path and load from disk.
     * Must be called after Minecraft is available.
     */
    public void initialize()
    {
        if (initialized) return;

        try
        {
            File gameDir = Minecraft.getInstance().gameDirectory;
            File configDir = new File(gameDir, "config");
            configDir.mkdirs();
            this.configFile = new File(configDir, CONFIG_FILE_NAME);
            load();
            initialized = true;
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to initialize MoBends config", e);
        }
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
            ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
            if (loaded != null)
            {
                this.data = loaded;
                LOGGER.info("Loaded MoBends config: {} entity overrides, {} applied packs",
                        data.enabledEntities.size(), data.appliedPacks.size());
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
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to save MoBends config", e);
        }
    }

    public List<String> getAppliedPacks()
    {
        return new ArrayList<>(data.appliedPacks);
    }

    public void setAppliedPacks(List<String> packs)
    {
        data.appliedPacks = new ArrayList<>(packs);
        save();
    }

    public boolean isEnabled()
    {
        return data.enabled;
    }

    public void setEnabled(boolean enabled)
    {
        data.enabled = enabled;
        save();
    }

    public boolean isEntityEnabled(String entity)
    {
        return data.enabledEntities.getOrDefault(entity, true);
    }

    public void setEntityEnabled(String entity, boolean enabled)
    {
        data.enabledEntities.put(entity, enabled);
        save();
    }

    public boolean isEntityAnimated(String entity)
    {
        return isEntityEnabled(entity);
    }

    private static class ConfigData
    {
        boolean enabled = true;
        List<String> appliedPacks = new ArrayList<>();
        Map<String, Boolean> enabledEntities = new HashMap<>();
    }
}
