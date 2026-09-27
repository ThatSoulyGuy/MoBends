package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;

import java.lang.reflect.Type;

public class LayerTemplateSerializer implements JsonDeserializer<LayerTemplate>
{

    @Override
    public LayerTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        Gson gson = new Gson();
        LayerTemplate abstractLayer = gson.fromJson(json, LayerTemplate.class);
        return KumoSerializer.INSTANCE.layerGson.fromJson(json, abstractLayer.getLayerType().getTemplateType());
    }

}
