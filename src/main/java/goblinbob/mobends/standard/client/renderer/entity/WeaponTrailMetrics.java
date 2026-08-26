package goblinbob.mobends.standard.client.renderer.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class WeaponTrailMetrics
{
    public static final float VANILLA_SPAN = 16.0F;

    private static final float SPAN_GROWTH = 0.5F;
    private static final float MAX_SPAN = 24.0F;
    private static final float MIN_LENGTH_RATIO = 0.5F;
    private static final float MAX_LENGTH_RATIO = 6.0F;
    private static final int MAX_CACHED_REACHES = 256;

    private static final Map<BakedModel, Float> REACH_CACHE = new IdentityHashMap<>();
    private static final Map<Item, Float> GEOMETRY_REACH_CACHE = new HashMap<>();
    private static final RandomSource RANDOM = RandomSource.create();

    private static float referenceReach = -1.0F;

    private WeaponTrailMetrics()
    {
    }

    public static void clearCache()
    {
        REACH_CACHE.clear();
        GEOMETRY_REACH_CACHE.clear();
        referenceReach = -1.0F;
    }

    public static float[] getTrailExtent(ItemStack itemStack, LivingEntity entity, ItemDisplayContext displayContext)
    {
        final float[] fallback = { 0.0F, VANILLA_SPAN };

        if (itemStack == null || itemStack.isEmpty() || entity == null)
        {
            return fallback;
        }

        float outer;

        final float geometryUnits = geometryUnitsOf(itemStack, entity, displayContext);

        if (geometryUnits > 0.0F)
        {
            outer = geometryUnits;
        }
        else
        {
            final float reference = referenceReach(entity);
            if (reference <= 0.0F)
            {
                return fallback;
            }

            final float reach = reachOf(itemStack, entity, displayContext);
            if (reach <= 0.0F)
            {
                return fallback;
            }

            outer = VANILLA_SPAN * Math.max(MIN_LENGTH_RATIO,
                    Math.min(MAX_LENGTH_RATIO, reach / reference));
        }

        outer = Math.max(VANILLA_SPAN * MIN_LENGTH_RATIO,
                Math.min(VANILLA_SPAN * MAX_LENGTH_RATIO, outer));

        float span = VANILLA_SPAN + (outer - VANILLA_SPAN) * SPAN_GROWTH;
        span = Math.min(span, MAX_SPAN);
        span = Math.min(span, outer);

        return new float[] { outer - span, outer };
    }

    private static float referenceReach(LivingEntity entity)
    {
        if (referenceReach < 0.0F)
        {
            referenceReach = reachOf(new ItemStack(Items.IRON_SWORD), entity,
                    ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        }

        return referenceReach;
    }

    private static float reachOf(ItemStack itemStack, LivingEntity entity, ItemDisplayContext displayContext)
    {
        try
        {
            final BakedModel model = Minecraft.getInstance().getItemRenderer()
                    .getModel(itemStack, entity.level(), entity, entity.getId());

            if (model == null)
            {
                return 0.0F;
            }

            if (model.isCustomRenderer())
            {
                return 0.0F;
            }

            final Float cached = REACH_CACHE.get(model);
            if (cached != null)
            {
                return cached;
            }

            final float reach = measureReach(model, displayContext);

            if (REACH_CACHE.size() >= MAX_CACHED_REACHES)
            {
                REACH_CACHE.clear();
            }
            REACH_CACHE.put(model, reach);

            return reach;
        }
        catch (Throwable t)
        {
            return 0.0F;
        }
    }

    private static float geometryUnitsOf(ItemStack itemStack, LivingEntity entity, ItemDisplayContext displayContext)
    {
        try
        {
            final BakedModel model = Minecraft.getInstance().getItemRenderer()
                    .getModel(itemStack, entity.level(), entity, entity.getId());

            if (model == null || !model.isCustomRenderer())
            {
                return 0.0F;
            }

            return geometryReach(itemStack, model, displayContext);
        }
        catch (Throwable t)
        {
            return 0.0F;
        }
    }

    private static float geometryReach(ItemStack itemStack, BakedModel model, ItemDisplayContext displayContext)
    {
        final Item item = itemStack.getItem();

        Float cached = GEOMETRY_REACH_CACHE.get(item);
        if (cached == null)
        {
            cached = measureGeometryModel(item);

            if (GEOMETRY_REACH_CACHE.size() >= MAX_CACHED_REACHES)
            {
                GEOMETRY_REACH_CACHE.clear();
            }
            GEOMETRY_REACH_CACHE.put(item, cached);
        }

        if (cached <= 0.0F)
        {
            return 0.0F;
        }

        final ItemTransform transform = model.getTransforms().getTransform(displayContext);
        final float scale = Math.max(Math.abs(transform.scale.x()),
                Math.max(Math.abs(transform.scale.y()), Math.abs(transform.scale.z())));

        return cached * scale;
    }

    private static float measureGeometryModel(Item item)
    {
        try
        {
            final ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            final String fileName = itemId.getPath() + ".geo.json";

            final Map<ResourceLocation, Resource> candidates = Minecraft.getInstance().getResourceManager()
                    .listResources("geo", location -> location.getNamespace().equals(itemId.getNamespace())
                            && location.getPath().endsWith("/" + fileName));

            if (candidates.isEmpty())
            {
                return 0.0F;
            }

            final Resource resource = candidates.values().iterator().next();

            try (java.io.BufferedReader reader = resource.openAsReader())
            {
                final JsonObject root = GsonHelper.parse(reader);
                return farthestCubeCorner(root);
            }
        }
        catch (Throwable t)
        {
            return 0.0F;
        }
    }

    private static float farthestCubeCorner(JsonObject root)
    {
        final JsonArray geometry = GsonHelper.getAsJsonArray(root, "minecraft:geometry", null);
        if (geometry == null || geometry.isEmpty())
        {
            return 0.0F;
        }

        float maxDistanceSq = 0.0F;

        for (final JsonElement geometryElement : geometry)
        {
            if (!geometryElement.isJsonObject())
            {
                continue;
            }

            final JsonArray bones = GsonHelper.getAsJsonArray(geometryElement.getAsJsonObject(), "bones", null);
            if (bones == null)
            {
                continue;
            }

            for (final JsonElement boneElement : bones)
            {
                if (!boneElement.isJsonObject())
                {
                    continue;
                }

                final JsonArray cubes = GsonHelper.getAsJsonArray(boneElement.getAsJsonObject(), "cubes", null);
                if (cubes == null)
                {
                    continue;
                }

                for (final JsonElement cubeElement : cubes)
                {
                    if (!cubeElement.isJsonObject())
                    {
                        continue;
                    }

                    final JsonObject cube = cubeElement.getAsJsonObject();
                    final float[] origin = readVector(cube, "origin");
                    final float[] size = readVector(cube, "size");

                    if (origin == null || size == null)
                    {
                        continue;
                    }

                    for (int axis = 0; axis < 3; ++axis)
                    {
                        final float extent = Math.max(Math.abs(origin[axis]),
                                Math.abs(origin[axis] + size[axis]));

                        if (extent > maxDistanceSq)
                        {
                            maxDistanceSq = extent;
                        }
                    }
                }
            }
        }

        return maxDistanceSq;
    }

    private static float[] readVector(JsonObject object, String key)
    {
        final JsonArray array = GsonHelper.getAsJsonArray(object, key, null);
        if (array == null || array.size() < 3)
        {
            return null;
        }

        return new float[] {
                array.get(0).getAsFloat(),
                array.get(1).getAsFloat(),
                array.get(2).getAsFloat()
        };
    }

    private static float measureReach(BakedModel model, ItemDisplayContext displayContext)
    {
        final ItemTransform transform = model.getTransforms().getTransform(displayContext);

        final PoseStack poseStack = new PoseStack();
        transform.apply(false, poseStack);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        final Matrix4f matrix = poseStack.last().pose();

        float outlineSq = measureQuads(model, matrix, true);

        if (outlineSq <= 0.0F)
        {
            outlineSq = measureQuads(model, matrix, false);
        }

        return (float) Math.sqrt(outlineSq);
    }

    private static float measureQuads(BakedModel model, Matrix4f matrix, boolean outlineOnly)
    {
        float maxDistanceSq = farthestVertexSq(model.getQuads(null, null, RANDOM), matrix, outlineOnly);

        for (final Direction direction : Direction.values())
        {
            maxDistanceSq = Math.max(maxDistanceSq,
                    farthestVertexSq(model.getQuads(null, direction, RANDOM), matrix, outlineOnly));
        }

        return maxDistanceSq;
    }

    private static float farthestVertexSq(List<BakedQuad> quads, Matrix4f matrix, boolean outlineOnly)
    {
        if (quads == null || quads.isEmpty())
        {
            return 0.0F;
        }

        float maxDistanceSq = 0.0F;

        for (final BakedQuad quad : quads)
        {
            if (outlineOnly)
            {
                final Direction face = quad.getDirection();
                if (face == Direction.NORTH || face == Direction.SOUTH)
                {
                    continue;
                }
            }

            final int[] vertices = quad.getVertices();
            if (vertices.length < 4)
            {
                continue;
            }

            final int stride = vertices.length / 4;
            if (stride < 3)
            {
                continue;
            }

            for (int index = 0; index + 2 < vertices.length; index += stride)
            {
                final Vector4f vertex = new Vector4f(
                        Float.intBitsToFloat(vertices[index]),
                        Float.intBitsToFloat(vertices[index + 1]),
                        Float.intBitsToFloat(vertices[index + 2]),
                        1.0F);

                vertex.mul(matrix);

                final float distanceSq = vertex.x() * vertex.x()
                        + vertex.y() * vertex.y()
                        + vertex.z() * vertex.z();

                if (distanceSq > maxDistanceSq)
                {
                    maxDistanceSq = distanceSq;
                }
            }
        }

        return maxDistanceSq;
    }
}
