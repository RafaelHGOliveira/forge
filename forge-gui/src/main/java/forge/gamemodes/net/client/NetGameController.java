package forge.gamemodes.net.client;

import forge.game.card.CardView;
import forge.game.phase.PhaseType;
import forge.game.player.PlayerView;
import forge.game.player.actions.PlayerAction;
import forge.game.spellability.SpellAbilityView;
import forge.gamemodes.match.NextGameDecision;
import forge.gamemodes.net.GameProtocolSender;
import forge.gamemodes.net.ProtocolMethod;
import forge.interfaces.IDevModeCheats;
import forge.interfaces.IGameController;
import forge.interfaces.IMacroSystem;
import forge.localinstance.properties.ForgeConstants;
import forge.localinstance.properties.ForgePreferences;
import forge.model.FModel;
import forge.player.AutoYieldStore;
import forge.player.PersistentAutoDecisionStore;
import forge.util.ITriggerEvent;

import java.util.List;

public class NetGameController implements IGameController {

    private final GameProtocolSender sender;

    private final AutoYieldStore yieldStore = new AutoYieldStore();

    public NetGameController(final IToServer server) {
        sender = new GameProtocolSender(server);
    }

    private void send(final ProtocolMethod method, final Object... args) {
        sender.send(method, args);
    }

    private <T> T sendAndWait(final ProtocolMethod method, final Object... args) {
        return sender.sendAndWait(method, args);
    }

    @Override
    public void useMana(final byte color) {
        send(ProtocolMethod.useMana, color);
    }

    @Override
    public void undoLastAction() {
        send(ProtocolMethod.undoLastAction);
    }

    @Override
    public void selectPlayer(final PlayerView playerView, final ITriggerEvent triggerEvent) {
        send(ProtocolMethod.selectPlayer, playerView, null/*triggerEvent*/); //some platform don't have mousetriggerevent class or it will not allow them to click/tap
    }

    @Override
    public boolean selectCard(final CardView cardView, final List<CardView> otherCardViewsToSelect, final ITriggerEvent triggerEvent) {
        send(ProtocolMethod.selectCard, cardView, otherCardViewsToSelect, null/*triggerEvent*/); //some platform don't have mousetriggerevent class or it will not allow them to click/tap
        // Difference from local games! Always consider a card as successfully selected,
        // to avoid blocks where server and client wait for each other to respond.
        // Some cost in functionality but a huge gain in stability & speed.
        return true;
    }

    @Override
    public void selectButtonOk() {
        send(ProtocolMethod.selectButtonOk);
    }

    @Override
    public void selectButtonCancel() {
        send(ProtocolMethod.selectButtonCancel);
    }

    @Override
    public void selectAbility(final SpellAbilityView sa) {
        send(ProtocolMethod.selectAbility, sa);
    }

    @Override
    public void passPriorityUntilEndOfTurn() {
        send(ProtocolMethod.passPriorityUntilEndOfTurn);
    }

    @Override
    public void passPriority() {
        send(ProtocolMethod.passPriority);
    }

    @Override
    public void nextGameDecision(final NextGameDecision decision) {
        send(ProtocolMethod.nextGameDecision, decision);
    }

    @Override
    public boolean mayLookAtAllCards() {
        // Don't do this over network
        return false;
    }

    @Override
    public String getActivateDescription(final CardView card) {
        return sendAndWait(ProtocolMethod.getActivateDescription, card);
    }

    @Override
    public void concede() {
        send(ProtocolMethod.concede);
    }

    @Override
    public IDevModeCheats cheat() {
        // No cheating in network games!
        return IDevModeCheats.NO_CHEAT;
    }

    @Override
    public boolean canPlayUnlimitedLands() {
        // Don't do this over network
        return false;
    }

    @Override
    public void alphaStrike() {
        send(ProtocolMethod.alphaStrike);
    }

    @Override
    public void reorderHand(final CardView card, final int index) {
        send(ProtocolMethod.reorderHand, card, index);
    }

    @Override
    public void requestResync() {
        send(ProtocolMethod.requestResync);
    }

    private boolean activeModeIsInstall() {
        return ForgeConstants.AUTO_YIELD_PER_ABILITY_INSTALL.equals(
                FModel.getPreferences().getPref(ForgePreferences.FPref.UI_AUTO_YIELD_MODE));
    }

