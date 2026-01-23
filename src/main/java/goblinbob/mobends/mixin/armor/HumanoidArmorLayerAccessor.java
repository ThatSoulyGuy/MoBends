package goblinbob.mobends.mixin.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin to get the inner and outer armor models from HumanoidArmorLayer.
 */
@Mixin(HumanoidArmorLayer.class)
public interface HumanoidArmorLayerAccessor {

    @Accessor("innerModel")
    HumanoidModel<?> mobends$getInnerModel();

    @Accessor("outerModel")
    HumanoidModel<?> mobends$getOuterModel();
}
