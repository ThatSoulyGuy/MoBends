package goblinbob.mobends.standard.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.core.util.GlHelper;
import goblinbob.mobends.standard.data.SpiderData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Spider;

public class SpiderMutator extends Mutator<SpiderData, Spider, SpiderModel<Spider>>
{

    public BendsModelPart spiderHead;
    public BendsModelPart spiderNeck;
    public BendsModelPart spiderBody;
    public BendsModelPart[] spiderUpperLimbs;
    public BendsModelPart[] spiderLowerLimbs;

    // Store the current data for use in renderMutated
    // The spider model center is at Y=15 in model units (15/16 = 0.9375 blocks)
    // This is where we want to pivot rotations around
    private static final float SPIDER_MODEL_CENTER_Y = 15.0F / 16.0F;
    private SpiderData currentData;

    public SpiderMutator(IEntityDataFactory<Spider> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public void storeVanillaModel(SpiderModel<Spider> model)
    {
        // In 1.20.1, we use composition pattern - no need to store vanilla parts
    }

    @Override
    public void applyVanillaModel(SpiderModel<Spider> model)
    {
        // In 1.20.1, demutation handled differently
    }

    @Override
    public void swapLayer(LivingEntityRenderer<Spider, SpiderModel<Spider>> renderer, int index, boolean isModelVanilla)
    {
        // No custom layers for spider
    }

    @Override
    public void deswapLayer(LivingEntityRenderer<Spider, SpiderModel<Spider>> renderer, int index)
    {
        // No custom layers for spider
    }

    @Override
    public boolean createParts(SpiderModel<Spider> original, float scaleFactor)
    {
        float legLength = 12F;
        float foreLegLength = 12F;

        // Spider Head
        spiderHead = new BendsModelPart(32, 4)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 15.0F, -3.0F);
        spiderHead.addCube(-4.0F, -4.0F, -8.0F, 8, 8, 8, scaleFactor);

        // Spider Neck
        spiderNeck = new BendsModelPart(0, 0)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 15.0F, 0.0F);
        spiderNeck.addCube(-3.0F, -3.0F, -3.0F, 6, 6, 6, scaleFactor);

        // Spider Body
        spiderBody = new BendsModelPart(0, 12)
                .setTextureSize(64, 32)
                .setPosition(0.0F, 15.0F, 9.0F);
        spiderBody.addCube(-5.0F, -4.0F, -6.0F, 10, 8, 12, scaleFactor);

        // Spider Legs (8 total)
        spiderUpperLimbs = new BendsModelPart[8];
        spiderLowerLimbs = new BendsModelPart[8];

        for (int i = 0; i < 8; ++i)
        {
            boolean odd = i % 2 == 1;
            int z = 2 - (i / 2);

            // Upper limb: position at body, cube extends outward with width = legLength (12)
            spiderUpperLimbs[i] = new BendsModelPart(odd ? 18 : 26, 0)
                    .setTextureSize(64, 32)
                    .setPosition(odd ? 4F : -4F, 15F, z);
            // Original: developBox(...).setWidth(legLength) - width is 12, not 8
            spiderUpperLimbs[i].addCube(odd ? -1F : (-legLength + 1F), -1.0F, -1.0F, (int) legLength, 2, 2, scaleFactor);

            // Lower limb: attached at end of upper limb, cube extends outward with width = foreLegLength (12)
            spiderLowerLimbs[i] = new BendsModelPart(odd ? 26 : 18, 0)
                    .setTextureSize(64, 32)
                    .setPosition(odd ? foreLegLength : -foreLegLength, 0F, 0F);
            // Original: developBox(...).resize(foreLegLength, 1.99F, 1.99F) - width is 12, height/depth ~2
            spiderLowerLimbs[i].addCube(odd ? 0F : -foreLegLength, 0F, -1F, (int) foreLegLength, 2, 2, scaleFactor);
            spiderUpperLimbs[i].addChild(spiderLowerLimbs[i]);
        }

