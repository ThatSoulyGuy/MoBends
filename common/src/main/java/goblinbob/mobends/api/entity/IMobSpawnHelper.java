package goblinbob.mobends.api.entity;

public interface IMobSpawnHelper
{
    void finalizeSpawn(Object mob, Object serverLevel, Object difficulty, Object spawnType);

    class Holder
    {
        private static IMobSpawnHelper helper;

        public static void setHelper(IMobSpawnHelper helper)
        {
            Holder.helper = helper;
        }

        public static IMobSpawnHelper getHelper()
        {
            return helper;
        }
    }
}
