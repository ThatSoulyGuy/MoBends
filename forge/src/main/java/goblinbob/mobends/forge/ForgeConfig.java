package goblinbob.mobends.forge;

import goblinbob.mobends.standard.main.ModConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeConfig
{
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue DISABLE_SPIN_SWING;

    public static final ForgeConfigSpec.BooleanValue DISABLE_MOVEMENT_IN_GUI;

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

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
