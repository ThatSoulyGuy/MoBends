package goblinbob.mobends.core.kumo.state.keyframe;

import static org.junit.jupiter.api.Assertions.*;

import goblinbob.mobends.core.kumo.FakeAnimationData;
import goblinbob.mobends.core.kumo.state.ILayerState;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.template.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.ConnectionTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeLayerTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.StandardKeyframeNodeTemplate;
import goblinbob.mobends.lib.animation.keyframe.Bone;
import goblinbob.mobends.lib.animation.keyframe.Keyframe;
import goblinbob.mobends.lib.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.lib.data.IEntityAnimationData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class KeyframeLayerStateTest
{
    private static final float EPSILON = 1e-4f;

    private static final String ALWAYS = "test:always";

    static
    {
        goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry.instance
                .register(ALWAYS, context -> true);
    }


    private static final class Ctx implements IKumoContext, IKumoInstancingContext
    {
        final FakeAnimationData data;
        final HashMap<String, KeyframeAnimation> animations = new HashMap<>();
        INodeState current;

        Ctx(FakeAnimationData data) { this.data = data; }

        @Override public IEntityAnimationData getEntityData() { return data; }
        @Override public ILayerState getLayerState() { return null; }
        @Override public INodeState getCurrentNode() { return current; }
        @Override public void setCurrentNode(INodeState node) { this.current = node; }
        @Override public KeyframeAnimation getAnimation(String key) { return animations.get(key); }
    }

    private static KeyframeAnimation animation(int frames, float offsetX, String... bones)
    {
        KeyframeAnimation animation = new KeyframeAnimation();
        animation.bones = new HashMap<>();
        for (String boneName : bones)
        {
            List<Keyframe> keyframes = new ArrayList<>();
            for (int i = 0; i < frames; i++)
            {
                Keyframe k = new Keyframe();
                k.position = new float[] { offsetX, 0, 0 };
                k.rotation = new float[] { 0, 0, 0, 1 };
                k.scale = new float[] { 1, 1, 1 };
                keyframes.add(k);
            }
            Bone bone = new Bone();
            bone.keyframes = keyframes;
            animation.bones.put(boneName, bone);
        }
        return animation;
    }

    private static StandardKeyframeNodeTemplate node(String animationKey, Integer targetNode, float duration)
    {
        StandardKeyframeNodeTemplate t = new StandardKeyframeNodeTemplate();
        t.animationKey = animationKey;
        t.playbackSpeed = 1;
        t.looping = true;
        t.connections = new ArrayList<>();
        if (targetNode != null)
        {
            ConnectionTemplate c = new ConnectionTemplate();
            c.targetNodeIndex = targetNode;
            c.transitionDuration = duration;
            c.transitionEasing = ConnectionTemplate.Easing.LINEAR;
            c.triggerCondition = new goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate();
            c.triggerCondition.type = ALWAYS;
            t.connections.add(c);
        }
        return t;
    }

    private static KeyframeLayerState layer(Ctx ctx, StandardKeyframeNodeTemplate... nodes)
            throws MalformedKumoTemplateException
    {
        return layer(ctx, false, nodes);
    }

    private static KeyframeLayerState layer(Ctx ctx, boolean additive, StandardKeyframeNodeTemplate... nodes)
            throws MalformedKumoTemplateException
    {
        KeyframeLayerTemplate template = new KeyframeLayerTemplate();
        template.entryNode = 0;
        template.additive = additive;
        template.nodes = new ArrayList<>(Arrays.asList(nodes));
        return new KeyframeLayerState(ctx, template);
    }

    private static KeyframeAnimation rotatedAnimation(int frames, float degreesAboutY, String... bones)
    {
        double half = Math.toRadians(degreesAboutY) / 2.0;
        float qy = (float) Math.sin(half);
        float qw = (float) Math.cos(half);

        KeyframeAnimation animation = new KeyframeAnimation();
        animation.bones = new HashMap<>();
        for (String boneName : bones)
        {
            List<Keyframe> keyframes = new ArrayList<>();
            for (int i = 0; i < frames; i++)
            {
                Keyframe k = new Keyframe();
                k.position = new float[] { 0, 0, 0 };
                k.rotation = new float[] { 0, qy, 0, qw };
                k.scale = new float[] { 1, 1, 1 };
                keyframes.add(k);
            }
            Bone bone = new Bone();
            bone.keyframes = keyframes;
            animation.bones.put(boneName, bone);
        }
        return animation;
    }

    private static float yAngleDegrees(goblinbob.mobends.lib.math.SmoothOrientation orientation)
    {
        goblinbob.mobends.lib.math.Quaternion q = orientation.getSmooth();
        double angle = 2.0 * Math.atan2(q.y, q.w);
        double degrees = Math.toDegrees(angle);
        return (float) ((degrees % 360.0 + 360.0) % 360.0);
    }


    @Test
    public void aSingleLayerWritesExactlyTheAuthoredPoseOnce()
    {
        FakeAnimationData data = new FakeAnimationData().withBones("body");
        Ctx ctx = new Ctx(data);
        ctx.animations.put("a", animation(4, 3.0f, "body"));

        KeyframeLayerState state = assertDoesNotThrow(() -> layer(ctx, node("a", null, 0)));
        assertDoesNotThrow(() -> state.start(ctx));

        for (int frame = 0; frame < 10; frame++)
        {
            assertDoesNotThrow(() -> state.update(ctx, 1.0f));

            float x = data.part("body").getOffset().getX();
            assertEquals(-3.0f, x, EPSILON,
                    "offset should be the authored pose exactly, once, on frame " + frame);

            assertEquals(1.0f, magnitude(data.part("body").getRotation()), EPSILON,
                    "rotation must stay a unit quaternion on frame " + frame
                            + " -- the renderer scales geometry by its squared magnitude");
        }
    }

    @Test
    public void twoLayersOverTheSameBoneStillLeaveAUnitRotation()
    {
        FakeAnimationData data = new FakeAnimationData().withBones("tongue");
        Ctx ctx = new Ctx(data);
        ctx.animations.put("base", animation(4, 2.0f, "tongue"));
        ctx.animations.put("overlay", animation(4, 1.0f, "tongue"));

        KeyframeLayerState base = assertDoesNotThrow(() -> layer(ctx, node("base", null, 0)));
        KeyframeLayerState overlay = assertDoesNotThrow(() -> layer(ctx, node("overlay", null, 0)));
        assertDoesNotThrow(() -> base.start(ctx));
        assertDoesNotThrow(() -> overlay.start(ctx));

        for (int frame = 0; frame < 10; frame++)
        {
            assertDoesNotThrow(() -> base.update(ctx, 1.0f));
            assertDoesNotThrow(() -> overlay.update(ctx, 1.0f));

            assertEquals(1.0f, magnitude(data.part("tongue").getRotation()), EPSILON,
                    "two layers writing one bone left |q| != 1 on frame " + frame);
            assertEquals(-1.0f, data.part("tongue").getOffset().getX(), EPSILON,
                    "the later layer should replace the earlier pose, not add to it");
        }
    }

    private static float magnitude(goblinbob.mobends.lib.math.SmoothOrientation orientation)
    {
        goblinbob.mobends.lib.math.Quaternion q = orientation.getSmooth();
        return (float) Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w);
    }


    @Test
    public void anAdditiveLayerComposesOntoWhatAnEarlierLayerWrote()
    {
        FakeAnimationData data = new FakeAnimationData().withBones("tongue");
        Ctx ctx = new Ctx(data);
        ctx.animations.put("base", rotatedAnimation(4, 30.0f, "tongue"));
        ctx.animations.put("overlay", rotatedAnimation(4, 20.0f, "tongue"));

        KeyframeLayerState base = assertDoesNotThrow(() -> layer(ctx, node("base", null, 0)));
        KeyframeLayerState overlay = assertDoesNotThrow(() -> layer(ctx, true, node("overlay", null, 0)));
        assertDoesNotThrow(() -> base.start(ctx));
        assertDoesNotThrow(() -> overlay.start(ctx));

        assertDoesNotThrow(() -> base.update(ctx, 1.0f));
        assertEquals(30.0f, yAngleDegrees(data.part("tongue").getRotation()), 0.01f,
                "the base layer should have posed the bone");

        assertDoesNotThrow(() -> overlay.update(ctx, 1.0f));
        assertEquals(50.0f, yAngleDegrees(data.part("tongue").getRotation()), 0.01f,
                "an additive overlay must compose onto the base pose, not replace it");
    }

    @Test
    public void anAdditiveLayerLeavesAUnitQuaternionAcrossManyFrames()
    {
        FakeAnimationData data = new FakeAnimationData().withBones("tongue");
        Ctx ctx = new Ctx(data);
        ctx.animations.put("base", rotatedAnimation(4, 30.0f, "tongue"));
        ctx.animations.put("overlay", rotatedAnimation(4, 20.0f, "tongue"));

        KeyframeLayerState base = assertDoesNotThrow(() -> layer(ctx, node("base", null, 0)));
        KeyframeLayerState overlay = assertDoesNotThrow(() -> layer(ctx, true, node("overlay", null, 0)));
        assertDoesNotThrow(() -> base.start(ctx));
        assertDoesNotThrow(() -> overlay.start(ctx));

        for (int frame = 0; frame < 30; frame++)
        {
            assertDoesNotThrow(() -> base.update(ctx, 1.0f));
            assertDoesNotThrow(() -> overlay.update(ctx, 1.0f));

            assertEquals(1.0f, magnitude(data.part("tongue").getRotation()), EPSILON,
                    "|q| left unit length on frame " + frame);
            assertEquals(50.0f, yAngleDegrees(data.part("tongue").getRotation()), 0.01f,
                    "the composed angle should be stable, not creeping, on frame " + frame);
        }
    }

    @Test
    public void anAdditiveLayerScalesItsRotationByTheBlendAmount()
    {
        FakeAnimationData data = new FakeAnimationData().withBones("tongue");
        Ctx ctx = new Ctx(data);
        ctx.animations.put("overlay", rotatedAnimation(4, 40.0f, "tongue"));

        KeyframeLayerState overlay = assertDoesNotThrow(() -> layer(ctx, true, node("overlay", null, 0)));
        assertDoesNotThrow(() -> overlay.start(ctx));

        overlay.applyKeyframeAnimation(data, ctx.animations.get("overlay"), 0.0f, 0.5f);

        assertEquals(20.0f, yAngleDegrees(data.part("tongue").getRotation()), 0.01f,
                "half the blend amount should apply half the angle");
        assertEquals(1.0f, magnitude(data.part("tongue").getRotation()), EPSILON);
    }


    @Test
    public void aBoneOnlyInTheOutgoingAnimationDoesNotAccumulate() throws Exception
    {
        FakeAnimationData data = new FakeAnimationData().withBones("body", "tail");
        Ctx ctx = new Ctx(data);
        ctx.animations.put("outgoing", animation(4, 1.0f, "body", "tail"));
        ctx.animations.put("incoming", animation(4, 1.0f, "body"));

        KeyframeLayerState state = layer(ctx,
                node("outgoing", 1, 10.0f),
                node("incoming", null, 0));
        state.start(ctx);

        state.update(ctx, 1.0f);
        float steady = Math.abs(data.part("tail").getOffset().getX());

        for (int i = 0; i < 30; i++)
        {
            state.update(ctx, 1.0f);
        }

        float settled = Math.abs(data.part("tail").getOffset().getX());
        assertTrue(settled <= steady + EPSILON,
                "outgoing-only bone accumulated: steady=" + steady + " settled=" + settled);
    }

    @Test
    public void sharedBonesAreUnaffectedByTheExtraRestPose() throws Exception
    {
        FakeAnimationData data = new FakeAnimationData().withBones("body");
        Ctx ctx = new Ctx(data);
        ctx.animations.put("a", animation(4, 2.0f, "body"));
        ctx.animations.put("b", animation(4, 2.0f, "body"));

        KeyframeLayerState state = layer(ctx, node("a", 1, 4.0f), node("b", null, 0));
        state.start(ctx);

        for (int i = 0; i < 20; i++)
        {
            state.update(ctx, 1.0f);
            float x = Math.abs(data.part("body").getOffset().getX());
            assertTrue(x <= 2.0f + EPSILON, "shared bone drifted to " + x + " on tick " + i);
        }
    }


    @Test
    public void inactiveNodesDoNotAdvance() throws Exception
    {
        FakeAnimationData data = new FakeAnimationData().withBones("body");
        Ctx ctx = new Ctx(data);
        ctx.animations.put("a", animation(8, 1.0f, "body"));
        ctx.animations.put("b", animation(8, 1.0f, "body"));

        KeyframeLayerState state = layer(ctx, node("a", null, 0), node("b", null, 0));
        state.start(ctx);

        for (int i = 0; i < 15; i++)
        {
            state.update(ctx, 1.0f);
        }

        INodeState idle = state.getNodeStates().get(1);
        assertEquals(0.0f, idle.getProgress(), EPSILON,
                "a node that was never entered should not have advanced");
    }


    @Test
    public void aNonAnimatablePartIsSkippedRatherThanCrashing() throws Exception
    {
        FakeAnimationData data = new FakeAnimationData().withBones("body");
        data.withNonAnimatablePart("centerRotation");
        Ctx ctx = new Ctx(data);
        ctx.animations.put("a", animation(4, 1.0f, "body", "centerRotation"));

        KeyframeLayerState state = layer(ctx, node("a", null, 0));
        state.start(ctx);
        assertDoesNotThrow(() -> state.update(ctx, 1.0f));
    }

    @Test
    public void aBoneTheSkeletonDoesNotHaveIsSkipped() throws Exception
    {
        FakeAnimationData data = new FakeAnimationData().withBones("body");
        Ctx ctx = new Ctx(data);
        ctx.animations.put("a", animation(4, 1.0f, "body", "nonexistent"));

        KeyframeLayerState state = layer(ctx, node("a", null, 0));
        state.start(ctx);
        assertDoesNotThrow(() -> state.update(ctx, 1.0f));
    }
}
