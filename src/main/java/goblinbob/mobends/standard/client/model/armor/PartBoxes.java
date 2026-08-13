package goblinbob.mobends.standard.client.model.armor;

import goblinbob.mobends.core.client.model.MutatedBox;
import net.minecraft.client.model.geom.ModelPart;

import java.util.*;

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
