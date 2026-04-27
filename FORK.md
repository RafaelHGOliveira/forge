# Fork Differences vs Card-Forge/forge

This file tracks what's unique in this fork (RafaelHGOliveira/forge) relative to upstream.
Updated: 2026-04-27 (master reset to upstream/master + selective PR merges; +#9806 card info tooltips; +#10466 client-side reconnect; +#10506 persistent always-yes/no triggers; +#9643 expanded yield system).

## Open PRs submitted upstream (awaiting merge)

| PR | Status | Description |
|----|--------|-------------|
| [#10533](https://github.com/Card-Forge/forge/pull/10533) | OPEN | Auto-call coin flips for win/lose outcomes — `UI_AUTO_CALL_COIN_FLIP` pref skips redundant Heads/Tails prompt when only the call result matters (Krark's Thumb, Mana Clash, etc.) |
| [#10483](https://github.com/Card-Forge/forge/pull/10483) | OPEN | Respect mana-loss prompt during phase skip and auto-pass |
| [#10376](https://github.com/Card-Forge/forge/pull/10376) | OPEN | `FLabel` icon scaling via `min(w,h)` + per-cell `DragCell.minW` for VField — prevents overflow in narrow panels |
| [#10247](https://github.com/Card-Forge/forge/pull/10247) | OPEN | URL-based deck import (EDHREC, Moxfield, Archidekt, TappedOut, MTGGoldfish, Deckstats) — has open reviewer feedback (Moxfield permission, EDHREC JSON endpoint, TappedOut sections) |

## Recently merged upstream (originated here)

| PR | Description |
|----|-------------|
| [#10445](https://github.com/Card-Forge/forge/pull/10445) | `feat(net): show all network interfaces when hosting a match` — merged into upstream/master |

## Upstream PRs ported into this fork (still open upstream)

These PRs are open in Card-Forge/forge and were merged here on top of `upstream/master`.

| PR | Description |
|----|-------------|
| [#10258](https://github.com/Card-Forge/forge/pull/10258) | Show commander tax in adjusted mana cost overlay |
| [#9899](https://github.com/Card-Forge/forge/pull/9899) | Host/Join landing page in online lobby |
| [#9751](https://github.com/Card-Forge/forge/pull/9751) | Token grouping (group identical permanents with numeric badge) |
| [#10517](https://github.com/Card-Forge/forge/pull/10517) | Format dropdown and commander shortcuts in desktop deck editor |
| [#9806](https://github.com/Card-Forge/forge/pull/9806) | Card info tooltips — hover/zoom keyword explanations, related cards, and card image overlay |
| [#10466](https://github.com/Card-Forge/forge/pull/10466) | Client-side automatic reconnect — exponential-backoff loop + live modal; `SeatLostEvent`; `/simulatedisconnect` test hook; server HeartbeatEvent echo |
| [#10506](https://github.com/Card-Forge/forge/pull/10506) | Persistent always-yes/no trigger preferences — `PersistentAutoDecisionStore`; trigger choice persists across matches |
| [#9643](https://github.com/Card-Forge/forge/pull/9643) | Expanded yield system / auto-pass options — `YieldController`, `YieldPrefs`, `YieldMarker`; yield-to-phase marker; auto-pass no-actions; stack-yield; per-player yield prefs in multiplayer |

## Custom features (not submitted upstream)

### Disconnect indicator + Replace-with-AI button

Per-player button in `VField`; host calls `FServerManager.replaceDisconnectedWithAI(username)`. Requires `RemoteClientGuiGame.getSlotIndex()` helper (delegates to `client.getIndex()`).

Branch: `feat/disconnect-indicator-replace-ai` (also merged into master).

Key files: `VField.java`, `CMatchUI.java`, `FServerManager.java`, `RemoteClientGuiGame.java`, `IGuiGame.java`

### Notes on merged PRs

- `#9806` conflicts were all additive (prefs enum ordering, settings UI fields, en-US.properties) — resolved manually 2026-04-27.
- `#10506` + `#9643` were merged manually 2026-04-27 with careful conflict resolution; both PRs rewrite the yield/trigger subsystem and were merged in order (#10506 first, then #9643).

## Backup branches

- `master-backup-2026-04-27` (local + origin) — snapshot of master at commit `3866e2fadd0` before the reset to upstream and selective re-merge.
