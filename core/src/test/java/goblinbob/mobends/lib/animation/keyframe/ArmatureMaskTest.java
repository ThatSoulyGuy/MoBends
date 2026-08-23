package goblinbob.mobends.lib.animation.keyframe;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

/**
 * A mask is Gson-populated from user pack JSON, so every field can arrive null: Gson builds the
 * object through Unsafe, which skips the constructor's field initialisers entirely.
 *
 * <p>These all used to throw from inside the render loop, where nothing catches them — the pack
 * performer only catches its own template exception — so a one-character mistake in a pack was a
 * hard client crash rather than a logged error.
 */
public class ArmatureMaskTest
{
    private static final Gson GSON = new Gson();

    private static ArmatureMask fromJson(String json)
    {
        return GSON.fromJson(json, ArmatureMask.class);
    }

    @Test
    public void gsonReallyDoesSkipTheFieldInitialisers()
    {
        // The premise of every other test here. If this ever fails, Gson changed and the
        // null-tolerance below is no longer load-bearing.
        ArmatureMask mask = fromJson("{\"mode\":\"INCLUDE_ONLY\"}");
        assertEquals(ArmatureMask.Mode.INCLUDE_ONLY, mask.getMode());
        assertFalse(mask.doesAllow("head"), "an INCLUDE_ONLY mask with no list includes nothing");
    }

    @Test
    public void includeOnlyWithoutItsListDoesNotThrow()
    {
        ArmatureMask mask = fromJson("{\"mode\":\"INCLUDE_ONLY\"}");
        assertDoesNotThrow(() -> mask.doesAllow("head"));
        assertFalse(mask.doesAllow("head"));
    }

    @Test
    public void excludeOnlyWithoutItsListAllowsEverything()
    {
        // This was the reported crash: an EXCLUDE_ONLY mask authored with no excludedParts array.
        ArmatureMask mask = fromJson("{\"mode\":\"EXCLUDE_ONLY\"}");
        assertDoesNotThrow(() -> mask.doesAllow("head"));
        assertTrue(mask.doesAllow("head"), "excluding nothing should allow everything");
    }

    @Test
    public void anUnrecognisedModeAllowsEverythingRatherThanThrowing()
    {
        // Gson maps an unknown enum constant to null, so `switch (mode)` threw as well.
        ArmatureMask mask = fromJson("{\"mode\":\"INCLUDE\"}");
        assertNull(mask.getMode());
        assertDoesNotThrow(() -> mask.doesAllow("head"));
        assertTrue(mask.doesAllow("head"));
    }

    @Test
    public void anEmptyObjectAllowsEverything()
    {
        ArmatureMask mask = fromJson("{}");
        assertDoesNotThrow(() -> mask.doesAllow("head"));
        assertTrue(mask.doesAllow("head"));
    }

    @Test
    public void populatedListsStillFilterCorrectly()
    {
        ArmatureMask include = fromJson("{\"mode\":\"INCLUDE_ONLY\",\"includedParts\":[\"tongue\",\"mouth\"]}");
        assertTrue(include.doesAllow("tongue"));
        assertTrue(include.doesAllow("mouth"));
        assertFalse(include.doesAllow("tail"));

        ArmatureMask exclude = fromJson("{\"mode\":\"EXCLUDE_ONLY\",\"excludedParts\":[\"tail\"]}");
        assertFalse(exclude.doesAllow("tail"));
        assertTrue(exclude.doesAllow("head"));
    }

    @Test
    public void theProgrammaticConstructorStillWorks()
    {
        ArmatureMask mask = new ArmatureMask(ArmatureMask.Mode.INCLUDE_ONLY);
        mask.include("head");
        assertTrue(mask.doesAllow("head"));
        assertFalse(mask.doesAllow("tail"));
    }

    @Test
    public void mutatorsTolerateAGsonBuiltInstance()
    {
        ArmatureMask mask = fromJson("{\"mode\":\"INCLUDE_ONLY\"}");
        assertDoesNotThrow(() -> mask.include("head"));
        assertTrue(mask.doesAllow("head"));
    }
}
