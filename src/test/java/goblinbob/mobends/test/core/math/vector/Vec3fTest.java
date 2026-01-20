package goblinbob.mobends.test.core.math.vector;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.core.math.vector.VectorUtils;

public class Vec3fTest
{
    @Test
    public void lengthSqTest()
    {
        Vec3f vec = new Vec3f(1, 1, 0);
        assertEquals(2, vec.lengthSq(), 0.001F);
    }

    @Test
    public void lengthTest()
    {
        Vec3f vec = new Vec3f(1, 1, 0);
        assertEquals(Math.sqrt(2), vec.length(), 0.001F);
    }

    @Test
    public void normalizeTest()
    {
        Vec3f vec = new Vec3f(1, 0, 1);
        VectorUtils.normalize(vec);
        assertEquals(1 / Math.sqrt(2), vec.x, 0.001F);
        assertEquals(0, vec.y, 0.001F);
        assertEquals(1 / Math.sqrt(2), vec.z, 0.001F);
    }
}
