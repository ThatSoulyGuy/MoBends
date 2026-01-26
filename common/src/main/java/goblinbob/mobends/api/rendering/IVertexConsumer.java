package goblinbob.mobends.api.rendering;

/**
 * Platform-agnostic vertex consumer abstraction.
 * Wraps Minecraft's VertexConsumer for vertex output.
 */
public interface IVertexConsumer
{
    /**
     * Adds a vertex position
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return this for chaining
     */
    IVertexConsumer addVertex(float x, float y, float z);

    /**
     * Sets the vertex color
     * @param red Red component (0-255)
     * @param green Green component (0-255)
     * @param blue Blue component (0-255)
     * @param alpha Alpha component (0-255)
     * @return this for chaining
     */
    IVertexConsumer setColor(int red, int green, int blue, int alpha);

    /**
     * Sets the vertex color
     * @param color The packed ARGB color
     * @return this for chaining
     */
    default IVertexConsumer setColor(int color)
    {
        return setColor(
                (color >> 16) & 0xFF,
                (color >> 8) & 0xFF,
                color & 0xFF,
                (color >> 24) & 0xFF
        );
    }

    /**
     * Sets the texture UV coordinates
     * @param u U coordinate
     * @param v V coordinate
     * @return this for chaining
     */
    IVertexConsumer setUv(float u, float v);

    /**
     * Sets the overlay coordinates
     * @param u U coordinate
     * @param v V coordinate
     * @return this for chaining
     */
    IVertexConsumer setOverlay(int u, int v);

    /**
     * Sets the overlay from packed value
     * @param overlay The packed overlay value
     * @return this for chaining
     */
    IVertexConsumer setOverlay(int overlay);

    /**
     * Sets the light coordinates
     * @param u U coordinate
     * @param v V coordinate
     * @return this for chaining
     */
    IVertexConsumer setLight(int u, int v);

    /**
     * Sets the light from packed value
     * @param light The packed light value
     * @return this for chaining
     */
    IVertexConsumer setLight(int light);

    /**
     * Sets the vertex normal
     * @param x X component
     * @param y Y component
     * @param z Z component
     * @return this for chaining
     */
    IVertexConsumer setNormal(float x, float y, float z);

    /**
     * @return The native platform VertexConsumer object
     */
    Object getNative();
}
