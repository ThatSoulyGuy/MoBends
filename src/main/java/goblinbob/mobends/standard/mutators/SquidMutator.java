package goblinbob.mobends.standard.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.core.util.GlHelper;
import goblinbob.mobends.standard.data.SquidData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.animal.Squid;

public class SquidMutator extends Mutator<SquidData, Squid, SquidModel<Squid>>
{

	public BendsModelPart squidBody;
	public BendsModelPart[][] squidTentacles = new BendsModelPart[8][SquidData.TENTACLE_SECTIONS];

	// Store the current data for use in renderMutated
	private SquidData currentData;

	// The squid model center is at Y=8 in model units (8/16 = 0.5 blocks)
	// This is where the body is positioned
	private static final float SQUID_MODEL_CENTER_Y = 8.0F / 16.0F;

	public SquidMutator(IEntityDataFactory<Squid> dataFactory)
	{
		super(dataFactory);
	}

	@Override
	public void storeVanillaModel(SquidModel<Squid> model)
	{
		// In 1.20.1, model parts are accessed differently
		// SquidModel uses root.getChild() pattern
		this.vanillaModel = model;
	}

	@Override
	public void applyVanillaModel(SquidModel<Squid> model)
	{
		// In 1.20.1, we can't directly set model fields
		// The vanilla model restoration would need to be handled via mixin
	}

	@Override
	public void swapLayer(LivingEntityRenderer<Squid, SquidModel<Squid>> renderer, int index, boolean isModelVanilla)
	{
		// No behaviour
	}

	@Override
	public void deswapLayer(LivingEntityRenderer<Squid, SquidModel<Squid>> renderer, int index)
	{
		// No behaviour
	}

	@Override
	public boolean createParts(SquidModel<Squid> original, float scaleFactor)
	{
		// Create squid body - centered at (0, 8, 0)
		this.squidBody = new BendsModelPart(0, 0)
				.setTextureSize(64, 32)
				.setPosition(0.0F, 8.0F, 0.0F);
		this.squidBody.addCube(-6.0F, -8.0F, -6.0F, 12, 16, 12, scaleFactor);

		// Create tentacles - 8 tentacles arranged in a circle
		for (int i = 0; i < this.squidTentacles.length; ++i)
		{
			// Calculate position around circle
			double angle = (double) i * Math.PI * 2.0D / (double) this.squidTentacles.length;
			float x = (float) Math.cos(angle) * 4.0F;
			float z = (float) Math.sin(angle) * 4.0F;

			// Calculate rotation to point outward (Y rotation)
			float yRotDegrees = (float) (-i * 360.0D / (double) this.squidTentacles.length + 90.0D);

			// First tentacle section (root)
			this.squidTentacles[i][0] = new BendsModelPart(48, 0)
					.setTextureSize(64, 32)
					.setPosition(x, 16.0F, z);
			this.squidTentacles[i][0].addCube(-1.0F, 0.0F, 0.0F, 2, SquidData.SECTION_HEIGHT, 2, scaleFactor);
			// Apply initial Y rotation for direction
			this.squidTentacles[i][0].rotation.rotateY(yRotDegrees);
			this.squidTentacles[i][0].finish();

			// Subsequent tentacle sections (children of previous)
			for (int j = 1; j < SquidData.TENTACLE_SECTIONS; ++j)
			{
				this.squidTentacles[i][j] = new BendsModelPart(48, 0)
						.setTextureSize(64, 32)
						.setPosition(0.0F, SquidData.SECTION_HEIGHT, 0.0F);
				this.squidTentacles[i][j].addCube(-1.0F, 0.0F, -2.0F, 2, SquidData.SECTION_HEIGHT, 2, scaleFactor);
				this.squidTentacles[i][j - 1].addChild(this.squidTentacles[i][j]);
			}
		}

		return true;
	}

	@Override
	public void syncUpWithData(SquidData data)
	{
		// Store current data for use in renderMutated
		this.currentData = data;

		if (this.squidBody != null)
		{
			this.squidBody.syncUp(data.squidBody);
		}

		for (int i = 0; i < this.squidTentacles.length; ++i)
		{
			for (int j = 0; j < SquidData.TENTACLE_SECTIONS; ++j)
			{
				if (this.squidTentacles[i][j] != null)
				{
					this.squidTentacles[i][j].syncUp(data.squidTentacles[i][j]);
				}
			}
		}
	}

	@Override
	public boolean isModelVanilla(SquidModel<Squid> model)
	{
		// Check if we've already created custom parts
		return this.squidBody == null;
	}

	@Override
	public boolean shouldModelBeSkipped(EntityModel<?> model)
	{
		return !(model instanceof SquidModel);
	}

	@Override
	public boolean shouldRenderCustom()
	{
		// Return true when we have created custom parts
		return this.squidBody != null;
	}

	@Override
	public void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
	                          int packedLight, int packedOverlay,
	                          float red, float green, float blue, float alpha)
	{
		if (currentData == null)
		{
			// Fallback: just render without transforms
			renderParts(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
			return;
		}

		poseStack.pushPose();

		float scale = 1.0F / 16.0F;

		// Apply globalOffset
		float gx = currentData.globalOffset.getX();
		float gy = currentData.globalOffset.getY();
		float gz = currentData.globalOffset.getZ();
		if (gx != 0 || gy != 0 || gz != 0)
		{
			poseStack.translate(gx * scale, gy * scale, gz * scale);
		}

		// Apply centerRotation around the squid's center height
		Quaternion centerRot = currentData.centerRotation.getSmooth();
		if (!centerRot.isIdentity())
		{
			poseStack.translate(0, SQUID_MODEL_CENTER_Y, 0);
			GlHelper.rotate(poseStack, centerRot);
			poseStack.translate(0, -SQUID_MODEL_CENTER_Y, 0);
		}

		// Apply renderRotation
		Quaternion renderRot = currentData.renderRotation.getSmooth();
		if (!renderRot.isIdentity())
		{
			poseStack.translate(0, SQUID_MODEL_CENTER_Y, 0);
			GlHelper.rotate(poseStack, renderRot);
			poseStack.translate(0, -SQUID_MODEL_CENTER_Y, 0);
		}

		// Apply localOffset (after rotations)
		float lx = currentData.localOffset.getX();
		float ly = currentData.localOffset.getY();
		float lz = currentData.localOffset.getZ();
		if (lx != 0 || ly != 0 || lz != 0)
		{
			poseStack.translate(lx * scale, ly * scale, lz * scale);
		}

		// Render all squid parts
		renderParts(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);

		poseStack.popPose();
	}

	private void renderParts(PoseStack poseStack, VertexConsumer vertexConsumer,
	                         int packedLight, int packedOverlay,
	                         float red, float green, float blue, float alpha)
	{
		// Render body
		if (squidBody != null)
		{
			squidBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		}

		// Render tentacles (only the root, children are rendered automatically)
		for (int i = 0; i < squidTentacles.length; ++i)
		{
			if (squidTentacles[i][0] != null)
			{
				squidTentacles[i][0].render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
			}
		}
	}

}
