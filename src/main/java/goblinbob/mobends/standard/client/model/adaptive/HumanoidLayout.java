package goblinbob.mobends.standard.client.model.adaptive;

import net.minecraft.client.model.HumanoidModel;

public final class HumanoidLayout
{
    private static final float BOUNDS_TOLERANCE = 0.01F;
    private static final float UV_TOLERANCE = 1.0e-4F;

    public static final HumanoidLayout ZOMBIE = new HumanoidLayout(64.0F, 64.0F,
            new Slot(0, 0, -4, -8, -4, 4, 0, 4, 0.0F),
            new Slot(32, 0, -4, -8, -4, 4, 0, 4, 0.5F),
            new Slot(16, 16, -4, 0, -2, 4, 12, 2, 0.0F),
            new Slot(40, 16, -1, -2, -2, 3, 10, 2, 0.0F).or(32, 48),
            new Slot(40, 16, -3, -2, -2, 1, 10, 2, 0.0F),
            new Slot(0, 16, -2, 0, -2, 2, 12, 2, 0.0F).or(16, 48),
            new Slot(0, 16, -2, 0, -2, 2, 12, 2, 0.0F));

    public static final HumanoidLayout SKELETON = new HumanoidLayout(64.0F, 32.0F,
            new Slot(0, 0, -4, -8, -4, 4, 0, 4, 0.0F),
            new Slot(32, 0, -4, -8, -4, 4, 0, 4, 0.5F),
            new Slot(16, 16, -4, 0, -2, 4, 12, 2, 0.0F),
            new Slot(40, 16, -1, -2, -1, 1, 10, 1, 0.0F),
            new Slot(40, 16, -1, -2, -1, 1, 10, 1, 0.0F),
            new Slot(0, 16, -1, 0, -1, 1, 12, 1, 0.0F),
            new Slot(0, 16, -1, 0, -1, 1, 12, 1, 0.0F));

    private final float textureWidth, textureHeight;
    private final Slot head, hat, body, leftArm, rightArm, leftLeg, rightLeg;

    private HumanoidLayout(float textureWidth, float textureHeight,
                           Slot head, Slot hat, Slot body,
                           Slot leftArm, Slot rightArm,
                           Slot leftLeg, Slot rightLeg)
    {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.head = head;
        this.hat = hat;
        this.body = body;
        this.leftArm = leftArm;
        this.rightArm = rightArm;
        this.leftLeg = leftLeg;
        this.rightLeg = rightLeg;
    }

    public boolean describes(HumanoidModel<?> model)
    {
        if (model == null || model.head == null || model.body == null
                || model.leftArm == null || model.rightArm == null
                || model.leftLeg == null || model.rightLeg == null)
        {
            return false;
        }

        return matches(head, PartCapture.ofOwnCubes(model.head), true)
                && matches(body, PartCapture.ofOwnCubes(model.body), true)
                && matches(leftArm, PartCapture.ofOwnCubes(model.leftArm), true)
                && matches(rightArm, PartCapture.ofOwnCubes(model.rightArm), true)
                && matches(leftLeg, PartCapture.ofOwnCubes(model.leftLeg), true)
                && matches(rightLeg, PartCapture.ofOwnCubes(model.rightLeg), true)
                && matches(hat, PartCapture.ofOwnCubes(model.hat), false);
    }

    private boolean matches(Slot expected, PartCapture capture, boolean required)
    {
        if (capture.isEmpty())
        {
            return !required;
        }

        if (capture.cubeCount != 1)
        {
            return false;
        }

        final float grow = expected.grow;

        if (Math.abs(capture.minX - (expected.minX - grow)) > BOUNDS_TOLERANCE
                || Math.abs(capture.minY - (expected.minY - grow)) > BOUNDS_TOLERANCE
                || Math.abs(capture.minZ - (expected.minZ - grow)) > BOUNDS_TOLERANCE
                || Math.abs(capture.maxX - (expected.maxX + grow)) > BOUNDS_TOLERANCE
                || Math.abs(capture.maxY - (expected.maxY + grow)) > BOUNDS_TOLERANCE
                || Math.abs(capture.maxZ - (expected.maxZ + grow)) > BOUNDS_TOLERANCE)
        {
            return false;
        }

        if (matchesUv(expected, expected.u, expected.v, capture))
        {
            return true;
        }

        return expected.hasAlternate && matchesUv(expected, expected.altU, expected.altV, capture);
    }

    private boolean matchesUv(Slot expected, float u, float v, PartCapture capture)
    {
        final float width = expected.maxX - expected.minX;
        final float height = expected.maxY - expected.minY;
        final float depth = expected.maxZ - expected.minZ;

        return near(capture.frontUMin, (u + depth) / textureWidth)
                && near(capture.frontUMax, (u + depth + width) / textureWidth)
                && near(capture.frontVMin, (v + depth) / textureHeight)
                && near(capture.frontVMax, (v + depth + height) / textureHeight);
    }

    private static boolean near(float actual, float expected)
    {
        return Math.abs(actual - expected) <= UV_TOLERANCE;
    }

    private static final class Slot
    {
        private final float u, v;
        private final float minX, minY, minZ, maxX, maxY, maxZ;
        private final float grow;

        private float altU, altV;
        private boolean hasAlternate;

        private Slot(float u, float v,
                     float minX, float minY, float minZ,
                     float maxX, float maxY, float maxZ,
                     float grow)
        {
            this.u = u;
            this.v = v;
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
            this.grow = grow;
        }

        private Slot or(float altU, float altV)
        {
            this.altU = altU;
            this.altV = altV;
            this.hasAlternate = true;
            return this;
        }
    }
}
