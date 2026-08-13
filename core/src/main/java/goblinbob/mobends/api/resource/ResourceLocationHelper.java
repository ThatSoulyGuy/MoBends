package goblinbob.mobends.api.resource;


public interface ResourceLocationHelper
{
    Object create(String namespace, String path);

    Object parse(String location);

    class Holder
    {
        private static ResourceLocationHelper helper;

        public static void setHelper(ResourceLocationHelper helper)
        {
            Holder.helper = helper;
        }

        public static ResourceLocationHelper getHelper()
        {
            return helper;
        }
    }
}
