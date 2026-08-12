package goblinbob.mobends.api.rendering;

public interface IArmorHelper
{
    String getArmorMaterialName(Object armorItem);

    class Holder
    {
        private static IArmorHelper helper;

        public static void setHelper(IArmorHelper helper)
        {
            Holder.helper = helper;
        }

        public static IArmorHelper getHelper()
        {
            return helper;
        }
    }
}
