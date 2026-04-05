package forge.screens.match.arena;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.util.Collections;

import forge.game.card.CardView;
import forge.game.player.PlayerView;
import forge.game.zone.ZoneType;
import forge.ImageKeys;
import net.miginfocom.swing.MigLayout;

/**
 * Vertical sidebar showing zone thumbnails (CMD, GY, EX, LIB) with card
 * counts. Replaces the horizontal ZoneBarView in Arena layout. Each slot
 * shows the last card that entered the zone; clicking toggles the inline
 * zone panel via the same ZoneBarState toggle API.
 */
public class ZoneSidebarView extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final Color BG_DEFAULT = new Color(18, 18, 28, 220);
    private static final Color BG_HOVER   = new Color(35, 35, 55, 230);
    private static final Color BG_ACTIVE  = new Color(50, 40, 80, 245);

    private static final Color CLR_CMD = new Color(124, 58,  237);
    private static final Color CLR_GY  = new Color(239, 68,  68);
    private static final Color CLR_EX  = new Color(59,  130, 246);
    private static final Color CLR_LIB = new Color(34,  197, 94);

    private static final int THUMB_W = 50;
    private static final int THUMB_H = 70;

    private final ZoneBarState state;
    private final boolean isLocal;
    private PlayerView player;
    private Consumer<ZoneBarState.Result> onToggle;

    private final ZoneSlot cmdSlot;
    private final ZoneSlot gySlot;
    private final ZoneSlot exSlot;
    private final ZoneSlot libSlot;
    private final Map<ZoneType, ZoneSlot> slots = new EnumMap<>(ZoneType.class);

    public ZoneSidebarView(final boolean isLocal) {
        super(new MigLayout("insets 2, gap 2, flowy, fillx, aligny top"));
        setOpaque(false);
        this.isLocal = isLocal;
        this.state = new ZoneBarState(isLocal);

        cmdSlot = new ZoneSlot("CMD", ZoneType.Command,   CLR_CMD, true,  false);
        gySlot  = new ZoneSlot("GY",  ZoneType.Graveyard, CLR_GY,  true,  false);
        exSlot  = new ZoneSlot("EX",  ZoneType.Exile,     CLR_EX,  true,  false);
        libSlot = new ZoneSlot("LIB", ZoneType.Library,   CLR_LIB, true,  true);

        slots.put(ZoneType.Command,   cmdSlot);
        slots.put(ZoneType.Graveyard, gySlot);
        slots.put(ZoneType.Exile,     exSlot);
        slots.put(ZoneType.Library,   libSlot);

        add(cmdSlot, "growx");
        add(gySlot,  "growx");
        add(exSlot,  "growx");
        add(libSlot, "growx");
    }

    public void bind(final PlayerView p) {
        this.player = p;
        refresh();
    }

    public void setOnToggle(final Consumer<ZoneBarState.Result> h) {
        this.onToggle = h;
    }

    public ZoneBarState getState() {
        return state;
    }

    public void refresh() {
        if (player == null) return;
        cmdSlot.update(lastCard(player.getCommand()),    player.getZoneSize(ZoneType.Command));
        gySlot .update(lastCard(player.getGraveyard()), player.getZoneSize(ZoneType.Graveyard));
        exSlot .update(lastCard(player.getExile()),      player.getZoneSize(ZoneType.Exile));
        libSlot.update(lastCard(player.getCards(ZoneType.Library)), player.getZoneSize(ZoneType.Library));
    }

    private static CardView lastCard(final Iterable<CardView> cards) {
        if (cards == null) return null;
        CardView last = null;
        for (CardView c : cards) last = c;
        return last;
    }

    private void onSlotClick(final ZoneType zone) {
        ZoneBarState.Result r = state.toggle(zone);
        ZoneType active = state.getExpandedZone();
        for (Map.Entry<ZoneType, ZoneSlot> e : slots.entrySet()) {
            e.getValue().setActive(e.getKey().equals(active));
        }
        if (onToggle != null) onToggle.accept(r);
    }

    // -------------------------------------------------------------------------

    private class ZoneSlot extends JPanel {
        private final ZoneType zone;
        private final Color accent;
        private final boolean showThumbnail;
        private final boolean faceDown;
        private final JLabel thumbLabel;
        private final JLabel countLabel;
        private CardView currentCard;
        private boolean active;

        ZoneSlot(final String name, final ZoneType zone, final Color accent,
                 final boolean showThumbnail, final boolean faceDown) {
            super(new MigLayout("insets 1, gap 0, flowy, fillx, aligny center, alignx center"));
            this.zone = zone;
            this.accent = accent;
            this.showThumbnail = showThumbnail;
            this.faceDown = faceDown;

            setOpaque(true);
            setBackground(BG_DEFAULT);
            setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, accent));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            thumbLabel = new JLabel(name, SwingConstants.CENTER);
            thumbLabel.setFont(new Font("SansSerif", Font.BOLD, 8));
            thumbLabel.setForeground(accent);
            thumbLabel.setPreferredSize(new java.awt.Dimension(THUMB_W, THUMB_H));

            countLabel = new JLabel("0", SwingConstants.CENTER);
            countLabel.setFont(new Font("SansSerif", Font.BOLD, 9));
            countLabel.setForeground(accent);

            add(thumbLabel, "growx, h " + THUMB_H + "!");
            add(countLabel, "growx, h 12!");

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(final MouseEvent e) { onSlotClick(zone); }
                @Override public void mouseEntered(final MouseEvent e) { if (!active) setBackground(BG_HOVER); }
                @Override public void mouseExited(final MouseEvent e)  { if (!active) setBackground(BG_DEFAULT); }
            });
        }

        void setActive(final boolean a) {
            this.active = a;
            setBackground(a ? BG_ACTIVE : BG_DEFAULT);
        }

        void update(final CardView card, final int count) {
            countLabel.setText(String.valueOf(count));

            if (!showThumbnail) {
                revalidate();
                repaint();
                return;
            }

            if (card == null) {
                if (currentCard != null || thumbLabel.getIcon() != null) {
                    currentCard = null;
                    thumbLabel.setIcon(null);
                    thumbLabel.setText(zone == ZoneType.Library ? "LIB" :
                                       zone == ZoneType.Command  ? "CMD" : "");
                    revalidate();
                    repaint();
                }
                return;
            }

            // For face-down zones (library) use zone owner as viewer so revealed
            // cards show face-up. For public zones (CMD/GY/EX) pass null to bypass
            // the visibility check and always get the real image key.
            final Iterable<PlayerView> viewers = (faceDown && player != null)
                    ? Collections.singleton(player) : null;
            final String imageKey = card.getCurrentState().getImageKey(viewers);
            final boolean isHidden = imageKey != null
                    && imageKey.equals(ImageKeys.getTokenKey(ImageKeys.HIDDEN_CARD));

            if (faceDown && isHidden) {
                // Library card that is NOT revealed: always show card back,
                // but only reload if something changed.
                if (card == currentCard && thumbLabel.getIcon() != null) {
                    return;
                }
                currentCard = card;
                loadCardBackAsync();
                return;
            }

            // Face-up (CMD, GY, EX, or a revealed library card)
            if (card == currentCard) {
                return;
            }
            currentCard = card;
            thumbLabel.setIcon(null);
            thumbLabel.setText("…");
            loadImageAsync(imageKey, card);
        }

        private void loadImageAsync(final String imageKey, final CardView forCard) {
            Thread loader = new Thread(() -> {
                try {
                    File f = ImageKeys.getImageFile(imageKey);
                    if (f != null && f.exists()) {
                        BufferedImage img = ImageIO.read(f);
                        if (img != null) {
                            Image scaled = img.getScaledInstance(THUMB_W, THUMB_H, Image.SCALE_SMOOTH);
                            SwingUtilities.invokeLater(() -> {
                                if (forCard == currentCard) {
                                    thumbLabel.setIcon(new ImageIcon(scaled));
                                    thumbLabel.setText(null);
                                    revalidate();
                                    repaint();
                                }
                            });
                        }
                    }
                } catch (Exception ignored) { }
            });
            loader.setDaemon(true);
            loader.start();
        }

        private void loadCardBackAsync() {
            Thread loader = new Thread(() -> {
                try {
                    File f = ImageKeys.getImageFile(ImageKeys.getTokenKey(ImageKeys.HIDDEN_CARD));
                    if (f != null && f.exists()) {
                        BufferedImage img = ImageIO.read(f);
                        if (img != null) {
                            Image scaled = img.getScaledInstance(THUMB_W, THUMB_H, Image.SCALE_SMOOTH);
                            final ImageIcon icon = new ImageIcon(scaled);
                            SwingUtilities.invokeLater(() -> {
                                thumbLabel.setIcon(icon);
                                thumbLabel.setText(null);
                                revalidate();
                                repaint();
                            });
                        }
                    }
                } catch (Exception ignored) { }
            });
            loader.setDaemon(true);
            loader.start();
        }
    }
}
