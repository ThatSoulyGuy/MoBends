package goblinbob.mobends.standard.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.BendsCube;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomBipedArmor;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomHeldItem;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

public abstract class BipedMutator<D extends BipedEntityData<E>,
                                   E extends LivingEntity,
                                   M extends HumanoidModel<E>>
                                  extends Mutator<D, E, M>
{
    // Custom bendable parts
    protected BendsModelPart body;
    protected BendsModelPart head;
    protected BendsModelPart headwear;
    protected BendsModelPart leftArm;
    protected BendsModelPart rightArm;
    protected BendsModelPart leftForeArm;
    protected BendsModelPart rightForeArm;
    protected BendsModelPart leftLeg;
    protected BendsModelPart rightLeg;
    protected BendsModelPart leftForeLeg;
    protected BendsModelPart rightForeLeg;

    // Store vanilla model parts for demutation
    protected ModelPart vanillaBody;
    protected ModelPart vanillaHead;
    protected ModelPart vanillaHat;
    protected ModelPart vanillaLeftArm;
    protected ModelPart vanillaRightArm;
    protected ModelPart vanillaLeftLeg;
    protected ModelPart vanillaRightLeg;

    protected LayerCustomBipedArmor<E, M> layerArmor;
    protected HumanoidArmorLayer<E, M, ?> layerArmorVanilla;
    protected LayerCustomHeldItem<E, M> layerHeldItem;
    protected ItemInHandLayer<E, M> layerHeldItemVanilla;
    protected CustomHeadLayer<E, M> layerCustomHead;
    protected CustomHeadLayer<E, M> layerCustomHeadVanilla;

    public BipedMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }

    /**
     * Used to store the model parameter as the
     * vanilla model, so then the mutation can be
     * reversed.
     */
    @Override
    public void storeVanillaModel(M model)
    {
        this.vanillaBody = model.body;
        this.vanillaHead = model.head;
        this.vanillaHat = model.hat;
        this.vanillaLeftArm = model.leftArm;
        this.vanillaRightArm = model.rightArm;
        this.vanillaLeftLeg = model.leftLeg;
        this.vanillaRightLeg = model.rightLeg;
    }

    /**
     * Sets the model parameter back to it's vanilla
     * state. Used to demutate the model.
     */
    @Override
    public void applyVanillaModel(M model)
    {
        // In 1.20.1, model parts are final and cannot be directly replaced
        // The demutation will need to handle this differently
        // For now, we just track that we need to restore vanilla rendering
    }

    /**
     * Swaps out the vanilla layers for their custom counterparts,
     * and if it's a vanilla model, it stores the vanilla layers
     * for future mutation reversal.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void swapLayer(LivingEntityRenderer<E, M> renderer, int index, boolean isModelVanilla)
    {
        RenderLayer<E, M> layer = layerRenderers.get(index);
        if (layer instanceof HumanoidArmorLayer)
        {
            HumanoidArmorLayer<E, M, ?> vanillaArmor = (HumanoidArmorLayer<E, M, ?>) layer;
            if (isModelVanilla)
                this.layerArmorVanilla = vanillaArmor;

            // Create our custom armor layer
            this.layerArmor = new LayerCustomBipedArmor<>(renderer, this);
            this.layerArmor.setVanillaArmorLayer(vanillaArmor);

            // Try to get armor models from the vanilla layer using reflection or standard models
            try
            {
                // Use standard humanoid armor models
                net.minecraft.client.model.geom.ModelLayerLocation innerLocation =
                    net.minecraft.client.model.geom.ModelLayers.PLAYER_INNER_ARMOR;
                net.minecraft.client.model.geom.ModelLayerLocation outerLocation =
                    net.minecraft.client.model.geom.ModelLayers.PLAYER_OUTER_ARMOR;

                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                HumanoidModel<?> innerModel = new HumanoidModel<>(mc.getEntityModels().bakeLayer(innerLocation));
                HumanoidModel<?> outerModel = new HumanoidModel<>(mc.getEntityModels().bakeLayer(outerLocation));

                this.layerArmor.setArmorModels(innerModel, outerModel);
            }
            catch (Exception e)
            {
                // Continue silently - vanilla fallback will be used
            }

            layerRenderers.set(index, this.layerArmor);
        }
        else if (layer instanceof ItemInHandLayer)
        {
            this.layerHeldItem = new LayerCustomHeldItem<>(renderer, this);
            if (isModelVanilla)
                this.layerHeldItemVanilla = (ItemInHandLayer<E, M>) layer;
            layerRenderers.set(index, this.layerHeldItem);
        }
        else if (layer instanceof CustomHeadLayer)
        {
            // For custom head layer, we need special handling
            if (isModelVanilla)
                this.layerCustomHeadVanilla = (CustomHeadLayer<E, M>) layer;
            // Don't swap - let vanilla handle it for now
        }
    }

    /**
     * Swaps the custom layers back with the vanilla layers.
     * Used to demutate the model.
     */
    @Override
    public void deswapLayer(LivingEntityRenderer<E, M> renderer, int index)
    {
        RenderLayer<E, M> layer = layerRenderers.get(index);
        if (layer instanceof LayerCustomBipedArmor && this.layerArmorVanilla != null)
        {
            layerRenderers.set(index, this.layerArmorVanilla);
        }
        else if (layer instanceof LayerCustomHeldItem && this.layerHeldItemVanilla != null)
        {
            layerRenderers.set(index, this.layerHeldItemVanilla);
        }
        else if (layer == this.layerCustomHead && this.layerCustomHeadVanilla != null)
        {
            layerRenderers.set(index, this.layerCustomHeadVanilla);
        }
    }

    /**
     * Creates all the custom parts you need! It creates custom
     * BendsModelPart instances for bendable limbs.
     */
    @Override
    public boolean createParts(M original, float scaleFactor)
    {
        // Create custom bendable parts
        // Body - the root of the upper body hierarchy
        body = new BendsModelPart(16, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12.0F, 0.0F);
        body.addCube(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor);

        // Head - child of body
        head = new BendsModelPart(0, 0)
                .setTextureSize(64, 64)
                .setPosition(0.0F, -12.0F, 0.0F);
        head.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor);
        body.addChild(head);

        // Headwear - child of head
        headwear = new BendsModelPart(32, 0)
                .setTextureSize(64, 64);
        headwear.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor + 0.5F);
        head.addChild(headwear);

        // Arms
        int armWidth = 4;
        float armY = -10F;

        // Left arm - child of body
        leftArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 64)
                .setPosition(5.0F, armY, 0.0F)
                .setMirror(true);
        leftArm.addCube(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor);
        body.addChild(leftArm);

        // Right arm - child of body
        rightArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 64)
                .setPosition(-5.0F, armY, 0.0F);
        rightArm.addCube(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor);
        body.addChild(rightArm);

        // Left forearm - child of left arm
        leftForeArm = new BendsModelPart(40, 22)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 4.0F, 2.0F)
                .setMirror(true);
        leftForeArm.addCube(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor);
        leftArm.addChild(leftForeArm);

        // Right forearm - child of right arm
        rightForeArm = new BendsModelPart(40, 22)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 4.0F, 2.0F);
        rightForeArm.addCube(-armWidth + 1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor);
        rightArm.addChild(rightForeArm);

        // Legs - independent roots (not children of body)
        rightLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12F, 0F);
        rightLeg.addCube(-3.9F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);

        leftLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12.0F, 0.0F)
                .setMirror(true);
        leftLeg.addCube(-0.1F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);

        // Left foreleg - child of left leg
        leftForeLeg = new BendsModelPart(0, 22)
                .setTextureSize(64, 64)
                .setPosition(0, 6.0F, -2.0F)
                .setMirror(true);
        leftForeLeg.addCube(-0.1F, 0.0F, 0.0F, 4, 6, 4, scaleFactor);
        leftLeg.addChild(leftForeLeg);

        // Right foreleg - child of right leg
        rightForeLeg = new BendsModelPart(0, 22)
                .setTextureSize(64, 64)
                .setPosition(0, 6.0F, -2.0F);
        rightForeLeg.addCube(-3.9F, 0.0F, 0.0F, 4, 6, 4, scaleFactor);
        rightLeg.addChild(rightForeLeg);

        return true;
    }

    @Override
    public void syncUpWithData(D data)
    {
        head.syncUp(data.head);
        body.syncUp(data.body);
        leftArm.syncUp(data.leftArm);
        rightArm.syncUp(data.rightArm);
        leftLeg.syncUp(data.leftLeg);
        rightLeg.syncUp(data.rightLeg);
        leftForeArm.syncUp(data.leftForeArm);
        rightForeArm.syncUp(data.rightForeArm);
        leftForeLeg.syncUp(data.leftForeLeg);
        rightForeLeg.syncUp(data.rightForeLeg);
    }

    /**
     * True, if this renderer wasn't mutated before.
     */
    @Override
    public boolean isModelVanilla(M model)
    {
        // Check if we've already created custom parts
        return this.body == null;
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof HumanoidModel);
    }

    @Override
    public boolean shouldRenderCustom()
    {
        return this.body != null;
    }

    @Override
    public void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                              int packedLight, int packedOverlay, int color)
    {
        // Render body and attached parts
        if (body != null)
        {
            body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }

        // Render legs (not attached to body)
        if (leftLeg != null)
        {
            leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
        if (rightLeg != null)
        {
            rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
    }

    // Getters for layers to access parts
    public BendsModelPart getBody() { return body; }
    public BendsModelPart getHead() { return head; }
    public BendsModelPart getLeftArm() { return leftArm; }
    public BendsModelPart getRightArm() { return rightArm; }
    public BendsModelPart getLeftForeArm() { return leftForeArm; }
    public BendsModelPart getRightForeArm() { return rightForeArm; }
    public BendsModelPart getLeftLeg() { return leftLeg; }
    public BendsModelPart getRightLeg() { return rightLeg; }
    public BendsModelPart getLeftForeLeg() { return leftForeLeg; }
    public BendsModelPart getRightForeLeg() { return rightForeLeg; }

    // Store the vanilla default positions so we can restore + offset
    private float[] vanillaBodyPos, vanillaHeadPos, vanillaLeftArmPos, vanillaRightArmPos,
                    vanillaLeftLegPos, vanillaRightLegPos;
    private boolean vanillaPositionsStored = false;

    /**
     * Sync animated poses from BendsModelParts to vanilla HumanoidModel ModelParts.
     * This allows vanilla layers (armor, held items) to use our animated poses.
     */
    public void syncPosesToVanillaModel(HumanoidModel<?> model)
    {
        if (model == null) return;

        // Store vanilla default positions on first call
        if (!vanillaPositionsStored)
        {
            vanillaBodyPos = new float[]{model.body.x, model.body.y, model.body.z};
            vanillaHeadPos = new float[]{model.head.x, model.head.y, model.head.z};
            vanillaLeftArmPos = new float[]{model.leftArm.x, model.leftArm.y, model.leftArm.z};
            vanillaRightArmPos = new float[]{model.rightArm.x, model.rightArm.y, model.rightArm.z};
            vanillaLeftLegPos = new float[]{model.leftLeg.x, model.leftLeg.y, model.leftLeg.z};
            vanillaRightLegPos = new float[]{model.rightLeg.x, model.rightLeg.y, model.rightLeg.z};
            vanillaPositionsStored = true;
        }

        syncPartToModelPart(body, model.body, vanillaBodyPos);
        syncPartToModelPart(head, model.head, vanillaHeadPos);
        syncPartToModelPart(leftArm, model.leftArm, vanillaLeftArmPos);
        syncPartToModelPart(rightArm, model.rightArm, vanillaRightArmPos);
        syncPartToModelPart(leftLeg, model.leftLeg, vanillaLeftLegPos);
        syncPartToModelPart(rightLeg, model.rightLeg, vanillaRightLegPos);

        // Sync hat visibility with head
        if (model.hat != null && head != null)
        {
            model.hat.visible = head.isShowing();
            // Sync hat position/rotation with head
            model.hat.x = model.head.x;
            model.hat.y = model.head.y;
            model.hat.z = model.head.z;
            model.hat.xRot = model.head.xRot;
            model.hat.yRot = model.head.yRot;
            model.hat.zRot = model.head.zRot;
        }
    }

    /**
     * Sync a single BendsModelPart's transform to a vanilla ModelPart.
     * Syncs rotation, visibility, and animation position offsets.
     */
    private void syncPartToModelPart(BendsModelPart bendsPart, ModelPart modelPart, float[] vanillaPos)
    {
        if (bendsPart == null || modelPart == null) return;

        // Sync animation offset to vanilla model position
        // Add the animation offset to the stored vanilla base position
        if (vanillaPos != null)
        {
            modelPart.x = vanillaPos[0] + bendsPart.offset.x;
            modelPart.y = vanillaPos[1] + bendsPart.offset.y;
            modelPart.z = vanillaPos[2] + bendsPart.offset.z;
        }

        // Convert quaternion rotation to Euler angles (XYZ order)
        Quaternion q = bendsPart.rotation.getSmooth();
        float[] euler = quaternionToEulerXYZ(q);
        modelPart.xRot = euler[0];
        modelPart.yRot = euler[1];
        modelPart.zRot = euler[2];

        // Sync visibility
        modelPart.visible = bendsPart.isShowing();
    }

    /**
     * Convert quaternion to Euler angles in XYZ rotation order (radians).
     * Returns [xRot, yRot, zRot].
     */
    private static float[] quaternionToEulerXYZ(Quaternion q)
    {
        float[] euler = new float[3];

        // X rotation (pitch)
        float sinX = 2.0f * (q.w * q.x + q.y * q.z);
        float cosX = 1.0f - 2.0f * (q.x * q.x + q.y * q.y);
        euler[0] = (float) Math.atan2(sinX, cosX);

        // Y rotation (yaw)
        float sinY = 2.0f * (q.w * q.y - q.z * q.x);
        if (Math.abs(sinY) >= 1.0f)
        {
            euler[1] = (float) Math.copySign(Math.PI / 2, sinY);
        }
        else
        {
            euler[1] = (float) Math.asin(sinY);
        }

        // Z rotation (roll)
        float sinZ = 2.0f * (q.w * q.z + q.x * q.y);
        float cosZ = 1.0f - 2.0f * (q.y * q.y + q.z * q.z);
        euler[2] = (float) Math.atan2(sinZ, cosZ);

        return euler;
    }

}
