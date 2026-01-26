package goblinbob.mobends.api.entity;

import goblinbob.mobends.api.resource.IResourcePath;

import javax.annotation.Nullable;

/**
 * Platform-agnostic item stack abstraction.
 */
public interface IItemStack
{
    /**
     * @return Whether this item stack is empty
     */
    boolean isEmpty();

    /**
     * @return The count of items in this stack
     */
    int getCount();

    /**
     * @return The registry ID of this item (e.g., "minecraft:diamond_sword")
     */
    @Nullable
    IResourcePath getItemId();

    /**
     * @return Whether this item is a tool (sword, pickaxe, axe, etc.)
     */
    boolean isTool();

    /**
     * @return Whether this item is a bow
     */
    boolean isBow();

    /**
     * @return Whether this item is a crossbow
     */
    boolean isCrossbow();

    /**
     * @return Whether this item is a shield
     */
    boolean isShield();

    /**
     * @return Whether this item is food
     */
    boolean isFood();

    /**
     * @return Whether this item is a trident
     */
    boolean isTrident();

    /**
     * @return Whether this item is a spyglass
     */
    boolean isSpyglass();

    /**
     * @return The native platform item stack object
     */
    Object getNative();
}
