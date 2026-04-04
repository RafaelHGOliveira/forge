package forge.screens.home.online;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.BindException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import forge.gamemodes.net.ChatMessage;
import forge.gamemodes.net.NetConnectUtil;
import forge.gamemodes.net.server.FServerManager;
import forge.gui.FNetOverlay;
import forge.gui.FThreads;
import forge.gui.SOverlayUtils;
import forge.gui.error.BugReporter;
import forge.gui.framework.EDocID;
import forge.gui.framework.ICDoc;
import forge.gui.util.SOptionPane;
import forge.localinstance.properties.ForgeConstants;
import forge.localinstance.properties.ForgeNetPreferences;
import forge.localinstance.properties.ForgePreferences.FPref;
import forge.menus.IMenuProvider;
import forge.menus.MenuUtil;
import forge.model.FModel;
import forge.player.GamePlayerUtil;
import forge.screens.home.CHomeUI;
import forge.screens.home.CLobby;
import forge.screens.home.VLobby;
import forge.screens.home.sanctioned.ConstructedGameMenu;
import forge.toolbox.FButton;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FSkin;
import forge.toolbox.FTextField;
import forge.util.Localizer;

import com.google.common.collect.ImmutableList;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

public enum CSubmenuOnlineLobby implements ICDoc, IMenuProvider {
    SINGLETON_INSTANCE;

    private CLobby lobby;

    void setLobby(final VLobby lobbyView) {
        lobby = new CLobby(lobbyView);
        initialize();
    }

    void connectToServer() {
        final String url = showConnectDialog();
        if (url == null) { return; }

        FThreads.invokeInBackgroundThread(() -> {
            if (!url.isEmpty()) {
                join(url);
            }
            else {
                try {
                    host();
                } catch (Exception ex) {
                    // IntelliJ swears that BindException isn't thrown in this try block, but it is!
                    if (ex.getClass() == BindException.class) {
                        SOptionPane.showErrorDialog(Localizer.getInstance().getMessage("lblUnableStartServerPortAlreadyUse"));
                        SOverlayUtils.hideOverlay();
                    } else {
                        BugReporter.reportException(ex);
                    }
                }
            }
        });
    }

