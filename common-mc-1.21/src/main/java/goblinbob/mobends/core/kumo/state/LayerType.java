package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.template.DriverLayerTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeLayerTemplate;
import goblinbob.mobends.core.kumo.state.template.procedural.ProceduralLayerTemplate;

import java.lang.reflect.Type;

public enum LayerType
{

    KEYFRAME(KeyframeLayerTemplate.class),
    DRIVER(DriverLayerTemplate.class),
    PROCEDURAL(ProceduralLayerTemplate.class);

    private Type templateType;

    LayerType(Type templateType)
    {
        this.templateType = templateType;
    }

    public Type getTemplateType()
    {
        return templateType;
    }

}
