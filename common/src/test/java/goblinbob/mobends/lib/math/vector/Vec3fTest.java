package goblinbob.mobends.lib.math.vector;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class Vec3fTest
{
    private static final float EPSILON = 0.001f;

    @Test
    public void constructorDefault()
    {
        Vec3f vec = new Vec3f();
        assertEquals(0, vec.x, EPSILON);
        assertEquals(0, vec.y, EPSILON);
        assertEquals(0, vec.z, EPSILON);
    }

    @Test
    public void constructorWithValues()
    {
        Vec3f vec = new Vec3f(1.5f, 2.5f, 3.5f);
        assertEquals(1.5f, vec.x, EPSILON);
        assertEquals(2.5f, vec.y, EPSILON);
        assertEquals(3.5f, vec.z, EPSILON);
    }

    @Test
    public void lengthSqTest()
    {
        Vec3f vec = new Vec3f(1, 1, 0);
        assertEquals(2, vec.lengthSq(), EPSILON);
    }

    @Test
    public void lengthTest()
    {
        Vec3f vec = new Vec3f(1, 1, 0);
        assertEquals((float) Math.sqrt(2), vec.length(), EPSILON);
    }

    @Test
    public void lengthUnitVector()
    {
        Vec3f vec = new Vec3f(1, 0, 0);
        assertEquals(1, vec.length(), EPSILON);
    }

    @Test
    public void normalizeTest()
    {
        Vec3f vec = new Vec3f(1, 0, 1);
        VectorUtils.normalize(vec);
        float expected = (float) (1 / Math.sqrt(2));
        assertEquals(expected, vec.x, EPSILON);
        assertEquals(0, vec.y, EPSILON);
        assertEquals(expected, vec.z, EPSILON);
    }

    @Test
    public void setValues()
    {
        Vec3f vec = new Vec3f();
        vec.set(5f, 10f, 15f);
        assertEquals(5f, vec.x, EPSILON);
        assertEquals(10f, vec.y, EPSILON);
        assertEquals(15f, vec.z, EPSILON);
    }

    @Test
    public void copyFrom()
    {
        Vec3f source = new Vec3f(1, 2, 3);
        Vec3f target = new Vec3f();
        target.set(source);
        assertEquals(source.x, target.x, EPSILON);
        assertEquals(source.y, target.y, EPSILON);
        assertEquals(source.z, target.z, EPSILON);
    }
}
