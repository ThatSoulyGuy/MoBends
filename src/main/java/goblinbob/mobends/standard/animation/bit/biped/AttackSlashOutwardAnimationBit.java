package goblinbob.mobends.standard.animation.bit.biped;

public class AttackSlashOutwardAnimationBit extends AttackSlashSideAnimationBit
{
	@Override
	protected float armZ(float armSwing)
	{
		return 70F + armSwing * 40F;
	}
}
