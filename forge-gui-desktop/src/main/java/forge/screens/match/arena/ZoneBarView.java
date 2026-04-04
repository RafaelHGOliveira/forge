package forge.screens.match.arena;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import forge.game.player.PlayerView;
import forge.game.zone.ZoneType;

public class ZoneBarView extends JPanel {
    private static final long serialVersionUID = 1L;

    private final ZoneBarState state;
    private final Map<ZoneType, JLabel> counts = new EnumMap<>(ZoneType.class);
    private final Map<ZoneType, JButton> buttons = new EnumMap<>(ZoneType.class);
    private PlayerView player;
    private Consumer<ZoneBarState.Result> onToggle;

    public ZoneBarView(final boolean isLocalPlayer) {
        super(new MigLayout("insets 2, gap 3"));
        setOpaque(false);
        this.state = new ZoneBarState(isLocalPlayer);

        addZoneButton(ZoneType.Command,   "CMD",  new Color(124, 58, 237));
        addZoneButton(ZoneType.Graveyard, "GY",   new Color(239, 68, 68));
        addZoneButton(ZoneType.Exile,     "EX",   new Color(59, 130, 246));
        addZoneButton(ZoneType.Hand,      "HAND", new Color(34, 197, 94));
        if (!isLocalPlayer) buttons.get(ZoneType.Hand).setEnabled(false);
    }

    private void addZoneButton(final ZoneType zone, final String label, final Color accent) {
        JButton b = new JButton(label);
        b.setFont(b.getFont().deriveFont(10f));
        b.setForeground(accent);
        b.setMargin(new Insets(2, 6, 2, 6));
        b.setFocusable(false);
        b.addActionListener(e -> {
            ZoneBarState.Result r = state.toggle(zone);
            if (onToggle != null) onToggle.accept(r);
        });
        JLabel c = new JLabel("0");
        c.setFont(c.getFont().deriveFont(Font.BOLD, 10f));
        c.setForeground(new Color(226, 232, 240));
        buttons.put(zone, b);
        counts.put(zone, c);
        add(b);
        add(c);
    }

    public void bind(final PlayerView p) { this.player = p; refresh(); }
    public void setOnToggle(final Consumer<ZoneBarState.Result> h) { this.onToggle = h; }
    public ZoneBarState getState() { return state; }

    public void refresh() {
        if (player == null) return;
        updateCount(ZoneType.Command,   player.getZoneSize(ZoneType.Command));
        updateCount(ZoneType.Graveyard, player.getZoneSize(ZoneType.Graveyard));
        updateCount(ZoneType.Exile,     player.getZoneSize(ZoneType.Exile));
        updateCount(ZoneType.Hand,      player.getZoneSize(ZoneType.Hand));
    }

    private void updateCount(final ZoneType zone, final int count) {
        JLabel lbl = counts.get(zone);
        if (lbl != null) lbl.setText(String.valueOf(count));
    }
}
