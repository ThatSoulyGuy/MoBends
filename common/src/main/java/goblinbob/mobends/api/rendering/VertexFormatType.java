package goblinbob.mobends.api.rendering;

/**
 * Platform-agnostic vertex format enumeration.
 * Maps to Minecraft's DefaultVertexFormat constants on each platform.
 */
public enum VertexFormatType
{
    /** Position only (x, y, z) */
    POSITION,

    /** Position + Color (x, y, z, r, g, b, a) */
    POSITION_COLOR,

    /** Position + Texture UV (x, y, z, u, v) */
    POSITION_TEX,

    /** Position + Texture UV + Color (x, y, z, u, v, r, g, b, a) */
    POSITION_TEX_COLOR,

    /** Position + Texture UV + Color + Normal (x, y, z, u, v, r, g, b, a, nx, ny, nz) */
    POSITION_TEX_COLOR_NORMAL
}
