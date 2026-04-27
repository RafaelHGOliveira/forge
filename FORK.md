# Fork Differences vs Card-Forge/forge

This file tracks what's unique in this fork (RafaelHGOliveira/forge) relative to upstream.
Updated: 2026-04-27 (master reset to upstream/master + selective PR merges; +#9806 card info tooltips).

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

## Custom features (not submitted upstream)

### Disconnect indicator + Replace-with-AI button

Per-player button in `VField`; host calls `FServerManager.replaceDisconnectedWithAI(username)`. Requires `RemoteClientGuiGame.getSlotIndex()` helper (delegates to `client.getIndex()`).

Branch: `feat/disconnect-indicator-replace-ai` (also merged into master).

Key files: `VField.java`, `CMatchUI.java`, `FServerManager.java`, `RemoteClientGuiGame.java`, `IGuiGame.java`

## Upstream PRs evaluated but NOT merged (conflict-heavy — deferred)

These were attempted with `-X theirs` but produced compile failures due to deep semantic overlap with already-merged code or with each other. Re-evaluate once upstream merges any of them, or merge selectively with manual conflict resolution.

| PR | Reason deferred |
|----|-----------------|
| [#9643](https://github.com/Card-Forge/forge/pull/9643) | Yield rework — overlaps heavily with our auto-pass/yield code paths in `PlayerControllerHuman`, `IGameController`, `ProtocolMethod`, `RemoteClientGuiGame` |
| [#10466](https://github.com/Card-Forge/forge/pull/10466) | Client-side automatic reconnect — overlaps with our disconnect-indicator branch (`NetworkGuiGame`, `FServerManager`, `GameClientHandler`) |
| [#10506](https://github.com/Card-Forge/forge/pull/10506) | Auto-triggers / `PersistentYieldStore` → `PersistentAutoDecisionStore` rename — overlaps with #9643 yield system and our existing yield code |

### Notes for future merge attempts

- `#9643` and `#10506` together rewrite the yield/trigger subsystem; merging needs upstream to land #9643 first OR a careful manual port that picks one design.
- `#10466` and our disconnect-indicator branch both touch reconnect/disconnect plumbing in `FServerManager` — pick one approach.
- `#9806` is mostly additive but its prefs entries collide with other open PRs touching `ForgePreferences` enum ordering.

## Backup branches

- `master-backup-2026-04-27` (local + origin) — snapshot of master at commit `3866e2fadd0` before the reset to upstream and selective re-merge.
