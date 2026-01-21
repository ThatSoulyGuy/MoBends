package goblinbob.mobends.standard.client.model.armor;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Stores all data for a captured vertex from armor rendering.
 * Immutable - created once during capture.
 */
@OnlyIn(Dist.CLIENT)
public class CapturedVertex
{
    public final float x, y, z;
    public final float red, green, blue, alpha;
    public final float u, v;
    public final int overlayUV;
    public final int lightmapUV;
    public final float normalX, normalY, normalZ;

    public CapturedVertex(float x, float y, float z,
                          float red, float green, float blue, float alpha,
                          float u, float v,
                          int overlayUV, int lightmapUV,
                          float normalX, float normalY, float normalZ)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.u = u;
        this.v = v;
        this.overlayUV = overlayUV;
        this.lightmapUV = lightmapUV;
        this.normalX = normalX;
        this.normalY = normalY;
        this.normalZ = normalZ;
    }
}
