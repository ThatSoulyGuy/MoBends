package goblinbob.mobends.lib.time;

/**
 * Supplies the animation clock, in ticks, to code in this module.
 *
 * <p>The mod installs {@code DataUpdateHandler::getTicks} into {@link Holder} during client setup;
 * unit tests install a hand-advanced fake. Until something installs a source the clock reads a
 * constant zero, so this never throws on an unwired holder — but note that a constant clock makes
 * every elapsed-time condition read zero elapsed ticks and therefore never fire. That fails
 * silently, which is why {@code DataUpdateHandler} installs itself from a static initialiser as
 * well as from each loader's setup.
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
