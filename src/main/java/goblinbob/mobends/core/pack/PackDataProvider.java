package goblinbob.mobends.core.pack;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import goblinbob.mobends.core.animation.keyframe.AnimationLoader;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.IKumoValidationContext;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PackDataProvider
{
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();


    public static final PackDataProvider INSTANCE = new PackDataProvider();

    private Map<IBendsPack, BendsPackData> dataMap = new HashMap<>();

    private BendsPackData appliedData;

    private PackDataProvider()
    {
    }

    public void createBendsPackData(Collection<IBendsPack> packs) throws InvalidPackFormatException
    {
        final List<BendsPackData> dataList = new LinkedList<>();
        for (IBendsPack pack : packs)
        {
            final BendsPackData data = getDataForPack(pack);
            if (data != null)
            {
                dataList.add(data);
            }
        }

        if (dataList.isEmpty())
        {
            appliedData = null;
            return;
        }

        appliedData = PackCombiner.combineData(dataList);
    }

    public void clearCache()
    {
        this.dataMap.clear();
    }

    public BendsPackData getDataForPack(IBendsPack bendsPack) throws InvalidPackFormatException
    {
        if (dataMap.containsKey(bendsPack))
        {
            return dataMap.get(bendsPack);
        }

        if (bendsPack instanceof LocalBendsPack)
        {
            try
            {
                File file = PackManager.INSTANCE.getDataFileForPack(bendsPack.getKey());
                BendsPackData data;
                try (JsonReader fileReader = new JsonReader(new FileReader(file)))
                {
                    data = KumoSerializer.INSTANCE.gson.fromJson(fileReader, BendsPackData.class);
                }

                validatePackData(data);

                dataMap.put(bendsPack, data);
                return data;
            }
            catch (IOException ex)
            {
                LOGGER.error("Data for pack '{}' couldn't be fetched", bendsPack.getKey(), ex);
                throw new InvalidPackFormatException(getPackName(bendsPack), String.format("Data for pack '%s' couldn't be fetched", bendsPack.getKey()));
            }
            catch (JsonSyntaxException | MalformedKumoTemplateException ex)
            {
                LOGGER.error("The '{}' pack isn't in a correct format", bendsPack.getKey(), ex);
                throw new InvalidPackFormatException(getPackName(bendsPack), String.format("The '%s' pack isn't in a correct format.", bendsPack.getKey()));
            }
            catch (RuntimeException | StackOverflowError ex)
            {
                LOGGER.error("The '{}' pack couldn't be deserialised", bendsPack.getKey(), ex);
                throw new InvalidPackFormatException(getPackName(bendsPack), String.format("The '%s' pack isn't in a correct format.", bendsPack.getKey()));
            }
        }

        return null;
    }

    private static String getPackName(IBendsPack bendsPack)
    {
        final String displayName = bendsPack.getDisplayName();
        return displayName != null ? displayName : String.valueOf(bendsPack.getKey());
    }

    public void validatePackData(BendsPackData data) throws MalformedKumoTemplateException
    {
        if (data == null)
        {
            throw new MalformedKumoTemplateException("The pack data is empty.");
        }

        if (data.targets == null)
        {
            throw new MalformedKumoTemplateException("No targets were specified!");
        }

        IKumoValidationContext context = new IKumoValidationContext() {
            @Override
            public boolean doesAnimationExist(String animationKey)
            {
                if (data.keyframeAnimations != null && data.keyframeAnimations.containsKey(animationKey))
                {
                    return true;
                }

                try
                {
                    return AnimationLoader.loadFromPath(animationKey) != null;
                }
                catch (IOException e)
                {
                    LOGGER.error("Couldn't load animation '{}'", animationKey, e);
                    return false;
                }
            }
        };

        for (AnimatorTemplate animator : data.targets.values())
        {
            if (animator == null || animator.layers == null)
            {
                throw new MalformedKumoTemplateException("No layers were specified");
            }

            for (LayerTemplate layer : animator.layers)
            {
                if (layer == null)
                {
                    throw new MalformedKumoTemplateException("An empty layer was specified");
                }

                layer.validate(context);
            }
        }
    }

    public BendsPackData getAppliedData()
    {
        return appliedData;
    }

}
