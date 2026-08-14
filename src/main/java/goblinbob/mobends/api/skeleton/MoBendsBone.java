package goblinbob.mobends.api.skeleton;

public enum MoBendsBone
{
    HEAD("head"),
    BODY("body"),
    LEFT_ARM("left_arm"),
    LEFT_FORE_ARM("left_fore_arm"),
    RIGHT_ARM("right_arm"),
    RIGHT_FORE_ARM("right_fore_arm"),
    LEFT_LEG("left_leg"),
    LEFT_FORE_LEG("left_fore_leg"),
    RIGHT_LEG("right_leg"),
    RIGHT_FORE_LEG("right_fore_leg");

    private final String boneName;

    MoBendsBone(String boneName)
    {
        this.boneName = boneName;
    }

    public String boneName()
    {
        return this.boneName;
    }

    public static MoBendsBone byName(String name)
    {
        for (final MoBendsBone bone : values())
        {
            if (bone.boneName.equals(name))
            {
                return bone;
            }
        }
        return null;
    }

    public MoBendsBone parent()
    {
        switch (this)
        {
            case HEAD:
            case LEFT_ARM:
            case RIGHT_ARM:
                return BODY;
            case LEFT_FORE_ARM:
                return LEFT_ARM;
            case RIGHT_FORE_ARM:
                return RIGHT_ARM;
            case LEFT_FORE_LEG:
                return LEFT_LEG;
            case RIGHT_FORE_LEG:
                return RIGHT_LEG;
            default:
                return null;
        }
    }

    public boolean isForeLimb()
    {
        return this == LEFT_FORE_ARM || this == RIGHT_FORE_ARM
                || this == LEFT_FORE_LEG || this == RIGHT_FORE_LEG;
    }
}
