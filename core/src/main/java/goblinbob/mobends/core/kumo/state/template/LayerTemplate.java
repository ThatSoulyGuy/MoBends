package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.lib.animation.keyframe.ArmatureMask;

public class LayerTemplate
{

    private LayerType type;

    public LayerType getLayerType()
    {
        return type;
    }

    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
    }

    protected static void validateMask(ArmatureMask mask, String layerDescription)
            throws MalformedKumoTemplateException
    {
        if (mask != null && mask.getMode() == null)
        {
            throw new MalformedKumoTemplateException(String.format(
                    "The %s has a mask with an unrecognised 'mode'. Expected one of %s.",
                    layerDescription, java.util.Arrays.toString(ArmatureMask.Mode.values())));
        }
    }

}
