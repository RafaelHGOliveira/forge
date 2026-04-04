package forge.screens.match.arena;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public final class CommanderDeckExtractor {
    private CommanderDeckExtractor() {}

    public static String extractName(final Supplier<Iterable<String>> cardNamesSupplier) {
        if (cardNamesSupplier == null) return "?";
        Iterable<String> cards = cardNamesSupplier.get();
        if (cards == null) return "?";
        Iterator<String> it = cards.iterator();
        if (!it.hasNext()) return "?";
        String name = it.next();
        return name != null ? name : "?";
    }

    public static boolean hasPartner(final List<String> commanderNames) {
        return commanderNames != null && commanderNames.size() >= 2;
    }
}
