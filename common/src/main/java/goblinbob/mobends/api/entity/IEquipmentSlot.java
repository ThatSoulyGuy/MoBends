package goblinbob.mobends.api.entity;

/**
 * Platform-agnostic equipment slot enumeration.
 */
public enum IEquipmentSlot
{
    MAINHAND,
    OFFHAND,
    FEET,
    LEGS,
    CHEST,
    HEAD;

    /**
     * @return Whether this slot is a hand slot (mainhand or offhand)
     */
    public boolean isHand()
    {
        return this == MAINHAND || this == OFFHAND;
    }

    /**
     * @return Whether this slot is an armor slot
     */
    public boolean isArmor()
    {
        return this == FEET || this == LEGS || this == CHEST || this == HEAD;
    }
}
