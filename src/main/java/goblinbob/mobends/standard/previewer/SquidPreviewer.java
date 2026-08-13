package goblinbob.mobends.standard.previewer;

import goblinbob.mobends.core.bender.BoneMetadata;
import goblinbob.mobends.core.bender.IPreviewer;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.SquidData;
import net.minecraft.world.entity.animal.Squid;

import java.util.Map;

public class SquidPreviewer implements IPreviewer<SquidData>
{

	private static final float PUMP_DURATION = 20.0F;

	@Override
	public void prePreview(SquidData data, String animationToPreview)
	{
		final Squid squid = data.getEntity();

		data.overrideOnGroundState(false);

		switch (animationToPreview)
		{
			case "swim":
			case "move":
				prepareForSwim(data, squid);
				break;
			default:
				prepareForDefault(data, squid);
		}
	}

	private void prepareForSwim(SquidData data, Squid squid)
	{
		final float ticks = DataUpdateHandler.getTicks();
		final float phase = (ticks % PUMP_DURATION) / PUMP_DURATION * (float) Math.PI;

		if (squid != null)
		{
			squid.oldTentacleMovement = phase;
			squid.tentacleMovement = phase;
		}

		data.overrideStillness(false);
	}

	private void prepareForDefault(SquidData data, Squid squid)
	{
		if (squid != null)
		{
			squid.oldTentacleMovement = 0.0F;
			squid.tentacleMovement = 0.0F;
		}

		data.overrideStillness(true);
	}

	@Override
	public void postPreview(SquidData data, String animationToPreview)
	{
	}

	@Override
	public Map<String, BoneMetadata> getBoneMetadata()
	{
		return null;
	}

}
