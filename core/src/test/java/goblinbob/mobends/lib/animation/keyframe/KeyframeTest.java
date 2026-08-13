package goblinbob.mobends.lib.animation.keyframe;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class KeyframeTest
{
    private static final float EPSILON = 0.001f;

    private Keyframe createKeyframe()
    {
        Keyframe kf = new Keyframe();
        kf.position = new float[3];
        kf.rotation = new float[] { 0, 0, 0, 1 };
        kf.scale = new float[] { 1, 1, 1 };
        return kf;
    }

    @Test
    public void fieldsAreNullByDefault()
    {
        Keyframe kf = new Keyframe();
        assertNull(kf.position);
        assertNull(kf.rotation);
        assertNull(kf.scale);
    }

    @Test
    public void setPosition()
    {
        Keyframe kf = createKeyframe();
        kf.position[0] = 1.0f;
        kf.position[1] = 2.0f;
        kf.position[2] = 3.0f;

        assertEquals(1.0f, kf.position[0], EPSILON);
        assertEquals(2.0f, kf.position[1], EPSILON);
        assertEquals(3.0f, kf.position[2], EPSILON);
    }

    @Test
    public void setRotation()
    {
        Keyframe kf = createKeyframe();
        kf.rotation[0] = 0.0f;
        kf.rotation[1] = 0.707f;
        kf.rotation[2] = 0.0f;
        kf.rotation[3] = 0.707f;

        assertEquals(0.0f, kf.rotation[0], EPSILON);
        assertEquals(0.707f, kf.rotation[1], EPSILON);
        assertEquals(0.0f, kf.rotation[2], EPSILON);
        assertEquals(0.707f, kf.rotation[3], EPSILON);
    }

    @Test
    public void setScale()
    {
        Keyframe kf = createKeyframe();
        kf.scale[0] = 2.0f;
        kf.scale[1] = 2.0f;
        kf.scale[2] = 2.0f;

        assertEquals(2.0f, kf.scale[0], EPSILON);
        assertEquals(2.0f, kf.scale[1], EPSILON);
        assertEquals(2.0f, kf.scale[2], EPSILON);
    }

    @Test
    public void mirrorRotationYZ()
    {
        Keyframe kf = createKeyframe();
        kf.rotation[1] = 0.5f;
        kf.rotation[2] = 0.3f;

        kf.mirrorRotationYZ();

        assertEquals(-0.5f, kf.rotation[1], EPSILON);
        assertEquals(-0.3f, kf.rotation[2], EPSILON);
    }

    @Test
    public void swapRotationYZ()
    {
        Keyframe kf = createKeyframe();
        kf.rotation[1] = 0.5f;
        kf.rotation[2] = 0.3f;

        kf.swapRotationYZ();

        assertEquals(0.3f, kf.rotation[1], EPSILON);
        assertEquals(0.5f, kf.rotation[2], EPSILON);
    }
}
