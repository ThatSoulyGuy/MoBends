package goblinbob.mobends.standard.animation.bit.biped;

public class AttackSlashUpAnimationBit extends AttackSlashAcrossAnimationBit
{
	@Override
	protected float armZ(float armSwing)
	{
		return 110F * armSwing;
	}

	@Override
	protected float foreArmX()
	{
		return -20F;
	}

	@Override
	protected float itemX()
	{
		return 180F;
	}
}
