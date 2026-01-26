package goblinbob.mobends.core.supporters;

import java.util.Collections;
import java.util.List;

/**
 * Details about an accessory item.
 */
public class AccessoryDetails
{
    private final List<AccessoryPart> parts;

    public AccessoryDetails()
    {
        this.parts = Collections.emptyList();
    }

    public AccessoryDetails(List<AccessoryPart> parts)
    {
        this.parts = parts;
    }

    public List<AccessoryPart> getParts()
    {
        return parts;
    }
}
