package afk;

import java.util.prefs.Preferences;

public class AppConfig {

    private static final Preferences PREFS = Preferences.userNodeForPackage(AppConfig.class);

    private static final String KEY_ALWAYS_ON_TOP      = "alwaysOnTop";
    private static final String KEY_CONTINUOUS_PLAYBACK = "continuousPlayback";
    private static final String KEY_PLAYBACK_LOOP      = "playbackLoop";
    private static final String KEY_RECORD_HOTKEY      = "recordHotkey";
    private static final String KEY_PLAY_HOTKEY        = "playHotkey";

    // Defaults: F6 = Record, F5 = Play
    private boolean alwaysOnTop        = PREFS.getBoolean(KEY_ALWAYS_ON_TOP, false);
    private boolean continuousPlayback = PREFS.getBoolean(KEY_CONTINUOUS_PLAYBACK, false);
    private int     playbackLoop       = PREFS.getInt(KEY_PLAYBACK_LOOP, 1);
    private int     recordHotkey       = PREFS.getInt(KEY_RECORD_HOTKEY, java.awt.event.KeyEvent.VK_F6);
    private int     playHotkey         = PREFS.getInt(KEY_PLAY_HOTKEY,   java.awt.event.KeyEvent.VK_F5);

    // ── Getters ──────────────────────────────────────────
    public boolean isAlwaysOnTop()        { return alwaysOnTop; }
    public boolean isContinuousPlayback() { return continuousPlayback; }
    public int     getPlaybackLoop()      { return playbackLoop; }
    public int     getRecordHotkey()      { return recordHotkey; }
    public int     getPlayHotkey()        { return playHotkey; }

    // ── Setters (with persistence) ────────────────────────
    public void setAlwaysOnTop(boolean v) {
        alwaysOnTop = v;
        PREFS.putBoolean(KEY_ALWAYS_ON_TOP, v);
    }

    public void setContinuousPlayback(boolean v) {
        continuousPlayback = v;
        PREFS.putBoolean(KEY_CONTINUOUS_PLAYBACK, v);
    }

    public void setPlaybackLoop(int v) {
        playbackLoop = Math.max(1, v);
        PREFS.putInt(KEY_PLAYBACK_LOOP, playbackLoop);
    }

    public void setRecordHotkey(int keyCode) {
        recordHotkey = keyCode;
        PREFS.putInt(KEY_RECORD_HOTKEY, keyCode);
    }

    public void setPlayHotkey(int keyCode) {
        playHotkey = keyCode;
        PREFS.putInt(KEY_PLAY_HOTKEY, keyCode);
    }
}
