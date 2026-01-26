package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class RidingAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	private static final String[] ACTIONS = new String[] { "riding" };
	
	private static final float PI = (float) Math.PI;
	
	@Override
	public String[] getActions(BipedEntityData<?> entityData)
	{
		return ACTIONS;
	}

	@Override
	public void perform(BipedEntityData<?> data)
	{
		final LivingEntity living = data.getEntity();

		data.localOffset.slideToZero(0.3F);
		data.renderRotation.orientZero();
		data.centerRotation.setSmoothness(.3F).orientZero();
		data.renderLeftItemRotation.orientZero();
		data.renderRightItemRotation.orientZero();
		
		data.head.rotation.orientX(data.headPitch.get())
		  				  .rotateY(data.headYaw.get());
		data.body.rotation.orientY(0).setSmoothness(0.5F);
		
		data.leftLeg.rotation.orientX(-90.0F).rotateZ(-10.0F).rotateY(-25.0F);
		data.rightLeg.rotation.orientX(-90.0F).rotateZ(10.0F).rotateY(25.0F);
		data.leftForeLeg.rotation.orientX(60.0F);
		data.rightForeLeg.rotation.orientX(60.0F);
		
		data.leftArm.rotation.orientX(0.0F).rotateZ(-10F);
		data.leftForeArm.rotation.orientX(-10.0F);
		data.rightArm.rotation.orientX(0.0F).rotateZ(10F);
		data.rightForeArm.rotation.orientX(-10.0F);
		
		Entity ridden = living.getVehicle();
		if (ridden instanceof LivingEntity riddenLiving)
		{
			float relativeHeadYaw = Mth.wrapDegrees(living.getYRot() - riddenLiving.yBodyRot);
			float relativeYaw = Mth.wrapDegrees(living.getYRot() - data.headYaw.get() - riddenLiving.yBodyRot);

			data.body.rotation.orientZ(Mth.clamp(-relativeHeadYaw * 0.25F, -20.0F, 20.0F));
			data.leftLeg.rotation.rotateX(-Mth.sin(relativeYaw / 180.0F * PI * 1.5F) * 45.0F);
			data.rightLeg.rotation.rotateX(Mth.sin(relativeYaw / 180.0F * PI * 1.5F) * 45.0F);
		}
		
		if (!data.isStillHorizontally())
		{
			data.body.rotation.orientX(25.0F);
			data.leftArm.rotation.orientX(-45.0F).rotateZ(10F);
			data.leftForeArm.rotation.orientX(-10.0F);
			data.rightArm.rotation.orientX(-45.0F).rotateZ(-10F);
			data.rightForeArm.rotation.orientX(-10.0F);
			
			float motionMagnitude = (float) (Math.sqrt(living.getDeltaMovement().x*living.getDeltaMovement().x + living.getDeltaMovement().z*living.getDeltaMovement().z)) * 100;
			if (motionMagnitude > 1)
			{
				float ticks = DataUpdateHandler.getTicks() * 0.5F;
				float bodyRotation = 45.0F + Mth.cos(ticks) * 10F;
				data.body.rotation.orientX(bodyRotation);
				data.head.rotation.rotateX(-bodyRotation);
				data.leftArm.rotation.rotateX(-bodyRotation);
				data.rightArm.rotation.rotateX(-bodyRotation);
				data.globalOffset.slideY(Mth.sin(ticks) * 0.3F);
			}
			else
			{
				data.head.rotation.rotateX(-25.0F);
			}
		}
	}
}
