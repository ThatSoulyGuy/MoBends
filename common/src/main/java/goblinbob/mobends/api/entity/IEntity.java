package goblinbob.mobends.api.entity;

public interface IEntity
{
    int getId();

    double getX();

    double getY();

    double getZ();

    double getPrevX();

    double getPrevY();

    double getPrevZ();

    float getYRot();

    float getXRot();

    float getPrevYRot();

    float getPrevXRot();

    boolean isOnGround();

    boolean isInWater();

    boolean isPassenger();

    float getBbWidth();

    float getBbHeight();

    Object getNative();

    default double getLerpedX(float partialTicks)
    {
        return getPrevX() + (getX() - getPrevX()) * partialTicks;
    }

    default double getLerpedY(float partialTicks)
    {
        return getPrevY() + (getY() - getPrevY()) * partialTicks;
    }

    default double getLerpedZ(float partialTicks)
    {
        return getPrevZ() + (getZ() - getPrevZ()) * partialTicks;
    }
}
