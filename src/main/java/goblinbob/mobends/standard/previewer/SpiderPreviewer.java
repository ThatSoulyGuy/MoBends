package goblinbob.mobends.standard.previewer;

import goblinbob.mobends.core.bender.BoneMetadata;
import goblinbob.mobends.core.bender.IPreviewer;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.SpiderData;
import net.minecraft.world.entity.monster.Spider;

import java.util.Map;

public class SpiderPreviewer implements IPreviewer<SpiderData>
{

	protected double previewYOffset = 0;

	@Override
	public void prePreview(SpiderData data, String animationToPreview)
	{
		data.limbSwingAmount.override(0F);
		previewYOffset = 0;

		switch (animationToPreview)
		{
			case "jump":
				{
					final float ticks = DataUpdateHandler.getTicks();

					final float JUMP_DURATION = 10;
					final float WAIT_DURATION = 10;
					final float TOTAL_DURATION = JUMP_DURATION + WAIT_DURATION;
					float t = ticks % TOTAL_DURATION;

					if (t <= JUMP_DURATION)
					{
						data.overrideOnGroundState(false);

						previewYOffset = Math.sin(t/JUMP_DURATION * Math.PI) * 1.5;
					} else {
						data.overrideOnGroundState(true);
					}

					data.limbSwingAmount.override(0F);
					data.overrideStillness(true);
				}
				break;
			case "walk":
			case "move":
				{
					final float ticks = DataUpdateHandler.getTicks();

					Spider entity = data.getEntity();
					if (entity != null)
					{
						entity.noPhysics = true;
					}
					data.limbSwing.override(ticks * 0.6F);
					data.overrideOnGroundState(true);
					data.limbSwingAmount.override(1F);
					data.overrideStillness(false);
				}
				break;
			case "climb":
				{
					final float ticks = DataUpdateHandler.getTicks();

					data.setClimbing(true);
					data.overrideOnGroundState(false);
					data.limbSwing.override(ticks * 0.5F);
					data.limbSwingAmount.override(0.7F);
					data.overrideStillness(false);
				}
				break;
			default:
				data.overrideOnGroundState(true);
				data.overrideStillness(true);
		}
	}

	@Override
	public void postPreview(SpiderData data, String animationToPreview)
	{
		if ("climb".equals(animationToPreview))
		{
			data.setClimbing(false);
		}
	}

	@Override
	public Map<String, BoneMetadata> getBoneMetadata()
	{
		return null;
	}

	public double getPreviewYOffset()
	{
		return previewYOffset;
	}

}
