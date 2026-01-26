package goblinbob.mobends.core.supporters;

import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.PlayerData;

import java.util.function.Function;

/**
 * Defines where an accessory binds to the player model.
 */
public class BindPoint
{
    private final Function<PlayerData, IModelPart> partSelector;

    public BindPoint(Function<PlayerData, IModelPart> partSelector)
    {
        this.partSelector = partSelector;
    }

    public Function<PlayerData, IModelPart> getPartSelector()
    {
        return partSelector;
    }

    // Common bind points
    public static final BindPoint HEAD = new BindPoint(data -> data.head);
    public static final BindPoint BODY = new BindPoint(data -> data.body);
    public static final BindPoint LEFT_ARM = new BindPoint(data -> data.leftArm);
    public static final BindPoint RIGHT_ARM = new BindPoint(data -> data.rightArm);
}
