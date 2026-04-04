package forge.screens.match.arena;

import java.util.List;

public final class CommanderDamageCalculator {
    public static final int LETHAL = 21;
    public static final int WARN = 18;

    public interface DamageLookup {
        int getDamage(int commanderId);
    }

    private CommanderDamageCalculator() {}

    public static int sumDamage(final List<Integer> commanderIds, final DamageLookup lookup) {
        if (commanderIds == null || lookup == null) return 0;
        int total = 0;
        for (Integer id : commanderIds) {
            if (id == null) continue;
            total += lookup.getDamage(id);
        }
        return total;
    }

    public static boolean isLethal(int damage) { return damage >= LETHAL; }
    public static boolean isWarning(int damage) { return damage >= WARN && damage < LETHAL; }
}
