package forge.screens.match.arena;

import forge.game.zone.ZoneType;

public final class ZoneBarState {
    public enum Result { OPENED, CLOSED, SWITCHED, IGNORED }

    private final boolean isLocalPlayer;
    private ZoneType expanded;

    public ZoneBarState(final boolean isLocalPlayer) { this.isLocalPlayer = isLocalPlayer; }
    public ZoneType getExpandedZone() { return expanded; }

    public boolean canExpand(final ZoneType zone) {
        if (zone == ZoneType.Hand) return isLocalPlayer;
        return zone == ZoneType.Command || zone == ZoneType.Graveyard || zone == ZoneType.Exile;
    }

    public Result toggle(final ZoneType zone) {
        if (!canExpand(zone)) return Result.IGNORED;
        if (expanded == zone) { expanded = null; return Result.CLOSED; }
        Result r = (expanded == null) ? Result.OPENED : Result.SWITCHED;
        expanded = zone;
        return r;
    }
}
