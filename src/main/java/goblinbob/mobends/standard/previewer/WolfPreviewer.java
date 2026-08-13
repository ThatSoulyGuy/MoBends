package goblinbob.mobends.standard.previewer;

import goblinbob.mobends.core.bender.BoneMetadata;
import goblinbob.mobends.core.bender.IPreviewer;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.WolfData;
import net.minecraft.world.entity.animal.Wolf;

import java.util.Map;

public class WolfPreviewer implements IPreviewer<WolfData>
{

	@Override
	public void prePreview(WolfData data, String animationToPreview)
	{
		final Wolf wolf = data.getEntity();

		data.headYaw.override(0F);
		data.headPitch.override(0F);
		data.overrideOnGroundState(true);

		switch (animationToPreview)
		{
			case "walk":
			case "move":
				prepareForWalk(data, wolf);
				break;
			case "sit":
				prepareForSit(data, wolf);
				break;
			default:
				prepareForDefault(data, wolf);
		}
	}

	private void prepareForWalk(WolfData data, Wolf wolf)
	{
		final float ticks = DataUpdateHandler.getTicks();

		setSitting(wolf, false);
		data.limbSwing.override(ticks * 0.6F);
		data.limbSwingAmount.override(1F);
		data.overrideStillness(false);
	}

	private void prepareForSit(WolfData data, Wolf wolf)
	{
		setSitting(wolf, true);
		data.limbSwingAmount.override(0F);
		data.overrideStillness(true);
	}

	private void prepareForDefault(WolfData data, Wolf wolf)
	{
		setSitting(wolf, false);
		data.limbSwingAmount.override(0F);
		data.overrideStillness(true);
	}

	private void setSitting(Wolf wolf, boolean sitting)
	{
		if (wolf != null && wolf.isInSittingPose() != sitting)
		{
			wolf.setInSittingPose(sitting);
		}
	}

	@Override
	public void postPreview(WolfData data, String animationToPreview)
	{
	}

	@Override
	public Map<String, BoneMetadata> getBoneMetadata()
	{
		return null;
	}

}
