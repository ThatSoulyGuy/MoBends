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

    /**
     * Rejects a mask whose mode Gson could not resolve.
     *
     * <p>{@link ArmatureMask#doesAllow} treats a null mode as "allow everything" so that a bad
     * mask cannot crash the render loop. That safety net would otherwise turn a typo like
     * {@code "mode": "INCLUDE"} into a mask that silently does nothing, which is its own kind of
     * bad: the author sees no error and no masking. Catching it here, at load, means the pack
     * fails with a message naming the layer instead.
     */
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
