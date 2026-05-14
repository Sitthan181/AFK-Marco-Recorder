package afk;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

/**
 * Replays a list of RecordedEvents using java.awt.Robot.
 * playAsync() runs on a daemon thread and calls the onDone callback when finished.
 */
public class Player {

    private List<RecordedEvent> events = null;
    private volatile boolean    playing = false;
    private Thread              playThread;
    private final Recorder      recorder = new Recorder();

    // ── Public API ─────────────────────────────────────────────────

    public boolean hasRecording() { return events != null && !events.isEmpty(); }
    public boolean isPlaying()    { return playing; }

    public boolean loadFromFile(File file) {
        List<RecordedEvent> loaded = recorder.loadFromFile(file);
        if (loaded != null) { events = loaded; return true; }
        return false;
    }

    /** Non-blocking playback; onDone is called on the play thread when finished. */
    public void playAsync(Runnable onDone) {
        if (!hasRecording()) return;
        playing    = true;
        playThread = new Thread(() -> {
            try { playSync(); } catch (Exception e) { e.printStackTrace(); }
            finally {
                playing = false;
                if (onDone != null) onDone.run();
            }
        }, "AFK-Player");
        playThread.setDaemon(true);
        playThread.start();
    }

    public void stopPlayback() {
        playing = false;
        if (playThread != null) playThread.interrupt();
    }

    // ── Playback Logic ─────────────────────────────────────────────

    private void playSync() throws AWTException, InterruptedException {
        Robot robot = new Robot();
        robot.setAutoDelay(0);
        robot.setAutoWaitForIdle(false);

        for (RecordedEvent ev : events) {
            if (!playing) break;

            // Respect inter-event delay
            if (ev.delayMs > 0) Thread.sleep(ev.delayMs);
            if (!playing) break;

            switch (ev.type) {
                case MOUSE_MOVE    -> robot.mouseMove(ev.x, ev.y);
                case MOUSE_PRESS   -> robot.mousePress(buttonMask(ev.button));
                case MOUSE_RELEASE -> robot.mouseRelease(buttonMask(ev.button));
                case MOUSE_SCROLL  -> robot.mouseWheel(ev.scrollAmount);
                case KEY_PRESS     -> safeKeyPress(robot, ev.keyCode);
                case KEY_RELEASE   -> safeKeyRelease(robot, ev.keyCode);
            }
        }
    }

    private int buttonMask(int button) {
        return switch (button) {
            case 2  -> InputEvent.BUTTON2_DOWN_MASK;
            case 3  -> InputEvent.BUTTON3_DOWN_MASK;
            default -> InputEvent.BUTTON1_DOWN_MASK;
        };
    }

    private void safeKeyPress(Robot robot, int keyCode) {
        try { robot.keyPress(keyCode); } catch (IllegalArgumentException ignored) {}
    }

    private void safeKeyRelease(Robot robot, int keyCode) {
        try { robot.keyRelease(keyCode); } catch (IllegalArgumentException ignored) {}
    }
}
