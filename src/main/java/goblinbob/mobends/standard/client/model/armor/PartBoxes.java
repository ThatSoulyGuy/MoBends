package goblinbob.mobends.standard.client.model.armor;

import goblinbob.mobends.core.client.model.MutatedBox;
import net.minecraft.client.model.geom.ModelPart;

import java.util.*;

/**
 * Stores MutatedBox instances mapped to ModelPart instances.
 * Updated for Minecraft 1.20.1 - uses modern ModelPart and our custom MutatedBox.
 *
 * @deprecated This class is part of the legacy armor rendering system.
 *             Use the three-tier rendering system caching infrastructure
 *             (ArmorStructureCache, ArmorGeometryCache) instead.
 *             This class is kept for backward compatibility and will be removed in a future version.
 */
@Deprecated
public class PartBoxes
{
    protected HashMap<ModelPart, List<MutatedBox>> modelToBoxesMap = new HashMap<>();

    public void put(ModelPart part, MutatedBox box)
    {
        if (!modelToBoxesMap.containsKey(part))
        {
            modelToBoxesMap.put(part, new LinkedList<>());
        }

        modelToBoxesMap.get(part).add(box);
    }

    public void clear()
    {
        this.modelToBoxesMap.clear();
    }

    public void clearPart(ModelPart part)
    {
        modelToBoxesMap.remove(part);
    }

    public Set<Map.Entry<ModelPart, List<MutatedBox>>> entrySet()
    {
        return modelToBoxesMap.entrySet();
    }

    public Set<ModelPart> keySet()
    {
        return modelToBoxesMap.keySet();
    }
}
