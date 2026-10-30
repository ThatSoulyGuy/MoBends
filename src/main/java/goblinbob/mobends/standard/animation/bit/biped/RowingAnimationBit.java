package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;

public class RowingAnimationBit<T extends BipedEntityData<?>> extends SittingAnimationBit<T>
{
	private static final Rig BOAT = new Rig(2.0F, 0.5235988F, 11.0F, 12.3F, -12.0F, -2.0F);
	private static final Rig RAFT = new Rig(0.0F, 0.7853982F, 10.0F, 17.9F, -4.0F, 8.0F);

	private static final float OARLOCK_X = 3.0F;
	private static final float PADDLE_TILT = 0.19634955F;
	private static final float SHOULDER_ABOVE_WAIST = 10.0F;
	private static final float SHOULDER_SIDE = 5.0F;
	private static final float UPPER_ARM = 4.0F;
	private static final float FOREARM = 5.5F;
	private static final float FRONT_SEAT = 3.2F;
	private static final float HANDLE_REACH = 6.0F;
	private static final float EXTRA_REACH = 1.5F;
	private static final float SMOOTHNESS = 3.0F;
	private static final float SWING_TICKS = 10.0F;
	private static final float SWING_FADE_TICKS = 5.0F;

	private record Rig(float oarShift, float oarSweep, float handleHeight, float shoulderHeight,
					   float leanPulled, float leanReached)
	{
	}

	private static Rig rigOf(Boat boat)
	{
		return boat.getVariant() == Boat.Type.BAMBOO ? RAFT : BOAT;
	}

	public static boolean isRowing(LivingEntity living)
	{
		return living.getVehicle() instanceof Boat boat && boat.getControllingPassenger() == living;
	}

	public static boolean isPaddling(LivingEntity living)
	{
		return isRowing(living) && living.getVehicle() instanceof Boat boat
				&& (boat.getPaddleState(Boat.PADDLE_LEFT) || boat.getPaddleState(Boat.PADDLE_RIGHT));
	}

	public static void animateOars(Boat boat, ModelPart leftPaddle, ModelPart rightPaddle, float partialTicks)
	{
		leftPaddle.x = OARLOCK_X;
		rightPaddle.x = OARLOCK_X;

		if (!(boat.getControllingPassenger() instanceof Player driver) || !BenderHelper.isEntityAnimated(driver))
		{
			return;
		}

		final Rig rig = rigOf(boat);
		final float oarlock = OARLOCK_X + rig.oarShift() + (boat.getPassengers().size() > 1 ? FRONT_SEAT : 0.0F);
		leftPaddle.x = oarlock;
		rightPaddle.x = oarlock;
		leftPaddle.yRot = oarYaw(rig, boat.getRowingTime(Boat.PADDLE_LEFT, partialTicks));
		rightPaddle.yRot = Mth.PI - oarYaw(rig, boat.getRowingTime(Boat.PADDLE_RIGHT, partialTicks));
	}

	private static float oarYaw(Rig rig, float rowingTime)
	{
		return rig.oarSweep() * Mth.sin(1.0F - rowingTime);
	}

	@Override
	public void perform(T data)
	{
		super.perform(data);

		if (!(data.getEntity().getVehicle() instanceof Boat boat))
		{
			return;
		}

		final Rig rig = rigOf(boat);
		final float partialTicks = DataUpdateHandler.partialTicks;
		final float[] left = grip(rig, boat.getRowingTime(Boat.PADDLE_LEFT, partialTicks));
		final float[] right = grip(rig, boat.getRowingTime(Boat.PADDLE_RIGHT, partialTicks));
		final float reach = ((left[0] + right[0]) * 0.5F - rig.oarShift()) / HANDLE_REACH;
		final float rowLean = Mth.lerp(Mth.clamp(reach, 0.0F, 1.0F), rig.leanPulled(), rig.leanReached());
		final float bodyLean = rowLean
				* Mth.clamp((data.getTicksAfterAnyAttack() - SWING_TICKS) / SWING_FADE_TICKS, 0.0F, 1.0F);

		data.body.rotation.setSmoothness(SMOOTHNESS).orientX(bodyLean);
		data.head.rotation.setSmoothness(SMOOTHNESS).orientX(data.headPitch.get())
				.rotateY(data.headYaw.get())
				.rotateX(-bodyLean);

		reach(data.leftArm, data.leftForeArm, left, rig, rowLean, bodyLean, -1.0F);
		reach(data.rightArm, data.rightForeArm, right, rig, rowLean, bodyLean, 1.0F);
	}

	private static float[] grip(Rig rig, float rowingTime)
	{
		final float pitch = Mth.lerp((Mth.sin(-rowingTime) + 1.0F) * 0.5F, -1.0471976F, -0.2617994F);
		final float yaw = oarYaw(rig, rowingTime);
		final float y = Mth.cos(pitch) + 4.0F * Mth.sin(pitch);
		final float z = Mth.sin(pitch) - 4.0F * Mth.cos(pitch);
		final float x = z * Mth.sin(yaw);

		return new float[] {
				OARLOCK_X + rig.oarShift() + x * Mth.cos(PADDLE_TILT) - y * Mth.sin(PADDLE_TILT),
				rig.handleHeight() - x * Mth.sin(PADDLE_TILT) - y * Mth.cos(PADDLE_TILT),
				9.0F + z * Mth.cos(yaw) - SHOULDER_SIDE
		};
	}

	private static void reach(ModelPartTransform arm, ModelPartTransform foreArm, float[] grip, Rig rig, float lean,
							  float bodyLean, float side)
	{
		final float leanRadians = lean * Mth.DEG_TO_RAD;
		final float forward = grip[0] + EXTRA_REACH - SHOULDER_ABOVE_WAIST * Mth.sin(leanRadians);
		final float down = rig.shoulderHeight() - grip[1] + SHOULDER_ABOVE_WAIST * (1.0F - Mth.cos(leanRadians));
		final float distance = Mth.clamp(Mth.sqrt(forward * forward + down * down),
				FOREARM - UPPER_ARM + 0.1F, FOREARM + UPPER_ARM - 0.1F);

		final float bend = (float) Math.acos(Mth.clamp(
				(distance * distance - UPPER_ARM * UPPER_ARM - FOREARM * FOREARM) / (2.0F * UPPER_ARM * FOREARM),
				-1.0F, 1.0F));
		final float upper = (float) Math.atan2(forward, down)
				- (float) Math.atan2(FOREARM * Mth.sin(bend), UPPER_ARM + FOREARM * Mth.cos(bend));
		final float flare = (float) Math.atan2(grip[2], distance);

		arm.rotation.setSmoothness(SMOOTHNESS).orientX(-upper * Mth.RAD_TO_DEG - bodyLean)
				.rotateZ(side * flare * Mth.RAD_TO_DEG);
		foreArm.rotation.setSmoothness(SMOOTHNESS).orientX(-bend * Mth.RAD_TO_DEG);
	}
}
