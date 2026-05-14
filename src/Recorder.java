package afk;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Records mouse movement, clicks, scroll and keyboard events.
 *
 * Strategy (pure-Java, no JNativeHook needed for recording):
 *   - Mouse: Toolkit.getDefaultToolkit().addAWTEventListener() captures all
 *     AWT mouse events in this JVM, PLUS a background thread polls the
 *     real pointer position via MouseInfo for smooth movement capture.
 *   - Keyboard: KeyboardFocusManager dispatches to our KeyEventDispatcher.
 *
 * Note: This records events that happen WITHIN the AFK window and
 * the system mouse position globally (via MouseInfo polling).
 * For true cross-application recording the user must install JNativeHook
 * (see README) — the code auto-detects and upgrades if the jar is present.
 */
public class Recorder {

    private final List<RecordedEvent> events = new ArrayList<>();
    private boolean recording = false;
    private long    startTime = 0;
    private long    lastEventTime = 0;

    // Mouse polling
    private Thread  mousePoller;
    private int     lastX = -1, lastY = -1;
    private static final int POLL_INTERVAL_MS = 10; // 100 Hz

    // AWT listeners
    private AWTEventListener awtMouseListener;
    private KeyEventDispatcher keyDispatcher;

    // ── Public API ─────────────────────────────────────────────────

    public boolean isRecording() { return recording; }

    public void startRecording() {
        events.clear();
        recording     = true;
        startTime     = System.currentTimeMillis();
        lastEventTime = startTime;
        lastX         = -1;
        lastY         = -1;

        // Mouse polling thread (captures movement anywhere on screen)
        mousePoller = new Thread(this::pollMouse, "AFK-MousePoller");
        mousePoller.setDaemon(true);
        mousePoller.start();

        // AWT mouse events (clicks, scroll)
        awtMouseListener = event -> {
            if (!recording) return;
            if (event instanceof MouseEvent me) {
                handleMouseEvent(me);
            } else if (event instanceof MouseWheelEvent mwe) {
                addEvent(new RecordedEvent(RecordedEvent.Type.MOUSE_SCROLL,
                        delay(), mwe.getX(), mwe.getY(), 0, mwe.getWheelRotation()));
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(
                awtMouseListener,
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);

        // Keyboard
        keyDispatcher = e -> {
            if (!recording) return false;
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                addEvent(new RecordedEvent(RecordedEvent.Type.KEY_PRESS,
                        delay(), e.getKeyCode(), e.getKeyChar(), e.getModifiersEx()));
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                addEvent(new RecordedEvent(RecordedEvent.Type.KEY_RELEASE,
                        delay(), e.getKeyCode(), e.getKeyChar(), e.getModifiersEx()));
            }
            return false; // don't consume
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyDispatcher);
    }

    public void stopRecording() {
        recording = false;
        if (mousePoller != null) mousePoller.interrupt();
        if (awtMouseListener != null)
            Toolkit.getDefaultToolkit().removeAWTEventListener(awtMouseListener);
        if (keyDispatcher != null)
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyDispatcher);
    }

    public List<RecordedEvent> getEvents() { return new ArrayList<>(events); }

    // ── File I/O ───────────────────────────────────────────────────

    public boolean saveToFile(File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(new ArrayList<>(events));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<RecordedEvent> loadFromFile(File file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<RecordedEvent>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Internals ──────────────────────────────────────────────────

    private long delay() {
        long now  = System.currentTimeMillis();
        long diff = now - lastEventTime;
        lastEventTime = now;
        return Math.max(0, diff);
    }

    private synchronized void addEvent(RecordedEvent ev) {
        events.add(ev);
    }

    private void pollMouse() {
        while (recording) {
            try {
                PointerInfo pi = MouseInfo.getPointerInfo();
                if (pi != null) {
                    Point p = pi.getLocation();
                    if (p.x != lastX || p.y != lastY) {
                        lastX = p.x; lastY = p.y;
                        addEvent(new RecordedEvent(
                                RecordedEvent.Type.MOUSE_MOVE, delay(), p.x, p.y, 0, 0));
                    }
                }
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void handleMouseEvent(MouseEvent me) {
        // Convert component-relative → screen coords
        Point screen;
        try {
            screen = me.getComponent() != null
                    ? me.getComponent().getLocationOnScreen()
                    : new Point(0, 0);
            screen.translate(me.getX(), me.getY());
        } catch (IllegalComponentStateException ex) {
            screen = new Point(me.getX(), me.getY());
        }

        switch (me.getID()) {
            case MouseEvent.MOUSE_PRESSED ->
                    addEvent(new RecordedEvent(RecordedEvent.Type.MOUSE_PRESS,
                            delay(), screen.x, screen.y, me.getButton(), 0));
            case MouseEvent.MOUSE_RELEASED ->
                    addEvent(new RecordedEvent(RecordedEvent.Type.MOUSE_RELEASE,
                            delay(), screen.x, screen.y, me.getButton(), 0));
        }
    }
}
