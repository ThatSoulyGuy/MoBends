package goblinbob.mobends.core.client;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.api.platform.PlatformServices;
import goblinbob.mobends.api.rendering.DrawMode;
import goblinbob.mobends.api.rendering.IBufferBuilder;
import goblinbob.mobends.api.rendering.ITesselator;
import goblinbob.mobends.api.rendering.VertexFormatType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class TrailRenderQueue
{

    private static final int INITIAL_CAPACITY = 512;

    private static float[] positions = new float[INITIAL_CAPACITY * 3];
    private static int[] colors = new int[INITIAL_CAPACITY];
    private static int vertexCount = 0;

    private static final Matrix4f levelView = new Matrix4f();
    private static final Matrix4f levelViewInverse = new Matrix4f();
    private static final Vector3f scratch = new Vector3f();
    private static double cameraX, cameraY, cameraZ;
    private static boolean frameReady = false;

    private TrailRenderQueue()
    {
    }

    public static void beginFrame(Matrix4f pose, double camX, double camY, double camZ)
    {
        levelView.set(pose);
        levelViewInverse.set(pose).invert();
        cameraX = camX;
        cameraY = camY;
        cameraZ = camZ;
        frameReady = true;
    }

    public static boolean hasFrame()
    {
        return frameReady;
    }

    public static Vector3f worldToView(double x, double y, double z, Vector3f dest)
    {
        dest.set((float) (x - cameraX), (float) (y - cameraY), (float) (z - cameraZ));
        return levelView.transformPosition(dest);
    }

    public static void viewToWorld(float x, float y, float z, double[] dest)
    {
        scratch.set(x, y, z);
        levelViewInverse.transformPosition(scratch);
        dest[0] = cameraX + scratch.x;
        dest[1] = cameraY + scratch.y;
        dest[2] = cameraZ + scratch.z;
    }

    public static void vertex(float x, float y, float z, int color)
    {
        ensureCapacity(vertexCount + 1);

        final int offset = vertexCount * 3;
        positions[offset] = x;
        positions[offset + 1] = y;
        positions[offset + 2] = z;
        colors[vertexCount] = color;
        vertexCount++;
    }

    private static void ensureCapacity(int required)
    {
        if (required <= colors.length)
        {
            return;
        }

        int capacity = colors.length;
        while (capacity < required)
        {
            capacity *= 2;
        }

        final float[] newPositions = new float[capacity * 3];
        final int[] newColors = new int[capacity];
        System.arraycopy(positions, 0, newPositions, 0, vertexCount * 3);
        System.arraycopy(colors, 0, newColors, 0, vertexCount);
        positions = newPositions;
        colors = newColors;
    }

    public static void clear()
    {
        vertexCount = 0;
    }

    private static int applyFog(int color, float x, float y, float z,
                                float[] fogColor, float fogStart, float fogEnd)
    {
        final float distance = (float) Math.sqrt(x * x + y * y + z * z);

        if (distance <= fogStart || fogEnd <= fogStart)
        {
            return color;
        }

        float t = (distance - fogStart) / (fogEnd - fogStart);
        t = Math.max(0.0F, Math.min(1.0F, t));

        final float fogValue = t * t * (3.0F - 2.0F * t) * fogColor[3];

        final int alpha = (color >>> 24) & 0xFF;
        final int red = (color >> 16) & 0xFF;
        final int green = (color >> 8) & 0xFF;
        final int blue = color & 0xFF;

        final int fogRed = (int) (fogColor[0] * 255.0F);
        final int fogGreen = (int) (fogColor[1] * 255.0F);
        final int fogBlue = (int) (fogColor[2] * 255.0F);

        final int mixedRed = (int) (red + (fogRed - red) * fogValue);
        final int mixedGreen = (int) (green + (fogGreen - green) * fogValue);
        final int mixedBlue = (int) (blue + (fogBlue - blue) * fogValue);

        return (alpha << 24) | (mixedRed << 16) | (mixedGreen << 8) | mixedBlue;
    }

    public static void flush()
    {
        if (vertexCount == 0)
        {
            return;
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(515);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        PlatformServices.get().setPositionColorShader();

        final ITesselator tesselator = ITesselator.getInstance();
        final IBufferBuilder buffer = tesselator.begin(DrawMode.QUADS, VertexFormatType.POSITION_COLOR);

        final float[] fogColor = RenderSystem.getShaderFogColor();
        final float fogStart = RenderSystem.getShaderFogStart();
        final float fogEnd = RenderSystem.getShaderFogEnd();

        for (int i = 0; i < vertexCount; i++)
        {
            final int offset = i * 3;
            final float x = positions[offset];
            final float y = positions[offset + 1];
            final float z = positions[offset + 2];

            buffer.addVertex(x, y, z)
                    .setColorPacked(applyFog(colors[i], x, y, z, fogColor, fogStart, fogEnd));
        }

        tesselator.endAndDraw(buffer);

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        vertexCount = 0;
    }

}
