package goblinbob.mobends.standard.main;

import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.standard.UseActionType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

public class ModConfig
{
    private static ModConfig instance;

    public static boolean showSwordTrail = true;

    public static boolean swordTrailFullBright = false;

    public static boolean showArrowTrails = true;

    public static boolean arrowTrailFullBright = false;

    public static boolean tridentTrail = true;

    public static boolean newEnchantGlint = false;

    public static boolean performSpinAttack = true;

    public static boolean mobsCanSpin = false;

    public static boolean disableMovementInGui = false;

    public static ModConfig getInstance()
    {
        if (instance == null)
        {
            instance = new ModConfig();
        }
        return instance;
    }

    public boolean isEntityEnabled(String entityKey)
    {
        return true;
    }

    public boolean isAnimationsEnabled()
    {
        return true;
    }

    public static boolean shouldKeepArmorAsVanilla(ArmorItem armorItem)
    {
        return false;
    }

    public static boolean shouldKeepEntityAsVanilla(LivingEntity entity)
    {
        return false;
    }

    public static UseActionType getItemUseAction(Item item)
    {
        return null;
    }

    public static AttackActionType getItemAttackAction(Item item)
    {
        return null;
    }
}
