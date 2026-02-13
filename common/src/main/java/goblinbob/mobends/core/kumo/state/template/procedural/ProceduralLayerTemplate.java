package goblinbob.mobends.core.kumo.state.template.procedural;

import goblinbob.mobends.lib.animation.keyframe.ArmatureMask;
import goblinbob.mobends.core.kumo.state.template.IKumoValidationContext;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.Map;

/**
 * Template for a procedural animation layer.
 * Procedural layers define bone transforms using mathematical expressions
 * that are evaluated every frame.
 *
 * Example JSON:
 * {
 *   "type": "PROCEDURAL",
 *   "bones": {
 *     "rightArm": {
 *       "rotationX": "sin(limbSwing + PI) * limbSwingAmount * 40",
 *       "rotationZ": "0"
 *     },
 *     "body": {
 *       "rotationZ": "sin(ticks * 0.05) * 3"
 *     }
 *   },
 *   "additive": false,
 *   "blendWeight": 1.0
 * }
 */
public class ProceduralLayerTemplate extends LayerTemplate
{
    /**
     * Map of bone names to their procedural transform templates.
     */
    public Map<String, ProceduralBoneTemplate> bones;

    /**
     * Optional armature mask to limit which bones are affected.
     */
    public ArmatureMask mask;

    /**
     * Blend weight for this layer (0.0 to 1.0).
     * Controls how much this layer's transforms contribute to the final pose.
     */
    public float blendWeight = 1.0f;

    /**
     * If true, this layer's transforms are added to the existing pose
     * rather than replacing it.
     */
    public boolean additive = false;

    @Override
    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        super.validate(context);

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
