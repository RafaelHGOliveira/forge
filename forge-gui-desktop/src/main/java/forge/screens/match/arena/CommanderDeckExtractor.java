package forge.screens.match.arena;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class CommanderDeckExtractor {
    private CommanderDeckExtractor() {}

    public static Optional<String> extractName(final Supplier<Iterable<String>> cardNamesSupplier) {
        if (cardNamesSupplier == null) return Optional.empty();
        Iterable<String> cards = cardNamesSupplier.get();
        if (cards == null) return Optional.empty();
        Iterator<String> it = cards.iterator();
        if (!it.hasNext()) return Optional.empty();
        return Optional.ofNullable(it.next());
    }

    public static boolean hasPartner(final List<String> commanderNames) {
        return commanderNames != null && commanderNames.size() >= 2;
    }
}
