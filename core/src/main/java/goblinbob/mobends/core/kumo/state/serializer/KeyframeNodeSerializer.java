package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.keyframe.KeyframeNodeRegistry;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;

import java.lang.reflect.Type;

public class KeyframeNodeSerializer implements JsonDeserializer<KeyframeNodeTemplate>
{

    @Override
    public KeyframeNodeTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        Gson gson = new Gson();
        KeyframeNodeTemplate abstractNode = gson.fromJson(json, KeyframeNodeTemplate.class);
        return KumoSerializer.INSTANCE.keyframeNodeGson.fromJson(json, KeyframeNodeRegistry.INSTANCE.getTemplateClass(abstractNode.getType()));
    }

}
