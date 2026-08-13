package goblinbob.mobends.lib.math;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class QuaternionTest
{
    private static final float EPSILON = 0.001f;

    @Test
    public void constructorDefault()
    {
        Quaternion q = new Quaternion();
        assertEquals(0, q.x, EPSILON);
        assertEquals(0, q.y, EPSILON);
        assertEquals(0, q.z, EPSILON);
        assertEquals(1, q.w, EPSILON);
    }

    @Test
    public void constructorWithValues()
    {
        Quaternion q = new Quaternion(0.1f, 0.2f, 0.3f, 0.9f);
        assertEquals(0.1f, q.x, EPSILON);
        assertEquals(0.2f, q.y, EPSILON);
        assertEquals(0.3f, q.z, EPSILON);
        assertEquals(0.9f, q.w, EPSILON);
    }

    @Test
    public void identityQuaternion()
    {
        Quaternion q = new Quaternion();
        float length = (float) Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w);
        assertEquals(1, length, EPSILON);
    }

    @Test
    public void setIdentity()
    {
        Quaternion q = new Quaternion(1, 2, 3, 4);
        q.setIdentity();
        assertEquals(0, q.x, EPSILON);
        assertEquals(0, q.y, EPSILON);
        assertEquals(0, q.z, EPSILON);
        assertEquals(1, q.w, EPSILON);
    }

    @Test
    public void copyFrom()
    {
        Quaternion source = new Quaternion(0.1f, 0.2f, 0.3f, 0.9f);
        Quaternion target = new Quaternion();
        target.set(source);
        assertEquals(source.x, target.x, EPSILON);
        assertEquals(source.y, target.y, EPSILON);
        assertEquals(source.z, target.z, EPSILON);
        assertEquals(source.w, target.w, EPSILON);
    }

    @Test
    public void multiplyIdentity()
    {
        Quaternion q = new Quaternion(0.1f, 0.2f, 0.3f, 0.9f);
        Quaternion identity = new Quaternion();
        Quaternion result = new Quaternion();

        Quaternion.mul(q, identity, result);
        assertEquals(q.x, result.x, EPSILON);
        assertEquals(q.y, result.y, EPSILON);
        assertEquals(q.z, result.z, EPSILON);
        assertEquals(q.w, result.w, EPSILON);
    }

    @Test
    public void fromAxisAngle()
    {
        Quaternion q = new Quaternion();
        q.setFromAxisAngle(0, 1, 0, (float)(Math.PI / 2));

        assertEquals(0, q.x, EPSILON);
        assertEquals(0.707f, q.y, 0.01f);
        assertEquals(0, q.z, EPSILON);
        assertEquals(0.707f, q.w, 0.01f);
    }
}
