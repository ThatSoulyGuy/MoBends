package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.TransformUtils;
import goblinbob.mobends.lib.math.matrix.IMat4x4d;
import goblinbob.mobends.lib.math.physics.ICollider;
import goblinbob.mobends.lib.math.vector.IVec3f;
import goblinbob.mobends.lib.math.vector.Vec3f;
import goblinbob.mobends.core.util.GlHelper;
import net.minecraft.client.model.geom.ModelPart;

/**
 * A container that wraps a vanilla ModelPart and provides animated transformations.
 * Updated for Minecraft 1.20.1 - no longer extends ModelRenderer (which doesn't exist).
 * Instead, it contains a reference to a vanilla ModelPart and applies transformations via PoseStack.
 *
 * @deprecated This class is part of the legacy armor rendering system.
 *             Use the three-tier rendering system (ArmorRenderingFacade) with
 *             TransformInjector and ModelPartTransformer instead.
 *             This class is kept for backward compatibility and will be removed in a future version.
 */
@Deprecated
public class PartContainer implements IModelPart
{
    public Vec3f position;
    /**
     * A secondary position variable is used to offset
     * the model relative to the first position, which may
     * be overridden by animation.
     */
    public Vec3f offset;
    /**
     * Used to offset the container item relative to
     * the rotation point
     */
    public Vec3f innerOffset;
    public Vec3f scale;
    public SmoothOrientation rotation;
    public float offsetScale = 1.0F;
    /**
     * Offset applied before the parent transformation.
     */
    public Vec3f globalOffset = new Vec3f();

    private ModelPart innerModel;
    public boolean showModel = true;
    public boolean isHidden = false;
    public boolean mirror = false;

    protected IModelPart parent;
    protected ICollider collider;

    public PartContainer(ModelPart model)
    {
        this.innerModel = model;
        this.position = new Vec3f();
        this.offset = new Vec3f();
        this.innerOffset = new Vec3f();
        this.scale = new Vec3f(1, 1, 1);
        this.rotation = new SmoothOrientation();
    }

    public ModelPart getModel() { return this.innerModel; }
    public IModelPart getParent() { return this.parent; }

    public PartContainer setParent(IModelPart parent)
    {
        this.parent = parent;
        return this;
    }

    public PartContainer setPosition(float x, float y, float z)
    {
        this.position.set(x, y, z);
        return this;
    }

    public PartContainer setInnerOffset(float x, float y, float z)
    {
        this.innerOffset.set(x, y, z);
        return this;
    }

