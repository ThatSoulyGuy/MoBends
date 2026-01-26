package goblinbob.mobends.core.animation.keyframe;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
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

    /**
     * This could hold animations that are embedded in kumo animators.
     */
    private static Map<String, KeyframeAnimation> internalRegistry = new HashMap<>();

    /**
     * This holds animations that are loaded from mod resources.
     */
    private static Map<ResourceLocation, KeyframeAnimation> cachedAnimations = new HashMap<>();

    public static void clearCache()
    {
        internalRegistry.clear();
        cachedAnimations.clear();
    }

    public static KeyframeAnimation loadFromFile(File file) throws IOException
    {
        if (file.getName().endsWith(".json"))
        {
            JsonReader fileReader = new JsonReader(new FileReader(file));
            return (new Gson()).fromJson(fileReader, KeyframeAnimation.class);
        }
        else
        {
            return BinaryAnimationLoader.loadFromBinaryInputStream(new BufferedInputStream(new FileInputStream(file)));
        }
    }

    public static KeyframeAnimation loadFromString(String animationJson)
    {
        return (new Gson()).fromJson(animationJson, KeyframeAnimation.class);
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

    /**
     * Loads the animation from an ambiguous path, either from a resource (modid:path) or an internal registry (key).
     * @param key
     * @return
     */
    public static KeyframeAnimation loadFromPath(String key) throws IOException
    {
        int colonIndex = key.indexOf(":");
        if (colonIndex != -1)
        {
            // The passed path is a resource path.
            final String domain = key.substring(0, colonIndex);
            final String path = key.substring(colonIndex + 1);

            return loadFromResource(ResourceLocationFactory.create(domain, path));
        }

        return internalRegistry.get(key);
    }

}
