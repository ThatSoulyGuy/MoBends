package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.keyframe.KeyframeLayerState;
import goblinbob.mobends.core.kumo.state.procedural.ProceduralLayerState;
import goblinbob.mobends.core.kumo.state.template.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.DriverLayerTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeLayerTemplate;
import goblinbob.mobends.core.kumo.state.template.procedural.ProceduralLayerTemplate;

public interface ILayerState
{
    void start(IKumoContext context);

    void update(IKumoContext context, float deltaTime) throws MalformedKumoTemplateException;

    static ILayerState createFromTemplate(IKumoInstancingContext context, LayerTemplate template) throws MalformedKumoTemplateException
    {
        return switch (template.getLayerType())
        {
            case KEYFRAME -> new KeyframeLayerState(context, (KeyframeLayerTemplate) template);
            case DRIVER -> new DriverLayerState((DriverLayerTemplate) template);
            case PROCEDURAL -> new ProceduralLayerState(context, (ProceduralLayerTemplate) template);
        };
    }

}
