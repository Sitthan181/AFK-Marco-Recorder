package afk;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Settings dialog styled after JMenuBar layout.
 * Each row has a left "checkmark zone" (≥20 px) followed by the item text.
 */
public class SettingDialog extends JDialog {

    private final MainFrame    frame;
    private final AppConfig    config;
    private final HotkeyManager hotkeyManager;

    // Colors
    private static final Color BG       = new Color(40, 40, 40);
    private static final Color ROW_HOVER = new Color(60, 80, 120);
    private static final Color FG       = new Color(220, 220, 220);
    private static final Color DIM_FG   = new Color(140, 140, 140);

    public SettingDialog(MainFrame frame, AppConfig config, HotkeyManager hotkey) {
        super(frame, "Settings", true);
        this.frame        = frame;
        this.config       = config;
        this.hotkeyManager = hotkey;
        buildUI();
    }

    // ── Build ──────────────────────────────────────────────────────

    private void buildUI() {
        setUndecorated(false);
        setResizable(false);
        getContentPane().setBackground(BG);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(4, 0, 4, 0));

        // ── 1. Record Hotkey ──────────────────────────────
        panel.add(makeHotkeyRow("Record Hotkey", config.getRecordHotkey(), keyCode -> {
            config.setRecordHotkey(keyCode);
            hotkeyManager.reregister();
        }));

        panel.add(makeSeparator());

        // ── 2. Play Hotkey ────────────────────────────────
        panel.add(makeHotkeyRow("Play Hotkey", config.getPlayHotkey(), keyCode -> {
            config.setPlayHotkey(keyCode);
            hotkeyManager.reregister();
        }));

        panel.add(makeSeparator());

        // ── 3. Always on Top ─────────────────────────────
        panel.add(makeCheckRow("Always on Top", config.isAlwaysOnTop(), checked -> {
            config.setAlwaysOnTop(checked);
            frame.applyAlwaysOnTop(checked);
        }));

        panel.add(makeSeparator());

        // ── 4. Continuous Playback ────────────────────────
        panel.add(makeCheckRow("Continuous Playback", config.isContinuousPlayback(), checked ->
                config.setContinuousPlayback(checked)));

        panel.add(makeSeparator());

        // ── 5. Set Playback Loop ──────────────────────────
        panel.add(makeActionRow("Set Playback Loop (" + config.getPlaybackLoop() + ")",
                this::showLoopDialog));

        setContentPane(new JScrollPane(panel));
        pack();
        setMinimumSize(new Dimension(280, 0));
        setLocationRelativeTo(frame);
    }

    // ── Row Factories ──────────────────────────────────────────────

    /** Row with check-mark toggle */
    private JPanel makeCheckRow(String label, boolean initial, java.util.function.Consumer<Boolean> onChange) {
        final boolean[] state = {initial};
        JPanel row = createRow();

        JLabel chk  = makeCheckLabel(state[0]);
        JLabel text = makeTextLabel(label);

        row.add(chk);
        row.add(text);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                state[0] = !state[0];
                chk.setText(state[0] ? "✔" : "  ");
                onChange.accept(state[0]);
            }
            @Override public void mouseEntered(MouseEvent e) { row.setBackground(ROW_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { row.setBackground(BG); }
        });
        return row;
    }

    /** Row for hotkey capture — no checkmark */
    private JPanel makeHotkeyRow(String label, int currentKey, java.util.function.Consumer<Integer> onChange) {
        JPanel row = createRow();

        JLabel spacer = makeCheckLabel(false); // empty check zone
        spacer.setText("  ");

        String keyName = KeyEvent.getKeyText(currentKey);
        JLabel text = makeTextLabel(label + "   [" + keyName + "]");

        row.add(spacer);
        row.add(text);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                showHotkeyCapture(label, captured -> {
                    onChange.accept(captured);
                    text.setText(label + "   [" + KeyEvent.getKeyText(captured) + "]");
                });
            }
            @Override public void mouseEntered(MouseEvent e) { row.setBackground(ROW_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { row.setBackground(BG); }
        });
        return row;
    }

    /** Plain action row — no checkmark */
    private JPanel makeActionRow(String label, Runnable action) {
        JPanel row = createRow();
        JLabel spacer = makeCheckLabel(false);
        spacer.setText("  ");
        JLabel text = makeTextLabel(label);
        row.add(spacer);
        row.add(text);
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { action.run(); }
            @Override public void mouseEntered(MouseEvent e) { row.setBackground(ROW_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { row.setBackground(BG); }
        });
        return row;
    }

    // ── Helpers ────────────────────────────────────────────────────

    private JPanel createRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setBorder(new EmptyBorder(4, 6, 4, 6));
        return row;
    }

    private JLabel makeCheckLabel(boolean checked) {
        JLabel lbl = new JLabel(checked ? "✔" : "  ");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(80, 200, 120));
        lbl.setPreferredSize(new Dimension(24, 20));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    private JLabel makeTextLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(FG);
        lbl.setBorder(new EmptyBorder(0, 6, 0, 0));
        return lbl;
    }

    private JSeparator makeSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(70, 70, 70));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // ── Hotkey Capture Dialog ──────────────────────────────────────

    private void showHotkeyCapture(String label, java.util.function.Consumer<Integer> onCapture) {
        JDialog dlg = new JDialog(this, "Set " + label, true);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.getContentPane().setBackground(BG);

        JLabel msg = new JLabel("Press any key...", SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(FG);
        msg.setBorder(new EmptyBorder(20, 20, 10, 20));
        dlg.add(msg, BorderLayout.CENTER);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dlg.dispose());
        JPanel south = new JPanel();
        south.setBackground(BG);
        south.add(cancel);
        dlg.add(south, BorderLayout.SOUTH);

        dlg.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int kc = e.getKeyCode();
                if (kc == KeyEvent.VK_ESCAPE) { dlg.dispose(); return; }
                onCapture.accept(kc);
                dlg.dispose();
            }
        });
        dlg.setFocusable(true);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(240, 130));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ── Loop Count Dialog ──────────────────────────────────────────

    private void showLoopDialog() {
        JDialog dlg = new JDialog(this, "Playback Loop", true);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.getContentPane().setBackground(BG);

        JLabel msg = new JLabel("Please Set Number Of Playback Loop", SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        msg.setForeground(FG);
        msg.setBorder(new EmptyBorder(16, 16, 6, 16));

        JTextField tf = new JTextField(String.valueOf(config.getPlaybackLoop()), 8);
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(0, 20, 0, 20));
        center.add(msg, BorderLayout.NORTH);
        center.add(tf, BorderLayout.CENTER);

        JButton ok     = new JButton("Ok");
        JButton cancel = new JButton("Cancel");

        ok.addActionListener(e -> {
            String val = tf.getText().trim();
            if (!val.matches("\\d+")) {
                JOptionPane.showMessageDialog(dlg, "Please Enter Number Only",
                        "AFK", JOptionPane.WARNING_MESSAGE);
                return;
            }
            config.setPlaybackLoop(Integer.parseInt(val));
            // Refresh the action-row label — rebuild dialog
            dlg.dispose();
            dispose();
            new SettingDialog(frame, config, hotkeyManager).setVisible(true);
        });
        cancel.addActionListener(e -> dlg.dispose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        south.setBackground(BG);
        south.add(ok); south.add(cancel);

        dlg.add(center, BorderLayout.CENTER);
        dlg.add(south,  BorderLayout.SOUTH);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(300, 150));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
}
