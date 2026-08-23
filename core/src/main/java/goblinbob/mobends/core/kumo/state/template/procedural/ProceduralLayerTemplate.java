package goblinbob.mobends.core.kumo.state.template.procedural;

import goblinbob.mobends.lib.animation.keyframe.ArmatureMask;
import goblinbob.mobends.core.kumo.state.template.IKumoValidationContext;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.Map;

public class ProceduralLayerTemplate extends LayerTemplate
{
    public Map<String, ProceduralBoneTemplate> bones;

    public ArmatureMask mask;

    public float blendWeight = 1.0f;

    public boolean additive = false;

    @Override
    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        super.validate(context);
        validateMask(mask, "procedural layer");

        if (bones == null || bones.isEmpty())
        {
            throw new MalformedKumoTemplateException("Procedural layer must define at least one bone.");
        }

        for (Map.Entry<String, ProceduralBoneTemplate> entry : bones.entrySet())
        {
            if (entry.getValue() == null)
            {
                throw new MalformedKumoTemplateException(
                        String.format("Bone template for '%s' is null.", entry.getKey()));
            }
            if (!entry.getValue().hasAnyExpression())
            {
                throw new MalformedKumoTemplateException(
                        String.format("Bone '%s' has no expressions defined.", entry.getKey()));
            }
        }
    }
}
