package goblinbob.mobends.lib.math.vector;

public interface IVec3fRead
{

    float getX();

    float getY();

    float getZ();

    float lengthSq();

    default float length()
    {
        return (float) Math.sqrt(this.lengthSq());
    }

}
