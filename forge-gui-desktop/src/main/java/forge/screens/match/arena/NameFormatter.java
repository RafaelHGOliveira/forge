package forge.screens.match.arena;

public final class NameFormatter {
    public static final int MAX_LENGTH = 6;
    private NameFormatter() {}

    public static String shortName(final String name) {
        if (name == null) return "?";
        if (name.length() <= MAX_LENGTH) return name;
        return name.substring(0, MAX_LENGTH - 1) + "\u2026";
    }
}
