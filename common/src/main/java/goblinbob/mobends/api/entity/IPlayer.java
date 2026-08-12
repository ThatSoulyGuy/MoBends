package goblinbob.mobends.api.entity;

import java.util.UUID;

public interface IPlayer extends ILivingEntity
{
    UUID getUUID();

    String getName();

    boolean hasSlimArms();

    boolean isFlying();

    boolean isCreative();

    boolean isSpectator();

    boolean isSprinting();

    boolean isBlocking();

    boolean isClimbing();

    int getFoodLevel();

    int getExperienceLevel();

    boolean isLocalPlayer();

    int getCapeAnimationTick();

    double getPrevCapeX();

    double getPrevCapeY();

    double getPrevCapeZ();

    double getCapeX();

    double getCapeY();

    double getCapeZ();
}
