package goblinbob.mobends.lib.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class GUtilTest
{
    private static final double EPSILON = 0.0001;
    private static final double TAU = Math.PI * 2;

    @Test
    public void wrapRadiansLeavesAnglesAlreadyInRange()
    {
        assertEquals(0.0, GUtil.wrapRadians(0.0), EPSILON);
        assertEquals(1.5, GUtil.wrapRadians(1.5), EPSILON);
        assertEquals(-1.5, GUtil.wrapRadians(-1.5), EPSILON);
        assertEquals(-Math.PI, GUtil.wrapRadians(-Math.PI), EPSILON);
    }

    @Test
    public void wrapRadiansSubtractsAFullTurnNotAHalfTurn()
    {
        // The spider's even-index limbs sit at neutral yaws above PI; these are the values
        // that were previously folded by only PI and so compared against the wrong angle.
        assertEquals(4.442 - TAU, GUtil.wrapRadians(4.442), EPSILON);
        assertEquals(3.699 - TAU, GUtil.wrapRadians(3.699), EPSILON);
        assertEquals(6.0 - TAU, GUtil.wrapRadians(6.0), EPSILON);
    }

    @Test
    public void wrapRadiansOutputAlwaysLiesInRange()
    {
        for (double a = -20.0; a <= 20.0; a += 0.13)
        {
            double w = GUtil.wrapRadians(a);
            assertTrue(w >= -Math.PI && w < Math.PI, "wrapRadians(" + a + ") = " + w + " is out of [-PI, PI)");
        }
    }

    @Test
    public void wrapRadiansPreservesTheAngleModuloAFullTurn()
    {
        for (double a = -20.0; a <= 20.0; a += 0.17)
        {
            double w = GUtil.wrapRadians(a);
            double turns = (a - w) / TAU;
            assertEquals(Math.round(turns), turns, EPSILON, "wrapRadians(" + a + ") shifted by a non-integer number of turns");
        }
    }

    @Test
    public void anglesAHalfTurnApartDoNotCollapse()
    {
        // Folding by PI instead of a full turn made opposite directions compare as identical,
        // which is what made the spider's leg-deviation test meaningless.
        assertNotEquals(GUtil.wrapRadians(0.5), GUtil.wrapRadians(0.5 + Math.PI), EPSILON);
        assertEquals(Math.PI, GUtil.getRadianDifference(0.5, 0.5 + Math.PI), EPSILON);
    }

    @Test
    public void radianDifferenceIsCorrectAcrossThePiBoundary()
    {
        // Both arguments on the same side of PI always worked; straddling it did not.
        assertEquals(0.2, GUtil.getRadianDifference(Math.PI + 0.1, Math.PI - 0.1), EPSILON);
        assertEquals(0.2, GUtil.getRadianDifference(0.1, -0.1), EPSILON);
    }

    @Test
    public void radianDifferenceIsSymmetricAndBounded()
    {
        for (double a = -8.0; a <= 8.0; a += 0.37)
        {
            for (double b = -8.0; b <= 8.0; b += 0.53)
            {
                double d = GUtil.getRadianDifference(a, b);
                assertTrue(d >= 0.0 && d <= Math.PI + EPSILON, "difference " + d + " out of [0, PI]");
                assertEquals(d, GUtil.getRadianDifference(b, a), EPSILON, "not symmetric for " + a + ", " + b);
            }
        }
    }

    @Test
    public void spiderLimbNearNeutralReportsASmallDeviation()
    {
        // Regression guard for the real symptom: limb 0's neutral yaw is PI + 1.3, and a limb
        // pointing almost exactly at it used to report a deviation near PI, tripping the 0.9
        // re-plant threshold on essentially every frame.
        double neutralYaw = Math.PI + 1.3;
        double almostNeutral = neutralYaw - 0.05;
        assertEquals(0.05, GUtil.getRadianDifference(neutralYaw, almostNeutral), EPSILON);
        assertTrue(GUtil.getRadianDifference(neutralYaw, almostNeutral) < 0.9);
    }
}
