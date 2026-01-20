package goblinbob.mobends.mixin.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to ensure elytra renders correctly with Mo'Bends body transforms.
 * The elytra attaches to the player's back and needs to follow body rotation.
 */
@Mixin(ElytraLayer.class)
public abstract class ElytraLayerMixin<T extends LivingEntity>
{
    /**
     * Inject at the start of elytra rendering to apply body transforms.
     */
    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD")
    )
    private void mobends$onRenderStart(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci)
    {
        // Get Mo'Bends data for this entity
        BipedEntityData<?> entityData = mobends$getEntityData(entity);
        if (entityData == null)
        {
            return;
        }

        // The body transform will be applied to the elytra through the parent
        // model's transform. If additional adjustments are needed, they can be
        // applied here.
        //
        // Note: The actual elytra transform handling is done in the dedicated
        // LayerCustomElytra class for entities that Mo'Bends controls.
        // This mixin ensures compatibility when elytra is rendered through
        // the vanilla path.
    }

    /**
     * Get Mo'Bends entity data for an entity.
     */
    @Unique
    @SuppressWarnings("unchecked")
    private BipedEntityData<?> mobends$getEntityData(T entity)
    {
        Object data = EntityDatabase.instance.get(entity);
        if (data == null)
        {
            return null;
        }
        if (data instanceof BipedEntityData<?>)
        {
            return (BipedEntityData<?>) data;
        }
        return null;
    }
}
