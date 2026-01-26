package goblinbob.mobends.api.rendering;

/**
 * Platform-agnostic buffer builder abstraction.
 * Provides immediate-mode vertex submission with a fluent API.
 *
 * Handles the API difference between:
 * - MC 1.20.1: vertex().color(r,g,b,a).uv(u,v).endVertex()
 * - MC 1.21.1: addVertex().setColor(packed).setUv(u,v) (no endVertex)
 */
public interface IBufferBuilder
{
    /**
     * Adds a vertex at the specified position.
     * In 1.21.1 this maps to addVertex(), in 1.20.1 to vertex().
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return This builder for chaining
     */
    IBufferBuilder addVertex(float x, float y, float z);

    /**
     * Sets the color for the current vertex using float components (0.0-1.0).
     *
     * @param r Red component
     * @param g Green component
     * @param b Blue component
     * @param a Alpha component
     * @return This builder for chaining
     */
    IBufferBuilder setColor(float r, float g, float b, float a);

    /**
     * Sets the color for the current vertex using integer components (0-255).
     *
     * @param r Red component
     * @param g Green component
     * @param b Blue component
     * @param a Alpha component
     * @return This builder for chaining
     */
    IBufferBuilder setColor(int r, int g, int b, int a);

    /**
     * Sets the color for the current vertex using a packed ARGB integer.
     *
     * @param packedColor The packed ARGB color
     * @return This builder for chaining
     */
    IBufferBuilder setColorPacked(int packedColor);

    /**
     * Sets the texture UV coordinates for the current vertex.
     *
     * @param u U coordinate
     * @param v V coordinate
     * @return This builder for chaining
     */
    IBufferBuilder setUv(float u, float v);

    /**
     * Sets the overlay coordinates (used for damage overlay, etc.).
     *
     * @param overlay The packed overlay coordinates
     * @return This builder for chaining
     */
    IBufferBuilder setOverlay(int overlay);

    /**
     * Sets the light coordinates (block + sky light).
     *
     * @param light The packed light coordinates
     * @return This builder for chaining
     */
    IBufferBuilder setLight(int light);

    /**
     * Sets the normal vector for the current vertex.
     *
     * @param x Normal X component
     * @param y Normal Y component
     * @param z Normal Z component
     * @return This builder for chaining
     */
    IBufferBuilder setNormal(float x, float y, float z);

    /**
     * Returns the native buffer builder object for platform-specific operations.
     *
     * @return The native BufferBuilder
     */
    Object getNative();
}
