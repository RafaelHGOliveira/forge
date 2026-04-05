/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.screens.match.views;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import java.awt.Dimension;

import forge.ImageKeys;

import forge.game.card.CardView;
import forge.game.card.CounterEnumType;
import forge.game.player.PlayerView;
import forge.game.zone.ZoneType;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.framework.IVDoc;
import forge.localinstance.properties.ForgePreferences.FPref;
import forge.localinstance.skin.FSkinProp;
import forge.model.FModel;
import forge.screens.match.CMatchUI;
import forge.screens.match.arena.CommanderDamageView;
import forge.screens.match.arena.ZoneBarState;
import forge.screens.match.arena.ZoneSidebarView;
import forge.screens.match.controllers.CField;
import forge.toolbox.FLabel;
import forge.toolbox.FScrollPane;
import forge.toolbox.FSkin;
import forge.toolbox.FSkin.SkinImage;
import forge.toolbox.FSkin.SkinnedPanel;
import forge.toolbox.special.PhaseIndicator;
import forge.toolbox.special.PlayerDetailsPanel;
import forge.util.Localizer;
import forge.view.arcane.CardPanel;
import forge.view.arcane.HandArea;
import forge.view.arcane.PlayArea;
import net.miginfocom.swing.MigLayout;

/** 
 * Assembles Swing components of a player field instance.
 * 
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 */
public class VField implements IVDoc<CField> {
    private final static int LIFE_CRITICAL = 5;
    private final static int LIFE_WARNING  = 10; // orange warning below this
    private final static int POISON_CRITICAL = 8;

    // MigLayout constraints for lblAvatar height: leaves room for stat rows below.
    // 20px life row + 3px gap = 23px; + 20px counter row = 43px.
    private static final String AVATAR_CONSTRAINT        = "w 100%-6px!, h 100%-23px!, wrap, gap 3 3 3 0";
    private static final String AVATAR_CONSTRAINT_COUNTER = "w 100%-6px!, h 100%-43px!, wrap, gap 3 3 3 0";

    // Fields used with interface IVDoc
    private final CField control;
    private DragCell parentCell;
    private final EDocID docID;
    private final DragTab tab = new DragTab(Localizer.getInstance().getMessage("lblField"));

    // Other fields
    private final PlayerView player;
    private final CMatchUI matchUI;

    // Arena mode components
    private ZoneSidebarView zoneBarView;
    private CommanderDamageView cmdDamageView;
    private JPanel inlineZonePanel;

    // Top-level containers
    private final FScrollPane scroller = new FScrollPane(false);
    private final PlayArea tabletop;
    private HandArea handArea;
    private FScrollPane handScroller;
    private final SkinnedPanel avatarArea = new SkinnedPanel();

    private final PlayerDetailsPanel detailsPanel;

    // Avatar area
    private final FLabel lblAvatar     = new FLabel.Builder().fontAlign(SwingConstants.CENTER).iconScaleFactor(1.0f).build();
    private final FLabel lblLife       = new FLabel.Builder().fontAlign(SwingConstants.CENTER).fontStyle(Font.BOLD).build();
    private final FLabel lblPoison     = new FLabel.Builder().fontAlign(SwingConstants.CENTER).fontStyle(Font.BOLD).icon(FSkin.getImage(FSkinProp.IMG_POISON)).iconInBackground().build();
    private final FLabel lblEnergy     = new FLabel.Builder().fontAlign(SwingConstants.CENTER).fontStyle(Font.BOLD).icon(FSkin.getImage(FSkinProp.IMG_ENERGY)).iconInBackground().build();
    private final FLabel lblExperience = new FLabel.Builder().fontAlign(SwingConstants.CENTER).fontStyle(Font.BOLD).icon(FSkin.getImage(FSkinProp.IMG_EXPERIENCE)).iconInBackground().build();
    private final FLabel lblTicket     = new FLabel.Builder().fontAlign(SwingConstants.CENTER).fontStyle(Font.BOLD).icon(FSkin.getImage(FSkinProp.IMG_TICKET)).iconInBackground().build();
    private final FLabel lblRad        = new FLabel.Builder().fontAlign(SwingConstants.CENTER).fontStyle(Font.BOLD).icon(FSkin.getImage(FSkinProp.IMG_RAD)).iconInBackground().build();

