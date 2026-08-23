package goblinbob.mobends.core.expression;

import static org.junit.jupiter.api.Assertions.*;

import goblinbob.mobends.core.expression.ast.ExpressionNode;
import goblinbob.mobends.core.expression.ast.LiteralNode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Covers the expression language used by procedural Kumo layers and {@code core:expression}
 * trigger conditions.
 *
 * <p>Nothing Mo' Bends ships reaches this engine — it is only exercised by user-authored packs
 * in {@code <gamedir>/bendspacks} — so playing the game proves nothing about it. These tests are
 * the only verification it has.
 */
public class ExpressionTest
{
    private static final double EPSILON = 1e-9;

    /** Records which variables were actually read, so laziness can be asserted. */
    private static final class RecordingContext implements ExpressionContext
    {
        private final Map<String, Double> values = new HashMap<>();
        private final Map<String, Integer> reads = new HashMap<>();

        RecordingContext set(String name, double value)
        {
            values.put(name, value);
            return this;
        }

        int readCount(String name)
        {
            return reads.getOrDefault(name, 0);
        }

        @Override
        public double getVariable(String name)
        {
            reads.merge(name, 1, Integer::sum);
            Double v = values.get(name);
            if (v == null) throw new ExpressionException("Unknown variable: " + name, name, 0);
            return v;
        }

        @Override
        public boolean hasVariable(String name)
        {
            return values.containsKey(name);
        }
    }

    private static double eval(String source, ExpressionContext context)
    {
        return new ExpressionParser(source).parseAndOptimize().evaluate(context);
    }

    private static double eval(String source)
    {
        return eval(source, new RecordingContext());
    }

    // ---------- tokenizer and parser ----------

    @Test
    public void parsesLiteralsAndArithmetic()
    {
        assertEquals(3.0, eval("1 + 2"), EPSILON);
        assertEquals(-1.0, eval("1 - 2"), EPSILON);
        assertEquals(6.0, eval("2 * 3"), EPSILON);
        assertEquals(2.5, eval("5 / 2"), EPSILON);
        assertEquals(1.0, eval("5 % 2"), EPSILON);
        assertEquals(8.0, eval("2 ^ 3"), EPSILON);
        assertEquals(0.5, eval("0.5"), EPSILON);
    }

    @Test
    public void honoursPrecedenceAndParentheses()
    {
        assertEquals(7.0, eval("1 + 2 * 3"), EPSILON);
        assertEquals(9.0, eval("(1 + 2) * 3"), EPSILON);
        assertEquals(-5.0, eval("-(2 + 3)"), EPSILON);
        assertEquals(1.0, eval("2 - 2 + 1"), EPSILON);
    }

    @Test
    public void readsVariablesFromTheContext()
    {
        RecordingContext ctx = new RecordingContext().set("limb_swing", 0.25);
        assertEquals(0.5, eval("limb_swing * 2", ctx), EPSILON);
        assertEquals(1, ctx.readCount("limb_swing"));
    }

    @Test
    public void rejectsTrailingGarbage()
    {
        assertThrows(ExpressionException.class, () -> eval("1 + 2 3"));
    }

    // ---------- comparison and logic ----------

    @Test
    public void comparisonsYieldOneOrZero()
    {
        assertEquals(1.0, eval("2 > 1"), EPSILON);
        assertEquals(0.0, eval("2 < 1"), EPSILON);
        assertEquals(1.0, eval("2 >= 2"), EPSILON);
        assertEquals(1.0, eval("2 <= 2"), EPSILON);
        assertEquals(1.0, eval("2 == 2"), EPSILON);
        assertEquals(1.0, eval("2 != 3"), EPSILON);
    }

    @Test
    public void andShortCircuitsOnAFalseLeftOperand()
    {
        RecordingContext ctx = new RecordingContext().set("a", 0.0).set("b", 1.0);
        assertEquals(0.0, eval("a && b", ctx), EPSILON);
        assertEquals(0, ctx.readCount("b"), "&& evaluated its right operand despite a false left");
    }

    @Test
    public void orShortCircuitsOnATrueLeftOperand()
    {
        RecordingContext ctx = new RecordingContext().set("a", 1.0).set("b", 0.0);
        assertEquals(1.0, eval("a || b", ctx), EPSILON);
        assertEquals(0, ctx.readCount("b"), "|| evaluated its right operand despite a true left");
    }

    @Test
    public void ternaryEvaluatesOnlyTheTakenBranch()
    {
        RecordingContext ctx = new RecordingContext().set("c", 1.0).set("taken", 5.0).set("other", 9.0);
        assertEquals(5.0, eval("c ? taken : other", ctx), EPSILON);
        assertEquals(1, ctx.readCount("taken"));
        assertEquals(0, ctx.readCount("other"), "ternary evaluated the branch it did not take");
    }

    // ---------- constant folding ----------

    @Test
    public void foldsConstantSubtreesToLiterals()
    {
        ExpressionNode folded = new ExpressionParser("2 * 3 + 4").parseAndOptimize();
        assertInstanceOf(LiteralNode.class, folded, "a wholly constant expression should fold to one literal");
        assertEquals(10.0, folded.evaluate(null), EPSILON);
    }

