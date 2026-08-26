package goblinbob.mobends.neoforge.main;

import goblinbob.mobends.core.network.SharedNetworkConfiguration;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-side settings, sent to each client on login.
 *
 * <p>A SERVER config in NeoForge lives in the world's {@code serverconfig} folder and is loaded
 * when the world loads, which is why {@link #sync()} is driven from the server-starting event as
 * well as from config load and reload.
 *
 * <p>Clients connecting to a server without Mo' Bends installed simply never receive a response,
 * and {@code SharedNetworkConfiguration.resetToDefaults()} on login leaves them permissive.
 */
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

    /** Pushes the configured values into the structure the config packet serialises. */
    public static void sync()
    {
        SharedNetworkConfiguration.INSTANCE.setBendsPacksAllowed(ALLOW_BENDSPACKS.get());
        SharedNetworkConfiguration.INSTANCE.setMovementLimited(LIMIT_MOVEMENT.get());
    }
}
