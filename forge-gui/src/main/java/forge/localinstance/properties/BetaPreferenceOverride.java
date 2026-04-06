package forge.localinstance.properties;

import java.util.Properties;
import java.util.function.BiConsumer;

public final class BetaPreferenceOverride {
    public static final String SYSTEM_PROPERTY = "forge.commander.enhanced";
    public static final String PREF_KEY = "UI_COMMANDER_ENHANCED";
    public static final String LAYOUT_KEY = "UI_MULTIPLAYER_FIELD_LAYOUT";
    public static final String ARENA_VALUE = "ARENA";

    private BetaPreferenceOverride() {}

    /** Returns true if the JVM was launched with {@code -Dforge.commander.enhanced=true}. */
    public static boolean isBetaLaunch() {
        return "true".equals(System.getProperty(SYSTEM_PROPERTY));
    }

    /**
     * Unconditionally applies beta arena preferences.
     * Callers are responsible for deciding when to call this vs {@link #reset}.
     */
    public static void apply(final BiConsumer<String, String> prefSetter,
                             final java.util.function.Supplier<String> currentLayoutGetter) {
        prefSetter.accept(PREF_KEY, "true");
        // Default layout to ARENA only when the user hasn't changed it from "OFF"
        if ("OFF".equals(currentLayoutGetter.get())) {
            prefSetter.accept(LAYOUT_KEY, ARENA_VALUE);
        }
    }

    /** Convenience overload: always defaults layout to ARENA when layout pref is "OFF". */
    public static void apply(final BiConsumer<String, String> prefSetter) {
        apply(prefSetter, () -> "OFF");
    }

    /** @deprecated Use {@link #isBetaLaunch()} + {@link #apply}/{@link #reset} separately. */
    @Deprecated
    public static boolean apply(final BiConsumer<String, String> prefSetter,
                                 final Properties systemProps,
                                 final java.util.function.Supplier<String> currentLayoutGetter) {
        if (!isBetaLaunch()) return false;
        apply(prefSetter, currentLayoutGetter);
        return true;
    }

    /**
     * Resets beta-only preferences so the stable launcher is never contaminated
     * by a previous forge-beta session that persisted its overrides.
     */
    public static void reset(final BiConsumer<String, String> prefSetter) {
        prefSetter.accept(PREF_KEY, "false");
        prefSetter.accept(LAYOUT_KEY, "OFF");
    }
}