        return true;
    }

    @Override
    public void syncUpWithData(SpiderData data)
    {
        // Store current data for use in renderMutated
        this.currentData = data;

        if (spiderHead != null) spiderHead.syncUp(data.spiderHead);
        if (spiderNeck != null) spiderNeck.syncUp(data.spiderNeck);
        if (spiderBody != null) spiderBody.syncUp(data.spiderBody);

        for (int i = 0; i < 8; ++i)
        {
            if (spiderUpperLimbs[i] != null)
                spiderUpperLimbs[i].syncUp(data.limbs[i].upperPart);
            if (spiderLowerLimbs[i] != null)
                spiderLowerLimbs[i].syncUp(data.limbs[i].lowerPart);
        }
    }

    @Override
    public boolean isModelVanilla(SpiderModel<Spider> model)
    {
        // Check if we've already created custom parts
        return this.spiderHead == null;
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof SpiderModel);
    }

    @Override
    public boolean shouldRenderCustom()
    {
        return this.spiderHead != null;
    }

    @Override
    public void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                              int packedLight, int packedOverlay,
                              float red, float green, float blue, float alpha)
    {
        if (currentData == null)
        {
            renderParts(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            return;
        }

        poseStack.pushPose();

        float scale = 1.0F / 16.0F;
        Spider spider = currentData.getEntity();

        // Check if spider is climbing (has a wall facing direction)
        boolean isClimbing = currentData.getWallFacing() != null;

        // Apply globalOffset
        float gx = currentData.globalOffset.getX();
        float gy = currentData.globalOffset.getY();
        float gz = currentData.globalOffset.getZ();
        if (gx != 0 || gy != 0 || gz != 0)
        {
            poseStack.translate(gx * scale, gy * scale, gz * scale);
        }

        // Apply centerRotation around the spider's center height
        Quaternion centerRot = currentData.centerRotation.getSmooth();
        if (!centerRot.isIdentity())
        {
            poseStack.translate(0, SPIDER_MODEL_CENTER_Y, 0);
            GlHelper.rotate(poseStack, centerRot);
            poseStack.translate(0, -SPIDER_MODEL_CENTER_Y, 0);
        }

        // When climbing, we need to handle rotation specially.
        // The renderer has already applied (180 - bodyYaw) rotation.
        // We need to undo that and apply our climbing rotation instead.
        Quaternion renderRot = currentData.renderRotation.getSmooth();
        if (!renderRot.isIdentity())
        {
            if (isClimbing && spider != null)
            {
                // Get the climbing rotation (direction spider should face)
                float climbingRotation = currentData.getCrawlingRotation();

                // Undo the renderer's body yaw rotation: it applied (180 - bodyYaw)
                // So we rotate by (bodyYaw - 180) to undo it
                float bodyYaw = Mth.lerp(goblinbob.mobends.core.client.event.DataUpdateHandler.partialTicks,
                        spider.yBodyRotO, spider.yBodyRot);

                poseStack.translate(0, SPIDER_MODEL_CENTER_Y, 0);

                // Undo the renderer's rotation
                poseStack.mulPose(Axis.YP.rotationDegrees(bodyYaw - 180.0F));

                // Apply the climbing rotation: face the wall direction
                poseStack.mulPose(Axis.YP.rotationDegrees(climbingRotation));

                // Tilt to face the wall (rotate 90 degrees around X)
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));

                poseStack.translate(0, -SPIDER_MODEL_CENTER_Y, 0);
            }
            else
            {
                // Not climbing - apply normal renderRotation
                poseStack.translate(0, SPIDER_MODEL_CENTER_Y, 0);
                GlHelper.rotate(poseStack, renderRot);
                poseStack.translate(0, -SPIDER_MODEL_CENTER_Y, 0);
            }
        }

        // Apply localOffset (after rotations)
        float lx = currentData.localOffset.getX();
        float ly = currentData.localOffset.getY();
        float lz = currentData.localOffset.getZ();
        if (lx != 0 || ly != 0 || lz != 0)
        {
            poseStack.translate(lx * scale, ly * scale, lz * scale);
        }

        // Render all spider parts
        renderParts(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);

        poseStack.popPose();
    }

    private void renderParts(PoseStack poseStack, VertexConsumer vertexConsumer,
                             int packedLight, int packedOverlay,
                             float red, float green, float blue, float alpha)
    {
        if (spiderHead != null)
        {
            spiderHead.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
        if (spiderNeck != null)
        {
            spiderNeck.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
        if (spiderBody != null)
        {
            spiderBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        for (int i = 0; i < 8; ++i)
        {
            if (spiderUpperLimbs[i] != null)
            {
                spiderUpperLimbs[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            }
        }
    }

}
