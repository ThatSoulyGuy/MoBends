package goblinbob.mobends.mixin.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderContext;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderingFacade;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept armor layer rendering and redirect to Mo'Bends armor rendering system.
 * Mo'Bends renders armor with split joint support (elbows/knees).
 *
 * Note: This mixin is NeoForge 1.21+ specific due to DataComponents and armor texture API changes.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>>
{
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends-ArmorTrim");

    @Unique
    private static ArmorRenderingFacade mobends$armorFacade;

    /**
     * Invoker for the private renderTrim method to call it after Mo'Bends renders armor.
     */
    @Invoker("renderTrim")
    protected abstract void mobends$invokeRenderTrim(
            Holder<ArmorMaterial> armorMaterial,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            ArmorTrim trim,
            A model,
            boolean leggings
    );

    /**
     * Inject at the head of renderArmorPiece to redirect to Mo'Bends rendering.
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

        if (!(armorStack.getItem() instanceof ArmorItem armorItem))
        {
            return;
        }

        // Get the armor texture using NeoForge's extension API
        // This allows mods to provide custom textures via IItemExtension.getArmorTexture()
        boolean isInnerModel = (slot == EquipmentSlot.LEGS);
        Holder<ArmorMaterial> materialHolder = armorItem.getMaterial();
        ArmorMaterial material = materialHolder.value();

        // Render each layer of the armor material
        ArmorRenderingFacade facade = mobends$getArmorFacade();
        if (facade == null)
        {
            return;
        }

        boolean anyLayerRendered = false;
        for (ArmorMaterial.Layer layer : material.layers())
        {
            // Get texture from the item - this calls NeoForge's IItemExtension.getArmorTexture()
            // which mods can override to provide custom textures
            ResourceLocation texture = armorItem.getArmorTexture(armorStack, entity, slot, layer, isInnerModel);
            if (texture == null)
            {
                continue;
            }

            // Render this layer with Mo'Bends
            boolean layerRendered = facade.renderArmor(
                    poseStack,
                    bufferSource,
                    packedLight,
                    entity,
                    slot,
                    armorStack,
                    armorItem,
                    armorModel,
                    entityData,
                    texture
            );

            if (layerRendered)
            {
                anyLayerRendered = true;
            }
        }

        if (anyLayerRendered)
        {
            // Mo'Bends handled the armor rendering - now render trim if present
            ArmorTrim trim = armorStack.get(DataComponents.TRIM);
            if (trim != null)
            {
                LOGGER.debug("Rendering trim for slot {} - pattern: {}", slot, trim.pattern());

                try
                {
                    // Sync Mo'Bends transforms to the armor model so vanilla's renderTrim works correctly
                    // Note: This syncs upper body transforms only (not split joints like elbows/knees)
                    mobends$syncTransformsToModel(armorModel, entityData);

                    // Render the trim using vanilla's method via invoker
                    boolean isLeggings = (slot == EquipmentSlot.LEGS);
                    mobends$invokeRenderTrim(materialHolder, poseStack, bufferSource, packedLight, trim, armorModel, isLeggings);
                    LOGGER.debug("Trim render call completed for slot {}", slot);
                }
                catch (Exception e)
                {
                    LOGGER.error("Error rendering trim for slot {}: {}", slot, e.getMessage(), e);
                }
            }

            // Cancel vanilla rendering
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

    /**
     * Sync Mo'Bends animation transforms to the armor model's ModelParts.
     * This allows vanilla's renderTrim to work with the correct poses.
     * Note: This syncs upper body transforms only - split joints (elbows/knees)
     * are not supported for trim rendering as vanilla armor models don't have them.
     */
    @Unique
    private void mobends$syncTransformsToModel(A armorModel, BipedEntityData<?> entityData)
    {
        // Reusable vector for Euler angle extraction
        Vector3f eulerAngles = new Vector3f();

        // Sync head
        if (entityData.head != null && armorModel.head != null)
        {
            mobends$applyQuaternionToModelPart(armorModel.head, entityData.head.rotation.getSmooth(), eulerAngles);
        }

        // Sync hat (follows head)
        if (entityData.head != null && armorModel.hat != null)
        {
            mobends$applyQuaternionToModelPart(armorModel.hat, entityData.head.rotation.getSmooth(), eulerAngles);
        }

        // Sync body
        if (entityData.body != null && armorModel.body != null)
        {
            mobends$applyQuaternionToModelPart(armorModel.body, entityData.body.rotation.getSmooth(), eulerAngles);
        }

        // Sync left arm (upper arm only - no forearm in vanilla model)
        if (entityData.leftArm != null && armorModel.leftArm != null)
        {
            mobends$applyQuaternionToModelPart(armorModel.leftArm, entityData.leftArm.rotation.getSmooth(), eulerAngles);
        }

        // Sync right arm (upper arm only - no forearm in vanilla model)
        if (entityData.rightArm != null && armorModel.rightArm != null)
        {
            mobends$applyQuaternionToModelPart(armorModel.rightArm, entityData.rightArm.rotation.getSmooth(), eulerAngles);
        }

        // Sync left leg (upper leg only - no lower leg in vanilla model)
        if (entityData.leftLeg != null && armorModel.leftLeg != null)
        {
            mobends$applyQuaternionToModelPart(armorModel.leftLeg, entityData.leftLeg.rotation.getSmooth(), eulerAngles);
        }

        // Sync right leg (upper leg only - no lower leg in vanilla model)
        if (entityData.rightLeg != null && armorModel.rightLeg != null)
        {
            mobends$applyQuaternionToModelPart(armorModel.rightLeg, entityData.rightLeg.rotation.getSmooth(), eulerAngles);
        }
    }

    /**
     * Convert a Mo'Bends quaternion to Euler angles and apply to a ModelPart.
     */
    @Unique
    private void mobends$applyQuaternionToModelPart(net.minecraft.client.model.geom.ModelPart part, Quaternion quat, Vector3f eulerOut)
    {
        // Convert Mo'Bends Quaternion to JOML Quaternionf
        Quaternionf jomlQuat = new Quaternionf(quat.x, quat.y, quat.z, quat.w);

        // Extract Euler angles (XYZ order, in radians)
        jomlQuat.getEulerAnglesXYZ(eulerOut);

        // Apply to ModelPart (Minecraft uses XYZ rotation order)
        part.xRot = eulerOut.x;
        part.yRot = eulerOut.y;
        part.zRot = eulerOut.z;
    }
}