    private final FLabel lblDisconnected = new FLabel.Builder()
            .fontAlign(SwingConstants.CENTER).fontStyle(Font.BOLD).fontSize(11)
            .opaque(true).build();
    private final FLabel btnReplaceAI = new FLabel.ButtonBuilder()
            .text("Replace AI").fontStyle(Font.BOLD).fontSize(10)
            .hoverable().selectable().build();

    private final PhaseIndicator phaseIndicator = new PhaseIndicator();

    private final Border borderAvatarSimple = new LineBorder(new Color(0, 0, 0, 0), 1);
    private final Border borderAvatarHighlighted = new LineBorder(Color.red, 2);


    //========= Constructor
    /**
     * Assembles Swing components of a player field instance.
     * 
     * @param p &emsp; {@link forge.game.player.Player}
     * @param id0 &emsp; {@link forge.gui.framework.EDocID}
     */
    public VField(final CMatchUI matchUI, final EDocID id0, final PlayerView p, final boolean mirror) {
        this.docID = id0;
        this.matchUI = matchUI;

        this.player = p;
        if (p != null) { tab.setText(Localizer.getInstance().getMessage("lblPlayField", p.getName())); }
        else { tab.setText(Localizer.getInstance().getMessage("lblNoPlayerForEDocID", docID.toString())); }

        detailsPanel = new PlayerDetailsPanel(player, CMatchUI.FLOATING_ZONE_TYPES);

        // TODO player is hard-coded into tabletop...should be dynamic
        // (haven't looked into it too deeply). Doublestrike 12-04-12
        tabletop = new PlayArea(matchUI, scroller, mirror, player, ZoneType.Battlefield);

        control = new CField(matchUI, player, this);

        lblAvatar.setFocusable(false);
        lblLife.setFocusable(false);
        lblPoison.setFocusable(false);
        lblEnergy.setFocusable(false);
        lblExperience.setFocusable(false);
        lblTicket.setFocusable(false);
        lblRad.setFocusable(false);

        lblDisconnected.setText("DISCONNECTED");
        lblDisconnected.setForeground(Color.WHITE);
        lblDisconnected.setBackground(new Color(180, 40, 40, 220));
        lblDisconnected.setVisible(false);
        lblDisconnected.setFocusable(false);

        btnReplaceAI.setVisible(false);
        btnReplaceAI.setFocusable(true);

        avatarArea.setOpaque(false);
        avatarArea.setBackground(FSkin.getColor(FSkin.Colors.CLR_HOVER));
        avatarArea.setLayout(new MigLayout("insets 0, gap 0"));
        avatarArea.add(lblDisconnected, "w 100%!, h 16px!, hidemode 3, wrap");
        avatarArea.add(btnReplaceAI, "w 100%!, h 18px!, hidemode 3, wrap");
        avatarArea.add(lblAvatar, AVATAR_CONSTRAINT);
        avatarArea.add(lblLife, "w 100%!, h 20px!, wrap");

        // Player area hover effect
        avatarArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {
                avatarArea.setOpaque(true);
                if (!isHighlighted()) {
                    avatarArea.setBorder(new FSkin.LineSkinBorder(FSkin.getColor(FSkin.Colors.CLR_BORDERS)));
                }
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                avatarArea.setOpaque(false);
                if (!isHighlighted()) {
                    avatarArea.setBorder(borderAvatarSimple);
                }
            }
        });

        tabletop.setBorder(new FSkin.MatteSkinBorder(0, 1, 0, 0, FSkin.getColor(FSkin.Colors.CLR_BORDERS)));
        tabletop.setOpaque(false);

        scroller.setViewportView(this.tabletop);

        handScroller = new FScrollPane(false);
        handArea = new HandArea(matchUI, handScroller);
        handScroller.setViewportView(handArea);
        handArea.setOpaque(true);
        handArea.setBackground(new java.awt.Color(18, 18, 28));
        handScroller.getViewport().setBackground(new java.awt.Color(18, 18, 28));

        updateDetails();
    }

    @Override
    public void populate() {
        final boolean enhanced = FModel.getPreferences().getPrefBoolean(FPref.UI_COMMANDER_ENHANCED);
        final String layout = FModel.getPreferences().getPref(FPref.UI_MULTIPLAYER_FIELD_LAYOUT);
        if (enhanced && "ARENA".equals(layout)) {
            populateArena(isLocalPlayer());
        } else {
            populateClassic();
        }
    }

    public boolean isLocalPlayer() {
        return matchUI != null && matchUI.isLocalPlayer(player);
    }

    private void populateClassic() {
        final JPanel pnl = parentCell.getBody();
        pnl.removeAll();
        pnl.setLayout(new MigLayout("insets 0, gap 0"));

        pnl.add(avatarArea, "w 10%!, h 35%!");
        pnl.add(phaseIndicator, "w 5%!, h 100%!, span 1 2");
        pnl.add(scroller, "w 85%!, h 100%!, span 1 2, wrap");
        pnl.add(detailsPanel, "w 10%!, h 64%!, gapleft 1px");
    }

    private void populateArena(final boolean isLocal) {
        final JPanel pnl = parentCell.getBody();
        pnl.removeAll();
        pnl.setLayout(new MigLayout("insets 0, gap 0, fill"));

        // Build zone sidebar (vertical: CMD, GY, EX, LIB)
        zoneBarView = new ZoneSidebarView(isLocal);
        zoneBarView.bind(player);
        zoneBarView.setOnToggle(result -> {
            if (result == ZoneBarState.Result.OPENED || result == ZoneBarState.Result.SWITCHED) {
                populateInlineZone(zoneBarView.getState().getExpandedZone());
            } else {
                hideInlineZone();
            }
        });

        // Inline zone panel (hidden initially, hidemode 3)
        inlineZonePanel = new JPanel(new MigLayout("insets 2, gap 2, wrap 8"));
        inlineZonePanel.setOpaque(false);
        inlineZonePanel.setVisible(false);

        if (isLocal) {
            phaseIndicator.setHorizontal();

            // Left sidebar: avatar + cmdDamageView + zone bar
            JPanel sidebar = new JPanel(new MigLayout("insets 0, gap 2, flowy, fillx, aligny top"));
            sidebar.setOpaque(false);
            sidebar.add(avatarArea, "w 62!, growx, h 60!");
            cmdDamageView = buildCmdDamageView();
            if (cmdDamageView != null) {
                sidebar.add(cmdDamageView, "growx");
            }
            sidebar.add(zoneBarView, "growx, growy");

            // Main area: battlefield → hand bar → hand (no phase strip)
            JPanel main = new JPanel(new MigLayout("insets 0, gap 0, flowy, fill"));
            main.setOpaque(false);
            main.add(scroller, "grow");
            main.add(buildHandBar(), "h 14!, growx");
            main.add(handScroller, "h 146!, growx");

            // Content row: sidebar | inline zone | main
            JPanel contentRow = new JPanel(new MigLayout("insets 0, gap 0, fill"));
            contentRow.setOpaque(false);
            contentRow.add(sidebar, "w 64!, growy");
            contentRow.add(inlineZonePanel, "hidemode 3, w 0:180:, growy");
            contentRow.add(main, "grow");

            // Phase strip sits above contentRow, left-aligned with the avatar column
            // so it stays anchored to the player's side regardless of card layout.
            pnl.setLayout(new MigLayout("insets 0, gap 0, flowy, fill"));
            pnl.add(phaseIndicator, "h 18!, wmin 180, wmax 360, align left");
            pnl.add(contentRow, "grow");
        } else {
            // Opponent: sidebar (avatar on top + zone bar) | inline zone | (hand / battlefield / phase strip)
            JPanel sidebarPanel = new JPanel(new MigLayout("insets 0, gap 2, flowy, fillx, aligny top"));
            sidebarPanel.setOpaque(false);
            sidebarPanel.add(avatarArea, "growx, h 72!");
            sidebarPanel.add(zoneBarView, "growx, growy");

            JPanel content = new JPanel(new MigLayout("insets 0, gap 0, flowy, fill"));
            content.setOpaque(false);
            content.add(handScroller, "h 96!, growx");
            content.add(scroller, "grow");
            content.add(phaseIndicator, "h 18!, growx");
            phaseIndicator.setHorizontal();

            pnl.add(sidebarPanel, "w 72!, growy");
            pnl.add(inlineZonePanel, "hidemode 3, w 0:180:, growy");
            pnl.add(content, "grow");
        }
    }

    private JPanel buildHandBar() {
        JPanel bar = new JPanel(new MigLayout("insets 0 2 0 2, gap 0, aligny center"));
        bar.setOpaque(false);

        JLabel sortBtn = new JLabel("↑CMC");
        sortBtn.setFont(new Font("SansSerif", Font.PLAIN, 9));
        sortBtn.setForeground(new Color(120, 120, 140));
        sortBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sortBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { sortHandByCmc(); }
            @Override public void mouseEntered(MouseEvent e) { sortBtn.setForeground(new Color(180, 180, 210)); }
            @Override public void mouseExited(MouseEvent e)  { sortBtn.setForeground(new Color(120, 120, 140)); }
        });

        bar.add(sortBtn, "");
        return bar;
    }

    private void sortHandByCmc() {
        if (handArea == null) return;
        List<CardPanel> panels = new ArrayList<>(handArea.getCardPanels());
        panels.sort(Comparator.comparingInt(p -> {
            if (p.getCard() == null || p.getCard().getCurrentState() == null) return 0;
            forge.card.mana.ManaCost mc = p.getCard().getCurrentState().getManaCost();
            return mc == null ? 0 : mc.getCMC();
        }));
        handArea.setCardPanels(panels);
    }

    private CommanderDamageView buildCmdDamageView() {
        if (matchUI == null) return null;
        List<PlayerView> opponents = new ArrayList<>();
        if (matchUI.getGameView() != null) {
            for (PlayerView p : matchUI.getGameView().getPlayers()) {
                if (!p.equals(player)) opponents.add(p);
            }
        }
        return new CommanderDamageView(player, opponents);
    }

    private void hideInlineZone() {
        if (inlineZonePanel != null) {
            inlineZonePanel.setVisible(false);
            inlineZonePanel.revalidate();
            inlineZonePanel.repaint();
        }
    }

    private static final int INLINE_THUMB_W = 48;
    private static final int INLINE_THUMB_H = 67;

    private void populateInlineZone(final ZoneType zone) {
        if (inlineZonePanel == null || zone == null) return;
        inlineZonePanel.removeAll();

        List<CardView> cards = resolveCards(zone);
        int max = Math.min(cards.size(), 16);
        for (int i = 0; i < max; i++) {
            CardView c = cards.get(i);
            JLabel thumb = new JLabel("…");
            thumb.setPreferredSize(new Dimension(INLINE_THUMB_W, INLINE_THUMB_H));
            thumb.setToolTipText(PlayArea.buildCardTooltip(c, false));
            thumb.setOpaque(true);
            thumb.setHorizontalAlignment(SwingConstants.CENTER);
            thumb.setForeground(new Color(100, 100, 120));
            thumb.setBackground(new Color(30, 30, 50));
            inlineZonePanel.add(thumb);
            loadInlineThumbnailAsync(c, thumb);
        }
        inlineZonePanel.setVisible(true);
        inlineZonePanel.revalidate();
        inlineZonePanel.repaint();
    }

    private void loadInlineThumbnailAsync(final CardView card, final JLabel label) {
        final String imageKey = card.getCurrentState().getImageKey(null);
        if (imageKey == null) return;
        final Thread loader = new Thread(() -> {
            try {
                final File f = ImageKeys.getImageFile(imageKey);
                if (f != null && f.exists()) {
                    final BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        final Image scaled = img.getScaledInstance(INLINE_THUMB_W, INLINE_THUMB_H, Image.SCALE_SMOOTH);
                        SwingUtilities.invokeLater(() -> {
                            label.setIcon(new ImageIcon(scaled));
                            label.setText(null);
                            label.revalidate();
                            label.repaint();
                        });
                    }
                }
            } catch (final Exception ignored) { }
        });
        loader.setDaemon(true);
        loader.start();
    }

    private List<CardView> resolveCards(final ZoneType zone) {
        List<CardView> result = new ArrayList<>();
        if (player == null) return result;
        Iterable<CardView> cards = null;
        if (zone == ZoneType.Graveyard) {
            cards = player.getGraveyard();
        } else if (zone == ZoneType.Exile) {
            cards = player.getExile();
        } else if (zone == ZoneType.Command) {
            cards = player.getCommand();
        } else if (zone == ZoneType.Hand) {
            cards = player.getHand();
        } else {
            cards = player.getCards(zone);
        }
        if (cards != null) for (CardView c : cards) result.add(c);
        return result;
    }

    /**
     * Populates the embedded HandArea for an opponent with their current hand
     * cards (face-down in network games, actual cards in local AI games where
     * the full game state is accessible). No-ops if this is the local player
     * or if the HandArea hasn't been created yet.
     */
    public void updateOpponentHand() {
        if (isLocalPlayer() || handArea == null || player == null) return;
        final Iterable<CardView> hand = player.getHand();
        final List<CardPanel> panels = new ArrayList<>();
        if (hand != null) {
            for (final CardView card : hand) {
                CardPanel cp = handArea.getCardPanel(card.getId());
                if (cp == null) {
                    cp = new CardPanel(matchUI, card);
                }
                panels.add(cp);
            }
        }
        handArea.setCardPanels(panels);
    }

    public void refreshInlineZone() {
        if (zoneBarView == null) return;
        ZoneType open = zoneBarView.getState().getExpandedZone();
        if (open != null) {
            populateInlineZone(open);
        }
        zoneBarView.refresh();
        if (cmdDamageView != null) cmdDamageView.refresh();
    }

    @Override
    public EDocID getDocumentID() {
        return docID;
    }

    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    @Override
    public CField getLayoutControl() {
        return control;
    }

    @Override
    public void setParentCell(final DragCell cell0) {
        this.parentCell = cell0;
    }

    @Override
    public DragCell getParentCell() {
        return this.parentCell;
    }

    public PlayArea getTabletop() {
        return this.tabletop;
    }

    public JPanel getAvatarArea() {
        return this.avatarArea;
    }

    public PhaseIndicator getPhaseIndicator() {
        return phaseIndicator;
    }

    public HandArea getHandArea() {
        return handArea;
    }

    public PlayerDetailsPanel getDetailsPanel() {
        return detailsPanel;
    }

    private boolean isHighlighted() {
        return control.getMatchUI().isHighlighted(player);
    }

    public void setDisconnected(final boolean disconnected, final Runnable replaceAction) {
        lblDisconnected.setVisible(disconnected);
        if (disconnected && replaceAction != null) {
            // Remove old listeners to avoid duplicates
            for (final java.awt.event.MouseListener ml : btnReplaceAI.getMouseListeners()) {
                if (ml instanceof java.awt.event.MouseAdapter) {
                    btnReplaceAI.removeMouseListener(ml);
                }
            }
            btnReplaceAI.setCommand(replaceAction);
            btnReplaceAI.setVisible(true);
        } else {
            btnReplaceAI.setVisible(false);
        }
        if (disconnected) {
            avatarArea.setBorder(new LineBorder(new Color(180, 40, 40), 2));
        } else {
            avatarArea.setBorder(isHighlighted() ? borderAvatarHighlighted : borderAvatarSimple);
        }
        avatarArea.revalidate();
        avatarArea.repaint();
    }

    public void setAvatar(final SkinImage avatar) {
        lblAvatar.setIcon(avatar);
        lblAvatar.getResizeTimer().start();
    }

    public void updateManaPool() {
        detailsPanel.updateManaPool();
    }
    public void updateZones() {
        detailsPanel.updateZones();
        if (zoneBarView != null) {
            zoneBarView.refresh();
        }
    }

    private void addCounterRow(final FLabel counterLbl) {
        avatarArea.remove(lblAvatar);
        avatarArea.remove(lblLife);
        lblLife.setIcon((javax.swing.Icon) null);
        avatarArea.add(lblAvatar, AVATAR_CONSTRAINT_COUNTER);
        avatarArea.add(lblLife, "w 100%!, h 20px!, wrap");
        avatarArea.add(counterLbl, "w 100%!, h 20px!, wrap");
    }

    private void removeCounterRow(final FLabel counterLbl) {
        avatarArea.remove(counterLbl);
        avatarArea.remove(lblAvatar);
        avatarArea.remove(lblLife);
        lblLife.setIcon((javax.swing.Icon) null);
        avatarArea.add(lblAvatar, AVATAR_CONSTRAINT);
        avatarArea.add(lblLife, "w 100%!, h 20px!, wrap");
    }

    private void addLblTicket() {
        if (lblTicket.isShowing() || lblExperience.isShowing() || lblEnergy.isShowing() || lblPoison.isShowing()) {
            return; // experience, energy, poison take precedence
        }
        addCounterRow(lblTicket);
    }

    private void removeLblTicket() {
        if (!lblTicket.isShowing()) {
            return;
        }
        removeCounterRow(lblTicket);
    }

    private void addLblRad() {
        if (lblRad.isShowing() || lblExperience.isShowing() || lblEnergy.isShowing() || lblPoison.isShowing()) {
            return;
        }
        addCounterRow(lblRad);
    }

    private void removeLblRad() {
        if (!lblRad.isShowing()) {
            return;
        }
        removeCounterRow(lblRad);
    }

    private void addLblExperience() {
        if (lblExperience.isShowing() || lblEnergy.isShowing() || lblPoison.isShowing()) {
            return; // energy and poison take precedence
        }
        addCounterRow(lblExperience);
    }

    private void removeLblExperience() {
        if (!lblExperience.isShowing()) {
            return;
        }
        removeCounterRow(lblExperience);
    }

    private void addLblEnergy() {
        if (lblEnergy.isShowing() || lblPoison.isShowing()) {
            return; // poison takes precedence
        }
        addCounterRow(lblEnergy);
    }

    private void removeLblEnergy() {
        if (!lblEnergy.isShowing()) {
            return;
        }
        removeCounterRow(lblEnergy);
    }

    private void addLblPoison() {
        if (lblPoison.isShowing()) {
            return;
        }
        addCounterRow(lblPoison);
    }

    private void removeLblPoison() {
        if (!lblPoison.isShowing()) {
            return;
        }
        removeCounterRow(lblPoison);
    }

    public void updateDetails() {
        // Update life total
        final int life = player.getLife();
        lblLife.setText(String.valueOf(life));
        if (life > LIFE_WARNING) {
            lblLife.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        } else if (life > LIFE_CRITICAL) {
            lblLife.setForeground(Color.ORANGE);
        } else {
            lblLife.setForeground(Color.RED);
        }

        // Update poison and/or energy counters, poison counters take precedence
        final int poison = player.getCounters(CounterEnumType.POISON);
        final int energy = player.getCounters(CounterEnumType.ENERGY);
        final int experience = player.getCounters(CounterEnumType.EXPERIENCE);
        final int rad = player.getCounters(CounterEnumType.RAD);
        final int ticket = player.getCounters(CounterEnumType.TICKET);

        if (poison > 0) {
            removeLblEnergy();
            removeLblExperience();
            removeLblRad();
            removeLblTicket();
            addLblPoison();
            lblPoison.setText(String.valueOf(poison));
            if (poison < POISON_CRITICAL) {
                lblPoison.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
            } else {
                lblPoison.setForeground(Color.RED);
            }
        } else {
            removeLblPoison();
        }

        if (energy > 0) {
            removeLblExperience();
            removeLblRad();
            removeLblTicket();
            if (poison == 0) {
                addLblEnergy();
                lblEnergy.setText(String.valueOf(energy));
            }
        } else {
            removeLblEnergy();
        }

        if (experience > 0) {
            removeLblRad();
            removeLblTicket();
            if (poison == 0 && energy == 0) {
                addLblExperience();
                lblExperience.setText(String.valueOf(experience));
            }
        } else {
            removeLblExperience();
        }

        if (rad > 0) {
            removeLblTicket();
            if (poison == 0 && energy == 0 && experience == 0) {
                addLblRad();
                lblRad.setText(String.valueOf(rad));
            }
        } else {
            removeLblRad();
        }

        if (ticket > 0) {
            if (poison == 0 && energy == 0 && experience == 0 && rad == 0) {
                addLblTicket();
                lblTicket.setText(String.valueOf(ticket));
            }
        } else {
            removeLblTicket();
        }

        final boolean highlighted = isHighlighted();
        this.avatarArea.setBorder(highlighted ? borderAvatarHighlighted : borderAvatarSimple );
        this.avatarArea.setOpaque(highlighted);
        this.avatarArea.setToolTipText(player.getDetailsHtml());
    }
}
