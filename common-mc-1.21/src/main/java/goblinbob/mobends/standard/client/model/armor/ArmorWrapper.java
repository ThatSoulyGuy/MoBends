package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.PlayerData;
import goblinbob.mobends.standard.previewer.PlayerPreviewer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for armor models that provides animated rendering.
 * Updated for Minecraft 1.20.1 - renders custom geometry with Mo' Bends animation.
 *
 * Instead of replacing vanilla model parts, this wrapper creates its own geometry
 * that can be rendered with proper transforms for bendable limbs.
 *
 * @deprecated This class is part of the legacy armor rendering system.
 *             Use {@link goblinbob.mobends.standard.client.model.armor.ArmorRenderingFacade}
 *             and the three-tier rendering system instead (Tier 1, 2, 3 renderers).
 *             This class is kept for backward compatibility and will be removed in a future version.
 */
@Deprecated
public class ArmorWrapper
{
    protected HumanoidModel<?> original;

    /**
     * Keeps track of whether the model is mutated or not.
     */
    protected boolean mutated = true;

    /**
     * True if this armor model should be shown in the mutated form.
     */
    protected boolean applied = false;

    /**
     * All part wrappers for syncing and rendering.
     */
    protected List<IPartWrapper> partWrappers = new ArrayList<>();

    /**
     * Individual part wrappers for direct access.
     */
    protected HumanoidPartWrapper bodyParts;
    protected HumanoidPartWrapper headParts;
    protected HumanoidPartWrapper headwearParts;
    protected HumanoidLimbWrapper leftArmParts;
    protected HumanoidLimbWrapper rightArmParts;
    protected HumanoidLimbWrapper leftLegParts;
    protected HumanoidLimbWrapper rightLegParts;

    /**
     * Body transform for parenting arm/head parts.
     */
    protected ModelPartTransform bodyTransform;

    /**
     * The inflation value used for this armor (0.5F for inner layer, 1.0F for outer layer).
     */
    protected float inflation;

    private ArmorWrapper(HumanoidModel<?> original, float inflation)
    {
        this.original = original;
        this.inflation = inflation;
        this.bodyTransform = new ModelPartTransform();

        // Create all part wrappers
        createPartWrappers(original, inflation);
    }

    /**
     * Create all the part wrappers for this armor model.
     */
    protected void createPartWrappers(HumanoidModel<?> model, float inflation)
    {
        // Body - root of upper body hierarchy
        this.bodyParts = new HumanoidPartWrapper(
                model, model.body, null,
                data -> data.body,
                HumanoidPartWrapper.PartType.BODY,
                inflation
        );
        bodyParts.offsetInner(0, -12.0F, 0);
        partWrappers.add(bodyParts);

        // Head - child of body
        this.headParts = new HumanoidPartWrapper(
                model, model.head, null,
                data -> data.head,
                HumanoidPartWrapper.PartType.HEAD,
                inflation
        );
        headParts.setParent(bodyTransform);
        partWrappers.add(headParts);

        // Headwear - child of body (follows head animation)
        this.headwearParts = new HumanoidPartWrapper(
                model, model.hat, null,
                data -> data.head,
                HumanoidPartWrapper.PartType.HEADWEAR,
                inflation
        );
        headwearParts.setParent(bodyTransform);
        partWrappers.add(headwearParts);

        // Left arm - child of body
        this.leftArmParts = new HumanoidLimbWrapper(
                model, model.leftArm, null,
                data -> data.leftArm,
                data -> data.leftForeArm,
                4.0F,  // Cut plane at elbow
                inflation + 0.001F
        );
        leftArmParts.offsetLower(0, -4.0F, -2.0F);
        leftArmParts.setParent(bodyTransform);
        partWrappers.add(leftArmParts);

        // Right arm - child of body
        this.rightArmParts = new HumanoidLimbWrapper(
                model, model.rightArm, null,
                data -> data.rightArm,
                data -> data.rightForeArm,
                4.0F,  // Cut plane at elbow
                inflation + 0.001F
        );
        rightArmParts.offsetLower(0, -4.0F, -2.0F);
        rightArmParts.setParent(bodyTransform);
        partWrappers.add(rightArmParts);

        // Left leg - NOT child of body (legs are independent)
        // Note: leg X offset comes from animation data, not innerOffset
        this.leftLegParts = new HumanoidLimbWrapper(
                model, model.leftLeg, null,
                data -> data.leftLeg,
                data -> data.leftForeLeg,
                6.0F,  // Cut plane at knee
                inflation
        );
        leftLegParts.offsetLower(0, -6.0F, 2.0F);  // No X offset - comes from animation
        // No innerOffset for legs - position comes from animation data
        partWrappers.add(leftLegParts);

        // Right leg - NOT child of body
        this.rightLegParts = new HumanoidLimbWrapper(
                model, model.rightLeg, null,
                data -> data.rightLeg,
                data -> data.rightForeLeg,
                6.0F,  // Cut plane at knee
                inflation
        );
        rightLegParts.offsetLower(0, -6.0F, 2.0F);  // No X offset - comes from animation
        // No innerOffset for legs - position comes from animation data
        partWrappers.add(rightLegParts);
    }

