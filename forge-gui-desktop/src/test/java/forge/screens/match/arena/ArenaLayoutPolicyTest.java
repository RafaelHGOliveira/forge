package forge.screens.match.arena;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ArenaLayoutPolicyTest {
    @Test public void shouldActivate_fourPlayersArenaEnabled_true() {
        assertTrue(ArenaLayoutPolicy.shouldActivate(4, "ARENA", true));
    }
    @Test public void shouldActivate_twoPlayers_false() {
        assertFalse(ArenaLayoutPolicy.shouldActivate(2, "ARENA", true));
    }
    @Test public void shouldActivate_threePlayers_false() {
        assertFalse(ArenaLayoutPolicy.shouldActivate(3, "ARENA", true));
    }
    @Test public void shouldActivate_notArenaMode_false() {
        assertFalse(ArenaLayoutPolicy.shouldActivate(4, "ROWS", true));
    }
    @Test public void shouldActivate_enhancedDisabled_false() {
        assertFalse(ArenaLayoutPolicy.shouldActivate(4, "ARENA", false));
    }
    @Test public void computeBounds_fullRect_splits40_55() {
        ArenaLayoutPolicy.Bounds b = ArenaLayoutPolicy.computeBounds(0.0, 0.0, 1.0, 1.0);
        assertEquals(b.phaseY, 0.0, 0.001);
        assertEquals(b.phaseH, 0.03, 0.001);
        assertEquals(b.opponentY, 0.03, 0.001);
        assertEquals(b.opponentH, 0.40, 0.001);
        assertEquals(b.localY, 0.43, 0.001);
        assertEquals(b.localH, 0.57, 0.001);
    }
    @Test public void computeBounds_offsetRect_preservesOrigin() {
        ArenaLayoutPolicy.Bounds b = ArenaLayoutPolicy.computeBounds(0.1, 0.2, 0.8, 0.6);
        assertEquals(b.phaseY, 0.2, 0.001);
        assertEquals(b.opponentY, 0.2 + 0.6*0.03, 0.001);
        assertEquals(b.x, 0.1, 0.001);
        assertEquals(b.w, 0.8, 0.001);
    }
}
