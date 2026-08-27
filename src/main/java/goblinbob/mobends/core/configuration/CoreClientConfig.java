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
            return;
        }

        try (FileReader reader = new FileReader(configFile))
        {
            ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
            if (loaded != null)
            {
                this.data = loaded;
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


    public boolean isArmorKeptVanilla(String itemId)
    {
        return data.vanillaArmorItems.getOrDefault(itemId, false);
    }

    public void setArmorKeptVanilla(String itemId, boolean keepVanilla)
    {
        if (keepVanilla)
        {
            data.vanillaArmorItems.put(itemId, true);
        }
        else
        {
            data.vanillaArmorItems.remove(itemId);
        }
        save();
    }

    public String getItemUseAction(String itemId)
    {
        return data.itemUseActions.get(itemId);
    }

    public void setItemUseAction(String itemId, String action)
    {
        if (action == null)
        {
            data.itemUseActions.remove(itemId);
        }
        else
        {
            data.itemUseActions.put(itemId, action);
        }
        save();
    }

    public String getItemAttackAction(String itemId)
    {
        return data.itemAttackActions.get(itemId);
    }

    public void setItemAttackAction(String itemId, String action)
    {
        if (action == null)
        {
            data.itemAttackActions.remove(itemId);
        }
        else
        {
            data.itemAttackActions.put(itemId, action);
        }
        save();
    }

    public String getPreviewSpinMode()
    {
        return data.previewSpinMode;
    }

    public void setPreviewSpinMode(String previewSpinMode)
    {
        data.previewSpinMode = previewSpinMode;
        save();
    }

    public String getBetterCombatAnimations()
    {
        return data.betterCombatAnimations;
    }

    public void setBetterCombatAnimations(String mode)
    {
        data.betterCombatAnimations = mode;
        save();
    }

    private static class ConfigData
    {
        List<String> appliedPacks = new ArrayList<>();
        Map<String, Boolean> enabledEntities = new HashMap<>();

        Map<String, Boolean> vanillaArmorItems = new HashMap<>();

        Map<String, String> itemUseActions = new HashMap<>();

        Map<String, String> itemAttackActions = new HashMap<>();
        String previewSpinMode = "HOVER";
        String betterCombatAnimations = null;
    }
}
