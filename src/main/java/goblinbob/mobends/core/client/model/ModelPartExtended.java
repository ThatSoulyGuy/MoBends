package goblinbob.mobends.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Extended model part that includes an optional extension part.
 * Updated for 1.20.1 to use PoseStack instead of GlStateManager.
 */
public class ModelPartExtended extends ModelPart
{

    protected IModelPart extension;

    public ModelPartExtended(int texOffsetX, int texOffsetY)
    {
        super(texOffsetX, texOffsetY);
    }

    public ModelPartExtended()
    {
        super();
    }

    public ModelPartExtended setExtension(IModelPart modelPart)
    {
        extension = modelPart;
        return this;
    }

    @Override
    public void renderPart(PoseStack poseStack, float scale)
    {
        if (!(this.isShowing())) return;

        poseStack.pushPose();

        this.applyCharacterTransform(poseStack, scale);
        // Render the cubes - handled by parent renderer in 1.20.1
        if (extension != null)
            extension.renderJustPart(poseStack, scale);

        if (this.childModels != null)
        {
            for (net.minecraft.client.model.geom.ModelPart childModel : this.childModels)
            {
                // Child models render themselves
            }
        }
        poseStack.popPose();
    }

    @Override
    public void renderJustPart(PoseStack poseStack, float scale)
    {
        if (!(this.isShowing())) return;

        poseStack.pushPose();

        this.applyLocalTransform(poseStack, scale);
        // Render the cubes
        if (extension != null)
            extension.renderJustPart(poseStack, scale);

        if (this.childModels != null)
        {
            for (net.minecraft.client.model.geom.ModelPart childModel : this.childModels)
            {
                // Child models render themselves
            }
        }
        poseStack.popPose();
    }

    @Override
    public void applyPostTransform(PoseStack poseStack, float scale)
    {
        if (extension != null)
            extension.propagateTransform(poseStack, scale);
    }

    @Override
    public void propagateTransform(PoseStack poseStack, float scale)
    {
        super.propagateTransform(poseStack, scale);
        this.applyPostTransform(poseStack, scale);
    }

}
