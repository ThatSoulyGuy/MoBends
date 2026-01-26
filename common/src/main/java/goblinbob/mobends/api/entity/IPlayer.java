package goblinbob.mobends.api.entity;

import java.util.UUID;

/**
 * Platform-agnostic player entity abstraction.
 * Extends ILivingEntity with properties specific to players.
 */
public interface IPlayer extends ILivingEntity
{
    /**
     * @return The player's UUID
     */
    UUID getUUID();

    /**
     * @return The player's display name
     */
    String getName();

    /**
     * @return Whether the player model should use slim arms (Alex model)
     */
    boolean hasSlimArms();

    /**
     * @return Whether the player is currently flying (creative/spectator mode)
     */
    boolean isFlying();

    /**
     * @return Whether the player is in creative mode
     */
    boolean isCreative();

    /**
     * @return Whether the player is in spectator mode
     */
    boolean isSpectator();

    /**
     * @return Whether the player is currently sprinting
     */
    boolean isSprinting();

    /**
     * @return Whether the player is blocking with a shield
     */
    boolean isBlocking();

    /**
     * @return Whether the player is currently climbing (on a ladder or vine)
     */
    boolean isClimbing();

    /**
     * @return The player's food level (0-20)
     */
    int getFoodLevel();

    /**
     * @return The player's experience level
     */
    int getExperienceLevel();

    /**
     * @return Whether this is the local client player
     */
    boolean isLocalPlayer();

    /**
     * @return The current tick count for cape animation
     */
    int getCapeAnimationTick();

    /**
     * @return The previous cape X position (for animation)
     */
    double getPrevCapeX();

    /**
     * @return The previous cape Y position (for animation)
     */
    double getPrevCapeY();

    /**
     * @return The previous cape Z position (for animation)
     */
    double getPrevCapeZ();

    /**
     * @return The current cape X position
     */
    double getCapeX();

    /**
     * @return The current cape Y position
     */
    double getCapeY();

    /**
     * @return The current cape Z position
     */
    double getCapeZ();
}
