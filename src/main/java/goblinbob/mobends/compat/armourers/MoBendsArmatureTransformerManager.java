package goblinbob.mobends.compat.armourers;

import moe.plushie.armourers_workshop.core.armature.ArmatureTransformerBuilder;
import moe.plushie.armourers_workshop.core.armature.ArmatureTransformerManager;
import moe.plushie.armourers_workshop.core.utils.OpenResourceKey;

public class MoBendsArmatureTransformerManager extends ArmatureTransformerManager
{
    public static final String ARMATURE_TYPE = "mobends:armature";

    public static final MoBendsArmatureTransformerManager INSTANCE = new MoBendsArmatureTransformerManager();

    @Override
    protected ArmatureTransformerBuilder createBuilder(OpenResourceKey name)
    {
        return new MoBendsArmatureTransformerBuilder(name);
    }
}
