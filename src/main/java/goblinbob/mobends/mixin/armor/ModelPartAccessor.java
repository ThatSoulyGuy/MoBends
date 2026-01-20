package goblinbob.mobends.mixin.armor;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

/**
 * Accessor mixin to expose internal fields of ModelPart.
 * Used by Tier 2 rendering for model analysis.
 */
@Mixin(ModelPart.class)
public interface ModelPartAccessor
{
    /**
     * Get the list of cubes in this model part.
     */
    @Accessor("cubes")
    List<ModelPart.Cube> mobends$getCubes();

    /**
     * Get the map of child parts.
     */
    @Accessor("children")
    Map<String, ModelPart> mobends$getChildren();

    /**
     * Get the initial X position.
     */
    @Accessor("x")
    float mobends$getX();

    /**
     * Get the initial Y position.
     */
    @Accessor("y")
    float mobends$getY();

    /**
     * Get the initial Z position.
     */
    @Accessor("z")
    float mobends$getZ();

    /**
     * Get the X rotation.
     */
    @Accessor("xRot")
    float mobends$getXRot();

    /**
     * Get the Y rotation.
     */
    @Accessor("yRot")
    float mobends$getYRot();

    /**
     * Get the Z rotation.
     */
    @Accessor("zRot")
    float mobends$getZRot();
}
