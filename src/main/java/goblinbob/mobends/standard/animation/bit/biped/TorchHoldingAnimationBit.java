package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class TorchHoldingAnimationBit extends AnimationBit<BipedEntityData<?>>
{


	private HumanoidArm getTorchHand(LivingEntity living)
	{
		final HumanoidArm mainHand = living.getMainArm();
		final HumanoidArm offHand = mainHand == HumanoidArm.LEFT ? HumanoidArm.RIGHT : HumanoidArm.LEFT;

		final Item mainItem = living.getItemInHand(InteractionHand.MAIN_HAND).getItem();
		final Item offItem = living.getItemInHand(InteractionHand.OFF_HAND).getItem();

		if (mainItem.equals(Items.TORCH))
			return mainHand;
		else if (offItem.equals(Items.TORCH))
			return offHand;
		else
			return null;
	}

	@Override
	public void perform(BipedEntityData<?> data)
	{
		final LivingEntity living = data.getEntity();
		final HumanoidArm torchHand = getTorchHand(living);

		if (torchHand == null)
			return;

		final IModelPart mainArm = torchHand == HumanoidArm.RIGHT ? data.rightArm : data.leftArm;
		final IModelPart mainForeArm = torchHand == HumanoidArm.RIGHT ? data.rightForeArm : data.leftForeArm;

		mainArm.getRotation().orientX(-90.0F + data.headPitch.get() * 0.5F)
							 .rotateY(data.headYaw.get() * 0.7F);
		mainForeArm.getRotation().orientX(-5.0F);
	}

}
