package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

import java.lang.reflect.Type;

public class TriggerConditionTemplateSerializer implements JsonSerializer<TriggerConditionTemplate>, JsonDeserializer<TriggerConditionTemplate>
{

    @Override
    public JsonElement serialize(TriggerConditionTemplate src, Type typeOfSrc, JsonSerializationContext context)
    {
        return KumoSerializer.INSTANCE.gson.toJsonTree(src);
    }

    @Override
    public TriggerConditionTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        final Gson gson = new Gson();

        TriggerConditionTemplate abstractTriggerCondition = gson.fromJson(json, TriggerConditionTemplate.class);
        String typeName = abstractTriggerCondition.getType();

        if (typeName == null)
            return null;

        Type templateType = TriggerConditionRegistry.instance.getTemplateClass(typeName);

        if (templateType == null)
            return null;

        if (templateType.equals(typeOfT))
        {
            return abstractTriggerCondition;
        }

        return KumoSerializer.INSTANCE.gson.fromJson(json, templateType);
    }

}
