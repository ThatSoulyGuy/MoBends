package goblinbob.mobends.neoforge.main;

import goblinbob.mobends.standard.main.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoForgeConfig
{
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue DISABLE_SPIN_SWING;

    public static final ModConfigSpec.BooleanValue DISABLE_MOVEMENT_IN_GUI;

    public static final ModConfigSpec.BooleanValue ARROW_TRAIL_FULL_BRIGHT;

    public static final ModConfigSpec.BooleanValue SHOW_SWORD_TRAIL;

    public static final ModConfigSpec.BooleanValue SHOW_ARROW_TRAIL;

    static
    {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        DISABLE_SPIN_SWING = builder
                .comment("Disable spin swing animation.")
                .define("disableSpinSwing", false);

        DISABLE_MOVEMENT_IN_GUI = builder
                .comment("Disables head movement when inside a GUI.")
                .define("disableMovementInGui", false);

        ARROW_TRAIL_FULL_BRIGHT = builder
                .comment("Arrow trail does not respect lighting conditions.")
                .define("arrowTrailFullBright", false);

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
        ModConfig.showSwordTrail = SHOW_SWORD_TRAIL.get();
        ModConfig.showArrowTrails = SHOW_ARROW_TRAIL.get();
    }
}
