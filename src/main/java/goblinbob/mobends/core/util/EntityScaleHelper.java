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

        final float npcScale = goblinbob.mobends.compat.CustomNpcsCompat.renderScaleOf(entity);
        if (npcScale != 1.0F)
        {
            return npcScale;
        }

        //? if >=1.21 {
        /*return entity.getScale();
        *///?} else {
        return 1.0F;
        //?}
    }
}
