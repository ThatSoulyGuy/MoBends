package goblinbob.mobends.core.expression.functions;

import goblinbob.mobends.core.expression.ExpressionException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class FunctionRegistry {
    private static final Map<String, ExpressionFunction> FUNCTIONS = new HashMap<>();
    private static final Random RANDOM = new Random();

    private static final int[] PERM = new int[512];
    static {
        int[] p = {151,160,137,91,90,15,131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
                190,6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,88,237,149,56,87,174,20,125,
                136,171,168,68,175,74,165,71,134,139,48,27,166,77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,
                41,55,46,245,40,244,102,143,54,65,25,63,161,1,216,80,73,209,76,132,187,208,89,18,169,200,196,135,130,
                116,188,159,86,164,100,109,198,173,186,3,64,52,217,226,250,124,123,5,202,38,147,118,126,255,82,85,212,
                207,206,59,227,47,16,58,17,182,189,28,42,223,183,170,213,119,248,152,2,44,154,163,70,221,153,101,155,
                167,43,172,9,129,22,39,253,19,98,108,110,79,113,224,232,178,185,112,104,218,246,97,228,251,34,242,193,
                238,210,144,12,191,179,162,241,81,51,145,235,249,14,239,107,49,192,214,31,181,199,106,157,184,84,204,
                176,115,121,50,45,127,4,150,254,138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180};
        for (int i = 0; i < 256; i++) {
            PERM[i] = p[i];
            PERM[256 + i] = p[i];
        }
    }

    static {
        register("sin", ExpressionFunction.of(1, args -> Math.sin(args[0])));
        register("cos", ExpressionFunction.of(1, args -> Math.cos(args[0])));
        register("tan", ExpressionFunction.of(1, args -> Math.tan(args[0])));
        register("asin", ExpressionFunction.of(1, args -> Math.asin(args[0])));
        register("acos", ExpressionFunction.of(1, args -> Math.acos(args[0])));
        register("atan", ExpressionFunction.of(1, args -> Math.atan(args[0])));
        register("atan2", ExpressionFunction.of(2, args -> Math.atan2(args[0], args[1])));

        register("abs", ExpressionFunction.of(1, args -> Math.abs(args[0])));
        register("floor", ExpressionFunction.of(1, args -> Math.floor(args[0])));
        register("ceil", ExpressionFunction.of(1, args -> Math.ceil(args[0])));
        register("round", ExpressionFunction.of(1, args -> (double) Math.round(args[0])));
        register("sqrt", ExpressionFunction.of(1, args -> Math.sqrt(args[0])));
        register("pow", ExpressionFunction.of(2, args -> Math.pow(args[0], args[1])));
        register("exp", ExpressionFunction.of(1, args -> Math.exp(args[0])));
        register("log", ExpressionFunction.of(1, args -> Math.log(args[0])));
        register("log10", ExpressionFunction.of(1, args -> Math.log10(args[0])));

        register("min", ExpressionFunction.of(2, Integer.MAX_VALUE, args -> {
            double result = args[0];
            for (int i = 1; i < args.length; i++) {
                result = Math.min(result, args[i]);
            }
            return result;
        }));
        register("max", ExpressionFunction.of(2, Integer.MAX_VALUE, args -> {
            double result = args[0];
            for (int i = 1; i < args.length; i++) {
                result = Math.max(result, args[i]);
            }
            return result;
        }));
        register("clamp", ExpressionFunction.of(3, args -> Math.max(args[1], Math.min(args[2], args[0]))));
        register("lerp", ExpressionFunction.of(3, args -> args[0] + (args[1] - args[0]) * args[2]));
        register("smoothstep", ExpressionFunction.of(3, args -> {
            double edge0 = args[0];
            double edge1 = args[1];
            double x = args[2];
            double t = Math.max(0, Math.min(1, (x - edge0) / (edge1 - edge0)));
            return t * t * (3 - 2 * t);
        }));
        register("sign", ExpressionFunction.of(1, args -> Math.signum(args[0])));
        register("frac", ExpressionFunction.of(1, args -> args[0] - Math.floor(args[0])));
        register("mod", ExpressionFunction.of(2, args -> args[1] != 0 ? args[0] % args[1] : 0.0));

        register("random", ExpressionFunction.impure(0, 2, args -> {
            if (args.length == 0) {
                return RANDOM.nextDouble();
            } else if (args.length == 1) {
                return RANDOM.nextDouble() * args[0];
            } else {
                return args[0] + RANDOM.nextDouble() * (args[1] - args[0]);
            }
        }));

        register("noise", ExpressionFunction.of(1, 3, args -> {
            if (args.length == 1) {
                return perlin1D(args[0]);
            } else if (args.length == 2) {
                return perlin2D(args[0], args[1]);
            } else {
                return perlin3D(args[0], args[1], args[2]);
            }
        }));

        register("degToRad", ExpressionFunction.of(1, args -> Math.toRadians(args[0])));
        register("radToDeg", ExpressionFunction.of(1, args -> Math.toDegrees(args[0])));

        register("if", ExpressionFunction.of(3, args -> args[0] != 0.0 ? args[1] : args[2]));

        register("step", ExpressionFunction.of(2, args -> args[1] >= args[0] ? 1.0 : 0.0));

        register("mix", ExpressionFunction.of(3, args -> args[0] * (1 - args[2]) + args[1] * args[2]));
    }

    private FunctionRegistry() {}

    public static void register(String name, ExpressionFunction function) {
        FUNCTIONS.put(name.toLowerCase(), function);
    }

    public static ExpressionFunction getFunction(String name) {
        ExpressionFunction func = FUNCTIONS.get(name.toLowerCase());
        if (func == null) {
            throw new ExpressionException("Unknown function: " + name, name, 0);
        }
        return func;
    }

    public static boolean hasFunction(String name) {
        return FUNCTIONS.containsKey(name.toLowerCase());
    }

    public static Set<String> getFunctionNames() {
        return FUNCTIONS.keySet();
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double grad1D(int hash, double x) {
        return (hash & 1) == 0 ? x : -x;
    }

    private static double grad2D(int hash, double x, double y) {
        int h = hash & 3;
        return switch (h) {
            case 0 -> x + y;
            case 1 -> -x + y;
            case 2 -> x - y;
            default -> -x - y;
        };
    }

    private static double grad3D(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private static double perlin1D(double x) {
        int X = (int) Math.floor(x) & 255;
        x -= Math.floor(x);
        double u = fade(x);
        int a = PERM[X];
        int b = PERM[X + 1];
        return lerp(grad1D(a, x), grad1D(b, x - 1), u);
    }

    private static double perlin2D(double x, double y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        double u = fade(x);
        double v = fade(y);
        int aa = PERM[PERM[X] + Y];
        int ab = PERM[PERM[X] + Y + 1];
        int ba = PERM[PERM[X + 1] + Y];
        int bb = PERM[PERM[X + 1] + Y + 1];
        return lerp(
                lerp(grad2D(aa, x, y), grad2D(ba, x - 1, y), u),
                lerp(grad2D(ab, x, y - 1), grad2D(bb, x - 1, y - 1), u),
                v
        );
    }

    private static double perlin3D(double x, double y, double z) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        int Z = (int) Math.floor(z) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);
        double u = fade(x);
        double v = fade(y);
        double w = fade(z);
        int aaa = PERM[PERM[PERM[X] + Y] + Z];
        int aba = PERM[PERM[PERM[X] + Y + 1] + Z];
        int aab = PERM[PERM[PERM[X] + Y] + Z + 1];
        int abb = PERM[PERM[PERM[X] + Y + 1] + Z + 1];
        int baa = PERM[PERM[PERM[X + 1] + Y] + Z];
        int bba = PERM[PERM[PERM[X + 1] + Y + 1] + Z];
        int bab = PERM[PERM[PERM[X + 1] + Y] + Z + 1];
        int bbb = PERM[PERM[PERM[X + 1] + Y + 1] + Z + 1];
        return lerp(
                lerp(lerp(grad3D(aaa, x, y, z), grad3D(baa, x - 1, y, z), u),
                     lerp(grad3D(aba, x, y - 1, z), grad3D(bba, x - 1, y - 1, z), u), v),
                lerp(lerp(grad3D(aab, x, y, z - 1), grad3D(bab, x - 1, y, z - 1), u),
                     lerp(grad3D(abb, x, y - 1, z - 1), grad3D(bbb, x - 1, y - 1, z - 1), u), v),
                w
        );
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }
}
