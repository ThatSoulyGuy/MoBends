package goblinbob.mobends.core.kumo.state;

import org.slf4j.LoggerFactory;
import goblinbob.mobends.core.kumo.state.keyframe.KeyframeLayerState;
import goblinbob.mobends.core.kumo.state.procedural.ProceduralLayerState;
import org.slf4j.Logger;
import goblinbob.mobends.core.kumo.state.template.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.DriverLayerTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeLayerTemplate;
import goblinbob.mobends.core.kumo.state.template.procedural.ProceduralLayerTemplate;

public interface ILayerState
{
    Logger LOGGER = LoggerFactory.getLogger(ILayerState.class);

    void start(IKumoContext context);

    void update(IKumoContext context, float deltaTime) throws MalformedKumoTemplateException;

    static ILayerState createFromTemplate(IKumoInstancingContext context, LayerTemplate template) throws MalformedKumoTemplateException
    {
        switch (template.getLayerType())
        {
            case KEYFRAME:
                return KeyframeLayerState.createFromTemplate(context, (KeyframeLayerTemplate) template);
            case DRIVER:
                return new DriverLayerState((DriverLayerTemplate) template);
            case PROCEDURAL:
                return ProceduralLayerState.createFromTemplate(context, (ProceduralLayerTemplate) template);
            default:
                LOGGER.warn(String.format("Unknown layer type was specified in state template: %d",
                        template.getLayerType().ordinal()));
        }

        return null;
    }

}