    private static String showConnectDialog() {
        if (StringUtils.isBlank(FModel.getPreferences().getPref(FPref.PLAYER_NAME))) {
            GamePlayerUtil.setPlayerName();
        }

        final Callable<String> task = () -> {
            final String[] resultUrl = {null};
            final boolean[] accepted = {false};
            final FOptionPane[] paneHolder = {null};

            // Top row: IP field + Connect + Host buttons
            final JPanel topRow = new JPanel(new MigLayout("insets 0, gap 4", "[grow][pref][pref]"));
            topRow.setOpaque(false);
            final FTextField txtIP = new FTextField.Builder().build();
            final FButton btnConnect = new FButton("Conectar");
            final FButton btnHost = new FButton("Hospedar");
            topRow.add(txtIP, "growx");
            topRow.add(btnConnect, "w 100!, h 26!");
            topRow.add(btnHost, "w 100!, h 26!");

            // Server list panel (rebuilt on star toggles)
            final JPanel serversPanel = new JPanel(new MigLayout("insets 0, gap 2 2, wrap 1", "[grow]"));
            serversPanel.setOpaque(false);
            final JScrollPane scroll = new JScrollPane(serversPanel,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);

            final Runnable[] rebuildRef = {null};
            rebuildRef[0] = () -> {
                serversPanel.removeAll();
                final List<String> favorites = NetConnectUtil.loadFavorites();
                final List<String> history = NetConnectUtil.loadHistory();

                if (!favorites.isEmpty()) {
                    serversPanel.add(makeSectionLabel("Favoritos"), "growx, gaptop 4");
                    for (final String url : favorites) {
                        serversPanel.add(makeServerRow(url, true, paneHolder, resultUrl, accepted, rebuildRef), "growx");
                    }
                }

                final List<String> historyOnly = new ArrayList<>();
                for (final String h : history) {
                    if (!favorites.contains(h)) { historyOnly.add(h); }
                }
                if (!historyOnly.isEmpty()) {
                    serversPanel.add(makeSectionLabel("Recentes"), "growx, gaptop 4");
                    for (final String url : historyOnly) {
                        serversPanel.add(makeServerRow(url, false, paneHolder, resultUrl, accepted, rebuildRef), "growx");
                    }
                }

                serversPanel.revalidate();
                serversPanel.repaint();
            };
            rebuildRef[0].run();

            // Outer panel
            final JPanel outer = new JPanel(new MigLayout("insets 0, gap 4 4, wrap 1", "[grow]"));
            outer.setOpaque(false);
            outer.add(topRow, "growx");

            final boolean hasEntries = !NetConnectUtil.loadFavorites().isEmpty()
                    || !NetConnectUtil.loadHistory().isEmpty();
            if (hasEntries) {
                outer.add(scroll, "growx, h 50::180");
            }

            // Wire up connect action
            final Runnable doConnect = () -> {
                resultUrl[0] = txtIP.getText().trim();
                accepted[0] = true;
                if (paneHolder[0] != null) { paneHolder[0].setResult(0); }
            };
            btnConnect.addActionListener(e -> doConnect.run());
            btnHost.addActionListener(e -> {
                resultUrl[0] = "";
                accepted[0] = true;
                if (paneHolder[0] != null) { paneHolder[0].setResult(0); }
            });
            txtIP.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(final KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) { doConnect.run(); }
                }
            });

            final FOptionPane pane = new FOptionPane(null, "Conectar ao servidor", null, outer,
                    ImmutableList.of("Cancelar"), -1);
            paneHolder[0] = pane;
            pane.setDefaultFocus(txtIP);
            pane.setVisible(true);
            pane.dispose();

            return accepted[0] ? resultUrl[0] : null;
        };

        final FutureTask<String> future = new FutureTask<>(task);
        FThreads.invokeInEdtAndWait(future);
        try {
            return future.get();
        } catch (final Exception e) {
            return null;
        }
    }

    private static JPanel makeServerRow(final String url, final boolean isFavorite,
            final FOptionPane[] paneHolder, final String[] resultUrl, final boolean[] accepted,
            final Runnable[] rebuildRef) {
        final JPanel row = new JPanel(new MigLayout("insets 2 0 2 0, gap 4", "[pref][grow][pref]"));
        row.setOpaque(false);

        final FButton btnStar = new FButton(isFavorite ? "\u2605" : "\u2606");
        btnStar.setFont(FSkin.getFont(14));
        btnStar.addActionListener(e -> {
            if (isFavorite) {
                NetConnectUtil.removeFromFavorites(url);
            } else {
                NetConnectUtil.addToFavorites(url);
            }
            rebuildRef[0].run();
        });

        final FLabel lblUrl = new FLabel.Builder().text(url).fontSize(12).build();

        final FButton btnConn = new FButton("Conectar");
        btnConn.setFont(FSkin.getFont(11));
        btnConn.addActionListener(e -> {
            resultUrl[0] = url;
            accepted[0] = true;
            if (paneHolder[0] != null) { paneHolder[0].setResult(0); }
        });

        row.add(btnStar, "w 30!, h 24!");
        row.add(lblUrl, "growx");
        row.add(btnConn, "w 80!, h 24!");
        return row;
    }

    private static FLabel makeSectionLabel(final String text) {
        return new FLabel.Builder().text(text).fontStyle(Font.BOLD).fontSize(12).build();
    }

    private void host() {
        SwingUtilities.invokeLater(() -> {
            SOverlayUtils.startGameOverlay(Localizer.getInstance().getMessage("lblStartingServer"));
            SOverlayUtils.showOverlay();
        });

        final ChatMessage result = NetConnectUtil.host(VSubmenuOnlineLobby.SINGLETON_INSTANCE, FNetOverlay.SINGLETON_INSTANCE);

        SwingUtilities.invokeLater(() -> {
            SOverlayUtils.hideOverlay();
            FNetOverlay.SINGLETON_INSTANCE.show(result);
            if (CHomeUI.SINGLETON_INSTANCE.getCurrentDocID() == EDocID.HOME_NETWORK) {
                VSubmenuOnlineLobby.SINGLETON_INSTANCE.populate();
            }
            showServerAddressesDialog();
        });
    }

    private void showServerAddressesDialog() {
        final int port = FModel.getNetPreferences().getPrefInt(ForgeNetPreferences.FNetPref.NET_PORT);
        final LinkedHashMap<String, String> addresses = FServerManager.getAllLocalAddresses();
        final String externalAddress = FServerManager.getExternalAddress();
        final Localizer localizer = Localizer.getInstance();

        final JPanel panel = new JPanel(new MigLayout("insets 0, gap 4 6, wrap 3", "[grow][grow][pref]"));
        panel.setOpaque(false);

        // Header
        panel.add(new FLabel.Builder().text(localizer.getMessage("lblInterface")).fontStyle(java.awt.Font.BOLD).fontSize(12).build(), "growx");
        panel.add(new FLabel.Builder().text(localizer.getMessage("lblAddress")).fontStyle(java.awt.Font.BOLD).fontSize(12).build(), "growx");
        panel.add(new FLabel.Builder().text("").build()); // empty header for copy column

        // External address row (if available)
        if (externalAddress != null) {
            final String externalUrl = externalAddress + ":" + port;
            panel.add(new FLabel.Builder().text("External (WAN)").fontSize(12).build(), "growx");
            panel.add(new FLabel.Builder().text(externalUrl).fontSize(12).build(), "growx");
            final FButton btnCopy = new FButton(localizer.getMessage("lblCopy"));
            btnCopy.setFont(FSkin.getFont(11));
            btnCopy.addActionListener(e -> copyToClipboard(externalUrl));
            panel.add(btnCopy, "w 70!, h 24!");
        }

        // Local address rows
        boolean first = true;
        for (final Map.Entry<String, String> entry : addresses.entrySet()) {
            final String url = entry.getValue() + ":" + port;
            final String label = first ? entry.getKey() + " \u2605" : entry.getKey();
            first = false;

            panel.add(new FLabel.Builder().text(label).fontSize(12).build(), "growx");
            panel.add(new FLabel.Builder().text(url).fontSize(12).build(), "growx");
            final FButton btnCopy = new FButton(localizer.getMessage("lblCopy"));
            btnCopy.setFont(FSkin.getFont(11));
            btnCopy.addActionListener(e -> copyToClipboard(url));
            panel.add(btnCopy, "w 70!, h 24!");
        }

        FOptionPane.showOptionDialog(
                localizer.getMessage("lblChooseAddressToCopy"),
                localizer.getMessage("lblServerURL"),
                FOptionPane.INFORMATION_ICON,
                panel,
                ImmutableList.of(localizer.getMessage("lblOK")),
                0);
    }

    private static void copyToClipboard(final String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    private void join(final String url) {
        SwingUtilities.invokeLater(() -> {
            SOverlayUtils.startGameOverlay(Localizer.getInstance().getMessage("lblConnectingToServer"));
            SOverlayUtils.showOverlay();
        });

        final ChatMessage result = NetConnectUtil.join(url, VSubmenuOnlineLobby.SINGLETON_INSTANCE, FNetOverlay.SINGLETON_INSTANCE);
        String message = result.getMessage();
        if (Objects.equals(message, ForgeConstants.CLOSE_CONN_COMMAND)) {
            FOptionPane.showErrorDialog(Localizer.getInstance().getMessage("UnableConnectToServer", url));
            SOverlayUtils.hideOverlay();
        } else if (message != null && message.startsWith(ForgeConstants.CONN_ERROR_PREFIX)) {
            String errorDetail = message.substring(ForgeConstants.CONN_ERROR_PREFIX.length());
            FOptionPane.showErrorDialog(errorDetail, Localizer.getInstance().getMessage("lblConnectionError"));
            SOverlayUtils.hideOverlay();
        } else if (Objects.equals(message, ForgeConstants.INVALID_HOST_COMMAND)) {
            FOptionPane.showErrorDialog(Localizer.getInstance().getMessage("lblDetectedInvalidHostAddress", url));
            SOverlayUtils.hideOverlay();
        } else {
            NetConnectUtil.addToHistory(url);
            SwingUtilities.invokeLater(() -> {
                SOverlayUtils.hideOverlay();
                if (result instanceof ChatMessage) {
                    FNetOverlay.SINGLETON_INSTANCE.show(result);
                    if (CHomeUI.SINGLETON_INSTANCE.getCurrentDocID() == EDocID.HOME_NETWORK) {
                        VSubmenuOnlineLobby.SINGLETON_INSTANCE.populate();
                    }
                }
            });
        }
    }

    @Override
    public void register() {
    }

    /* (non-Javadoc)
     * @see forge.gui.home.ICSubmenu#update()
     */
    @Override
    public void update() {
        MenuUtil.setMenuProvider(this);
        if (lobby != null) {
            lobby.update();
        }
    }

    /* (non-Javadoc)
     * @see forge.gui.home.ICSubmenu#initialize()
     */
    @Override
    public void initialize() {
        if (lobby != null) {
            lobby.initialize();
        }
    }

    /* (non-Javadoc)
     * @see forge.gui.menubar.IMenuProvider#getMenus()
     */
    @Override
    public List<JMenu> getMenus() {
        final List<JMenu> menus = new ArrayList<>();
        menus.add(ConstructedGameMenu.getMenu());
        return menus;
    }
}
