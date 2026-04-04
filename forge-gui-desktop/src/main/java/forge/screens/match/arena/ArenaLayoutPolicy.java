package forge.screens.match.arena;

public final class ArenaLayoutPolicy {
    public static final int PLAYERS_REQUIRED = 4;
    public static final String LAYOUT_VALUE = "ARENA";
    public static final double PHASE_STRIP_PCT = 0.03;
    public static final double OPPONENT_BAND_PCT = 0.40;

    private ArenaLayoutPolicy() {}

    public static boolean shouldActivate(final int playerCount,
                                          final String layoutPref,
                                          final boolean enhancedEnabled) {
        return enhancedEnabled
            && playerCount == PLAYERS_REQUIRED
            && LAYOUT_VALUE.equals(layoutPref);
    }

    public static final class Bounds {
        public final double x, w;
        public final double phaseY, phaseH;
        public final double opponentY, opponentH;
        public final double localY, localH;
        Bounds(double x, double w, double phaseY, double phaseH,
               double oppY, double oppH, double locY, double locH) {
            this.x = x; this.w = w;
            this.phaseY = phaseY; this.phaseH = phaseH;
            this.opponentY = oppY; this.opponentH = oppH;
            this.localY = locY; this.localH = locH;
        }
    }

    public static Bounds computeBounds(final double x, final double y,
                                        final double w, final double h) {
        double phaseH = h * PHASE_STRIP_PCT;
        double oppH = h * OPPONENT_BAND_PCT;
        double locH = h - phaseH - oppH;
        return new Bounds(x, w, y, phaseH, y + phaseH, oppH, y + phaseH + oppH, locH);
    }
}
