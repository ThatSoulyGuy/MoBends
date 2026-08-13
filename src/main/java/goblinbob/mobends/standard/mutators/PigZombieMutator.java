package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.PigZombieData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.monster.ZombifiedPiglin;

public class PigZombieMutator extends BipedMutator<PigZombieData, ZombifiedPiglin, PiglinModel<ZombifiedPiglin>>
{

	private static final float EAR_TILT = (float) (Math.PI / 6);
	private static final float NECK_FILL = 0.35F;

	protected BendsModelPart leftEar;
	protected BendsModelPart rightEar;

	protected boolean halfTexture = false;

	public PigZombieMutator(IEntityDataFactory<ZombifiedPiglin> dataFactory)
	{
		super(dataFactory);
	}

	@Override
	protected void createHeadParts(float scaleFactor)
	{
		head = new BendsModelPart(0, 0)
				.setTextureSize(64, 64)
				.setPosition(0.0F, -12.0F, 0.0F);
		head.addCube(-5.0F, -8.0F, -4.0F, 10, 8, 8, scaleFactor);
		head.setTextureOffset(31, 1);
		head.addCube(-2.0F, -4.0F, -5.0F, 4, 4, 1, scaleFactor);
		head.setTextureOffset(2, 4);
		head.addCube(2.0F, -2.0F, -5.0F, 1, 2, 1, scaleFactor);
		head.setTextureOffset(2, 0);
		head.addCube(-3.0F, -2.0F, -5.0F, 1, 2, 1, scaleFactor);
		head.setTextureOffset(16, 16);
		head.addCube(-4.0F, -1.0F, -2.0F, 8, 1, 4, scaleFactor + NECK_FILL);
		body.addChild(head);

		leftEar = new BendsModelPart(51, 6)
				.setTextureSize(64, 64)
				.setPosition(4.5F, -6.0F, 0.0F);
		leftEar.addCube(0.0F, 0.0F, -2.0F, 1, 5, 4, scaleFactor);
		leftEar.rotation.orientInstantZ(-EAR_TILT);
		head.addChild(leftEar);

		rightEar = new BendsModelPart(39, 6)
				.setTextureSize(64, 64)
				.setPosition(-4.5F, -6.0F, 0.0F);
		rightEar.addCube(-1.0F, 0.0F, -2.0F, 1, 5, 4, scaleFactor);
		rightEar.rotation.orientInstantZ(EAR_TILT);
		head.addChild(rightEar);

		headwear = new BendsModelPart(32, 0)
				.setTextureSize(64, 64);
		head.addChild(headwear);
	}

	@Override
	protected void createOuterHeadParts(float scaleFactor, float outerOffset)
	{
		outerHead = new BendsModelPart(0, 0)
				.setTextureSize(64, 64)
				.setPosition(0.0F, -12.0F, 0.0F);
		outerBody.addChild(outerHead);
	}

	@Override
	public void fetchFields(LivingEntityRenderer<ZombifiedPiglin, PiglinModel<ZombifiedPiglin>> renderer)
	{
		super.fetchFields(renderer);

		this.halfTexture = false;
	}

	@Override
	public void storeVanillaModel(PiglinModel<ZombifiedPiglin> model)
	{
		this.vanillaModel = model;

		super.storeVanillaModel(model);
	}

	@Override
	public boolean shouldModelBeSkipped(EntityModel<?> model)
	{
		return !(model instanceof PiglinModel);
	}

}
