package goblinbob.mobends.lib.time;

/**
 * Supplies the animation clock, in ticks, to code in this module.
 *
 * <p>There is exactly one production installer: {@code DataUpdateHandler}'s static initialiser.
 * That is enough because nothing can read the clock without first having touched that class — the
 * render path writes {@code DataUpdateHandler.ticksPerFrame} and {@code partialTicks} every frame,
 * before any animation runs — but it is a class-initialisation-ordering guarantee rather than an
 * explicit wiring step, so it is worth knowing about.
 *
 * <p>Until something installs a source the clock reads a constant zero. That fails SILENTLY rather
 * than loudly: elapsed-time conditions see zero ticks elapsed and simply never fire, so a state
 * machine waiting on one stalls with nothing in the log. Tests install a hand-advanced fake and
 * should call {@link Holder#reset()} afterwards.
 *
 * <p>Returns {@code float} rather than {@code double} deliberately: it preserves the exact
 * arithmetic of the tick comparisons that consume it.
 */
@FunctionalInterface
public interface ITickSource
{

    float getTicks();

    final class Holder
    {

        private static final ITickSource ZERO = () -> 0.0F;

        private static volatile ITickSource source = ZERO;

        private Holder()
        {
        }

        public static void setSource(ITickSource source)
        {
            Holder.source = source != null ? source : ZERO;
        }

        public static ITickSource getSource()
        {
            return source;
        }

        public static float getTicks()
        {
            return source.getTicks();
        }

        /** Restores the constant-zero clock. Intended for test teardown. */
        public static void reset()
        {
            source = ZERO;
        }

    }

}
