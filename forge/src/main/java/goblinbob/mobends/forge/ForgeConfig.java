package goblinbob.mobends.forge;

import goblinbob.mobends.standard.main.ModConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.Map;

public class ForgeConfig
{
    public static final ForgeConfigSpec SPEC;

    private static final Map<String, ForgeConfigSpec.BooleanValue> VALUES = new HashMap<>();

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        define(builder, "disableSpinSwing", "Disable spinning animation when swinging for players", false);
        define(builder, "mobsCanSpin", "Allow mobs to do the spinning animation when swinging", false);
        define(builder, "disableMovementInGui", "Disables movement when inside a GUI.", false);
        define(builder, "arrowTrailFullBright", "Arrow trail does not respect lighting conditions.", false);
        define(builder, "swordTrailFullBright", "Sword trail does not respect lighting conditions.", false);
        define(builder, "showSwordTrail", "Show the trail when swords are swung", true);
        define(builder, "showArrowTrail", "Show the trail when arrows are travelling", true);
        define(builder, "arrowTrailPotionColor", "The color of an arrow trail will match the color of its potion effect.", true);
        define(builder, "spectralArrowTrailEffect", "Enable the special golden trail effect for Spectral Arrows", true);
        define(builder, "tridentTrail", "Allow the Trident to have a trail", true);
        define(builder, "bendRobesOnlyWhenSitting", "Villager, witch, wandering trader and zombie villager robes only bend while sitting.", true);

        SPEC = builder.build();
    }

    private static void define(ForgeConfigSpec.Builder builder, String key, String comment, boolean defaultValue)
    {
        VALUES.put(key, builder.comment(comment).define(key, defaultValue));
    }

    private static boolean get(String key)
    {
        return VALUES.get(key).get();
    }

    public static void set(String key, boolean value)
    {
        ForgeConfigSpec.BooleanValue configValue = VALUES.get(key);
        if (configValue == null)
            return;

        configValue.set(value);
        SPEC.save();
        sync();
    }

    public static void sync()
    {
        ModConfig.performSpinAttack = !get("disableSpinSwing");
        ModConfig.mobsCanSpin = get("mobsCanSpin");
        ModConfig.disableMovementInGui = get("disableMovementInGui");
        ModConfig.arrowTrailFullBright = get("arrowTrailFullBright");
        ModConfig.swordTrailFullBright = get("swordTrailFullBright");
        ModConfig.showSwordTrail = get("showSwordTrail");
        ModConfig.showArrowTrails = get("showArrowTrail");
        ModConfig.arrowTrailPotionColor = get("arrowTrailPotionColor");
        ModConfig.spectralArrowTrailEffect = get("spectralArrowTrailEffect");
        ModConfig.tridentTrail = get("tridentTrail");
        ModConfig.bendRobesOnlyWhenSitting = get("bendRobesOnlyWhenSitting");
    }
}
