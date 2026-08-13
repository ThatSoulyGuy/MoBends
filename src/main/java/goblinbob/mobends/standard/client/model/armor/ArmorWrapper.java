package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.PlayerData;
import goblinbob.mobends.standard.previewer.PlayerPreviewer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class ArmorWrapper
{
    protected HumanoidModel<?> original;

    protected boolean mutated = true;

    protected boolean applied = false;

    protected List<IPartWrapper> partWrappers = new ArrayList<>();

    protected HumanoidPartWrapper bodyParts;
    protected HumanoidPartWrapper headParts;
    protected HumanoidPartWrapper headwearParts;
    protected HumanoidLimbWrapper leftArmParts;
    protected HumanoidLimbWrapper rightArmParts;
    protected HumanoidLimbWrapper leftLegParts;
    protected HumanoidLimbWrapper rightLegParts;

    protected ModelPartTransform bodyTransform;

    protected float inflation;

    private ArmorWrapper(HumanoidModel<?> original, float inflation)
    {
        this.original = original;
        this.inflation = inflation;
        this.bodyTransform = new ModelPartTransform();

        createPartWrappers(original, inflation);
    }

    protected void createPartWrappers(HumanoidModel<?> model, float inflation)
    {
        this.bodyParts = new HumanoidPartWrapper(
                model, model.body, null,
                data -> data.body,
                HumanoidPartWrapper.PartType.BODY,
                inflation
        );
        bodyParts.offsetInner(0, -12.0F, 0);
        partWrappers.add(bodyParts);

        this.headParts = new HumanoidPartWrapper(
                model, model.head, null,
                data -> data.head,
                HumanoidPartWrapper.PartType.HEAD,
                inflation
        );
        headParts.setParent(bodyTransform);
        partWrappers.add(headParts);

        this.headwearParts = new HumanoidPartWrapper(
                model, model.hat, null,
                data -> data.head,
                HumanoidPartWrapper.PartType.HEADWEAR,
                inflation
        );
        headwearParts.setParent(bodyTransform);
        partWrappers.add(headwearParts);

        this.leftArmParts = new HumanoidLimbWrapper(
                model, model.leftArm, null,
                data -> data.leftArm,
                data -> data.leftForeArm,
                4.0F,
                inflation + 0.001F
        );
        leftArmParts.offsetLower(0, -4.0F, -2.0F);
        leftArmParts.setParent(bodyTransform);
        partWrappers.add(leftArmParts);

        this.rightArmParts = new HumanoidLimbWrapper(
                model, model.rightArm, null,
                data -> data.rightArm,
                data -> data.rightForeArm,
                4.0F,
                inflation + 0.001F
        );
        rightArmParts.offsetLower(0, -4.0F, -2.0F);
        rightArmParts.setParent(bodyTransform);
        partWrappers.add(rightArmParts);

        this.leftLegParts = new HumanoidLimbWrapper(
                model, model.leftLeg, null,
                data -> data.leftLeg,
                data -> data.leftForeLeg,
                6.0F,
                inflation
        );
        leftLegParts.offsetLower(0, -6.0F, 2.0F);
        partWrappers.add(leftLegParts);

        this.rightLegParts = new HumanoidLimbWrapper(
                model, model.rightLeg, null,
                data -> data.rightLeg,
                data -> data.rightForeLeg,
                6.0F,
                inflation
        );
        rightLegParts.offsetLower(0, -6.0F, 2.0F);
        partWrappers.add(rightLegParts);
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer,
                       int packedLight, int packedOverlay,
                       LivingEntity entity, EquipmentSlot slot,
                       float red, float green, float blue, float alpha)
    {
        if (!this.mutated)
        {
            throw new MalformedArmorModelException("Operating on a demutated armor wrapper.");
        }

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

        bodyTransform.syncUp(dataBiped.body);

        for (IPartWrapper wrapper : partWrappers)
        {
            wrapper.syncUp(dataBiped);
        }

        renderSlot(poseStack, vertexConsumer, packedLight, packedOverlay, slot, red, green, blue, alpha);
    }

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
                if (original.leftLeg.visible)
                    leftLegParts.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                if (original.rightLeg.visible)
                    rightLegParts.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                break;

            default:
                break;
        }
    }

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

        this.bodyTransform.syncUp(dataBiped.body);

        for (IPartWrapper wrapper : this.partWrappers)
        {
            wrapper.syncUp(dataBiped);
        }

        this.apply();
    }

    public void finishRendering()
    {
        this.deapply();
    }

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

    public static ArmorWrapper createFor(HumanoidModel<?> src, float inflation)
    {
        return new ArmorWrapper(src, inflation);
    }

    public static ArmorWrapper createFor(HumanoidModel<?> src)
    {
        return createFor(src, 1.0F);
    }
}