    @Test
    public void doesNotFoldSubtreesThatReadVariables()
    {
        ExpressionNode folded = new ExpressionParser("x * 3").parseAndOptimize();
        assertFalse(folded instanceof LiteralNode, "an expression reading a variable must not fold");
        assertFalse(folded.isConstant());
    }

    @Test
    public void doesNotFoldImpureFunctions()
    {
        ExpressionNode folded = new ExpressionParser("random()").parseAndOptimize();
        assertFalse(folded instanceof LiteralNode, "random() must not be folded to a constant");
    }

    // ---------- functions ----------

    @Test
    public void mathFunctionsBehaveAsExpected()
    {
        assertEquals(1.0, eval("abs(-1)"), EPSILON);
        assertEquals(2.0, eval("floor(2.7)"), EPSILON);
        assertEquals(3.0, eval("ceil(2.1)"), EPSILON);
        assertEquals(3.0, eval("round(2.6)"), EPSILON);
        assertEquals(3.0, eval("sqrt(9)"), EPSILON);
        assertEquals(8.0, eval("pow(2, 3)"), EPSILON);
        assertEquals(0.0, eval("sin(0)"), EPSILON);
        assertEquals(1.0, eval("cos(0)"), EPSILON);
        assertEquals(Math.PI, eval("degToRad(180)"), EPSILON);
        assertEquals(180.0, eval("radToDeg(" + Math.PI + ")"), EPSILON);
    }

    @Test
    public void minMaxAcceptVariableArity()
    {
        assertEquals(1.0, eval("min(3, 1, 2)"), EPSILON);
        assertEquals(3.0, eval("max(3, 1, 2)"), EPSILON);
    }

    @Test
    public void clampOrdersItsArgumentsValueLowHigh()
    {
        assertEquals(5.0, eval("clamp(10, 0, 5)"), EPSILON);
        assertEquals(0.0, eval("clamp(-10, 0, 5)"), EPSILON);
        assertEquals(2.0, eval("clamp(2, 0, 5)"), EPSILON);
    }

    @Test
    public void stepTakesItsEdgeFirst()
    {
        // step(edge, x) returns 1 when x >= edge. The argument order is the opposite way round
        // from clamp's, which is a genuine trap when authoring packs.
        assertEquals(1.0, eval("step(1, 2)"), EPSILON);
        assertEquals(0.0, eval("step(2, 1)"), EPSILON);
    }

    @Test
    public void lerpAndMixAgreeDespiteDifferentFormulations()
    {
        // Deliberate GLSL-style aliases: lerp is a + (b - a) * t, mix is a(1 - t) + b*t.
        for (double t = 0.0; t <= 1.0; t += 0.125)
        {
            assertEquals(eval("lerp(2, 10, " + t + ")"), eval("mix(2, 10, " + t + ")"), 1e-12,
                    "lerp and mix disagreed at t=" + t);
        }
    }

    @Test
    public void unknownFunctionIsRejected()
    {
        assertThrows(ExpressionException.class, () -> eval("nosuchfunction(1)"));
    }

    @Test
    public void wrongArityIsRejected()
    {
        assertThrows(ExpressionException.class, () -> eval("sin(1, 2)"));
    }

    // ---------- division by zero ----------

    @Test
    public void divisionAndModuloByZeroYieldZeroRatherThanNaN()
    {
        // Deliberate: these values feed bone transforms, and a NaN there silently corrupts a
        // whole skeleton for the rest of the session. Returning 0 keeps a bad pack survivable.
        assertEquals(0.0, eval("1 / 0"), EPSILON);
        assertEquals(0.0, eval("1 % 0"), EPSILON);
        assertEquals(0.0, eval("mod(1, 0)"), EPSILON);
    }

    @Test
    public void noOperatorEverProducesNaNOrInfinityFromFiniteOperands()
    {
        RecordingContext ctx = new RecordingContext().set("z", 0.0).set("n", 5.0);
        for (String source : new String[] { "n / z", "n % z", "mod(n, z)", "z / z" })
        {
            double result = eval(source, ctx);
            assertTrue(Double.isFinite(result), source + " produced " + result);
        }
    }

    // ---------- caching ----------

    @Test
    public void cacheReturnsTheSameCompiledExpression()
    {
        ExpressionCache cache = ExpressionCache.getInstance();
        cache.remove("1 + 1");
        Expression first = cache.get("1 + 1");
        Expression second = cache.get("1 + 1");
        assertSame(first, second);
        assertEquals(2.0, first.evaluate(null), EPSILON);
        cache.remove("1 + 1");
    }

    @Test
    public void evaluateBooleanTreatsNonZeroAsTrue()
    {
        ExpressionCache cache = ExpressionCache.getInstance();
        assertTrue(cache.compile("2 > 1").evaluateBoolean(null));
        assertFalse(cache.compile("2 < 1").evaluateBoolean(null));
    }
}
