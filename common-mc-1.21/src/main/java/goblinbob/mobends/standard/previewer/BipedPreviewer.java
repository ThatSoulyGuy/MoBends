package goblinbob.mobends.standard.previewer;

import goblinbob.mobends.core.bender.BoneMetadata;
import goblinbob.mobends.core.bender.IPreviewer;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.lib.math.vector.IVec3fRead;
import goblinbob.mobends.lib.math.vector.Vec3fReadonly;
import goblinbob.mobends.standard.data.BipedEntityData;

import java.util.HashMap;
import java.util.Map;

public class BipedPreviewer<D extends BipedEntityData<?>> implements IPreviewer<D>
{

	private static final Vec3fReadonly VIEWPORT_ANCHOR = new Vec3fReadonly(0, 1.3F, 0);

	private static final Map<String, BoneMetadata> BONE_METADATA = new HashMap<String, BoneMetadata>() {{
		put("head", new BoneMetadata(-4F, -8F, -4F, 4F, 0F, 4F));
		put("body", new BoneMetadata(-4.0F, -12.0F, -2.0F, 4F, 0, 2F));
		put("leftArm", new BoneMetadata(-1.0F, -2.0F, -2.0F, 3F, 4F, 2F));
		put("rightArm", new BoneMetadata(-4F + 1F, -2.0F, -2.0F, 1F, 4F, 2F));

	}};

	protected double previewYOffset = 0;

	@Override
	public void prePreview(D data, String animationToPreview)
	{
		data.headYaw.override(0F);
		data.headPitch.override(0F);

		previewYOffset = 0;

		switch (animationToPreview)
		{
			case "walk":
				prepareForWalk(data);
				break;
			case "sprint":
				prepareForSprint(data);
				break;
			case "jump":
				prepareForJump(data);
				break;
			case "fall":
				prepareForFall(data);
				break;
			case "sneak":
				prepareForSneak(data);
				break;
			case "climb":
				prepareForClimb(data);
				break;
			default:
				prepareForDefault(data);
		}
	}

	protected void prepareForWalk(D data)
	{
		final float ticks = DataUpdateHandler.getTicks();

		data.limbSwing.override(ticks * 0.6F);
		data.overrideOnGroundState(true);
		data.limbSwingAmount.override(1F);
		data.overrideStillness(false);
	}

	protected void prepareForJump(D data)
	{
		final float ticks = DataUpdateHandler.getTicks();

		final float JUMP_DURATION = 10;
		final float WAIT_DURATION = 10;
		final float TOTAL_DURATION = JUMP_DURATION + WAIT_DURATION;
		float t = ticks % TOTAL_DURATION;

		if (t <= JUMP_DURATION)
		{
			data.overrideOnGroundState(false);

			previewYOffset = Math.sin(t/JUMP_DURATION * Math.PI) * 0.8;
		} else {
			data.overrideOnGroundState(true);
		}

		data.limbSwingAmount.override(0F);
		data.overrideStillness(true);
	}

	protected void prepareForSprint(D data)
	{
		final float ticks = DataUpdateHandler.getTicks();

		data.limbSwing.override(ticks * 0.9F);
		data.overrideOnGroundState(true);
		data.limbSwingAmount.override(1.5F);
		data.overrideStillness(false);
	}

	protected void prepareForFall(D data)
	{
		data.overrideOnGroundState(false);
		data.limbSwingAmount.override(0F);
		data.overrideStillness(true);
	}

	protected void prepareForSneak(D data)
	{
		final float ticks = DataUpdateHandler.getTicks();

		data.limbSwing.override(ticks * 0.4F);
		data.overrideOnGroundState(true);
		data.limbSwingAmount.override(0.5F);
		data.overrideStillness(false);
	}

	protected void prepareForClimb(D data)
	{
		final float ticks = DataUpdateHandler.getTicks();

		data.setClimbing(true);
		data.overrideOnGroundState(false);
		data.limbSwing.override(ticks * 0.5F);
		data.limbSwingAmount.override(0.7F);
		data.overrideStillness(false);
	}

	protected void prepareForDefault(D data)
	{
		data.overrideOnGroundState(true);
		data.limbSwingAmount.override(0F);
		data.overrideStillness(true);
	}

	@Override
	public void postPreview(D data, String animationToPreview)
	{
		if ("climb".equals(animationToPreview))
		{
			data.setClimbing(false);
		}
	}

	@Override
	public IVec3fRead getAnchorPoint() { return VIEWPORT_ANCHOR; }

	@Override
	public Map<String, BoneMetadata> getBoneMetadata()
	{
		return BONE_METADATA;
	}

	public double getPreviewYOffset()
	{
		return previewYOffset;
	}

}
