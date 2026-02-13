package goblinbob.mobends.core.kumo.state.template;

/**
 * A dummy class, that tells GSON to use a custom serializer.
 */
public class LayerTemplate
{

    private LayerType type;

    public LayerType getLayerType()
    {
        return type;
    }

    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        // Does nothing by default.
    }

}
