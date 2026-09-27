package goblinbob.mobends.core.animation.keyframe;

import com.google.gson.Gson;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import goblinbob.mobends.lib.animation.keyframe.BinaryAnimationLoader;
import goblinbob.mobends.lib.animation.keyframe.KeyframeAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AnimationLoader
{

    private static Map<ResourceLocation, KeyframeAnimation> cachedAnimations = new HashMap<>();

    public static void clearCache()
    {
        cachedAnimations.clear();
    }

    public static KeyframeAnimation loadFromResource(ResourceLocation location) throws IOException
    {
        if (cachedAnimations.containsKey(location))
        {
            return cachedAnimations.get(location);
        }

        Optional<Resource> resourceOpt = Minecraft.getInstance().getResourceManager().getResource(location);
        if (resourceOpt.isEmpty())
        {
            throw new IOException("Resource not found: " + location);
        }

        try (InputStream stream = resourceOpt.get().open())
        {
            KeyframeAnimation animation = null;
            if (location.getPath().endsWith(".json"))
            {
                animation = (new Gson()).fromJson(new InputStreamReader(stream), KeyframeAnimation.class);
            }
            else
            {
                animation = BinaryAnimationLoader.loadFromBinaryInputStream(stream);
            }

            if (animation != null)
            {
                cachedAnimations.put(location, animation);
            }
            return animation;
        }
    }

    public static KeyframeAnimation loadFromPath(String key) throws IOException
    {
        int colonIndex = key.indexOf(":");
        if (colonIndex != -1)
        {
            final String domain = key.substring(0, colonIndex);
            final String path = key.substring(colonIndex + 1);

            return loadFromResource(ResourceLocationFactory.create(domain, path));
        }

        return null;
    }

}
