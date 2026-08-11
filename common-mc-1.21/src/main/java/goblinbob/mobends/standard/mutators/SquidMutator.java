package goblinbob.mobends.standard.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.client.model.ModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.data.SquidData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.animal.Squid;

public class SquidMutator extends Mutator<SquidData, Squid, SquidModel<Squid>>
{

	public ModelPart squidBody;
	public ModelPart[][] squidTentacles = new ModelPart[8][SquidData.TENTACLE_SECTIONS];

	public SquidMutator(IEntityDataFactory<Squid> dataFactory)
	{
		super(dataFactory);
	}

	@Override
	public void storeVanillaModel(SquidModel<Squid> model)
	{
		this.vanillaModel = model;
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

		this.squidBody = new ModelPart(0, 0);
		this.squidBody.setPosition(0.0F, 8.0F, 0.0F);
		this.squidBody.developBox(-6.0F, -8.0F, -6.0F, 12, 16, 12, 0.0F).create();

		for (int i = 0; i < this.squidTentacles.length; ++i)
		{
			this.squidTentacles[i][0] = new ModelPart(48, 0);
			double d0 = (double) i * Math.PI * 2.0D / (double) this.squidTentacles.length;
			float f = (float) Math.cos(d0) * 4.0F;
			float f1 = (float) Math.sin(d0) * 4.0F;
			d0 = (double) i * -360.0D / (double) this.squidTentacles.length + 90.0D;
			this.squidTentacles[i][0].setPosition(f, 16.0F, f1);
			this.squidTentacles[i][0].developBox(-1.0F, 0.0F, 0.0F, 2, SquidData.SECTION_HEIGHT, 2, 0.0F).create();
			this.squidTentacles[i][0].rotation.rotateY((float) d0);

			for (int j = 1; j < SquidData.TENTACLE_SECTIONS; ++j)
			{
				this.squidTentacles[i][j] = new ModelPart(48, 0);
				this.squidTentacles[i][j].setPosition(0, SquidData.SECTION_HEIGHT, 0);
				this.squidTentacles[i][j].developBox(-1.0F, 0.0F, -2.0F, 2, SquidData.SECTION_HEIGHT, 2, 0.0F).create();
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
		return !(model instanceof IModelPart);
	}

	@Override
	public boolean shouldModelBeSkipped(EntityModel<?> model)
	{
		return !(model instanceof SquidModel);
	}

	@Override
	public boolean shouldRenderCustom()
	{
		return false;
	}

	@Override
	public void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
	                          int packedLight, int packedOverlay, int color)
	{
	}

}
