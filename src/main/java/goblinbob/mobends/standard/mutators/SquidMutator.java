package goblinbob.mobends.standard.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.data.SquidData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.animal.Squid;

public class SquidMutator extends Mutator<SquidData, Squid, SquidModel<Squid>>
{

	private static final int TEXTURE_WIDTH = 64;
	private static final int TEXTURE_HEIGHT = 32;

	public BendsModelPart squidBody;
	public BendsModelPart[][] squidTentacles = new BendsModelPart[8][SquidData.TENTACLE_SECTIONS];

	public SquidMutator(IEntityDataFactory<Squid> dataFactory)
	{
		super(dataFactory);
	}

	@Override
	public void storeVanillaModel(SquidModel<Squid> model)
	{
	}

	@Override
	public void applyVanillaModel(SquidModel<Squid> model)
	{
	}

	@Override
	public void swapLayer(LivingEntityRenderer<Squid, SquidModel<Squid>> renderer, int index, boolean isModelVanilla)
	{
	}

	@Override
	public void deswapLayer(LivingEntityRenderer<Squid, SquidModel<Squid>> renderer, int index)
	{
	}

	@Override
	public boolean createParts(SquidModel<Squid> original, float scaleFactor)
	{
		this.squidBody = new BendsModelPart(0, 0)
				.setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
				.setPosition(0.0F, 8.0F, 0.0F);
		this.squidBody.developBox(-6.0F, -8.0F, -6.0F, 12, 16, 12, scaleFactor).create();

		for (int i = 0; i < this.squidTentacles.length; ++i)
		{
			double angle = (double) i * Math.PI * 2.0D / (double) this.squidTentacles.length;
			float x = (float) Math.cos(angle) * 4.0F;
			float z = (float) Math.sin(angle) * 4.0F;
			double yaw = (double) i * -360.0D / (double) this.squidTentacles.length + 90.0D;

			this.squidTentacles[i][0] = new BendsModelPart(48, 0)
					.setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
					.setPosition(x, 16.0F, z);
			this.squidTentacles[i][0].developBox(-1.0F, 0.0F, 0.0F, 2, SquidData.SECTION_HEIGHT, 2, scaleFactor).create();
			this.squidTentacles[i][0].rotation.rotateY((float) yaw);

			for (int j = 1; j < SquidData.TENTACLE_SECTIONS; ++j)
			{
				this.squidTentacles[i][j] = new BendsModelPart(48, j * SquidData.SECTION_HEIGHT)
						.setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
						.setPosition(0, SquidData.SECTION_HEIGHT, 0);
				this.squidTentacles[i][j].developBox(-1.0F, 0.0F, -2.0F, 2, SquidData.SECTION_HEIGHT, 2, scaleFactor).create();
				this.squidTentacles[i][j - 1].addChild(this.squidTentacles[i][j]);
			}
		}

		return true;
	}

	@Override
	public void syncUpWithData(SquidData data)
	{
		this.squidBody.syncUp(data.squidBody);
		for (int i = 0; i < this.squidTentacles.length; ++i)
		{
			for (int j = 0; j < SquidData.TENTACLE_SECTIONS; ++j)
			{
				this.squidTentacles[i][j].syncUp(data.squidTentacles[i][j]);
			}
		}
	}

	@Override
	public boolean isModelVanilla(SquidModel<Squid> model)
	{
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
		return this.squidBody != null;
	}

	@Override
	public void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
	                          int packedLight, int packedOverlay, int color)
	{
		if (this.squidBody != null)
		{
			this.squidBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		}

		for (int i = 0; i < this.squidTentacles.length; ++i)
		{
			if (this.squidTentacles[i][0] != null)
			{
				this.squidTentacles[i][0].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
			}
		}
	}
}
