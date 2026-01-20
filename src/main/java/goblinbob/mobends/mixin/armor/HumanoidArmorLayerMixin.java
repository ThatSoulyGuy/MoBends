package goblinbob.mobends.mixin.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderContext;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderingFacade;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept armor layer rendering and redirect to Mo'Bends armor rendering system.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>>
{
    @Unique
    private static ArmorRenderingFacade mobends$armorFacade;

    /**
     * Inject at the head of renderArmorPiece to potentially redirect rendering.
     */
    @Inject(
            method = "renderArmorPiece",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mobends$onRenderArmorPiece(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            T entity,
            EquipmentSlot slot,
            int packedLight,
            A armorModel,
            CallbackInfo ci)
    {
        // Check if this entity has Mo'Bends animation data
        if (!mobends$shouldUseMoBendsRendering(entity))
        {
            return; // Let vanilla handle it
        }

        // Get entity data
        BipedEntityData<?> entityData = mobends$getEntityData(entity);
        if (entityData == null)
        {
            return; // No animation data, use vanilla
        }

        // Get armor stack
        ItemStack armorStack = entity.getItemBySlot(slot);
        if (armorStack.isEmpty())
        {
            return;
        }

        // Build render context
        ArmorRenderContext<T> context = ArmorRenderContext.<T>builder()
                .entity(entity)
                .entityData(entityData)
                .slot(slot)
                .armorStack(armorStack)
                .poseStack(poseStack)
                .bufferSource(bufferSource)
                .packedLight(packedLight)
                .packedOverlay(0) // Will be computed by the armor layer
                .partialTicks(0) // Not directly available here
                .armorModel(armorModel)
                .build();

        // Use Mo'Bends armor rendering facade
        ArmorRenderingFacade facade = mobends$getArmorFacade();
        if (facade != null && facade.render(context, armorModel))
        {
            // Mo'Bends handled the rendering
            ci.cancel();
        }
        // Otherwise, let vanilla continue
    }

    /**
     * Check if Mo'Bends rendering should be used for this entity.
     */
    @Unique
    private boolean mobends$shouldUseMoBendsRendering(T entity)
    {
        // Check if entity is in the database and has animation data
        return EntityDatabase.instance.get(entity) != null;
    }

    /**
     * Get the Mo'Bends entity data for an entity.
     */
    @Unique
    @SuppressWarnings("unchecked")
    private BipedEntityData<?> mobends$getEntityData(T entity)
    {
        Object data = EntityDatabase.instance.get(entity);
        if (data instanceof BipedEntityData<?>)
        {
            return (BipedEntityData<?>) data;
        }
        return null;
    }

    /**
     * Get or create the armor rendering facade.
     */
    @Unique
    private ArmorRenderingFacade mobends$getArmorFacade()
    {
        if (mobends$armorFacade == null)
        {
            mobends$armorFacade = new ArmorRenderingFacade();
        }
        return mobends$armorFacade;
    }
}
