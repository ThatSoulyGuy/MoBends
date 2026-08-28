package goblinbob.mobends.forge;

import goblinbob.mobends.standard.main.ModConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeConfig
{
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue DISABLE_SPIN_SWING;

    public static final ForgeConfigSpec.BooleanValue MOBS_CAN_SPIN;

    public static final ForgeConfigSpec.BooleanValue DISABLE_MOVEMENT_IN_GUI;

    public static final ForgeConfigSpec.BooleanValue ARROW_TRAIL_FULL_BRIGHT;

    public static final ForgeConfigSpec.BooleanValue SWORD_TRAIL_FULL_BRIGHT;

    public static final ForgeConfigSpec.BooleanValue SHOW_SWORD_TRAIL;

    public static final ForgeConfigSpec.BooleanValue SHOW_ARROW_TRAIL;

    public static final ForgeConfigSpec.BooleanValue ARROW_TRAIL_POTION_COLOR;

    public static final ForgeConfigSpec.BooleanValue SPECTRAL_ARROW_TRAIL_EFFECT;

    public static final ForgeConfigSpec.BooleanValue TRIDENT_TRAIL;

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        DISABLE_SPIN_SWING = builder
                .comment("Disable spinning animation when swinging for players")
                .define("disableSpinSwing", false);

        MOBS_CAN_SPIN = builder
                .comment("Allow mobs to do the spinning animation when swinging")
                .define("mobsCanSpin", false);

        DISABLE_MOVEMENT_IN_GUI = builder
                .comment("Disables movement when inside a GUI.")
                .define("disableMovementInGui", false);

        ARROW_TRAIL_FULL_BRIGHT = builder
                .comment("Arrow trail does not respect lighting conditions.")
                .define("arrowTrailFullBright", false);

        SWORD_TRAIL_FULL_BRIGHT = builder
                .comment("Sword trail does not respect lighting conditions.")
                .define("swordTrailFullBright", false);

        SHOW_SWORD_TRAIL = builder
                .comment("Show the trail when swords are swung")
                .define("showSwordTrail", true);

        SHOW_ARROW_TRAIL = builder
                .comment("Show the trail when arrows are travelling")
                .define("showArrowTrail", true);

        ARROW_TRAIL_POTION_COLOR = builder
                .comment("The color of an arrow trail will match the color of its potion effect.")
                .define("arrowTrailPotionColor", true);

        SPECTRAL_ARROW_TRAIL_EFFECT = builder
                .comment("Enable the special golden trail effect for Spectral Arrows")
                .define("spectralArrowTrailEffect", true);

        TRIDENT_TRAIL = builder
                .comment("Allow the Trident to have a trail")
                .define("tridentTrail", true);

        SPEC = builder.build();
    }

    public static void set(String key, boolean value)
    {
        switch (key)
        {
            case "disableSpinSwing": DISABLE_SPIN_SWING.set(value); break;
            case "mobsCanSpin": MOBS_CAN_SPIN.set(value); break;
            case "disableMovementInGui": DISABLE_MOVEMENT_IN_GUI.set(value); break;
            case "arrowTrailFullBright": ARROW_TRAIL_FULL_BRIGHT.set(value); break;
            case "swordTrailFullBright": SWORD_TRAIL_FULL_BRIGHT.set(value); break;
            case "showSwordTrail": SHOW_SWORD_TRAIL.set(value); break;
            case "showArrowTrail": SHOW_ARROW_TRAIL.set(value); break;
            case "arrowTrailPotionColor": ARROW_TRAIL_POTION_COLOR.set(value); break;
            case "spectralArrowTrailEffect": SPECTRAL_ARROW_TRAIL_EFFECT.set(value); break;
            case "tridentTrail": TRIDENT_TRAIL.set(value); break;
            default: return;
        }

        SPEC.save();
        sync();
    }

    public static void sync()
    {
        ModConfig.performSpinAttack = !DISABLE_SPIN_SWING.get();
        ModConfig.mobsCanSpin = MOBS_CAN_SPIN.get();
        ModConfig.disableMovementInGui = DISABLE_MOVEMENT_IN_GUI.get();
        ModConfig.arrowTrailFullBright = ARROW_TRAIL_FULL_BRIGHT.get();
        ModConfig.swordTrailFullBright = SWORD_TRAIL_FULL_BRIGHT.get();
        ModConfig.showSwordTrail = SHOW_SWORD_TRAIL.get();
        ModConfig.showArrowTrails = SHOW_ARROW_TRAIL.get();
        ModConfig.arrowTrailPotionColor = ARROW_TRAIL_POTION_COLOR.get();
        ModConfig.spectralArrowTrailEffect = SPECTRAL_ARROW_TRAIL_EFFECT.get();
        ModConfig.tridentTrail = TRIDENT_TRAIL.get();
    }
}
