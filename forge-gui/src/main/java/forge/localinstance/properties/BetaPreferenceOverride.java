package forge.localinstance.properties;

import java.util.Properties;
import java.util.function.BiConsumer;

public final class BetaPreferenceOverride {
    public static final String SYSTEM_PROPERTY = "forge.commander.enhanced";
    public static final String PREF_KEY = "UI_COMMANDER_ENHANCED";

    private BetaPreferenceOverride() {}

    public static boolean apply(final BiConsumer<String, String> prefSetter,
                                 final Properties systemProps) {
        String v = systemProps.getProperty(SYSTEM_PROPERTY);
        if (!"true".equals(v)) return false;
        prefSetter.accept(PREF_KEY, "true");
        return true;
    }
}
