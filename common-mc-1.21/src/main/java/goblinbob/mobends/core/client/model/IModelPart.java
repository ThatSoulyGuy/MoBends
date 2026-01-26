package goblinbob.mobends.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.matrix.IMat4x4d;
import goblinbob.mobends.lib.math.vector.IVec3f;

public interface IModelPart
{

	/**
	 * Applies the transform in global space (relative to the entity).
	 * This is being applied before the parent transform.
	 * @param poseStack The pose stack to apply transforms to.
	 * @param scale Controls the scale of the translation.
	 */
	void applyPreTransform(PoseStack poseStack, float scale);

	/**
	 * Applies the transform in global space (relative to the entity).
	 * This is being applied before the parent transform.
	 * @param scale Controls the scale of the translation.
	 * @param dest The matrix to be transformed.
	 */
	void applyPreTransform(float scale, IMat4x4d dest);

	/**
	 * Applies the transform in local space (relative to the parent).
	 * @param poseStack The pose stack to apply transforms to.
	 * @param scale Controls the scale of the translation.
	 */
	void applyLocalTransform(PoseStack poseStack, float scale);

	/**
	 * Applies the transform in local space (relative to the parent).
	 * @param scale Controls the scale of the translation.
	 * @param dest The matrix to be transformed.
	 */
	void applyLocalTransform(float scale, IMat4x4d dest);

	/**
	 * Applies the transform in character space (includes all parents).
	 * Parent transforms are applied first (root to leaf order), then this part's transforms.
	 * @param poseStack The pose stack to apply transforms to.
	 * @param scale Controls the scale of the translation.
	 */
	default void applyCharacterTransform(PoseStack poseStack, float scale)
	{
		// Apply parent transforms first (recursive up to root)
		if (this.getParent() != null)
		{
			this.getParent().applyCharacterTransform(poseStack, scale * getOffsetScale());
		}
		// Then apply this part's transforms
		this.applyPreTransform(poseStack, scale);
		this.applyLocalTransform(poseStack, scale);
	}

	/**
	 * Applies the transform in character space (includes all parents).
	 * Parent transforms are applied first (root to leaf order), then this part's transforms.
	 * @param scale Controls the scale of the translation.
	 * @param dest The matrix to be transformed.
	 */
	default void applyCharacterTransform(float scale, IMat4x4d dest)
	{
		// Apply parent transforms first (recursive up to root)
		if (this.getParent() != null)
		{
			this.getParent().applyCharacterTransform(scale * getOffsetScale(), dest);
		}
		// Then apply this part's transforms
		this.applyPreTransform(scale, dest);
		this.applyLocalTransform(scale, dest);
	}

	/**
	 * Made to transform and propagate downwards the stream of children parts.
	 * @param poseStack The pose stack to apply transforms to.
	 * @param scale Controls the scale of the translation.
 	 */
	default void propagateTransform(PoseStack poseStack, float scale)
	{
		this.applyLocalTransform(poseStack, scale);
		this.applyPostTransform(poseStack, scale);
	}

	/**
	 * This transform is applied after rendering the part, so that
	 * whatever is rendered after it (the children) gets that rotation.
	 * @param poseStack The pose stack to apply transforms to.
	 * @param scale Controls the scale of the translation.
	 */
	void applyPostTransform(PoseStack poseStack, float scale);

	void renderPart(PoseStack poseStack, float scale);
	void renderJustPart(PoseStack poseStack, float scale);
	void update(float ticksPerFrame);
	void syncUp(IModelPart part);
	void setVisible(boolean showModel);
	IVec3f getPosition();
	IVec3f getScale();
	IVec3f getOffset();
	SmoothOrientation getRotation();
	float getOffsetScale();
	IVec3f getGlobalOffset();
	IModelPart getParent();
	boolean isShowing();

}
