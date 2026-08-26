package goblinbob.mobends.lib.time;

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

        public static void reset()
        {
            source = ZERO;
        }

    }

}
