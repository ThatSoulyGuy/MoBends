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

/**
 * Layer-level behaviour: the per-bone pose write, cross-fades, and which nodes advance.
 */
public class KeyframeLayerStateTest
{
    private static final float EPSILON = 1e-4f;

    /** Fires on the first evaluation, so a transition starts on a known tick. */
    private static final String ALWAYS = "test:always";

    static
    {
        goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry.instance
                .register(ALWAYS, context -> true);
    }

    // ---------- harness ----------

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

    /** An animation holding every named bone at a constant pose, for `frames` keyframes. */
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
        KeyframeLayerTemplate template = new KeyframeLayerTemplate();
        template.entryNode = 0;
        template.nodes = new ArrayList<>(Arrays.asList(nodes));
        return new KeyframeLayerState(ctx, template);
    }

    // ---------- the pose write itself ----------

    @Test
    public void aSingleLayerWritesExactlyTheAuthoredPoseOnce()
    {
        // Nothing pinned the magnitude or sign of the per-bone write, which is how an "additive"
        // implementation that doubled every offset and left |q| = 2 on every shared bone passed
        // the whole suite. A quaternion that is not unit length is not just a wrong rotation:
        // the renderer scales geometry by its squared magnitude.
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
        // The shipped wolf animator stacks a masked overlay layer on top of a base layer, and
        // both name tongue and mouth. Each layer rest-poses before it writes, so the second one
        // replaces rather than compounds.
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

    // ---------- K1: cross-fade must not leave outgoing-only bones accumulating ----------

    @Test
    public void aBoneOnlyInTheOutgoingAnimationDoesNotAccumulate() throws Exception
    {
        // "tail" exists in the outgoing animation and not the incoming one. The rest pose names
        // only the incoming animation's bones, so before the fix this bone was written additively
        // every frame of the fade and never cleared -- growing without bound and then freezing
        // there for the rest of the entity's life.
        FakeAnimationData data = new FakeAnimationData().withBones("body", "tail");
        Ctx ctx = new Ctx(data);
        ctx.animations.put("outgoing", animation(4, 1.0f, "body", "tail"));
        ctx.animations.put("incoming", animation(4, 1.0f, "body"));

        // Node 0 immediately transitions to node 1 over 10 ticks.
        KeyframeLayerState state = layer(ctx,
                node("outgoing", 1, 10.0f),
                node("incoming", null, 0));
        state.start(ctx);

        // Steady state on the outgoing node writes exactly one unit.
        state.update(ctx, 1.0f);
        float steady = Math.abs(data.part("tail").getOffset().getX());

        // Drive well past the end of the fade.
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
        // Clearing the outgoing animation too is idempotent for bones both animations name --
        // they are simply zeroed twice before being written. This pins that.
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

    // ---------- K5: only the sampled nodes advance ----------

    @Test
    public void inactiveNodesDoNotAdvance() throws Exception
    {
        FakeAnimationData data = new FakeAnimationData().withBones("body");
        Ctx ctx = new Ctx(data);
        ctx.animations.put("a", animation(8, 1.0f, "body"));
        ctx.animations.put("b", animation(8, 1.0f, "body"));

        // No connections at all, so node 1 is never entered.
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

    // ---------- robustness ----------

    @Test
    public void aNonAnimatablePartIsSkippedRatherThanCrashing() throws Exception
    {
        // EntityData stores raw orientations in the same map as real bones; the runtime filters
        // them out with instanceof.
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
