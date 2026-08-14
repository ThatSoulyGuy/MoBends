package goblinbob.mobends.compat.armourers;

import moe.plushie.armourers_workshop.core.armature.ArmatureTransformerBuilder;
import moe.plushie.armourers_workshop.core.armature.JointModifier;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IODataObject;
import moe.plushie.armourers_workshop.core.utils.OpenResourceKey;

public class MoBendsArmatureTransformerBuilder extends ArmatureTransformerBuilder
{
    public MoBendsArmatureTransformerBuilder(OpenResourceKey name)
    {
        super(name);
    }

    @Override
    protected JointModifier buildJointTarget(String name, IODataObject parameters)
    {
        return new MoBendsJointBinder(name, parameters);
    }
}
