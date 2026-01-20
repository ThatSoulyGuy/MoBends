package goblinbob.mobends.mixin.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to handle armor trim rendering with Mo'Bends transforms.
 * Armor trims are decorative patterns on armor that need to follow the same transforms.
 *
 * Note: In 1.20.1, armor trims are rendered as part of HumanoidArmorLayer,
 * so this mixin may not need separate handling if HumanoidArmorLayerMixin covers it.
 * This is kept for potential future separation or edge cases.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorTrimLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>>
{
    /**
     * Inject before armor trim rendering to apply Mo'Bends transforms.
     * This ensures trim textures follow the animated armor geometry.
     */
    @Inject(
            method = "renderTrim",
            at = @At("HEAD"),
            cancellable = true,
            require = 0 // Don't fail if method doesn't exist in this version
    )
    private void mobends$onRenderTrim(
            CallbackInfo ci)
    {
        // Armor trims follow the same model as armor, so if the armor model
        // has been transformed by Mo'Bends, the trim will automatically follow.
        // This injection point is available for additional trim-specific handling
        // if needed in the future.
    }

    /**
     * Check if entity has Mo'Bends animation data.
     */
    @Unique
    private boolean mobends$hasAnimationData(T entity)
    {
        Object data = EntityDatabase.instance.get(entity);
        if (data == null)
        {
            return false;
        }
        return data instanceof BipedEntityData<?>;
    }
}
