package goblinbob.mobends.standard.animation.bit.biped;

public class AttackSlashInwardAnimationBit extends AttackSlashAcrossAnimationBit
{
	@Override
	protected float armZ(float armSwing)
	{
		return 90F;
	}

	@Override
	protected float foreArmX()
	{
		return -10F;
	}

	@Override
	protected float itemX()
	{
		return 50.0F;
	}
}
