package goblinbob.mobends.core.kumo.state.keyframe;

import static org.junit.jupiter.api.Assertions.*;

import goblinbob.mobends.lib.animation.keyframe.Bone;
import goblinbob.mobends.lib.animation.keyframe.Keyframe;
import goblinbob.mobends.lib.animation.keyframe.KeyframeAnimation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Playback bounds for a keyframe node.
 *
 * <p>These could not be written before the Kumo runtime moved into this module — exercising a
 * node previously meant standing up a Minecraft entity.
 */
public class StandardKeyframeNodeTest
{
    private static final float EPSILON = 1e-5f;

    /** An animation with one bone carrying {@code count} keyframes. */
    private static KeyframeAnimation animationOf(int count)
    {
        List<Keyframe> frames = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            Keyframe k = new Keyframe();
            k.position = new float[] { i, 0, 0 };
            k.rotation = new float[] { 0, 0, 0, 1 };
            k.scale = new float[] { 1, 1, 1 };
            frames.add(k);
        }

        Bone bone = new Bone();
        bone.keyframes = frames;

        KeyframeAnimation animation = new KeyframeAnimation();
        animation.bones = new HashMap<>();
        animation.bones.put("body", bone);
        return animation;
    }

    private static StandardKeyframeNode node(int frameCount, boolean looping, float speed)
    {
        return new StandardKeyframeNode(animationOf(frameCount), 0, speed, looping);
    }

    /** Advances by one tick at a time so the clamp is exercised, not jumped over. */
    private static void advance(StandardKeyframeNode node, int ticks)
    {
        for (int i = 0; i < ticks; i++)
        {
            node.update(null, 1.0f);
        }
    }

    // ---------- non-looping ----------

    @Test
    public void nonLoopingReachesItsFinalKeyframe()
    {
        // The whole point of K4: a 10-frame animation must end on frame 9, not frame 8.
        StandardKeyframeNode n = node(10, false, 1.0f);
        advance(n, 40);
        assertEquals(9.0f, n.getProgress(), EPSILON,
                "a non-looping animation should settle on its last keyframe");
    }

    @Test
    public void nonLoopingNeverOvershootsItsFinalKeyframe()
    {
        StandardKeyframeNode n = node(6, false, 3.0f);
        advance(n, 20);
        assertTrue(n.getProgress() <= 5.0f + EPSILON, "progress ran past the last keyframe");
    }

    @Test
    public void nonLoopingReportsFinishedExactlyWhenItStops()
    {
        StandardKeyframeNode n = node(5, false, 1.0f);
        assertFalse(n.isAnimationFinished(), "reported finished before it started");

        advance(n, 3);
        assertFalse(n.isAnimationFinished(), "reported finished at frame 3 of 5");

        advance(n, 1);
        assertEquals(4.0f, n.getProgress(), EPSILON);
        assertTrue(n.isAnimationFinished(),
                "must report finished once it reaches the last frame, or a state machine waiting "
                        + "on core:animation_finished soft-locks forever");
    }

    @Test
    public void finishedAgreesWithTheClampAcrossManyLengths()
    {
        // The clamp in update() and the bound in isAnimationFinished() have to stay in step;
        // this is what makes changing one of them in isolation fail loudly.
        for (int frames = 2; frames <= 24; frames++)
        {
            StandardKeyframeNode n = node(frames, false, 1.0f);
            advance(n, frames * 3);
            assertEquals(frames - 1, n.getProgress(), EPSILON, "wrong resting frame for " + frames);
            assertTrue(n.isAnimationFinished(), "not finished at rest for " + frames + " frames");
        }
    }

    @Test
    public void aNullAnimationCountsAsFinished()
    {
        StandardKeyframeNode n = new StandardKeyframeNode(null, 0, 1.0f, false);
        assertTrue(n.isAnimationFinished());
    }

    // ---------- looping ----------

    @Test
    public void loopingWrapsAndNeverReportsFinished()
    {
        StandardKeyframeNode n = node(8, true, 1.0f);
        advance(n, 30);
        assertTrue(n.getProgress() >= 0.0f && n.getProgress() < 7.0f,
                "looping progress escaped its range: " + n.getProgress());
        assertFalse(n.isAnimationFinished(), "a looping animation is never finished");
    }

    @Test
    public void loopingStaysInRangeAtHighPlaybackSpeed()
    {
        StandardKeyframeNode n = node(4, true, 11.0f);
        advance(n, 25);
        assertTrue(n.getProgress() >= 0.0f && n.getProgress() < 3.0f,
                "high playback speed escaped the loop range: " + n.getProgress());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void loopingASingleKeyframeDoesNotHang()
    {
        // duration - 1 == 0, so the wrap loop would subtract zero forever. This hung the client
        // with no crash log rather than throwing.
        StandardKeyframeNode n = node(1, true, 1.0f);
        advance(n, 5);
        assertEquals(0.0f, n.getProgress(), EPSILON);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void loopingAnEmptyAnimationDoesNotHang()
    {
        StandardKeyframeNode n = node(0, true, 1.0f);
        advance(n, 5);
    }

    // ---------- start frame ----------

    @Test
    public void startResetsProgressToTheAuthoredStartFrame()
    {
        StandardKeyframeNode n = new StandardKeyframeNode(animationOf(10), 6, 1.0f, false);
        advance(n, 2);
        assertTrue(n.getProgress() > 6.0f);

        n.start(null);
        assertEquals(6.0f, n.getProgress(), EPSILON, "start() must rewind to the start frame");
    }
}
