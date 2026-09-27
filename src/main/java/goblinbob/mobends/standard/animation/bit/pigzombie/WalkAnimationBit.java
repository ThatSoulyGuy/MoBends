package goblinbob.mobends.standard.animation.bit.pigzombie;

import goblinbob.mobends.standard.data.PigZombieData;
import net.minecraft.util.Mth;

public class WalkAnimationBit extends goblinbob.mobends.standard.animation.bit.biped.WalkAnimationBit<PigZombieData>
{
	@Override
	public void perform(PigZombieData data)
	{
		super.perform(data);
		StandAnimationBit.hunch(data);

		float limbSwing = data.limbSwing.get() * 0.6662F;
		data.globalOffset.slideY(Math.abs(Mth.sin(limbSwing)) * -1.4F - 3F);
	}
}
