package goblinbob.mobends.mixin.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.compat.ModCompatManager;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderingFacade;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
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

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>>
{
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends-ArmorTrim");

    @Unique
    private static ArmorRenderingFacade mobends$armorFacade;

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
        if (!mobends$shouldUseMoBendsRendering(entity))
        {
            return;
        }

        BipedEntityData<?> entityData = mobends$getEntityData(entity);
        if (entityData == null)
        {
            return;
        }

        ItemStack armorStack = entity.getItemBySlot(slot);
        if (armorStack.isEmpty())
        {
            return;
        }

        if (!(armorStack.getItem() instanceof ArmorItem armorItem))
        {
            return;
        }

        boolean isInnerModel = (slot == EquipmentSlot.LEGS);
        Holder<ArmorMaterial> materialHolder = armorItem.getMaterial();
        ArmorMaterial material = materialHolder.value();

        ArmorRenderingFacade facade = mobends$getArmorFacade();
        if (facade == null)
        {
            return;
        }

        boolean anyLayerRendered = false;
        for (ArmorMaterial.Layer layer : material.layers())
        {
            ResourceLocation texture = armorItem.getArmorTexture(armorStack, entity, slot, layer, isInnerModel);
            if (texture == null)
            {
                continue;
            }

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
            ArmorTrim trim = armorStack.get(DataComponents.TRIM);
            if (trim != null)
            {
                LOGGER.debug("Rendering trim for slot {} - pattern: {}", slot, trim.pattern());

                try
                {
                    mobends$syncTransformsToModel(armorModel, entityData);

                    boolean isLeggings = (slot == EquipmentSlot.LEGS);
                    mobends$invokeRenderTrim(materialHolder, poseStack, bufferSource, packedLight, trim, armorModel, isLeggings);
                    LOGGER.debug("Trim render call completed for slot {}", slot);
                }
                catch (Exception e)
                {
                    LOGGER.error("Error rendering trim for slot {}: {}", slot, e.getMessage(), e);
                }
            }

            ci.cancel();
        }
    }

    @Unique
    private boolean mobends$shouldUseMoBendsRendering(T entity)
    {
        return BenderHelper.isEntityAnimated(entity)
                && !ModCompatManager.shouldDeferAnimation(entity)
                && EntityDatabase.instance.get(entity) != null;
    }

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

    @Unique
    private ArmorRenderingFacade mobends$getArmorFacade()
    {
        if (mobends$armorFacade == null)
        {
            mobends$armorFacade = new ArmorRenderingFacade();
        }
        return mobends$armorFacade;
    }

    @Unique
    private void mobends$syncTransformsToModel(A armorModel, BipedEntityData<?> entityData)
    {
        Vector3f eulerAngles = new Vector3f();

        if (entityData.head != null && armorModel.head != null)
        {
            mobends$applyQuaternionToModelPart(armorModel.head, entityData.head.rotation.getSmooth(), eulerAngles);
        }

        if (entityData.head != null && armorModel.hat != null)
        {
            mobends$applyQuaternionToModelPart(armorModel.hat, entityData.head.rotation.getSmooth(), eulerAngles);
        }

        if (entityData.body != null && armorModel.body != null)
        {
            mobends$applyQuaternionToModelPart(armorModel.body, entityData.body.rotation.getSmooth(), eulerAngles);
        }

        if (entityData.leftArm != null && armorModel.leftArm != null)
        {
            mobends$applyQuaternionToModelPart(armorModel.leftArm, entityData.leftArm.rotation.getSmooth(), eulerAngles);
        }

        if (entityData.rightArm != null && armorModel.rightArm != null)
        {
            mobends$applyQuaternionToModelPart(armorModel.rightArm, entityData.rightArm.rotation.getSmooth(), eulerAngles);
        }

        if (entityData.leftLeg != null && armorModel.leftLeg != null)
        {
            mobends$applyQuaternionToModelPart(armorModel.leftLeg, entityData.leftLeg.rotation.getSmooth(), eulerAngles);
        }

        if (entityData.rightLeg != null && armorModel.rightLeg != null)
        {
            mobends$applyQuaternionToModelPart(armorModel.rightLeg, entityData.rightLeg.rotation.getSmooth(), eulerAngles);
        }
    }

    @Unique
    private void mobends$applyQuaternionToModelPart(net.minecraft.client.model.geom.ModelPart part, Quaternion quat, Vector3f eulerOut)
    {
        Quaternionf jomlQuat = new Quaternionf(quat.x, quat.y, quat.z, quat.w);

        jomlQuat.getEulerAnglesXYZ(eulerOut);

        part.xRot = eulerOut.x;
        part.yRot = eulerOut.y;
        part.zRot = eulerOut.z;
    }
}
