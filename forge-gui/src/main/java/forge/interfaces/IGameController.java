package forge.interfaces;

import java.util.List;

import forge.game.card.CardView;
import forge.game.phase.PhaseType;
import forge.game.player.PlayerView;
import forge.game.spellability.SpellAbilityView;
import forge.gamemodes.match.NextGameDecision;
import forge.gamemodes.match.YieldMarker;
import forge.gamemodes.match.YieldPrefs;
import forge.localinstance.properties.ForgePreferences;
import forge.player.AutoYieldStore;
import forge.util.ITriggerEvent;

public interface IGameController {

    boolean mayLookAtAllCards();

    boolean canPlayUnlimitedLands();

    void concede();

    void alphaStrike();

    void useMana(byte color);

    void selectButtonOk();

    void selectButtonCancel();

    void passPriority();

    void passPriorityUntilEndOfTurn();

    void selectPlayer(PlayerView playerView, ITriggerEvent triggerEvent);

    boolean selectCard(CardView cardView, List<CardView> otherCardViewsToSelect, ITriggerEvent triggerEvent);

    void selectAbility(SpellAbilityView sa);

    void undoLastAction();

    IDevModeCheats cheat();

    IMacroSystem macros();

    void nextGameDecision(NextGameDecision decision);

    String getActivateDescription(CardView card);

    void reorderHand(CardView card, int index);

    /** Request a full state resync from the server. */
    void requestResync();

    // --- Auto-yield preferences (per-player) ---
    boolean shouldAutoYield(String key);
    /**
     * @param isAbilityScope true if {@code key} is an ability suffix (Per Ability * modes);
     *   false if {@code key} is the full raw key (Per Card mode). Server-side handlers
     *   route storage by this flag instead of consulting the host's own UI_AUTO_YIELD_MODE.
     */
    void setShouldAutoYield(String key, boolean autoYield, boolean isAbilityScope);
    Iterable<String> getAutoYields();
    void clearAutoYields();
    boolean getDisableAutoYields();
    void setDisableAutoYields(boolean disable);

    // --- Trigger accept/decline preferences (per-player) ---
    boolean shouldAlwaysAcceptTrigger(String key);
    boolean shouldAlwaysDeclineTrigger(String key);
    void setShouldAlwaysAcceptTrigger(String key, boolean isAbilityScope);
    void setShouldAlwaysDeclineTrigger(String key, boolean isAbilityScope);
    void setShouldAlwaysAskTrigger(String key, boolean isAbilityScope);
    Iterable<java.util.Map.Entry<String, AutoYieldStore.TriggerDecision>> getAutoTriggers();
    boolean getDisableAutoTriggers();
    void setDisableAutoTriggers(boolean disable);

    void setUiShouldSkipPhase(PlayerView turnPlayer, PhaseType phase, boolean shouldSkip);

    // --- Yield marker (phase-targeted) and stack-yield state (per-player) ---
    default YieldMarker getYieldMarker() { return null; }
    default void setYieldMarker(PlayerView phaseOwner, PhaseType phase) { }
    default void clearYieldMarker() { }

    default boolean isStackYieldActive() { return false; }
    default void setStackYield(boolean active) { }

    // --- Interrupt preferences (per-player) ---
    default boolean getYieldInterruptPref(ForgePreferences.FPref pref) { return false; }
    default void setYieldInterruptPref(ForgePreferences.FPref pref, boolean value) { }
    default YieldPrefs getYieldPrefs() { return null; }
    default void setYieldPrefs(YieldPrefs prefs) { }
}
