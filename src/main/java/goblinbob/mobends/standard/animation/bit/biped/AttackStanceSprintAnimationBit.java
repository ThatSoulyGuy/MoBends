package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class AttackStanceSprintAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	@Override
	public void perform(BipedEntityData<?> data)
	{
		data.localOffset.slideToZero(0.3F);

		final LivingEntity living = data.getEntity();
		final HumanoidArm primaryHand = AttackArms.attackingArm(data, living);

		boolean mainHandSwitch = primaryHand == HumanoidArm.RIGHT;
		float handDirMtp = mainHandSwitch ? 1 : -1;
		IModelPart mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
		IModelPart offArm = mainHandSwitch ? data.leftArm : data.rightArm;
		IModelPart mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
		IModelPart offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;

		if (living.getItemInHand(AttackArms.handOf(living, primaryHand)).getItem() instanceof SwordItem)
		{
			data.swordTrail.add(data, primaryHand, 0.0F, 0.0F, -10.0F);
		}

		data.body.rotation.rotateY(20 * handDirMtp);
		data.head.rotation.rotateY(-20 * handDirMtp);
		mainArm.getRotation().orientZ(60.0F * handDirMtp);
		mainArm.getRotation().rotateY(60.0F * handDirMtp);
		offArm.getRotation().rotateZ(-30.0F * handDirMtp);

		if (mainHandSwitch)
		{
			data.renderRightItemRotation.setSmoothness(.3F).orientX(45);
		}
		else
		{
			data.renderLeftItemRotation.setSmoothness(.3F).orientX(45);
		}
	}
}
