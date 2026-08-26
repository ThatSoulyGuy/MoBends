package goblinbob.mobends.core.bender;

import net.minecraft.world.entity.Entity;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class PreviewHelper
{

    private static final Set<Entity> previewEntities = Collections.newSetFromMap(new IdentityHashMap<>());

    public static void registerPreviewEntity(Entity entity)
    {
        previewEntities.add(entity);
    }

    public static void unregisterPreviewEntity(Entity entity)
    {
        previewEntities.remove(entity);
    }

    public static boolean isPreviewEntity(Entity entity)
    {
        return previewEntities.contains(entity);
    }

}
