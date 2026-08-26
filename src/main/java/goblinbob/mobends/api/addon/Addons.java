package goblinbob.mobends.api.addon;

import goblinbob.mobends.core.Core;

import java.util.ArrayList;
import java.util.List;

public class Addons
{

    private static final Addons INSTANCE = new Addons();

    private final List<IAddon> addons = new ArrayList<>();

    /** Addons that registered before Core existed, waiting for {@link #flushPending()}. */
    private final List<PendingRegistration> pending = new ArrayList<>();

    private record PendingRegistration(String modId, IAddon addon) {}

    public static void registerAddon(String modId, IAddon addon)
    {
        if (INSTANCE.addons.contains(addon))
            return;

        INSTANCE.addons.add(addon);

        if (Core.getInstance() != null)
        {
            addon.registerContent(new AddonAnimationRegistry(modId));
        }
        else
        {
            // Core is not up yet. This used to drop the registration on the floor: the addon
            // still received tick callbacks, so it looked registered, but none of its entities,
            // trigger conditions or editors ever reached the registries. Mo' Bends' own addons
            // are safe by construction -- both loaders create Core first -- but a third-party
            // addon registering from its own setup event has no such guarantee.
            INSTANCE.pending.add(new PendingRegistration(modId, addon));
        }
    }

    /**
     * Registers the content of any addon that arrived before Core did.
     *
     * <p>Called once per loader immediately after Core is created. Safe to call again: the queue
     * is drained, and anything registering afterwards takes the immediate path above.
     */
    public static void flushPending()
    {
        if (Core.getInstance() == null || INSTANCE.pending.isEmpty())
            return;

        List<PendingRegistration> toRegister = new ArrayList<>(INSTANCE.pending);
        INSTANCE.pending.clear();

        for (PendingRegistration registration : toRegister)
        {
            registration.addon().registerContent(new AddonAnimationRegistry(registration.modId()));
        }
    }

    public static Iterable<IAddon> getRegistered()
    {
        return INSTANCE.addons;
    }

    public static void onRenderTick(float partialTicks)
    {
        INSTANCE.addons.forEach(addon -> addon.onRenderTick(partialTicks));
    }

    public static void onClientTick()
    {
        INSTANCE.addons.forEach(IAddon::onClientTick);
    }

    public static void onRefresh()
    {
        INSTANCE.addons.forEach(IAddon::onRefresh);
    }

}