    private boolean activeModeIsAbilityScope() {
        return !ForgeConstants.AUTO_YIELD_PER_CARD.equals(
                FModel.getPreferences().getPref(ForgePreferences.FPref.UI_AUTO_YIELD_MODE));
    }

    private AutoYieldStore.Tier activeTier() {
        String mode = FModel.getPreferences().getPref(ForgePreferences.FPref.UI_AUTO_YIELD_MODE);
        if (ForgeConstants.AUTO_YIELD_PER_CARD.equals(mode))            return AutoYieldStore.Tier.GAME;
        if (ForgeConstants.AUTO_YIELD_PER_ABILITY_SESSION.equals(mode)) return AutoYieldStore.Tier.SESSION;
        return AutoYieldStore.Tier.MATCH;
    }

    @Override
    public boolean shouldAutoYield(final String key) {
        if (yieldStore.isDisabled()) return false;
        if (activeModeIsInstall()) {
            return PersistentAutoDecisionStore.get().contains(AutoYieldStore.abilitySuffix(key));
        }
        String storageKey = activeModeIsAbilityScope() ? AutoYieldStore.abilitySuffix(key) : key;
        return yieldStore.shouldYield(activeTier(), storageKey);
    }

    @Override
    public void setShouldAutoYield(final String key, final boolean autoYield, final boolean isAbilityScope) {
        String storageKey = isAbilityScope ? AutoYieldStore.abilitySuffix(key) : key;
        if (activeModeIsInstall()) {
            PersistentAutoDecisionStore.get().setYield(storageKey, autoYield);
        } else {
            yieldStore.setYield(activeTier(), storageKey, autoYield);
        }
        send(ProtocolMethod.setShouldAutoYield, storageKey, autoYield, isAbilityScope);
    }

    @Override
    public Iterable<String> getAutoYields() {
        return activeModeIsInstall()
                ? PersistentAutoDecisionStore.get().getYields()
                : yieldStore.getYields(activeTier());
    }

    @Override
    public void clearAutoYields() {
        // No-op locally: tier lifecycle is driven separately. Server-side mirror is cleared by HostedMatch.
    }

    @Override
    public boolean getDisableAutoYields() { return yieldStore.isDisabled(); }

    @Override
    public void setDisableAutoYields(final boolean disable) {
        yieldStore.setDisabled(disable);
        send(ProtocolMethod.setDisableAutoYields, disable);
    }

    @Override
    public boolean shouldAlwaysAcceptTrigger(final String key) {
        if (yieldStore.isTriggerDecisionsDisabled()) return false;
        return readTriggerDecision(key) == AutoYieldStore.TriggerDecision.ACCEPT;
    }

    @Override
    public boolean shouldAlwaysDeclineTrigger(final String key) {
        if (yieldStore.isTriggerDecisionsDisabled()) return false;
        return readTriggerDecision(key) == AutoYieldStore.TriggerDecision.DECLINE;
    }

    @Override
    public void setShouldAlwaysAcceptTrigger(final String key, final boolean isAbilityScope) {
        String storageKey = isAbilityScope ? AutoYieldStore.abilitySuffix(key) : key;
        writeTriggerDecisionLocal(storageKey, AutoYieldStore.TriggerDecision.ACCEPT);
        send(ProtocolMethod.setShouldAlwaysAcceptTrigger, storageKey, isAbilityScope);
    }

    @Override
    public void setShouldAlwaysDeclineTrigger(final String key, final boolean isAbilityScope) {
        String storageKey = isAbilityScope ? AutoYieldStore.abilitySuffix(key) : key;
        writeTriggerDecisionLocal(storageKey, AutoYieldStore.TriggerDecision.DECLINE);
        send(ProtocolMethod.setShouldAlwaysDeclineTrigger, storageKey, isAbilityScope);
    }

    @Override
    public void setShouldAlwaysAskTrigger(final String key, final boolean isAbilityScope) {
        String storageKey = isAbilityScope ? AutoYieldStore.abilitySuffix(key) : key;
        writeTriggerDecisionLocal(storageKey, AutoYieldStore.TriggerDecision.ASK);
        send(ProtocolMethod.setShouldAlwaysAskTrigger, storageKey, isAbilityScope);
    }

