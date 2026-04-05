package forge.screens.match.arena;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import net.miginfocom.swing.MigLayout;
import forge.game.card.CardView;
import forge.game.player.PlayerView;

public class CommanderDamageView extends JPanel {
    private static final long serialVersionUID = 1L;

    private final PlayerView localPlayer;
    private final List<PlayerView> opponents;

    public CommanderDamageView(final PlayerView localPlayer,
                                final Iterable<PlayerView> opponents) {
        super(new MigLayout("insets 0, gap 2, flowy, fillx"));
        setOpaque(false);
        this.localPlayer = localPlayer;
        this.opponents = new ArrayList<>();
        if (opponents != null) for (PlayerView o : opponents) this.opponents.add(o);
        buildRows();
        setVisible(totalDamage() > 0);
    }

    private void buildRows() {
        removeAll();
        JLabel title = new JLabel("DANO CMD");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 7f));
        title.setForeground(new Color(124, 58, 237));
        add(title, "gapbottom 2");
        for (PlayerView opp : opponents) add(buildRow(opp), "growx");
    }

    private int totalDamage() {
        int total = 0;
        for (PlayerView opp : opponents) total += computeDamageFor(opp);
        return total;
    }

    private JPanel buildRow(final PlayerView opp) {
        JPanel row = new JPanel(new MigLayout("insets 0, gap 3"));
        row.setOpaque(false);
        int dmg = computeDamageFor(opp);

        JLabel name = new JLabel(NameFormatter.shortName(opp.getName()));
        name.setFont(name.getFont().deriveFont(7f));
        name.setForeground(new Color(148, 163, 184));

        JProgressBar bar = new JProgressBar(0, CommanderDamageCalculator.LETHAL);
        bar.setValue(dmg);
        bar.setPreferredSize(new Dimension(32, 5));
        bar.setBorderPainted(false);
        bar.setForeground(
            CommanderDamageCalculator.isLethal(dmg) ? new Color(239, 68, 68)
          : CommanderDamageCalculator.isWarning(dmg) ? new Color(239, 68, 68)
          : new Color(34, 197, 94));

        JLabel val = new JLabel(String.valueOf(dmg));
        val.setFont(val.getFont().deriveFont(Font.BOLD, 7f));
        val.setForeground((CommanderDamageCalculator.isWarning(dmg) || CommanderDamageCalculator.isLethal(dmg))
            ? new Color(239, 68, 68) : new Color(226, 232, 240));

        row.add(name, "w 28!");
        row.add(bar, "w 32!");
        row.add(val, "w 14!, align right");
        return row;
    }

    private int computeDamageFor(final PlayerView opp) {
        if (localPlayer == null || opp == null) return 0;
        List<CardView> cmds = opp.getCommanders();
        if (cmds == null) return 0;
        List<Integer> ids = new ArrayList<>(cmds.size());
        for (CardView c : cmds) ids.add(c.getId());
        return CommanderDamageCalculator.sumDamage(ids,
            cmdId -> {
                for (CardView c : opp.getCommanders()) {
                    if (c.getId() == cmdId) return localPlayer.getCommanderDamage(c);
                }
                return 0;
            });
    }

    public void refresh() {
        final int total = totalDamage();
        setVisible(total > 0);
        if (total > 0) {
            buildRows();
            revalidate();
            repaint();
        }
    }
}
