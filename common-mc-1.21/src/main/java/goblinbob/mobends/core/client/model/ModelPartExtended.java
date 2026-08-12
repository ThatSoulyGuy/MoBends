package goblinbob.mobends.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;

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
        if (extension != null)
            extension.renderJustPart(poseStack, scale);

        if (this.childModels != null)
        {
            for (net.minecraft.client.model.geom.ModelPart childModel : this.childModels)
            {
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
        if (extension != null)
            extension.renderJustPart(poseStack, scale);

        if (this.childModels != null)
        {
            for (net.minecraft.client.model.geom.ModelPart childModel : this.childModels)
            {
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
