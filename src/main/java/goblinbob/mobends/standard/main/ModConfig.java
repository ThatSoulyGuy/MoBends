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

    public static boolean performSpinAttack = true;

    public static boolean mobsCanSpin = false;

    public static boolean disableMovementInGui = false;


    private static String idOf(Item item)
    {
        if (item == null) return null;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return id != null ? id.toString() : null;
    }

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

    public static boolean shouldKeepArmorAsVanilla(ArmorItem armorItem)
    {
        String id = idOf(armorItem);
        return id != null && CoreClientConfig.getInstance().isArmorKeptVanilla(id);
    }

    public static UseActionType getItemUseAction(Item item)
    {
        String id = idOf(item);
        if (id == null) return null;
        return parseAction(UseActionType.class, CoreClientConfig.getInstance().getItemUseAction(id), id);
    }

    public static AttackActionType getItemAttackAction(Item item)
    {
        String id = idOf(item);
        if (id == null) return null;
        return parseAction(AttackActionType.class, CoreClientConfig.getInstance().getItemAttackAction(id), id);
    }
}
