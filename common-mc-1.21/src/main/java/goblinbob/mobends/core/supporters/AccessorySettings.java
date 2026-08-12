package goblinbob.mobends.core.supporters;

import goblinbob.mobends.core.util.Color;

public class AccessorySettings
{
    public static final AccessorySettings DEFAULT = new AccessorySettings();

    private final boolean unlocked;
    private final boolean hidden;
    private final Color color;

    public AccessorySettings()
    {
        this.unlocked = false;
        this.hidden = true;
        this.color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public AccessorySettings(boolean unlocked, boolean hidden, Color color)
    {
        this.unlocked = unlocked;
        this.hidden = hidden;
        this.color = color;
    }

    public boolean isUnlocked()
    {
        return unlocked;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public Color getColor()
    {
        return color;
    }
}
