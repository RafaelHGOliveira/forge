package forge.screens.match.arena;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class CommanderDeckExtractorTest {
    @Test public void deckWithOneCommander_returnsName() {
        Optional<String> name = CommanderDeckExtractor.extractName(() -> Arrays.asList("Edgar Markov"));
        assertTrue(name.isPresent());
        assertEquals(name.get(), "Edgar Markov");
    }
    @Test public void deckWithPartners_returnsFirst() {
        Optional<String> name = CommanderDeckExtractor.extractName(() -> Arrays.asList("Thrasios", "Tymna"));
        assertEquals(name.get(), "Thrasios");
    }
    @Test public void deckWithEmptySection_returnsEmpty() {
        Optional<String> name = CommanderDeckExtractor.extractName(Collections::emptyList);
        assertFalse(name.isPresent());
    }
    @Test public void nullSupplier_returnsEmpty() {
        Optional<String> name = CommanderDeckExtractor.extractName(null);
        assertFalse(name.isPresent());
    }
    @Test public void nullCards_returnsEmpty() {
        Optional<String> name = CommanderDeckExtractor.extractName(() -> null);
        assertFalse(name.isPresent());
    }
    @Test public void hasPartner_twoOrMoreCards() {
        assertFalse(CommanderDeckExtractor.hasPartner(Collections.emptyList()));
        assertFalse(CommanderDeckExtractor.hasPartner(Arrays.asList("Edgar")));
        assertTrue(CommanderDeckExtractor.hasPartner(Arrays.asList("Thrasios", "Tymna")));
    }
}
