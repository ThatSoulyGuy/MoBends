package goblinbob.mobends.neoforge.main;

import goblinbob.mobends.standard.main.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoForgeConfig
{
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue DISABLE_SPIN_SWING;

    public static final ModConfigSpec.BooleanValue DISABLE_MOVEMENT_IN_GUI;

    static
    {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        DISABLE_SPIN_SWING = builder
                .comment("Disable spin swing animation.")
                .define("disableSpinSwing", false);

        DISABLE_MOVEMENT_IN_GUI = builder
                .comment("Disables head movement when inside a GUI.")
                .define("disableMovementInGui", false);

        SPEC = builder.build();
    }

    public static void sync()
    {
        ModConfig.performSpinAttack = !DISABLE_SPIN_SWING.get();
        ModConfig.disableMovementInGui = DISABLE_MOVEMENT_IN_GUI.get();
    }
}
