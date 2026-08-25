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
     * <p>Mirrors {@code ProceduralLayerTemplate.additive}. Rotations compose MULTIPLICATIVELY --
     * see {@code KeyframeLayerState.composeRotation}. Summing raw quaternion components instead,
     * which is what the replacing path does after zeroing, would leave a non-unit quaternion
     * wherever two layers write the same bone, and the renderer scales geometry by its squared
     * magnitude.
     *
     * <p>Populated by Gson and never written from Java, which is normal for a template field.
     */
    public boolean additive = false;

    @Override
    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        super.validate(context);
        validateMask(mask, "keyframe layer");
    }

}
