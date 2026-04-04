package forge.screens.match.arena;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import java.util.Arrays;
import java.util.Collections;

public class CommanderDeckExtractorTest {
    @Test public void deckWithOneCommander_returnsName() {
        String name = CommanderDeckExtractor.extractName(() -> Arrays.asList("Edgar Markov"));
        assertEquals(name, "Edgar Markov");
    }
    @Test public void deckWithPartners_returnsFirst() {
        String name = CommanderDeckExtractor.extractName(() -> Arrays.asList("Thrasios", "Tymna"));
        assertEquals(name, "Thrasios");
    }
    @Test public void deckWithEmptySection_returnsEmpty() {
        String name = CommanderDeckExtractor.extractName(Collections::emptyList);
        assertEquals(name, "?");
    }
    @Test public void nullSupplier_returnsEmpty() {
        String name = CommanderDeckExtractor.extractName(null);
        assertEquals(name, "?");
    }
    @Test public void nullCards_returnsEmpty() {
        String name = CommanderDeckExtractor.extractName(() -> null);
        assertEquals(name, "?");
    }
    @Test public void hasPartner_twoOrMoreCards() {
        assertFalse(CommanderDeckExtractor.hasPartner(Collections.emptyList()));
        assertFalse(CommanderDeckExtractor.hasPartner(Arrays.asList("Edgar")));
        assertTrue(CommanderDeckExtractor.hasPartner(Arrays.asList("Thrasios", "Tymna")));
    }
}
