package forge.ai.simulation;

import forge.game.Game;
import forge.game.GameLogEntry;
import forge.game.GameLogEntryType;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.List;

public class GameLogLifeChangeTest extends SimulationTest {

    @Test
    public void testLifeLossLogged() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(0);
        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, p);

        int oldLife = p.getLife();
        p.loseLife(3, false, false);

        List<GameLogEntry> lifeEntries = game.getGameLog().getLogEntriesExact(GameLogEntryType.LIFE);
        AssertJUnit.assertFalse("Expected at least one LIFE log entry", lifeEntries.isEmpty());

        String lastEntry = lifeEntries.get(0).message;
        AssertJUnit.assertTrue("Log should contain player name: " + lastEntry,
                lastEntry.contains(p.getName()));
        AssertJUnit.assertTrue("Log should contain old life total: " + lastEntry,
                lastEntry.contains(String.valueOf(oldLife)));
        AssertJUnit.assertTrue("Log should contain new life total: " + lastEntry,
                lastEntry.contains(String.valueOf(p.getLife())));
        AssertJUnit.assertTrue("Log should contain arrow: " + lastEntry,
                lastEntry.contains("->"));
    }

    @Test
    public void testLifeGainLogged() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(0);
        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, p);

        p.loseLife(5, false, false);
        // Clear log tracking by recording count
        int entriesBefore = game.getGameLog().getLogEntriesExact(GameLogEntryType.LIFE).size();

        int oldLife = p.getLife();
        p.gainLife(3, null, null);

        List<GameLogEntry> lifeEntries = game.getGameLog().getLogEntriesExact(GameLogEntryType.LIFE);
        AssertJUnit.assertTrue("Expected new LIFE log entry for gain",
                lifeEntries.size() > entriesBefore);

        // Most recent entry is first (list is reversed)
        String lastEntry = lifeEntries.get(0).message;
        AssertJUnit.assertTrue("Log should contain old life: " + lastEntry,
                lastEntry.contains(String.valueOf(oldLife)));
        AssertJUnit.assertTrue("Log should contain new life: " + lastEntry,
                lastEntry.contains(String.valueOf(p.getLife())));
    }

    @Test
    public void testLifeLogFormat() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(0);
        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, p);

        p.loseLife(7, false, false);

        List<GameLogEntry> lifeEntries = game.getGameLog().getLogEntriesExact(GameLogEntryType.LIFE);
        AssertJUnit.assertFalse(lifeEntries.isEmpty());

        String msg = lifeEntries.get(0).message;
        // Format: "Life: PlayerName 20 -> 13"
        AssertJUnit.assertTrue("Log should start with 'Life:': " + msg,
                msg.startsWith("Life:"));
    }

    @Test
    public void testNoLifeLogForZeroLoss() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(0);
        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, p);

        int entriesBefore = game.getGameLog().getLogEntriesExact(GameLogEntryType.LIFE).size();

        // loseLife(0) should be a no-op per Rule 118.4
        p.loseLife(0, false, false);

        int entriesAfter = game.getGameLog().getLogEntriesExact(GameLogEntryType.LIFE).size();
        AssertJUnit.assertEquals("No LIFE entry for zero loss", entriesBefore, entriesAfter);
    }

    @Test
    public void testLifeSetLogged() {
        // "Life total becomes N" cards (Torgaar, Blessed Wind, Providence, etc.)
        // use Player.setLife(), which delegates to loseLife/gainLife per Rule 119.5.
        // Verify LIFE entries are produced for both directions.
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(0);
        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, p);

        // Life total becomes 10 (loss from 20, like Torgaar targeting self)
        AssertJUnit.assertEquals(20, p.getLife());
        p.setLife(10, null);
        AssertJUnit.assertEquals(10, p.getLife());

        List<GameLogEntry> lifeEntries = game.getGameLog().getLogEntriesExact(GameLogEntryType.LIFE);
        AssertJUnit.assertEquals("Expected 1 LIFE log entry after set-to-10", 1, lifeEntries.size());
        String msg = lifeEntries.get(0).message;
        AssertJUnit.assertTrue("Log should show 20 -> 10: " + msg,
                msg.contains("20") && msg.contains("10") && msg.contains("->"));

        // Life total becomes 26 (gain from 10, like Providence resolving)
        p.setLife(26, null);
        AssertJUnit.assertEquals(26, p.getLife());

        lifeEntries = game.getGameLog().getLogEntriesExact(GameLogEntryType.LIFE);
        AssertJUnit.assertEquals("Expected 2 LIFE log entries total", 2, lifeEntries.size());
        // Most recent entry is first
        msg = lifeEntries.get(0).message;
        AssertJUnit.assertTrue("Log should show 10 -> 26: " + msg,
                msg.contains("10") && msg.contains("26") && msg.contains("->"));

        // Life total becomes 26 (no-op, same value) — no new entry
        p.setLife(26, null);
        lifeEntries = game.getGameLog().getLogEntriesExact(GameLogEntryType.LIFE);
        AssertJUnit.assertEquals("No new entry when life total unchanged", 2, lifeEntries.size());
    }

    @Test
    public void testMultipleLifeChangesAllLogged() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(0);
        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, p);

        p.loseLife(2, false, false);
        p.loseLife(3, false, false);
        p.gainLife(1, null, null);

        List<GameLogEntry> lifeEntries = game.getGameLog().getLogEntriesExact(GameLogEntryType.LIFE);
        // 3 changes: lose 2, lose 3, gain 1
        AssertJUnit.assertEquals("Expected 3 LIFE log entries", 3, lifeEntries.size());
    }
}
