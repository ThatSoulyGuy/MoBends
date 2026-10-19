package goblinbob.mobends.compat;

public final class SporeCompat
{
    private static boolean initialized = false;
    private static Class<?> weaponClass;
    private static Class<?> shieldClass;

    private SporeCompat()
    {
    }

    public static boolean isMeleeWeapon(Object item)
    {
        if (!initialized)
        {
            initialized = true;
            try
            {
                weaponClass = Class.forName("com.Harbinger.Spore.Sitems.BaseWeapons.SporeToolsBaseItem");
                shieldClass = Class.forName("com.Harbinger.Spore.Sitems.InfectedShield");
            }
            catch (Throwable e)
            {
                weaponClass = null;
            }
        }

        return weaponClass != null && weaponClass.isInstance(item) && !shieldClass.isInstance(item);
    }
}
