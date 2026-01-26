package goblinbob.mobends.api.entity;

/**
 * Platform-agnostic helper for mob spawning.
 *
 * The Mob.finalizeSpawn() method has different signatures:
 * - MC 1.21.1: finalizeSpawn(ServerLevelAccessor, DifficultyInstance, MobSpawnType, SpawnGroupData) - 4 params
 * - MC 1.20.1: finalizeSpawn(ServerLevelAccessor, DifficultyInstance, MobSpawnType, SpawnGroupData, CompoundTag) - 5 params
 */
public interface IMobSpawnHelper
{
    /**
     * Finalizes the spawn of a mob entity.
     *
     * @param mob The native Mob entity
     * @param serverLevel The native ServerLevelAccessor (may be null for preview entities)
     * @param difficulty The native DifficultyInstance
     * @param spawnType The native MobSpawnType
     */
    void finalizeSpawn(Object mob, Object serverLevel, Object difficulty, Object spawnType);

    /**
     * Singleton holder for the platform-specific implementation.
     */
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
