package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.standard.UseActionType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

public class ModConfig
{
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    public static boolean showSwordTrail = true;

    public static boolean swordTrailFullBright = false;

    public static boolean showArrowTrails = true;

    public static boolean arrowTrailFullBright = false;

    public static boolean arrowTrailPotionColor = true;

    public static boolean spectralArrowTrailEffect = true;

    public static boolean tridentTrail = true;

    public static boolean newEnchantGlint = false;

    public static boolean performSpinAttack = true;

    public static boolean mobsCanSpin = false;

    public static boolean disableMovementInGui = false;

    // Per-item overrides, read from config/mobends-client.json via CoreClientConfig. These used to
    // return a hardcoded neutral answer for every item, so the per-item opt-outs the 1.12 line had
    // were unreachable even though the call sites still consulted them.
    //
    // Deleted alongside the wiring: getInstance, isEntityEnabled, isAnimationsEnabled and
    // shouldKeepEntityAsVanilla, which had the same shape but no callers at all. Per-entity
    // enable/disable is not gone with them — that lives on CoreClientConfig and was already wired.

    /** Registry id of an item, or null if it somehow is not registered. */
    private static String idOf(Item item)
    {
        if (item == null) return null;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return id != null ? id.toString() : null;
    }

    /**
     * Parses an enum name from config, returning null rather than throwing on a bad value.
     *
     * <p>These names come from a hand-editable JSON file, so a typo is expected input, not a bug.
     * Falling back to null puts the caller on its normal automatic path.
     */
    private static <E extends Enum<E>> E parseAction(Class<E> type, String name, String itemId)
    {
        if (name == null) return null;
        try
        {
            return Enum.valueOf(type, name.trim().toUpperCase(java.util.Locale.ROOT));
        }
        catch (IllegalArgumentException e)
        {
            LOGGER.warn("Unknown {} '{}' configured for item '{}'. Expected one of {}.",
                    type.getSimpleName(), name, itemId, java.util.Arrays.toString(type.getEnumConstants()));
            return null;
        }
    }

    /** Whether this armor should render as vanilla instead of being bent. */
    public static boolean shouldKeepArmorAsVanilla(ArmorItem armorItem)
    {
        String id = idOf(armorItem);
        return id != null && CoreClientConfig.getInstance().isArmorKeptVanilla(id);
    }

    /** Configured use animation for this item, or null to decide from the vanilla arm pose. */
    public static UseActionType getItemUseAction(Item item)
    {
        String id = idOf(item);
        if (id == null) return null;
        return parseAction(UseActionType.class, CoreClientConfig.getInstance().getItemUseAction(id), id);
    }

    /** Configured attack animation for this item, or null to decide from the item's class. */
    public static AttackActionType getItemAttackAction(Item item)
    {
        String id = idOf(item);
        if (id == null) return null;
        return parseAction(AttackActionType.class, CoreClientConfig.getInstance().getItemAttackAction(id), id);
    }
}
