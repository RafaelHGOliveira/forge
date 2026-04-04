package forge.screens.match.arena;

import forge.game.zone.ZoneType;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ZoneBarStateTest {
    @Test public void initial_noZoneExpanded() {
        ZoneBarState s = new ZoneBarState(false);
        assertNull(s.getExpandedZone());
    }
    @Test public void toggle_sameZoneTwice_opensThenCloses() {
        ZoneBarState s = new ZoneBarState(false);
        ZoneBarState.Result r1 = s.toggle(ZoneType.Graveyard);
        assertEquals(r1, ZoneBarState.Result.OPENED);
        assertEquals(s.getExpandedZone(), ZoneType.Graveyard);
        ZoneBarState.Result r2 = s.toggle(ZoneType.Graveyard);
        assertEquals(r2, ZoneBarState.Result.CLOSED);
        assertNull(s.getExpandedZone());
    }
    @Test public void toggle_differentZone_switchesExpansion() {
        ZoneBarState s = new ZoneBarState(false);
        s.toggle(ZoneType.Graveyard);
        ZoneBarState.Result r = s.toggle(ZoneType.Exile);
        assertEquals(r, ZoneBarState.Result.SWITCHED);
        assertEquals(s.getExpandedZone(), ZoneType.Exile);
    }
    @Test public void toggle_handForOpponent_ignored() {
        ZoneBarState s = new ZoneBarState(false);
        ZoneBarState.Result r = s.toggle(ZoneType.Hand);
        assertEquals(r, ZoneBarState.Result.IGNORED);
        assertNull(s.getExpandedZone());
    }
    @Test public void toggle_handForLocal_opens() {
        ZoneBarState s = new ZoneBarState(true);
        ZoneBarState.Result r = s.toggle(ZoneType.Hand);
        assertEquals(r, ZoneBarState.Result.OPENED);
        assertEquals(s.getExpandedZone(), ZoneType.Hand);
    }
    @Test public void canExpand_commandGraveyardExile_alwaysTrue() {
        ZoneBarState opp = new ZoneBarState(false);
        assertTrue(opp.canExpand(ZoneType.Command));
        assertTrue(opp.canExpand(ZoneType.Graveyard));
        assertTrue(opp.canExpand(ZoneType.Exile));
    }
    @Test public void canExpand_handOnlyForLocal() {
        assertFalse(new ZoneBarState(false).canExpand(ZoneType.Hand));
        assertTrue(new ZoneBarState(true).canExpand(ZoneType.Hand));
    }
}
