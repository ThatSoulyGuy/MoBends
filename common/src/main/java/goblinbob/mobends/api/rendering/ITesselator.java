package goblinbob.mobends.api.rendering;

import goblinbob.mobends.api.platform.PlatformServices;

/**
 * Platform-agnostic Tesselator abstraction.
 *
 * Handles the API difference between:
 * - MC 1.20.1: Tesselator.getInstance().getBuilder() then builder.begin()
 * - MC 1.21.1: Tesselator.getInstance().begin() returns the builder directly
 *
 * Also handles the end/build difference:
 * - MC 1.20.1: builder.end() returns data for upload
 * - MC 1.21.1: builder.buildOrThrow() returns data for upload
 */
public interface ITesselator
{
    /**
     * Begins building vertices with the specified mode and format.
     *
     * @param mode The drawing mode (QUADS, LINES, etc.)
     * @param format The vertex format (POSITION_COLOR, POSITION_TEX, etc.)
     * @return A buffer builder for adding vertices
     */
    IBufferBuilder begin(DrawMode mode, VertexFormatType format);

    /**
     * Ends the current build, uploads the geometry, and draws it with the current shader.
     *
     * This handles the difference between:
     * - MC 1.20.1: BufferUploader.drawWithShader(builder.end())
     * - MC 1.21.1: BufferUploader.drawWithShader(builder.buildOrThrow())
     *
     * @param builder The buffer builder to finalize and draw
     */
    void endAndDraw(IBufferBuilder builder);

    /**
     * Returns the native Tesselator object for platform-specific operations.
     *
     * @return The native Tesselator
     */
    Object getNative();

    /**
     * Gets the platform's Tesselator singleton instance.
     *
     * @return The Tesselator instance
     */
    static ITesselator getInstance()
    {
        return PlatformServices.get().getTesselator();
    }
}
