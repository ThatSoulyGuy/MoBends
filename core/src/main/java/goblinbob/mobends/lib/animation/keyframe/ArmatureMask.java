package goblinbob.mobends.lib.animation.keyframe;

import java.util.ArrayList;
import java.util.List;

public class ArmatureMask
{
	private Mode mode;
	private List<String> includedParts;
	private List<String> excludedParts;

	public ArmatureMask(Mode mode)
	{
		this.mode = mode;
		this.includedParts = new ArrayList<>();
		this.excludedParts = new ArrayList<>();
	}

	public Mode getMode()
	{
		return this.mode;
	}

	public void include(String bone)
	{
		if (this.includedParts == null) this.includedParts = new ArrayList<>();
		this.includedParts.add(bone);
	}

	private static boolean listContains(List<String> parts, String bone)
	{
		return parts != null && parts.contains(bone);
	}

	public boolean doesAllow(String bone)
	{
		if (this.mode == null)
		{
			return true;
		}

		switch (this.mode)
		{
			case INCLUDE_ONLY:
				return listContains(this.includedParts, bone);
			case EXCLUDE_ONLY:
				return !listContains(this.excludedParts, bone);
			default:
				return true;
		}
	}

	public enum Mode
	{
		INCLUDE_ONLY, EXCLUDE_ONLY
	}
}
