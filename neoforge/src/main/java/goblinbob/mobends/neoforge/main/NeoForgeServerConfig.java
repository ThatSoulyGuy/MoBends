package goblinbob.mobends.neoforge.main;

import goblinbob.mobends.core.network.SharedNetworkConfiguration;
import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoForgeServerConfig
{
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ALLOW_BENDSPACKS;

    public static final ModConfigSpec.BooleanValue LIMIT_MOVEMENT;

    static
    {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

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
