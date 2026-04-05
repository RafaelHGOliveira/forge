package forge.localinstance.properties;

import java.util.Properties;
import java.util.function.BiConsumer;

public final class BetaPreferenceOverride {
    public static final String SYSTEM_PROPERTY = "forge.commander.enhanced";
    public static final String PREF_KEY = "UI_COMMANDER_ENHANCED";
    public static final String LAYOUT_KEY = "UI_MULTIPLAYER_FIELD_LAYOUT";
    public static final String ARENA_VALUE = "ARENA";

    private BetaPreferenceOverride() {}

    public static boolean apply(final BiConsumer<String, String> prefSetter,
                                 final Properties systemProps,
                                 final java.util.function.Supplier<String> currentLayoutGetter) {
        String v = systemProps.getProperty(SYSTEM_PROPERTY);
        if (!"true".equals(v)) return false;
        prefSetter.accept(PREF_KEY, "true");
        // Default layout to ARENA only when the user hasn't changed it from "OFF"
        if ("OFF".equals(currentLayoutGetter.get())) {
            prefSetter.accept(LAYOUT_KEY, ARENA_VALUE);
        }
        return true;
    }

    /** Convenience overload: always defaults layout to ARENA when layout pref is "OFF". */
    public static boolean apply(final BiConsumer<String, String> prefSetter,
                                 final Properties systemProps) {
        return apply(prefSetter, systemProps, () -> "OFF");
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
