package goblinbob.mobends.forge;

import goblinbob.mobends.core.network.SharedNetworkConfiguration;
import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeServerConfig
{
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ALLOW_BENDSPACKS;

    public static final ForgeConfigSpec.BooleanValue LIMIT_MOVEMENT;

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        ALLOW_BENDSPACKS = builder
                .comment("Whether clients may apply bends packs on this server.",
                        "Bends packs replace entity animations, so a server can turn them off.")
                .define("allowBendspacks", true);

        LIMIT_MOVEMENT = builder
                .comment("Clamp how far an animation may displace an entity from its real position.",
                        "Limits how much a bends pack can misrepresent where a player actually is.")
                .define("limitMovement", false);

        SPEC = builder.build();
    }

    public static void sync()
    {
        SharedNetworkConfiguration.INSTANCE.setBendsPacksAllowed(ALLOW_BENDSPACKS.get());
        SharedNetworkConfiguration.INSTANCE.setMovementLimited(LIMIT_MOVEMENT.get());
    }
}
