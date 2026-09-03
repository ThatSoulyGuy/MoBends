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
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class WeaponTrailMetrics
{
    public static final float VANILLA_SPAN = 16.0F;

    private static final float UNITS_PER_BLOCK = 16.0F;
    private static final float SPAN_GROWTH = 0.5F;
    private static final float MAX_SPAN = 24.0F;
    private static final float MIN_LENGTH = VANILLA_SPAN * 0.25F;
    private static final float MAX_LENGTH = VANILLA_SPAN * 6.0F;
    private static final float TIP_TIE_TOLERANCE = 0.1F;
    private static final int MAX_CACHED_SEGMENTS = 256;

    public static final class Segment
    {
        public final float startX, startY, startZ;
        public final float endX, endY, endZ;

        public Segment(float startX, float startY, float startZ, float endX, float endY, float endZ)
        {
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.endX = endX;
            this.endY = endY;
            this.endZ = endZ;
        }
    }

    public static final Segment FALLBACK = new Segment(0.0F, 0.0F, 0.0F, 0.0F, VANILLA_SPAN, 0.0F);

    private static final Map<BakedModel, Segment> RIGHT_HAND_CACHE = new IdentityHashMap<>();
    private static final Map<BakedModel, Segment> LEFT_HAND_CACHE = new IdentityHashMap<>();
    private static final Map<Item, Float> GEOMETRY_REACH_CACHE = new HashMap<>();
    private static final RandomSource RANDOM = RandomSource.create();

    private WeaponTrailMetrics()
    {
    }

    public static void clearCache()
    {
        RIGHT_HAND_CACHE.clear();
        LEFT_HAND_CACHE.clear();
        GEOMETRY_REACH_CACHE.clear();
    }

    public static Segment getTrailSegment(ItemStack itemStack, LivingEntity entity, ItemDisplayContext displayContext)
    {
        if (itemStack == null || itemStack.isEmpty() || entity == null)
        {
            return FALLBACK;
        }

        try
        {
            final BakedModel model = Minecraft.getInstance().getItemRenderer()
                    .getModel(itemStack, entity.level(), entity, entity.getId());

            if (model == null)
            {
                return FALLBACK;
            }

            if (model.isCustomRenderer())
            {
                return geometrySegment(itemStack, model, displayContext);
            }

            final boolean leftHand = displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            final Map<BakedModel, Segment> cache = leftHand ? LEFT_HAND_CACHE : RIGHT_HAND_CACHE;

            Segment segment = cache.get(model);
            if (segment == null)
            {
                segment = measureSegment(model, displayContext, leftHand);

                if (cache.size() >= MAX_CACHED_SEGMENTS)
                {
                    cache.clear();
                }
                cache.put(model, segment);
            }

            return segment;
        }
        catch (Throwable t)
        {
            return FALLBACK;
        }
    }

    public static float displayOffsetX(ItemStack itemStack, LivingEntity entity, ItemDisplayContext displayContext)
    {
        if (itemStack == null || itemStack.isEmpty() || entity == null)
        {
            return 0.0F;
        }

        try
        {
            final BakedModel model = Minecraft.getInstance().getItemRenderer()
                    .getModel(itemStack, entity.level(), entity, entity.getId());

            return model == null ? 0.0F
                    : model.getTransforms().getTransform(displayContext).translation.x() * UNITS_PER_BLOCK;
        }
        catch (Throwable t)
        {
            return 0.0F;
        }
    }

    private static float spanFor(float length)
    {
        float span = VANILLA_SPAN + Math.max(0.0F, length - VANILLA_SPAN) * SPAN_GROWTH;
        span = Math.min(span, MAX_SPAN);
        return Math.min(span, length);
    }

    private static Segment geometrySegment(ItemStack itemStack, BakedModel model, ItemDisplayContext displayContext)
    {
        float outer = geometryReach(itemStack, model, displayContext);
        if (outer <= 0.0F)
        {
            return FALLBACK;
        }

        outer = Math.max(MIN_LENGTH, Math.min(MAX_LENGTH, outer));

        final float span = spanFor(outer);

        return new Segment(0.0F, outer - span, 0.0F, 0.0F, outer, 0.0F);
    }

    private static float geometryReach(ItemStack itemStack, BakedModel model, ItemDisplayContext displayContext)
    {
        final Item item = itemStack.getItem();

        Float cached = GEOMETRY_REACH_CACHE.get(item);
        if (cached == null)
        {
            cached = measureGeometryModel(item);

            if (GEOMETRY_REACH_CACHE.size() >= MAX_CACHED_SEGMENTS)
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

        float maxExtent = 0.0F;

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

                        if (extent > maxExtent)
                        {
                            maxExtent = extent;
                        }
                    }
                }
            }
        }

        return maxExtent;
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

    private static Segment measureSegment(BakedModel model, ItemDisplayContext displayContext, boolean leftHand)
    {
        final ItemTransform transform = model.getTransforms().getTransform(displayContext);

        final PoseStack poseStack = new PoseStack();
        transform.apply(leftHand, poseStack);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        final Matrix4f matrix = poseStack.last().pose();

        List<Vector3f> vertices = collectVertices(model, matrix, true);
        if (vertices.size() < 2)
        {
            vertices = collectVertices(model, matrix, false);
        }
        if (vertices.size() < 2)
        {
            return FALLBACK;
        }

        final Vector3f centroid = new Vector3f();
        for (final Vector3f vertex : vertices)
        {
            centroid.add(vertex);
        }
        centroid.div(vertices.size());

        Vector3f first = farthestFrom(vertices, centroid);
        Vector3f second = farthestFrom(vertices, first);
        first = farthestFrom(vertices, second);
        second = farthestFrom(vertices, first);

        final float firstDistanceSq = first.lengthSquared();
        final float secondDistanceSq = second.lengthSquared();

        final Vector3f tip;
        if (Math.abs(firstDistanceSq - secondDistanceSq) > TIP_TIE_TOLERANCE * Math.max(firstDistanceSq, secondDistanceSq))
        {
            tip = firstDistanceSq > secondDistanceSq ? first : second;
        }
        else
        {
            tip = first.y() >= second.y() ? first : second;
        }
        final Vector3f base = tip == first ? second : first;

        final Vector3f axis = new Vector3f(tip).sub(base);
        final float length = axis.length() * UNITS_PER_BLOCK;
        if (!(length >= MIN_LENGTH) || length > MAX_LENGTH)
        {
            return FALLBACK;
        }
        axis.normalize();

        final float span = spanFor(length);

        final float endX = tip.x() * UNITS_PER_BLOCK;
        final float endY = tip.y() * UNITS_PER_BLOCK;
        final float endZ = tip.z() * UNITS_PER_BLOCK;

        return new Segment(endX - axis.x() * span, endY - axis.y() * span, endZ - axis.z() * span,
                endX, endY, endZ);
    }

    private static Vector3f farthestFrom(List<Vector3f> vertices, Vector3f origin)
    {
        Vector3f farthest = vertices.get(0);
        float farthestDistanceSq = -1.0F;

        for (final Vector3f vertex : vertices)
        {
            final float distanceSq = vertex.distanceSquared(origin);
            if (distanceSq > farthestDistanceSq)
            {
                farthestDistanceSq = distanceSq;
                farthest = vertex;
            }
        }

        return farthest;
    }

    private static List<Vector3f> collectVertices(BakedModel model, Matrix4f matrix, boolean outlineOnly)
    {
        final List<Vector3f> vertices = new ArrayList<>();

        appendVertices(model.getQuads(null, null, RANDOM), matrix, outlineOnly, vertices);

        for (final Direction direction : Direction.values())
        {
            appendVertices(model.getQuads(null, direction, RANDOM), matrix, outlineOnly, vertices);
        }

        return vertices;
    }

    private static void appendVertices(List<BakedQuad> quads, Matrix4f matrix, boolean outlineOnly, List<Vector3f> out)
    {
        if (quads == null || quads.isEmpty())
        {
            return;
        }

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

            final int[] data = quad.getVertices();
            if (data.length < 4)
            {
                continue;
            }

            final int stride = data.length / 4;
            if (stride < 3)
            {
                continue;
            }

            for (int index = 0; index + 2 < data.length; index += stride)
            {
                final Vector4f vertex = new Vector4f(
                        Float.intBitsToFloat(data[index]),
                        Float.intBitsToFloat(data[index + 1]),
                        Float.intBitsToFloat(data[index + 2]),
                        1.0F);

                vertex.mul(matrix);

                out.add(new Vector3f(vertex.x(), vertex.y(), vertex.z()));
            }
        }
    }
}
