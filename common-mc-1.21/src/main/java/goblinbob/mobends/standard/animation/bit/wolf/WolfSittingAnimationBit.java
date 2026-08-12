package goblinbob.mobends.standard.animation.bit.wolf;

import goblinbob.mobends.core.animation.bit.KeyframeAnimationBit;
import goblinbob.mobends.platform.VersionAdapter;
import goblinbob.mobends.standard.data.WolfData;
import goblinbob.mobends.standard.main.ModStatics;

public class WolfSittingAnimationBit extends KeyframeAnimationBit<WolfData>
{

    private static final String[] ACTIONS = new String[] { "sitting" };
    private static final String SITTING_ANIMATION_PATH = "bends/animations/wolf_sitting_down.bendsanim";

    public WolfSittingAnimationBit(float animationSpeed)
    {
        super(VersionAdapter.Holder.get().createResourceLocation(ModStatics.MODID, SITTING_ANIMATION_PATH),
                animationSpeed);
    }

    @Override
    public String[] getActions(WolfData entityData)
    {
        return ACTIONS;
    }

    @Override
    public void perform(WolfData data)
    {
        super.perform(data);
    }

}