    /**
     * Renders the contained model with animated transformations applied.
     */
    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay)
    {
        if (!(this.isShowing())) return;

        poseStack.pushPose();
        this.applyCharacterTransform(poseStack, 1.0F / 16.0F);

        // Apply inner offset
        if (this.innerOffset.x != 0.0F || this.innerOffset.y != 0.0F || this.innerOffset.z != 0.0F)
        {
            float scale = 1.0F / 16.0F;
            poseStack.translate(this.innerOffset.x * scale, this.innerOffset.y * scale, this.innerOffset.z * scale);
        }

        // Render the contained vanilla model
        if (this.innerModel != null)
        {
            this.innerModel.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        }

        poseStack.popPose();
    }

    @Override
    public void renderPart(PoseStack poseStack, float scale)
    {
        if (!(this.isShowing())) return;

        poseStack.pushPose();
        this.applyCharacterTransform(poseStack, scale);

        // Apply inner offset
        if (this.innerOffset.x != 0.0F || this.innerOffset.y != 0.0F || this.innerOffset.z != 0.0F)
        {
            poseStack.translate(this.innerOffset.x * scale, this.innerOffset.y * scale, this.innerOffset.z * scale);
        }

        poseStack.popPose();
    }

    @Override
    public void renderJustPart(PoseStack poseStack, float scale)
    {
        if (!(this.isShowing())) return;

        poseStack.pushPose();
        this.applyLocalTransform(poseStack, scale);

        // Apply inner offset
        if (this.innerOffset.x != 0.0F || this.innerOffset.y != 0.0F || this.innerOffset.z != 0.0F)
        {
            poseStack.translate(this.innerOffset.x * scale, this.innerOffset.y * scale, this.innerOffset.z * scale);
        }

        poseStack.popPose();
    }

    @Override
    public void applyCharacterTransform(PoseStack poseStack, float scale)
    {
        if (this.parent != null)
            this.parent.applyCharacterTransform(poseStack, scale);
        this.applyLocalTransform(poseStack, scale);
    }

    @Override
    public void applyLocalTransform(PoseStack poseStack, float scale)
    {
        if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
            poseStack.translate(this.position.x * scale * offsetScale, this.position.y * scale * offsetScale, this.position.z * scale * offsetScale);

        if (this.offset.x != 0.0F || this.offset.y != 0.0F || this.offset.z != 0.0F)
            poseStack.translate(this.offset.x * scale * offsetScale, this.offset.y * scale * offsetScale, this.offset.z * scale * offsetScale);

        GlHelper.rotate(poseStack, this.rotation.getSmooth());

        if (this.scale.x != 0.0F || this.scale.y != 0.0F || this.scale.z != 0.0F)
            poseStack.scale(this.scale.x, this.scale.y, this.scale.z);
    }

    @Override
    public void applyPostTransform(PoseStack poseStack, float scale)
    {
    }

    @Override
    public void update(float ticksPerFrame)
    {
        this.rotation.update(ticksPerFrame);
    }

    @Override
    public Vec3f getPosition() { return this.position; }
    @Override
    public Vec3f getScale() { return this.scale; }
    @Override
    public Vec3f getOffset() { return this.offset; }
    @Override
    public SmoothOrientation getRotation() { return this.rotation; }
    @Override
    public float getOffsetScale() { return this.offsetScale; }
    @Override
    public IVec3f getGlobalOffset()
    {
        return globalOffset;
    }

    @Override
    public void syncUp(IModelPart part)
    {
        if(part == null)
            return;

        this.position.set(part.getPosition());
        this.rotation.set(part.getRotation());
        this.offset.set(part.getOffset());
        this.scale.set(part.getScale());
        this.offsetScale = part.getOffsetScale();
    }

    @Override
    public boolean isShowing()
    {
        return this.showModel && !this.isHidden;
    }

    @Override
    public void setVisible(boolean showModel)
    {
        this.showModel = showModel;
    }

    @Override
    public void applyPreTransform(PoseStack poseStack, float scale)
    {
        if (this.globalOffset.x != 0.0F || this.globalOffset.y != 0.0F || this.globalOffset.z != 0.0F)
            poseStack.translate(this.globalOffset.x * scale, this.globalOffset.y * scale, this.globalOffset.z * scale);
    }

    @Override
    public void applyPreTransform(float scale, IMat4x4d dest)
    {
        if (this.globalOffset.x != 0.0F || this.globalOffset.y != 0.0F || this.globalOffset.z != 0.0F)
            TransformUtils.translate(dest, this.globalOffset.x * scale, this.globalOffset.y * scale, this.globalOffset.z * scale);
    }

    @Override
    public void applyLocalTransform(float scale, IMat4x4d matrix)
    {
        if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
            TransformUtils.translate(matrix, this.position.x * scale * offsetScale, this.position.y * scale * offsetScale, this.position.z * scale * offsetScale, matrix);

        if (this.offset.x != 0.0F || this.offset.y != 0.0F || this.offset.z != 0.0F)
            TransformUtils.translate(matrix, this.offset.x * scale * offsetScale, this.offset.y * scale * offsetScale, this.offset.z * scale * offsetScale);

        TransformUtils.rotate(matrix, rotation.getSmooth());

        if(this.scale.x != 0.0F || this.scale.y != 0.0F || this.scale.z != 0.0F)
            TransformUtils.scale(matrix, this.scale.x, this.scale.y, this.scale.z);
    }
}
