package goblinbob.mobends.standard.animation.bit.player;

import goblinbob.mobends.standard.data.BipedEntityData;

public class SprintAnimationBit<T extends BipedEntityData<?>> extends goblinbob.mobends.standard.animation.bit.biped.SprintAnimationBit<T>
{
	@Override
	public void perform(T data)
	{
		super.perform(data);

		if (data.getTicksAfterAttack() < 10)
		{
			data.head.rotation.setSmoothness(0.5F).orientX(data.headPitch.get())
					.rotateY(data.headYaw.get());
		}
	}
}
