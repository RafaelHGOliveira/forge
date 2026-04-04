package forge.screens.match.arena;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class CommanderInfoStripTextTest {
    @Test public void fourPlayers_format() {
        assertEquals(CommanderInfoStripText.format(4),
            "COMMANDER \u00b7 4 players \u00b7 Starting life: 40 \u00b7 CMD damage lethal: 21");
    }
    @Test public void twoPlayers_format() {
        assertEquals(CommanderInfoStripText.format(2),
            "COMMANDER \u00b7 2 players \u00b7 Starting life: 40 \u00b7 CMD damage lethal: 21");
    }
    @Test public void onePlayer_singularFormat() {
        assertEquals(CommanderInfoStripText.format(1),
            "COMMANDER \u00b7 1 player \u00b7 Starting life: 40 \u00b7 CMD damage lethal: 21");
    }
    @Test public void zero_stillFormatted() {
        assertEquals(CommanderInfoStripText.format(0),
            "COMMANDER \u00b7 0 players \u00b7 Starting life: 40 \u00b7 CMD damage lethal: 21");
    }
}
