package goblinbob.mobends.mixin.armor;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public interface ModelPartAccessor
{
    @Accessor("cubes")
    List<ModelPart.Cube> mobends$getCubes();

    @Accessor("children")
    Map<String, ModelPart> mobends$getChildren();

    @Accessor("x")
    float mobends$getX();

    @Accessor("y")
    float mobends$getY();

    @Accessor("z")
    float mobends$getZ();

    @Accessor("xRot")
    float mobends$getXRot();

    @Accessor("yRot")
    float mobends$getYRot();

    @Accessor("zRot")
    float mobends$getZRot();
}
