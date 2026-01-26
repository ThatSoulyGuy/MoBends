package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import goblinbob.mobends.core.client.Mesh;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.core.util.MeshBuilder;
import goblinbob.mobends.standard.data.WolfData;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

/**
 * Layer for rendering wolf mouth and tongue with Mo' Bends animations.
 * Updated for 1.20.1 to use PoseStack and RenderSystem instead of GlStateManager.
 */
public class LayerWolfMisc extends RenderLayer<Wolf, WolfModel<Wolf>>
{

    private static final ResourceLocation WOLF_MISC_TEXTURE = ModStatics.getResource(
            "textures/entity/wolf_misc.png");
    private static final int textureWidth = 8;
    private static final int textureHeight = 8;

    private Mesh mouthBottom;
    private Mesh mouthTop;
    private Mesh mouthInside;
    private Mesh tongue;

    public LayerWolfMisc(RenderLayerParent<Wolf, WolfModel<Wolf>> renderer)
    {
        super(renderer);

        mouthBottom = new Mesh(6);
        mouthBottom.beginDrawing();
        MeshBuilder.texturedXZPlane(mouthBottom, -1.5F, 0F, -4F, 3, 4, true, Color.WHITE,
                new int[] { 0, 0, 3, 4 }, textureWidth, textureHeight);
        mouthBottom.finishDrawing();

        mouthTop = new Mesh(6);
        mouthTop.beginDrawing();
        MeshBuilder.texturedXZPlane(mouthTop, -1.5F, 1F, -4F, 3, 4, false, Color.WHITE,
                new int[] { 0, 0, 3, 4 }, textureWidth, textureHeight);
        mouthTop.finishDrawing();

        mouthInside = new Mesh(6);
        mouthInside.beginDrawing();
        MeshBuilder.texturedXZPlane(mouthInside, -1.5F, -4.1F, -3.0F, 3, 1, true, Color.WHITE,
                new int[] { 0, 0, 3, 4 }, textureWidth, textureHeight);
        mouthInside.finishDrawing();

        tongue = new Mesh(6);
        tongue.beginDrawing();
        MeshBuilder.texturedXZPlane(tongue, -1.5F, 0F, -4F, 3, 6, true, Color.WHITE,
                new int[] { 3, 0, 6, 6 }, textureWidth, textureHeight);
        tongue.finishDrawing();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       Wolf wolf, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        final EntityData<?> entityData = EntityDatabase.instance.get(wolf);
        if (entityData instanceof WolfData)
        {
            final WolfData data = (WolfData) entityData;
            final float scale = 0.0625F;

            boolean isChild = wolf.isBaby();

            poseStack.pushPose();
            if (isChild)
            {
                poseStack.translate(0.0F, 10.0F * scale, 0.0F * scale);
                data.body.applyLocalTransform(poseStack, scale * 0.5F);
            }
            else
            {
                data.body.applyLocalTransform(poseStack, scale);
            }
            data.head.applyLocalTransform(poseStack, scale);

            RenderSystem.enableCull();
            // Mouth inside
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(scale, scale, scale);
            mouthInside.render(poseStack, bufferSource, WOLF_MISC_TEXTURE, packedLight);
            poseStack.popPose();

            // Mouth bottom
            poseStack.pushPose();
            data.nose.applyLocalTransform(poseStack, scale);
            poseStack.scale(scale, scale, scale);
            mouthTop.render(poseStack, bufferSource, WOLF_MISC_TEXTURE, packedLight);
            poseStack.popPose();

            // Mouth top
            poseStack.pushPose();
            data.mouth.applyLocalTransform(poseStack, scale);
            poseStack.scale(scale, scale, scale);
            mouthBottom.render(poseStack, bufferSource, WOLF_MISC_TEXTURE, packedLight);
            poseStack.popPose();

            // Tongue
            poseStack.pushPose();
            data.tongue.applyLocalTransform(poseStack, scale);
            poseStack.scale(scale, scale, scale);
            tongue.render(poseStack, bufferSource, WOLF_MISC_TEXTURE, packedLight);
            poseStack.popPose();

            poseStack.popPose();
        }
    }

}
