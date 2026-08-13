package goblinbob.mobends.core.pack;

import java.util.HashMap;
import java.util.List;

public class PackCombiner
{

    public static BendsPackData combineData(List<BendsPackData> packs)
    {
        BendsPackData combinedData = new BendsPackData();
        combinedData.targets = new HashMap<>();
        combinedData.keyframeAnimations = new HashMap<>();

        for (int i = packs.size() - 1; i >= 0; --i)
        {
            BendsPackData data = packs.get(i);
            combinedData.targets.putAll(data.targets);
            combinedData.keyframeAnimations.putAll(data.keyframeAnimations);
        }

        return combinedData;
    }

}
