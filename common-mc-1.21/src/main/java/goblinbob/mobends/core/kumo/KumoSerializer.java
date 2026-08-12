package goblinbob.mobends.core.kumo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import goblinbob.mobends.core.kumo.state.serializer.KeyframeNodeSerializer;
import goblinbob.mobends.core.kumo.state.serializer.LayerTemplateSerializer;
import goblinbob.mobends.core.kumo.state.serializer.TriggerConditionTemplateSerializer;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;

public class KumoSerializer
{

    public static final KumoSerializer INSTANCE = new KumoSerializer();

    public final Gson gson;

    public final Gson layerGson;

    public final Gson keyframeNodeGson;

    private KumoSerializer()
    {
        {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(LayerTemplate.class, new LayerTemplateSerializer());
            gsonBuilder.registerTypeAdapter(KeyframeNodeTemplate.class, new KeyframeNodeSerializer());
            gsonBuilder.registerTypeAdapter(TriggerConditionTemplate.class, new TriggerConditionTemplateSerializer());
            gson = gsonBuilder.create();
        }

        {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(KeyframeNodeTemplate.class, new KeyframeNodeSerializer());
            gsonBuilder.registerTypeAdapter(TriggerConditionTemplate.class, new TriggerConditionTemplateSerializer());
            layerGson = gsonBuilder.create();
        }

        {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(TriggerConditionTemplate.class, new TriggerConditionTemplateSerializer());
            keyframeNodeGson = gsonBuilder.create();
        }
    }

}
