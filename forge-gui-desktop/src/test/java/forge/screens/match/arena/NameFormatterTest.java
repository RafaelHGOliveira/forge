package forge.screens.match.arena;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class NameFormatterTest {
    @Test public void shortName_null_returnsQuestionMark() { assertEquals(NameFormatter.shortName(null), "?"); }
    @Test public void shortName_empty_returnsEmpty()       { assertEquals(NameFormatter.shortName(""), ""); }
    @Test public void shortName_underSixChars_unchanged()  { assertEquals(NameFormatter.shortName("Alice"), "Alice"); }
    @Test public void shortName_exactlySix_unchanged()     { assertEquals(NameFormatter.shortName("Bobbie"), "Bobbie"); }
    @Test public void shortName_overSix_truncatedWithEllipsis() { assertEquals(NameFormatter.shortName("Alexander"), "Alexa\u2026"); }
    @Test public void shortName_sevenChars_truncated()     { assertEquals(NameFormatter.shortName("Charlie"), "Charl\u2026"); }
}
