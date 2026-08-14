package goblinbob.mobends.forge;

import goblinbob.mobends.standard.main.ModConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeConfig
{
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue DISABLE_SPIN_SWING;

    public static final ForgeConfigSpec.BooleanValue DISABLE_MOVEMENT_IN_GUI;

    public static final ForgeConfigSpec.BooleanValue ARROW_TRAIL_FULL_BRIGHT;

    public static final ForgeConfigSpec.BooleanValue NEW_ENCHANT_GLINT;

    public static final ForgeConfigSpec.BooleanValue SWORD_TRAIL_FULL_BRIGHT;

    public static final ForgeConfigSpec.BooleanValue SHOW_SWORD_TRAIL;

    public static final ForgeConfigSpec.BooleanValue SHOW_ARROW_TRAIL;

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        DISABLE_SPIN_SWING = builder
                .comment("Disable spin swing animation.")
                .define("disableSpinSwing", false);

        DISABLE_MOVEMENT_IN_GUI = builder
                .comment("Disables head movement when inside a GUI.")
                .define("disableMovementInGui", false);

        ARROW_TRAIL_FULL_BRIGHT = builder
                .comment("Arrow trail does not respect lighting conditions.")
                .define("arrowTrailFullBright", false);

        NEW_ENCHANT_GLINT = builder
                .comment("Upscale the enchant glint texture for armor. This was originally a bug, but I thought it was cool, so I made it a feature.")
                .define("newEnchantGlint", false);

        SWORD_TRAIL_FULL_BRIGHT = builder
                .comment("Sword trail does not respect lighting conditions.")
                .define("swordTrailFullBright", false);

        SHOW_SWORD_TRAIL = builder
                .comment("Show the trail when swords are swung")
                .define("showSwordTrail", true);

        SHOW_ARROW_TRAIL = builder
                .comment("Show the trail when arrows are travelling")
                .define("showArrowTrail", true);

        SPEC = builder.build();
    }

    public static void set(String key, boolean value)
    {
        switch (key)
        {
            case "disableSpinSwing": DISABLE_SPIN_SWING.set(value); break;
            case "disableMovementInGui": DISABLE_MOVEMENT_IN_GUI.set(value); break;
            case "arrowTrailFullBright": ARROW_TRAIL_FULL_BRIGHT.set(value); break;
            case "newEnchantGlint": NEW_ENCHANT_GLINT.set(value); break;
            case "swordTrailFullBright": SWORD_TRAIL_FULL_BRIGHT.set(value); break;
            case "showSwordTrail": SHOW_SWORD_TRAIL.set(value); break;
            case "showArrowTrail": SHOW_ARROW_TRAIL.set(value); break;
            default: return;
        }

        SPEC.save();
        sync();
    }

    public static void sync()
    {
        ModConfig.performSpinAttack = !DISABLE_SPIN_SWING.get();
        ModConfig.disableMovementInGui = DISABLE_MOVEMENT_IN_GUI.get();
        ModConfig.arrowTrailFullBright = ARROW_TRAIL_FULL_BRIGHT.get();
        ModConfig.newEnchantGlint = NEW_ENCHANT_GLINT.get();
        ModConfig.swordTrailFullBright = SWORD_TRAIL_FULL_BRIGHT.get();
        ModConfig.showSwordTrail = SHOW_SWORD_TRAIL.get();
        ModConfig.showArrowTrails = SHOW_ARROW_TRAIL.get();
    }
}
