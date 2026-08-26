package goblinbob.mobends.lib.animation.keyframe;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

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
        ArmatureMask mask = fromJson("{\"mode\":\"EXCLUDE_ONLY\"}");
        assertDoesNotThrow(() -> mask.doesAllow("head"));
        assertTrue(mask.doesAllow("head"), "excluding nothing should allow everything");
    }

    @Test
    public void anUnrecognisedModeAllowsEverythingRatherThanThrowing()
    {
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
