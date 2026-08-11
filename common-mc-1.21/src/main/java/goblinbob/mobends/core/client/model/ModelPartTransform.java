package goblinbob.mobends.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.TransformUtils;
import goblinbob.mobends.lib.math.matrix.IMat4x4d;
import goblinbob.mobends.lib.math.vector.IVec3f;
import goblinbob.mobends.lib.math.vector.Vec3f;
import goblinbob.mobends.core.util.GlHelper;

public class ModelPartTransform implements IModelPart
{
	public Vec3f position;
	public Vec3f scale;
	public Vec3f offset;
	public SmoothOrientation rotation;
	public float offsetScale = 1.0F;
	public Vec3f globalOffset = new Vec3f();

	public final ModelPartTransform parent;

	public ModelPartTransform(ModelPartTransform parent)
	{
		this.position = new Vec3f();
		this.scale = new Vec3f(1, 1, 1);
		this.offset = new Vec3f();
		this.rotation = new SmoothOrientation();
		this.parent = parent;
	}

	public ModelPartTransform()
	{
		this(null);
	}

	@Override
	public void renderPart(PoseStack poseStack, float scale)
	{
	}

	@Override
	public void renderJustPart(PoseStack poseStack, float scale)
	{
	}

	@Override
	public void update(float ticksPerFrame)
	{
		this.rotation.update(ticksPerFrame);
	}

	@Override
	public IVec3f getPosition() { return this.position; }
	@Override
	public IVec3f getScale() { return this.scale; }
	@Override
	public IVec3f getOffset() { return this.offset; }
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
		return true;
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
	public void applyLocalTransform(PoseStack poseStack, float scale)
	{
		if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
        	poseStack.translate(this.position.x * scale * offsetScale, this.position.y * scale * offsetScale, this.position.z * scale * offsetScale);

		if (this.offset.x != 0.0F || this.offset.y != 0.0F || this.offset.z != 0.0F)
			poseStack.translate(this.offset.x * scale * offsetScale, this.offset.y * scale * offsetScale, this.offset.z * scale * offsetScale);

		GlHelper.rotate(poseStack, this.rotation.getSmooth());

		if(this.scale.x != 0.0F || this.scale.y != 0.0F || this.scale.z != 0.0F)
        	poseStack.scale(this.scale.x, this.scale.y, this.scale.z);
	}

	@Override
	public void propagateTransform(PoseStack poseStack, float scale)
	{
		this.applyLocalTransform(poseStack, scale);
	}

	@Override
	public void applyPostTransform(PoseStack poseStack, float scale)
	{
	}

	@Override
	public void setVisible(boolean showModel)
	{
	}

	@Override
	public void applyLocalTransform(float scale, IMat4x4d matrix)
	{
		if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
			TransformUtils.translate(matrix, this.position.x * scale, this.position.y * scale, this.position.z * scale);

		if (this.offset.x != 0.0F || this.offset.y != 0.0F || this.offset.z != 0.0F)
			TransformUtils.translate(matrix, this.offset.x * scale * offsetScale, this.offset.y * scale * offsetScale, this.offset.z * scale * offsetScale);

		TransformUtils.rotate(matrix, this.rotation.getSmooth());

	}

	@Override
	public IModelPart getParent()
	{
		return this.parent;
	}
}
