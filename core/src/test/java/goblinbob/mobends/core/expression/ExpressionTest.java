package goblinbob.mobends.core.expression;

import static org.junit.jupiter.api.Assertions.*;

import goblinbob.mobends.core.expression.ast.ExpressionNode;
import goblinbob.mobends.core.expression.ast.LiteralNode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ExpressionTest
{
    private static final double EPSILON = 1e-9;

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
        assertEquals(1.0, eval("step(1, 2)"), EPSILON);
        assertEquals(0.0, eval("step(2, 1)"), EPSILON);
    }

    @Test
    public void lerpAndMixAgreeDespiteDifferentFormulations()
    {
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


    @Test
    public void divisionAndModuloByZeroYieldZeroRatherThanNaN()
    {
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


    @Test
    public void ifEvaluatesOnlyTheTakenBranch()
    {
        RecordingContext ctx = new RecordingContext().set("c", 1.0).set("taken", 5.0).set("other", 9.0);
        assertEquals(5.0, eval("if(c, taken, other)", ctx), EPSILON);
        assertEquals(1, ctx.readCount("taken"));
        assertEquals(0, ctx.readCount("other"), "if() evaluated the branch it did not take");
    }

    @Test
    public void ifIsLoweredToATernaryNode()
    {
        ExpressionNode node = new ExpressionParser("if(x, 1, 2)").parse();
        assertInstanceOf(goblinbob.mobends.core.expression.ast.TernaryNode.class, node);
    }

    @Test
    public void ifAgreesWithTheTernaryOperator()
    {
        RecordingContext ctx = new RecordingContext().set("a", 3.0).set("b", 4.0);
        for (double c : new double[] { 0.0, 1.0, -1.0, 0.5 })
        {
            ctx.set("c", c);
            assertEquals(eval("c ? a : b", ctx), eval("if(c, a, b)", ctx), EPSILON, "disagreed at c=" + c);
        }
    }

    @Test
    public void ifGuardsItsOwnBranchAgainstDivisionByZero()
    {
        RecordingContext ctx = new RecordingContext().set("len", 0.0).set("x", 10.0);
        assertEquals(0.0, eval("if(len > 0, x / len, 0)", ctx), EPSILON);
        assertEquals(0, ctx.readCount("x"), "the guarded branch was evaluated anyway");
    }

    @Test
    public void ifStillRejectsWrongArity()
    {
        assertThrows(ExpressionException.class, () -> eval("if(1, 2)"));
        assertThrows(ExpressionException.class, () -> eval("if(1, 2, 3, 4)"));
    }


    @Test
    public void constantFoldingContextRefusesVariableReads()
    {
        ExpressionException thrown = assertThrows(ExpressionException.class,
                () -> ExpressionContext.CONSTANT_FOLDING.getVariable("limb_swing"));
        assertTrue(thrown.getMessage().contains("limb_swing"),
                "the folding error should name the variable, got: " + thrown.getMessage());
        assertFalse(ExpressionContext.CONSTANT_FOLDING.hasVariable("limb_swing"));
    }


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
