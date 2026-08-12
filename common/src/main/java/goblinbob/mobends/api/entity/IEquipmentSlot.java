package goblinbob.mobends.api.entity;

public enum IEquipmentSlot
{
    MAINHAND,
    OFFHAND,
    FEET,
    LEGS,
    CHEST,
    HEAD;

    public boolean isHand()
    {
        return this == MAINHAND || this == OFFHAND;
    }

    public boolean isArmor()
    {
        return this == FEET || this == LEGS || this == CHEST || this == HEAD;
    }
}
