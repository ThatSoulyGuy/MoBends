package goblinbob.mobends.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.vector.IVec3f;

public interface IModelPart extends goblinbob.mobends.lib.client.model.IAnimatedPart
{

	void applyPreTransform(PoseStack poseStack, float scale);

	void applyLocalTransform(PoseStack poseStack, float scale);

	default void applyCharacterTransform(PoseStack poseStack, float scale)
	{
		if (this.getParent() != null)
		{
			this.getParent().applyCharacterTransform(poseStack, scale * getOffsetScale());
		}
		this.applyPreTransform(poseStack, scale);
		this.applyLocalTransform(poseStack, scale);
	}

	void update(float ticksPerFrame);
	void syncUp(IModelPart part);
	void setVisible(boolean showModel);
	IVec3f getPosition();
	IVec3f getScale();
	default IVec3f getPreRotationScale() { return null; }
	IVec3f getOffset();
	SmoothOrientation getRotation();
	float getOffsetScale();
	IVec3f getGlobalOffset();
	IModelPart getParent();
	boolean isShowing();

}
