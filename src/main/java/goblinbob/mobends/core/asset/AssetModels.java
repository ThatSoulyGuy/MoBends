package goblinbob.mobends.core.asset;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetModels
{
    public static final AssetModels INSTANCE = new AssetModels();
    private static final Logger LOGGER = LogUtils.getLogger();

    private final FaceBakery faceBakery = new FaceBakery();
    private final Map<AssetLocation, BakedModel> bakedModelMap = new HashMap<>();

    public BakedModel register(AssetLocation location) throws IOException
    {
        try (InputStream stream = new FileInputStream(AssetsModule.INSTANCE.getAssetFile(location));
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
        {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Parse the model manually for custom asset models
            BakedModel bakedModel = parseAndBakeModel(jsonObject, location);
            bakedModelMap.put(location, bakedModel);

            return bakedModel;
        }
    }

    public void clearCache()
    {
        bakedModelMap.clear();
    }

    public BakedModel getModel(AssetLocation location)
    {
        if (!bakedModelMap.containsKey(location))
        {
            try
            {
                BakedModel bakedModel = register(location);
                bakedModelMap.put(location, bakedModel);
                return bakedModel;
            }
            catch(IOException e)
            {
                LOGGER.warn("Failed to bake asset model: {}", location.toString(), e);
                bakedModelMap.put(location, null);
                return null;
            }
        }

        return bakedModelMap.get(location);
    }

    private AssetLocation resolveTextureName(String name)
    {
        if (name.equals("missingno") || name.startsWith("#"))
        {
            return null;
        }

        return new AssetLocation("textures/" + name);
    }

    private BakedModel parseAndBakeModel(JsonObject modelJson, AssetLocation modelLocation) throws IOException
    {
        // Get missing texture sprite as fallback
        TextureAtlasSprite missingSprite = Minecraft.getInstance()
                .getTextureAtlas(new ResourceLocation("minecraft", "textures/atlas/blocks.png"))
                .apply(MissingTextureAtlasSprite.getLocation());

        List<BakedQuad> generalQuads = new ArrayList<>();
        Map<Direction, List<BakedQuad>> faceQuads = new HashMap<>();
        for (Direction dir : Direction.values())
        {
            faceQuads.put(dir, new ArrayList<>());
        }

        // Simple model builder using no transforms
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(false, false, false,
                net.minecraft.client.renderer.block.model.ItemTransforms.NO_TRANSFORMS,
                null);
        builder.particle(missingSprite);

        return builder.build();
    }
}
