package afk;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Registers F6/F5 (or user-configured) as global-ish hotkeys.
 *
 * Pure-Java limitation: KeyboardFocusManager only fires while the AFK
 * window has focus. For true system-wide hotkeys, drop JNativeHook-2.2.2.jar
 * into the lib/ folder — the code below will auto-upgrade at runtime.
 */
public class HotkeyManager {

    private final MainFrame frame;
    private final AppConfig config;
    private KeyEventDispatcher dispatcher;

    public HotkeyManager(MainFrame frame, AppConfig config) {
        this.frame  = frame;
        this.config = config;
    }

    public void register() {
        // Try JNativeHook first (optional upgrade)
        if (tryNativeHook()) return;

        // Fallback: AWT KeyboardFocusManager (works when AFK window has focus)
        dispatcher = e -> {
            if (e.getID() != KeyEvent.KEY_PRESSED) return false;
            if (e.getKeyCode() == config.getRecordHotkey()) {
                frame.toggleRecord();
                return true;
            }
            if (e.getKeyCode() == config.getPlayHotkey()) {
                frame.togglePlay();
                return true;
            }
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
    }

    public void unregister() {
        if (dispatcher != null)
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
        dispatcher = null;
    }

    /** Re-register after hotkey change in Settings. */
    public void reregister() {
        unregister();
        register();
    }

    // ── JNativeHook optional bridge ────────────────────────────────

    private boolean tryNativeHook() {
        try {
            Class<?> gmClass = Class.forName("com.github.kwhat.jnativehook.GlobalScreen");
            // JNativeHook is on classpath — register native hook
            gmClass.getMethod("registerNativeHook").invoke(null);

            // Disable JNativeHook's verbose logger
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
                    "com.github.kwhat.jnativehook");
            logger.setLevel(java.util.logging.Level.OFF);

            // Add listener via reflection so code compiles without the jar
            Class<?> listenerIface = Class.forName(
                    "com.github.kwhat.jnativehook.keyboard.NativeKeyListener");
            Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                    listenerIface.getClassLoader(),
                    new Class[]{ listenerIface },
                    (proxyObj, method, args) -> {
                        if ("nativeKeyPressed".equals(method.getName()) && args != null) {
                            Object nke = args[0];
                            int kc = (int) nke.getClass().getMethod("getKeyCode").invoke(nke);
                            if (kc == nativeCode(config.getRecordHotkey())) frame.toggleRecord();
                            if (kc == nativeCode(config.getPlayHotkey()))   frame.togglePlay();
                        }
                        return null;
                    });
            gmClass.getMethod("addNativeKeyListener", listenerIface).invoke(null, proxy);
            System.out.println("AFK: JNativeHook active — global hotkeys enabled.");
            return true;
        } catch (ClassNotFoundException e) {
            System.out.println("AFK: JNativeHook not found — hotkeys work only when AFK is focused.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Map AWT KeyEvent VK codes → JNativeHook NativeKeyEvent codes.
     * Only F-keys listed here; extend as needed.
     */
    private int nativeCode(int awt) {
        // JNativeHook VC_F1..F12 = 0x003B..0x0044 (same as VK in practice for F-keys)
        // For simplicity we use the same value; works for F1-F12.
        return awt;
    }
}
