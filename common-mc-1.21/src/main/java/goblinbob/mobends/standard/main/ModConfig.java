package goblinbob.mobends.standard.main;

import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.standard.UseActionType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

/**
 * Mod configuration abstraction.
 * Platform-specific implementations provide the actual config values.
 */
public class ModConfig
{
    private static ModConfig instance;

    /**
     * Whether to show sword trails (static field for compatibility).
     */
    public static boolean showSwordTrail = true;

    /**
     * Whether to show arrow trails (static field for compatibility).
     */
    public static boolean showArrowTrails = true;

    /**
     * Whether to perform spin attack animation.
     */
    public static boolean performSpinAttack = true;

    public static boolean disableMovementInGui = false;

    public static ModConfig getInstance()
    {
        if (instance == null)
        {
            instance = new ModConfig();
        }
        return instance;
    }

    /**
     * @param entityKey The entity key (e.g., "player", "zombie")
     * @return Whether the entity animation is enabled
     */
    public boolean isEntityEnabled(String entityKey)
    {
        return true;
    }

    /**
     * @return Whether animations are globally enabled
     */
    public boolean isAnimationsEnabled()
    {
        return true;
    }

    /**
     * Checks if the armor should render with vanilla rendering instead of Mo' Bends.
     * @param armorItem The armor item to check
     * @return true if the armor should use vanilla rendering
     */
    public static boolean shouldKeepArmorAsVanilla(ArmorItem armorItem)
    {
        return false;
    }

    /**
     * Checks if an entity should use vanilla rendering instead of Mo' Bends.
     * @param entity The entity to check
     * @return true if the entity should use vanilla rendering
     */
    public static boolean shouldKeepEntityAsVanilla(LivingEntity entity)
    {
        return false;
    }

    /**
     * Gets the use action type for an item.
     * @param item The item
     * @return The use action type, or null to use built-in logic
     */
    public static UseActionType getItemUseAction(Item item)
    {
        // Return null to let built-in logic handle it
        // Built-in logic properly handles food components, bow, crossbow, shield, etc.
        return null;
    }

    /**
     * Gets the attack action type for an item.
     * @param item The item
     * @return The attack action type, or null to use built-in logic
     */
    public static AttackActionType getItemAttackAction(Item item)
    {
        // Return null to let built-in logic handle it
        // Built-in logic properly handles Items.AIR -> FISTS, SwordItem -> SWORD, etc.
        return null;
    }
}
