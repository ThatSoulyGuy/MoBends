package goblinbob.mobends.core.configuration;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.standard.main.ModStatics;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

import java.util.*;

/**
 * Client-side configuration for Mo' Bends 1.20.1.
 */
public class CoreClientConfig extends CoreConfig
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private static ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static ModConfigSpec SPEC;

    // General settings
    public static ModConfigSpec.ConfigValue<List<? extends String>> APPLIED_PACKS;

    // Animated entities settings stored separately since they're dynamic
    private final Map<String, Boolean> animatedEntities = new HashMap<>();

    static
    {
        BUILDER.comment("Mo' Bends Client Configuration").push("general");

        APPLIED_PACKS = BUILDER
                .comment("List of applied animation pack keys")
                .defineListAllowEmpty(Collections.singletonList("appliedPacks"),
                        ArrayList::new,
                        obj -> obj instanceof String);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public CoreClientConfig()
    {
        load();
    }

    @Override
    public void save()
    {
        // Entity animation states are stored in memory and applied to benders
        // The applied packs are saved via the config spec
        LOGGER.debug("Saving Mo' Bends client configuration");
    }

    @Override
    public void load()
    {
        LOGGER.debug("Loading Mo' Bends client configuration");
    }

    public String[] getAppliedPacks()
    {
        List<? extends String> packs = APPLIED_PACKS.get();
        return packs.toArray(new String[0]);
    }

    public void setAppliedPacks(String[] packNames)
    {
        APPLIED_PACKS.set(Arrays.asList(packNames));
    }

    public void setAppliedPacks(Collection<String> packNames)
    {
        APPLIED_PACKS.set(new ArrayList<>(packNames));
    }

    public boolean isEntityAnimated(String alterEntryKey)
    {
        return animatedEntities.getOrDefault(alterEntryKey, true);
    }

    public void setEntityAnimated(String alterEntryKey, boolean animated)
    {
        animatedEntities.put(alterEntryKey, animated);
    }

    /**
     * Register this config with NeoForge.
     * Call during mod construction.
     */
    public static void register(ModContainer container)
    {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC, ModStatics.MODID + "-client.toml");
    }
}
