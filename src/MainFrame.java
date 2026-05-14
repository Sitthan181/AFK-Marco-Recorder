package afk;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class MainFrame extends JFrame {

    // ── UI Components ──────────────────────────────────────────────
    private JButton btnRecord, btnPlay, btnOpen, btnSetting;
    private JLabel  lblTitle;

    // ── State ──────────────────────────────────────────────────────
    private final Recorder   recorder;
    private final Player     player;
    private final AppConfig  config;
    private final HotkeyManager hotkeyManager;

    // Title tracking
    private String  currentFileName = "AFK";
    private Timer   recordTimer;
    private int     recordSeconds   = 0;
    private boolean isLooping       = false;
    private int     loopCount       = 0;

    // ── Constructor ────────────────────────────────────────────────
    public MainFrame() {
        config       = new AppConfig();
        recorder     = new Recorder();
        player       = new Player();
        hotkeyManager = new HotkeyManager(this, config);

        initUI();
        hotkeyManager.register();
    }

    // ── UI Setup ───────────────────────────────────────────────────
    private void initUI() {
        setTitle("AFK");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setAlwaysOnTop(config.isAlwaysOnTop());

        // Root panel
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(30, 30, 30));

        // Title label
        lblTitle = new JLabel("AFK", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(220, 220, 220));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        // Button panel  (2 x 2 grid)
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        btnPanel.setBackground(new Color(30, 30, 30));

        btnRecord  = makeButton("Record",  "icons/record.png",  new Color(180, 50,  50));
        btnPlay    = makeButton("Play",    "icons/play.png",    new Color(50,  150, 80));
        btnOpen    = makeButton("Open",    "icons/open.png",    new Color(60,  100, 180));
        btnSetting = makeButton("Setting", "icons/setting.png", new Color(100, 100, 100));

        btnPanel.add(btnRecord);
        btnPanel.add(btnPlay);
        btnPanel.add(btnOpen);
        btnPanel.add(btnSetting);
        root.add(btnPanel, BorderLayout.CENTER);

        setContentPane(root);
        pack();
        setMinimumSize(new Dimension(260, 160));
        setLocationRelativeTo(null);

        // ── Button Actions ─────────────────────────────────────────
        btnRecord.addActionListener(e -> toggleRecord());
        btnPlay.addActionListener(e -> togglePlay());
        btnOpen.addActionListener(e -> doOpen());
        btnSetting.addActionListener(e -> new SettingDialog(this, config, hotkeyManager).setVisible(true));

        // Window close: stop hotkey listener
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                hotkeyManager.unregister();
                System.exit(0);
            }
        });
    }

    // ── Button Factory ─────────────────────────────────────────────
    private JButton makeButton(String text, String iconPath, Color accent) {
        JButton btn = new JButton();
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(accent);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 50));

        // Try to load icon
        ImageIcon icon = loadIcon(iconPath, 28, 28);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setToolTipText(text);
        } else {
            btn.setText(text);
        }

        // Hover effect
        Color hover = accent.brighter();
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(accent); }
        });
        return btn;
    }

    private ImageIcon loadIcon(String resourcePath, int w, int h) {
        try {
            // 1) Try classpath (works inside JAR / exe)
            java.net.URL url = getClass().getClassLoader().getResource(resourcePath);
            // 2) Fallback: external file next to the exe
            if (url == null) {
                java.io.File f = new java.io.File(resourcePath);
                if (f.exists()) url = f.toURI().toURL();
            }
            if (url == null) return null;
            ImageIcon raw   = new ImageIcon(url);
            Image     scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception ex) { return null; }
    }

    // ── Record ─────────────────────────────────────────────────────
    public void toggleRecord() {
        if (!recorder.isRecording()) {
            startRecord();
        } else {
            stopRecord();
        }
    }

    private void startRecord() {
        recorder.startRecording();
        recordSeconds = 0;
        recordTimer = new Timer(1000, e -> {
            recordSeconds++;
            int m = recordSeconds / 60;
            int s = recordSeconds % 60;
            lblTitle.setText(String.format("%s (%02d:%02d*)", currentFileName, m, s));
        });
        recordTimer.start();
        btnRecord.setBackground(new Color(220, 80, 80));
    }

    private void stopRecord() {
        recorder.stopRecording();
        if (recordTimer != null) recordTimer.stop();
        btnRecord.setBackground(new Color(180, 50, 50));
        lblTitle.setText(currentFileName);

        // Save dialog
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Recording");
        fc.setSelectedFile(new File("recording.afk"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("AFK Recording (*.afk)", "afk"));
        int res = fc.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File out = fc.getSelectedFile();
            if (!out.getName().endsWith(".afk")) out = new File(out.getAbsolutePath() + ".afk");
            recorder.saveToFile(out);
            currentFileName = out.getName().replace(".afk", "");
            lblTitle.setText(currentFileName);
        }
    }

    // ── Play ───────────────────────────────────────────────────────
    public void togglePlay() {
        if (!player.hasRecording()) {
            JOptionPane.showMessageDialog(this, "No recording loaded.\nPlease press Open to load a recording.", "AFK", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (config.isContinuousPlayback()) {
            if (isLooping) {
                // Stop loop
                isLooping = false;
                player.stopPlayback();
                lblTitle.setText(currentFileName);
            } else {
                // Start loop
                isLooping = true;
                loopCount = 0;
                runLoopPlay();
            }
        } else {
            // Single or fixed-count play
            int loops = config.getPlaybackLoop();
            runFixedPlay(loops);
        }
    }

    private void runLoopPlay() {
        if (!isLooping) return;
        loopCount++;
        lblTitle.setText(currentFileName + " (" + loopCount + "*)");
        player.playAsync(() -> {
            if (isLooping) runLoopPlay();
        });
    }

    private void runFixedPlay(int total) {
        final int[] remaining = {total};
        Runnable next = new Runnable() {
            @Override public void run() {
                if (remaining[0] <= 0) {
                    SwingUtilities.invokeLater(() -> lblTitle.setText(currentFileName));
                    return;
                }
                remaining[0]--;
                player.playAsync(this);
            }
        };
        next.run();
    }

    // ── Open ───────────────────────────────────────────────────────
    private void doOpen() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Open Recording");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("AFK Recording (*.afk)", "afk"));
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (player.loadFromFile(f)) {
                currentFileName = f.getName().replace(".afk", "");
                lblTitle.setText(currentFileName);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load recording.", "AFK", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Public helpers (called from SettingDialog / HotkeyManager) ─
    public void applyAlwaysOnTop(boolean v) {
        setAlwaysOnTop(v);
    }

    // ── Main ───────────────────────────────────────────────────────
    public static void main(String[] args) {
        // Dark-mode-friendly LAF
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
