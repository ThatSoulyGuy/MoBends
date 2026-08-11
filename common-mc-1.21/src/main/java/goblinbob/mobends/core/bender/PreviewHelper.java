package goblinbob.mobends.core.bender;

import net.minecraft.world.entity.Entity;

import java.util.HashSet;
import java.util.Set;

public class PreviewHelper
{

    private static final Set<Entity> previewEntities = new HashSet<>();

    public static void registerPreviewEntity(Entity entity)
    {
        previewEntities.add(entity);
    }

    public static boolean isPreviewEntity(Entity entity)
    {
        return previewEntities.contains(entity);
    }

}
