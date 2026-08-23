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

    /**
     * When true, this layer composes onto whatever earlier layers already wrote for the bones it
     * touches, instead of clearing them first.
     *
     * <p>Mirrors {@code ProceduralLayerTemplate.additive} and means the same thing for the same
     * structural reason: a keyframe layer's write step is already purely accumulative — it only
     * ever calls the {@code KeyframeUtils.tween*Additive} helpers, which add rather than set. The
     * one destructive step is {@code applyRestPose}, which zeroes every bone the animation names.
     * So for a keyframe layer, additive means exactly "skip the rest pose".
     *
     * <p>Populated by Gson and never written from Java, which is normal for a template field.
     *
     * <p>An additive layer must not be the only writer of a bone: nothing zeroes part offsets
     * between frames, so a bone written solely by additive layers accumulates without bound.
     */
    public boolean additive = false;

    @Override
    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        super.validate(context);
        validateMask(mask, "keyframe layer");
    }

}
