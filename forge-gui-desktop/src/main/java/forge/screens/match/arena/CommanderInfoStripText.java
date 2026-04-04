package forge.screens.match.arena;

public final class CommanderInfoStripText {
    private CommanderInfoStripText() {}

    public static String format(final int playerCount) {
        String word = (playerCount == 1) ? "player" : "players";
        return "COMMANDER \u00b7 " + playerCount + " " + word
            + " \u00b7 Starting life: 40 \u00b7 CMD damage lethal: 21";
    }
}
