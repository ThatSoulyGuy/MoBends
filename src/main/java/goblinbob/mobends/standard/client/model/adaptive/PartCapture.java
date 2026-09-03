package goblinbob.mobends.standard.client.model.adaptive;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.standard.client.model.armor.ArmorPoseHelper;
import goblinbob.mobends.standard.client.model.armor.CapturedVertex;
import goblinbob.mobends.standard.client.model.armor.CapturingVertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PartCapture
{
    public static final float PIXELS = 16.0F;

    private static final PartCapture EMPTY = new PartCapture(Collections.emptyList(), 0, 0, 0);

    public final List<CapturedVertex[]> quads;

    public final float pivotX, pivotY, pivotZ;

    public final float minX, minY, minZ, maxX, maxY, maxZ;

    public final float baseMinX, baseMinY, baseMinZ, baseMaxX, baseMaxY, baseMaxZ;

    public final int cubeCount;

    public final float frontUMin, frontUMax, frontVMin, frontVMax;

    private PartCapture(List<CapturedVertex[]> quads, float pivotX, float pivotY, float pivotZ)
    {
        this.quads = quads;
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.pivotZ = pivotZ;
        this.cubeCount = quads.size() / 6;

        float lowX = Float.MAX_VALUE, lowY = Float.MAX_VALUE, lowZ = Float.MAX_VALUE;
        float highX = -Float.MAX_VALUE, highY = -Float.MAX_VALUE, highZ = -Float.MAX_VALUE;

        for (CapturedVertex[] quad : quads)
        {
            for (CapturedVertex vertex : quad)
            {
                lowX = Math.min(lowX, vertex.x);
                lowY = Math.min(lowY, vertex.y);
                lowZ = Math.min(lowZ, vertex.z);
                highX = Math.max(highX, vertex.x);
                highY = Math.max(highY, vertex.y);
                highZ = Math.max(highZ, vertex.z);
            }
        }

        if (quads.isEmpty())
        {
            lowX = lowY = lowZ = highX = highY = highZ = 0;
        }

        this.minX = lowX * PIXELS;
        this.minY = lowY * PIXELS;
        this.minZ = lowZ * PIXELS;
        this.maxX = highX * PIXELS;
        this.maxY = highY * PIXELS;
        this.maxZ = highZ * PIXELS;

        float baseLowX = this.minX, baseLowY = this.minY, baseLowZ = this.minZ;
        float baseHighX = this.maxX, baseHighY = this.maxY, baseHighZ = this.maxZ;

        if (cubeCount > 1)
        {
            baseLowX = baseLowY = baseLowZ = Float.MAX_VALUE;
            baseHighX = baseHighY = baseHighZ = -Float.MAX_VALUE;

            for (int i = 0; i < 6; ++i)
            {
                for (CapturedVertex vertex : quads.get(i))
                {
                    baseLowX = Math.min(baseLowX, vertex.x * PIXELS);
                    baseLowY = Math.min(baseLowY, vertex.y * PIXELS);
                    baseLowZ = Math.min(baseLowZ, vertex.z * PIXELS);
                    baseHighX = Math.max(baseHighX, vertex.x * PIXELS);
                    baseHighY = Math.max(baseHighY, vertex.y * PIXELS);
                    baseHighZ = Math.max(baseHighZ, vertex.z * PIXELS);
                }
            }
        }

        this.baseMinX = baseLowX;
        this.baseMinY = baseLowY;
        this.baseMinZ = baseLowZ;
        this.baseMaxX = baseHighX;
        this.baseMaxY = baseHighY;
        this.baseMaxZ = baseHighZ;

        float uLow = 0.0F, uHigh = 0.0F, vLow = 0.0F, vHigh = 0.0F;

        for (int i = 0; i < Math.min(6, quads.size()); ++i)
        {
            CapturedVertex[] quad = quads.get(i);
            if (Math.abs(quad[0].normalZ + 1.0F) > 1.0e-3F)
            {
                continue;
            }

            uLow = vLow = Float.MAX_VALUE;
            uHigh = vHigh = -Float.MAX_VALUE;

            for (CapturedVertex vertex : quad)
            {
                uLow = Math.min(uLow, vertex.u);
                vLow = Math.min(vLow, vertex.v);
                uHigh = Math.max(uHigh, vertex.u);
                vHigh = Math.max(vHigh, vertex.v);
            }
            break;
        }

        this.frontUMin = uLow;
        this.frontUMax = uHigh;
        this.frontVMin = vLow;
        this.frontVMax = vHigh;
    }

    public boolean isEmpty()
    {
        return quads.isEmpty();
    }

    public static PartCapture ofOwnCubes(ModelPart part)
    {
        return capture(part, true, false);
    }

    public static PartCapture ofOverlay(ModelPart part)
    {
        return capture(part, false, true);
    }

    public static PartCapture ofSubtree(ModelPart part)
    {
        return capture(part, false, false);
    }

    private static PartCapture capture(ModelPart part, boolean ownCubesOnly, boolean keepRestRotation)
    {
        if (part == null)
        {
            return EMPTY;
        }

        final List<ModelPart> subtree = new ArrayList<>();
        part.getAllParts().forEach(subtree::add);

        final boolean[] visibility = new boolean[subtree.size()];
        final boolean[] skipDraws = new boolean[subtree.size()];

        for (int i = 0; i < subtree.size(); ++i)
        {
            final ModelPart entry = subtree.get(i);
            visibility[i] = entry.visible;
            skipDraws[i] = entry.skipDraw;
            entry.visible = !ownCubesOnly || entry == part;
            entry.skipDraw = false;
        }

        final float[] storage = new float[6];
        ArmorPoseHelper.resetPartToOrigin(part, storage);

        if (keepRestRotation)
        {
            part.xRot = storage[3];
            part.yRot = storage[4];
            part.zRot = storage[5];
        }

        final CapturingVertexConsumer consumer = new CapturingVertexConsumer();
        List<CapturedVertex> vertices;

        goblinbob.mobends.compat.MorePlayerModelsCompat.beginNeutralCapture();

        try
        {
            part.render(new PoseStack(), consumer, 0, OverlayTexture.NO_OVERLAY);
            vertices = consumer.getVertices();
        }
        catch (Exception e)
        {
            vertices = Collections.emptyList();
        }
        finally
        {
            goblinbob.mobends.compat.MorePlayerModelsCompat.endNeutralCapture();

            ArmorPoseHelper.restorePartFromStorage(part, storage);

            for (int i = 0; i < subtree.size(); ++i)
            {
                subtree.get(i).visible = visibility[i];
                subtree.get(i).skipDraw = skipDraws[i];
            }
        }

        return new PartCapture(ArmorPoseHelper.groupIntoQuads(vertices),
                               storage[0], storage[1], storage[2]);
    }
}
