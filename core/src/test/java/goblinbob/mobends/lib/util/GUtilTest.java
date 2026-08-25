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
    public void twoNearlyIdenticalAnglesStraddlingPiReportATinyDeviation()
    {
        // Regression guard for the real symptom. The old `% Math.PI` only diverged when the two
        // angles landed on OPPOSITE sides of a multiple of PI -- and then it was off by exactly
        // PI, so two limbs a hundredth of a radian apart reported a deviation of ~3.13 and
        // tripped the spider's 0.9 re-plant threshold on essentially every frame.
        //
        // An earlier version of this test used PI+1.3 against PI+1.25, which sit on the SAME side
        // and therefore passed with the bug fully reintroduced. Straddling is the whole point.
        double justAbove = Math.PI + 0.02;
        double justBelow = Math.PI - 0.02;

        assertEquals(0.04, GUtil.getRadianDifference(justAbove, justBelow), EPSILON);
        assertTrue(GUtil.getRadianDifference(justAbove, justBelow) < 0.9,
                "a limb sitting almost exactly at its neutral yaw must not read as deviated");
    }

    @Test
    public void deviationIsSmallOnBothSidesOfEverySpiderNeutralYaw()
    {
        // The spider's eight limbs sit at these neutral yaws; the even-index ones exceed PI,
        // which is exactly why the fold was wrong for them.
        double[] neutralYaws = { -0.929, -0.186, 0.557, 1.3, Math.PI + 1.3, Math.PI + 0.557, 2.956, 2.213 };

        for (double neutral : neutralYaws)
        {
            for (double delta : new double[] { -0.02, 0.02 })
            {
                double d = GUtil.getRadianDifference(neutral, neutral + delta);
                assertEquals(Math.abs(delta), d, EPSILON,
                        "neutral=" + neutral + " delta=" + delta + " reported " + d);
            }
        }
    }
}
