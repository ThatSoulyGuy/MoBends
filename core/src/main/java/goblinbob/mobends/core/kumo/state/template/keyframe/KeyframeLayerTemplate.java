package goblinbob.mobends.core.kumo.state.template.keyframe;

import goblinbob.mobends.lib.animation.keyframe.ArmatureMask;
import goblinbob.mobends.core.kumo.state.template.IKumoValidationContext;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.List;

public class KeyframeLayerTemplate extends LayerTemplate
{

    public int entryNode = 0;
    public List<KeyframeNodeTemplate> nodes;
    public ArmatureMask mask;

    // NOTE: there is deliberately no `additive` field here, even though
    // ProceduralLayerTemplate has one and assets/mobends/bends/animators/wolf.json sets
    // "additive": true on its tongue/mouth overlay. Gson drops the unknown key, so that flag is
    // inert — which is the correct behaviour until a real implementation exists.
    //
    // The obvious implementation is wrong. A keyframe layer's write step composes rotations with
    // SmoothOrientation.add, which sums raw quaternion components without normalising, and
    // applyRestPose is what guarantees exactly one unit quaternion lands on a bone per frame.
    // Skipping the rest pose therefore leaves |q| = 2 where two layers write the same bone, and
    // JOML scales a rotation matrix by |q|^2 — a 4x scale-up on the affected bones — while
    // offsets simply double. Nothing renormalises between the Kumo write and the render read.
    //
    // A correct version has to mirror what ProceduralLayerState actually does for additive:
    // compose rotations MULTIPLICATIVELY (rotateInstantX/Y/Z), not by component addition, and
    // scale offsets by a blend weight rather than adding them raw.

    @Override
    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        super.validate(context);
        validateMask(mask, "keyframe layer");
    }

}
