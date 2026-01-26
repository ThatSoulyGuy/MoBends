package goblinbob.mobends.api.rendering;

/**
 * Platform-agnostic draw mode enumeration.
 * Maps to Minecraft's VertexFormat.Mode on each platform.
 */
public enum DrawMode
{
    QUADS,
    TRIANGLES,
    TRIANGLE_STRIP,
    TRIANGLE_FAN,
    LINES,
    LINE_STRIP,
    DEBUG_LINES,
    DEBUG_LINE_STRIP
}