    @Override
    public Iterable<java.util.Map.Entry<String, AutoYieldStore.TriggerDecision>> getAutoTriggers() {
        return activeTriggerModeIsInstall()
                ? PersistentAutoDecisionStore.get().getAutoTriggers()
                : yieldStore.getAutoTriggers(activeTriggerTier());
    }

    @Override
    public boolean getDisableAutoTriggers() { return yieldStore.isTriggerDecisionsDisabled(); }

    @Override
    public void setDisableAutoTriggers(final boolean disable) {
        yieldStore.setTriggerDecisionsDisabled(disable);
        send(ProtocolMethod.setDisableAutoTriggers, disable);
    }

    private AutoYieldStore.TriggerDecision readTriggerDecision(final String key) {
        if (key == null || key.isEmpty()) return AutoYieldStore.TriggerDecision.ASK;
        if (activeTriggerModeIsInstall()) return PersistentAutoDecisionStore.get().getTriggerDecision(AutoYieldStore.abilitySuffix(key));
        String storageKey = activeTriggerModeIsAbilityScope() ? AutoYieldStore.abilitySuffix(key) : key;
        return yieldStore.getTriggerDecision(activeTriggerTier(), storageKey);
    }

    private void writeTriggerDecisionLocal(final String storageKey, final AutoYieldStore.TriggerDecision decision) {
        if (activeTriggerModeIsInstall()) {
            PersistentAutoDecisionStore.get().setTriggerDecision(storageKey, decision);
        } else {
            yieldStore.setTriggerDecision(activeTriggerTier(), storageKey, decision);
        }
    }

    private boolean activeTriggerModeIsInstall() {
        return ForgeConstants.AUTO_TRIGGER_PER_ABILITY_INSTALL.equals(
                FModel.getPreferences().getPref(ForgePreferences.FPref.UI_AUTO_TRIGGER_MODE));
    }

    private boolean activeTriggerModeIsAbilityScope() {
        return !ForgeConstants.AUTO_TRIGGER_PER_CARD.equals(
                FModel.getPreferences().getPref(ForgePreferences.FPref.UI_AUTO_TRIGGER_MODE));
    }

    private AutoYieldStore.Tier activeTriggerTier() {
        String mode = FModel.getPreferences().getPref(ForgePreferences.FPref.UI_AUTO_TRIGGER_MODE);
        if (ForgeConstants.AUTO_TRIGGER_PER_CARD.equals(mode))            return AutoYieldStore.Tier.GAME;
        if (ForgeConstants.AUTO_TRIGGER_PER_ABILITY_SESSION.equals(mode)) return AutoYieldStore.Tier.SESSION;
        return AutoYieldStore.Tier.MATCH;
    }

    public void replayActiveYields() {
        boolean abilityScope = activeModeIsAbilityScope();
        for (String key : getAutoYields()) {
            send(ProtocolMethod.setShouldAutoYield, key, Boolean.TRUE, abilityScope);
        }
    }

    public void replayActiveTriggerDecisions() {
        boolean abilityScope = activeTriggerModeIsAbilityScope();
        for (java.util.Map.Entry<String, AutoYieldStore.TriggerDecision> entry : getAutoTriggers()) {
            switch (entry.getValue()) {
                case ACCEPT:
                    send(ProtocolMethod.setShouldAlwaysAcceptTrigger, entry.getKey(), abilityScope);
                    break;
                case DECLINE:
                    send(ProtocolMethod.setShouldAlwaysDeclineTrigger, entry.getKey(), abilityScope);
                    break;
                case ASK:
                    break;
            }
        }
    }

    @Override
    public void setUiShouldSkipPhase(final PlayerView turnPlayer, final PhaseType phase, final boolean shouldSkip) {
        send(ProtocolMethod.setUiShouldSkipPhase, turnPlayer, phase, shouldSkip);
    }

    private IMacroSystem macros;
    @Override
    public IMacroSystem macros() {
        if (macros == null) {
            macros = new NetMacroSystem();
        }
        return macros;
    }
    public class NetMacroSystem implements IMacroSystem {
        @Override
        public void addRememberedAction(PlayerAction action) {
            // DO i need to send this?
        }

        @Override
        public void setRememberedActions() {
            send(ProtocolMethod.setRememberedActions);
        }

        @Override
        public void nextRememberedAction() {
            send(ProtocolMethod.nextRememberedAction);
        }

        @Override
        public boolean isRecording() {
            return false;
        }

        @Override
        public String playbackText() {
            return null;
        }
    }
}
