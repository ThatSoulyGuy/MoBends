package goblinbob.mobends.standard.previewer;

import goblinbob.mobends.core.bender.IPreviewer;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.SpiderData;
import net.minecraft.world.entity.monster.Spider;

public class SpiderPreviewer implements IPreviewer<SpiderData>
{

	@Override
	public void prePreview(SpiderData data, String animationToPreview)
	{
		data.limbSwingAmount.override(0F);

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
			default:
				data.overrideOnGroundState(true);
				data.overrideStillness(true);
		}
	}
}
