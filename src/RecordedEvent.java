package afk;

import java.io.Serializable;

/**
 * One captured input event (mouse move, click, key press/release).
 * Stored with its timestamp offset from start-of-recording.
 */
public class RecordedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        MOUSE_MOVE,
        MOUSE_PRESS,
        MOUSE_RELEASE,
        MOUSE_SCROLL,
        KEY_PRESS,
        KEY_RELEASE
    }

    // ── Fields ─────────────────────────────────────────────────────
    public final Type type;
    public final long delayMs;   // ms from recording start (or from previous event)

    // Mouse fields
    public final int x, y;
    public final int button;     // MouseEvent.BUTTON1 / BUTTON2 / BUTTON3
    public final int scrollAmount;

    // Key fields
    public final int  keyCode;
    public final char keyChar;
    public final int  modifiers;

    // ── Constructors ───────────────────────────────────────────────

    /** Mouse move / press / release */
    public RecordedEvent(Type type, long delayMs, int x, int y, int button, int scrollAmount) {
        this.type         = type;
        this.delayMs      = delayMs;
        this.x            = x;
        this.y            = y;
        this.button       = button;
        this.scrollAmount = scrollAmount;
        this.keyCode      = 0;
        this.keyChar      = 0;
        this.modifiers    = 0;
    }

    /** Key press / release */
    public RecordedEvent(Type type, long delayMs, int keyCode, char keyChar, int modifiers) {
        this.type         = type;
        this.delayMs      = delayMs;
        this.x            = 0;
        this.y            = 0;
        this.button       = 0;
        this.scrollAmount = 0;
        this.keyCode      = keyCode;
        this.keyChar      = keyChar;
        this.modifiers    = modifiers;
    }

    @Override
    public String toString() {
        return type + " @" + delayMs + "ms" +
               (type == Type.MOUSE_MOVE || type == Type.MOUSE_PRESS || type == Type.MOUSE_RELEASE
                    ? " (" + x + "," + y + ")"
                    : " key=" + keyCode);
    }
}
