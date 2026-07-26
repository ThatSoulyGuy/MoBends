package goblinbob.mobends.forge;

import goblinbob.mobends.standard.main.ModConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeConfig
{
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue DISABLE_SPIN_SWING;

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        DISABLE_SPIN_SWING = builder
                .comment("Disable spin swing animation.")
                .define("disableSpinSwing", false);

        SPEC = builder.build();
    }

    public static void sync()
    {
        ModConfig.performSpinAttack = !DISABLE_SPIN_SWING.get();
    }
}