    /**
     * Render the armor for a specific equipment slot.
     */
    public void render(PoseStack poseStack, VertexConsumer vertexConsumer,
                       int packedLight, int packedOverlay,
                       LivingEntity entity, EquipmentSlot slot,
                       float red, float green, float blue, float alpha)
    {
        if (!this.mutated)
        {
            throw new MalformedArmorModelException("Operating on a demutated armor wrapper.");
        }

        // Get animation data
        EntityBender<LivingEntity> entityBender = EntityBenderRegistry.instance.getForEntity(entity);
        if (entityBender == null)
            return;

        EntityData<?> entityData = EntityDatabase.instance.get(entity);
        if (!(entityData instanceof BipedEntityData))
            return;

        if (entityData instanceof PlayerData && PlayerPreviewer.isPreviewInProgress())
        {
            entityData = PlayerPreviewer.getPreviewData();
        }

        final BipedEntityData<?> dataBiped = (BipedEntityData<?>) entityData;

        // Sync body transform
        bodyTransform.syncUp(dataBiped.body);

        // Sync all parts with animation data
        for (IPartWrapper wrapper : partWrappers)
        {
            wrapper.syncUp(dataBiped);
        }

        // Render the appropriate parts for this slot
        renderSlot(poseStack, vertexConsumer, packedLight, packedOverlay, slot, red, green, blue, alpha);
    }

    /**
     * Render parts for a specific equipment slot.
     */
    protected void renderSlot(PoseStack poseStack, VertexConsumer vertexConsumer,
                              int packedLight, int packedOverlay,
                              EquipmentSlot slot,
                              float red, float green, float blue, float alpha)
    {
        switch (slot)
        {
            case HEAD:
                if (original.head.visible)
                    headParts.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                if (original.hat.visible)
                    headwearParts.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                break;

            case CHEST:
                if (original.body.visible)
                    bodyParts.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                if (original.leftArm.visible)
                    leftArmParts.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                if (original.rightArm.visible)
                    rightArmParts.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                break;

            case LEGS:
                if (original.body.visible)
                    bodyParts.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                if (original.leftLeg.visible)
                    leftLegParts.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                if (original.rightLeg.visible)
                    rightLegParts.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                break;

            case FEET:
                // For boots, render the full leg geometry - the texture determines what's visible
                // Boots use layer_1 texture which has boot pattern on lower leg
                if (original.leftLeg.visible)
                    leftLegParts.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                if (original.rightLeg.visible)
                    rightLegParts.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                break;

            default:
                break;
        }
    }

    /**
     * Prepare the wrapper for rendering (syncs animation data).
     */
    public void prepareForRendering(LivingEntity entity, float partialTicks)
    {
        EntityBender<LivingEntity> entityBender = EntityBenderRegistry.instance.getForEntity(entity);
        if (entityBender == null)
            return;

        EntityData<?> entityData = EntityDatabase.instance.get(entity);
        if (!(entityData instanceof BipedEntityData))
            return;

        if (entityData instanceof PlayerData && PlayerPreviewer.isPreviewInProgress())
        {
            entityData = PlayerPreviewer.getPreviewData();
        }

        final BipedEntityData<?> dataBiped = (BipedEntityData<?>) entityData;

        // Sync the body transform
        this.bodyTransform.syncUp(dataBiped.body);

        // Sync all part wrappers
        for (IPartWrapper wrapper : this.partWrappers)
        {
            wrapper.syncUp(dataBiped);
        }

        this.apply();
    }

    /**
     * Called after rendering to restore the model to its original state.
     */
    public void finishRendering()
    {
        this.deapply();
    }

    /**
     * Brings the original model back to its vanilla state permanently.
     */
    public void demutate()
    {
        this.deapply();
        this.partWrappers.clear();
        this.mutated = false;
    }

    public void apply()
    {
        if (this.applied)
            return;

        for (IPartWrapper wrapper : partWrappers)
        {
            wrapper.apply(this);
        }

        this.applied = true;
    }

    public void deapply()
    {
        if (!this.applied)
            return;

        for (IPartWrapper wrapper : partWrappers)
        {
            wrapper.deapply(this);
        }

        this.applied = false;
    }

    public HumanoidModel<?> getOriginal()
    {
        return this.original;
    }

    public boolean isMutated()
    {
        return this.mutated;
    }

    public boolean isApplied()
    {
        return this.applied;
    }

    public ModelPartTransform getBodyTransform()
    {
        return bodyTransform;
    }

    /**
     * Create an armor wrapper for the given model.
     * @param src The source HumanoidModel
     * @param inflation The inflation amount (0.5F for leggings, 1.0F for armor)
     */
    public static ArmorWrapper createFor(HumanoidModel<?> src, float inflation)
    {
        return new ArmorWrapper(src, inflation);
    }

    /**
     * Create an armor wrapper with default inflation.
     */
    public static ArmorWrapper createFor(HumanoidModel<?> src)
    {
        return createFor(src, 1.0F);
    }
}
