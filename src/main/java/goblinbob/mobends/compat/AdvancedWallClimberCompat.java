package goblinbob.mobends.compat;

public final class AdvancedWallClimberCompat
{
    private static boolean initialized = false;
    private static Class<?> climberClass;

    private AdvancedWallClimberCompat()
    {
    }

    public static boolean isClimber(Object entity)
    {
        if (!initialized)
        {
            initialized = true;
            try
            {
                climberClass = Class.forName("com.nyfaria.awcapi.entity.IAdvancedClimber");
            }
            catch (Throwable e)
            {
                climberClass = null;
            }
        }

        return climberClass != null && climberClass.isInstance(entity);
    }
}
