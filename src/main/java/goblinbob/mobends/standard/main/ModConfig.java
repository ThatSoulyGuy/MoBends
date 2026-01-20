package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.util.ErrorReporter;
import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.core.util.WildcardPattern;
import goblinbob.mobends.standard.UseActionType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = ModStatics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.BooleanValue SHOW_ARROW_TRAILS;
    private static final ForgeConfigSpec.BooleanValue SHOW_SWORD_TRAIL;
    private static final ForgeConfigSpec.BooleanValue PERFORM_SPIN_ATTACK;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_USE_CLASSIFICATIONS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_ATTACK_CLASSIFICATIONS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> KEEP_ARMOR_AS_VANILLA;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> KEEP_ENTITY_AS_VANILLA;

    // Public accessors for backwards compatibility
    public static boolean showArrowTrails = true;
    public static boolean showSwordTrail = true;
    public static boolean performSpinAttack = true;
    public static String[] itemUseClassifications = new String[] {};
    public static String[] itemAttackClassifications = new String[] {};
    public static String[] keepArmorAsVanilla = new String[] {};
    public static String[] keepEntityAsVanilla = new String[] {};

    static {
        BUILDER.push("general");

        SHOW_ARROW_TRAILS = BUILDER
                .comment("Show arrow trails when arrows are in flight")
                .translation(ModStatics.MODID + ".config.show_arrow_trails")
                .define("showArrowTrails", true);

        SHOW_SWORD_TRAIL = BUILDER
                .comment("Show sword trail effects during attacks")
                .translation(ModStatics.MODID + ".config.show_sword_trails")
                .define("showSwordTrail", true);

        PERFORM_SPIN_ATTACK = BUILDER
                .comment("Enable spin attack animation")
                .translation(ModStatics.MODID + ".config.perform_spin_attack")
                .define("performSpinAttack", true);

        ITEM_USE_CLASSIFICATIONS = BUILDER
                .comment("Item use classifications (format: modid:item=ACTION)")
                .translation(ModStatics.MODID + ".config.item_use_classifications")
                .defineList("itemUseClassifications", ArrayList::new, obj -> obj instanceof String);

        ITEM_ATTACK_CLASSIFICATIONS = BUILDER
                .comment("Item attack classifications (format: modid:item=ACTION)")
                .translation(ModStatics.MODID + ".config.item_attack_classifications")
                .defineList("itemAttackClassifications", ArrayList::new, obj -> obj instanceof String);

        KEEP_ARMOR_AS_VANILLA = BUILDER
                .comment("Armor items to keep as vanilla rendering")
                .translation(ModStatics.MODID + ".config.keep_armor_as_vanilla")
                .defineList("keepArmorAsVanilla", ArrayList::new, obj -> obj instanceof String);

        KEEP_ENTITY_AS_VANILLA = BUILDER
                .comment("Entities to keep as vanilla rendering")
                .translation(ModStatics.MODID + ".config.keep_entity_as_vanilla")
                .defineList("keepEntityAsVanilla", ArrayList::new, obj -> obj instanceof String);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private static Map<Item, Boolean> keepArmorAsVanillaCache = new HashMap<>();
    private static Map<Entity, Boolean> keepEntityAsVanillaCache = new HashMap<>();
    private static Map<Item, UseActionType> itemUseClassificationCache = new HashMap<>();
    private static Map<Item, AttackActionType> itemAttackClassificationCache = new HashMap<>();
    private static LinkedList<ItemClassificationEntry<UseActionType>> itemUseClassificationEntries = new LinkedList<>();
    private static LinkedList<ItemClassificationEntry<AttackActionType>> itemAttackClassificationEntries = new LinkedList<>();

    private static List<Map<?, ?>> caches = Arrays.asList(
        keepArmorAsVanillaCache,
        keepEntityAsVanillaCache,
        itemUseClassificationCache,
        itemAttackClassificationCache
    );

    private static boolean configLoaded = false;

    @SubscribeEvent
    public static void onConfigLoading(final ModConfigEvent.Loading event)
    {
        if (event.getConfig().getModId().equals(ModStatics.MODID))
        {
            configLoaded = true;
            syncConfigValues();
        }
    }

    @SubscribeEvent
    public static void onConfigReloading(final ModConfigEvent.Reloading event)
    {
        if (event.getConfig().getModId().equals(ModStatics.MODID))
        {
            syncConfigValues();
        }
    }

    public static boolean isConfigLoaded()
    {
        return configLoaded;
    }

    private static void syncConfigValues()
    {
        if (!SPEC.isLoaded())
        {
            return;
        }

        // Sync values from config spec to static fields
        showArrowTrails = SHOW_ARROW_TRAILS.get();
        showSwordTrail = SHOW_SWORD_TRAIL.get();
        performSpinAttack = PERFORM_SPIN_ATTACK.get();
        itemUseClassifications = ITEM_USE_CLASSIFICATIONS.get().toArray(new String[0]);
        itemAttackClassifications = ITEM_ATTACK_CLASSIFICATIONS.get().toArray(new String[0]);
        keepArmorAsVanilla = KEEP_ARMOR_AS_VANILLA.get().toArray(new String[0]);
        keepEntityAsVanilla = KEEP_ENTITY_AS_VANILLA.get().toArray(new String[0]);

        // Clearing the caches
        for (Map<?, ?> cache : caches)
        {
            cache.clear();
        }

        itemUseClassificationEntries.clear();
        getOrMakeEntries(itemUseClassificationEntries, itemUseClassifications, UseActionType::valueOf);

        itemAttackClassificationEntries.clear();
        getOrMakeEntries(itemAttackClassificationEntries, itemAttackClassifications, AttackActionType::valueOf);

        MoBends.refreshSystems();
    }

    private static <T> LinkedList<ItemClassificationEntry<T>> getOrMakeEntries(LinkedList<ItemClassificationEntry<T>> entries, String[] rawEntries, Function<String, T> parseFunction)
    {
        if (rawEntries.length == 0)
            return entries;

        if (entries.size() == 0)
        {
            for (String rawEntry : rawEntries)
            {
                try
                {
                    entries.addFirst(ItemClassificationEntry.parse(rawEntry, parseFunction));
                }
                catch(MalformedConfigException e)
                {
                    ErrorReporter.showErrorToPlayer(String.format("Invalid configuration! %s", e.getMessage()));
                }
            }
        }

        return entries;
    }

    private static boolean doesLocationMatchPattern(ResourceLocation resourceLocation, String pattern)
    {
        final ResourceLocation patternLocation = new ResourceLocation(pattern);

        if (resourceLocation.equals(patternLocation))
            return true;

        WildcardPattern domainPattern = new WildcardPattern(patternLocation.getNamespace());
        WildcardPattern pathPattern = new WildcardPattern(patternLocation.getPath());

        return domainPattern.matches(resourceLocation.getNamespace()) &&
               pathPattern.matches(resourceLocation.getPath());
    }

    private static boolean checkForPatterns(ResourceLocation resourceLocation, String[] patterns)
    {
        if (resourceLocation == null)
            return false;

        final String resourceNamespace = resourceLocation.getNamespace();
        final String resourcePath = resourceLocation.getPath();

        for (String pattern : patterns)
        {
            final ResourceLocation patternLocation = new ResourceLocation(pattern);

            if (resourceLocation.equals(patternLocation))
                return true;

            WildcardPattern domainPattern = new WildcardPattern(patternLocation.getNamespace());
            WildcardPattern pathPattern = new WildcardPattern(patternLocation.getPath());

            if (!domainPattern.matches(resourceNamespace))
                continue;

            if (pathPattern.matches(resourcePath))
                return true;
        }

        return false;
    }

    public static UseActionType getItemUseAction(Item item)
    {
        // If cached before, returning the cached classification.
        return itemUseClassificationCache.computeIfAbsent(item, (i) -> {
            ResourceLocation location = ForgeRegistries.ITEMS.getKey(item);

            if (location != null)
            {
                List<ItemClassificationEntry<UseActionType>> entries = getOrMakeEntries(itemUseClassificationEntries, itemUseClassifications, UseActionType::valueOf);
                for (ItemClassificationEntry<UseActionType> e : entries)
                {
                    if (doesLocationMatchPattern(location, e.pattern))
                    {
                        return e.classification;
                    }
                }
            }

            // Unclassified
            return null;
        });
    }

    public static AttackActionType getItemAttackAction(Item item)
    {
        // If cached before, returning the cached classification.
        return itemAttackClassificationCache.computeIfAbsent(item, (i) -> {
            ResourceLocation location = ForgeRegistries.ITEMS.getKey(item);

            if (location != null)
            {
                List<ItemClassificationEntry<AttackActionType>> entries = getOrMakeEntries(itemAttackClassificationEntries, itemAttackClassifications, AttackActionType::valueOf);
                for (ItemClassificationEntry<AttackActionType> e : entries)
                {
                    if (doesLocationMatchPattern(location, e.pattern))
                    {
                        return e.classification;
                    }
                }
            }

            // Unclassified
            return null;
        });
    }

    public static boolean shouldKeepArmorAsVanilla(Item item)
    {
        // If cached before, returning the cached result.
        return keepArmorAsVanillaCache.computeIfAbsent(item, (i) -> checkForPatterns(ForgeRegistries.ITEMS.getKey(i), keepArmorAsVanilla));
    }

    public static boolean shouldKeepEntityAsVanilla(Entity entity)
    {
        // If cached before, returning the cached result.
        return keepEntityAsVanillaCache.computeIfAbsent(entity, (e) -> {
            ResourceLocation location = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());

            // The player, for example, doesn't have a key.
            return location != null && checkForPatterns(location, keepEntityAsVanilla);
        });
    }

    private static class ItemClassificationEntry<T>
    {
        public final String pattern;
        public final T classification;

        public ItemClassificationEntry(String pattern, T classification)
        {
            this.pattern = pattern;
            this.classification = classification;
        }

        public static <T> ItemClassificationEntry<T> parse(String encoded, Function<String, T> parsingFunction)
        {
            int indexOfEquals = encoded.indexOf("=");

            if (indexOfEquals == -1)
            {
                throw new IllegalArgumentException(String.format("No equals sign found in the item classification entry: %s", encoded));
            }

            String pattern = encoded.substring(0, indexOfEquals);
            String encodedActionType = encoded.substring(indexOfEquals + 1).toUpperCase();

            try
            {
                T actionType = parsingFunction.apply(encodedActionType);

                return new ItemClassificationEntry<>(pattern, actionType);
            }
            catch(IllegalArgumentException e)
            {
                throw new MalformedConfigException(String.format("Unknown action type: %s", encodedActionType), e);
            }
        }
    }
}
