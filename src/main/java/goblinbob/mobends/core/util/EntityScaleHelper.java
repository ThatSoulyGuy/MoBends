package goblinbob.mobends.core.util;

import net.minecraft.world.entity.LivingEntity;

public final class EntityScaleHelper
{
    private EntityScaleHelper()
    {
    }

    public static float getRenderScale(LivingEntity entity)
    {
        if (entity == null)
        {
            return 1.0F;
        }

        //? if >=1.21 {
        /*return entity.getScale();
        *///?} else {
        return 1.0F;
        //?}
    }
}
