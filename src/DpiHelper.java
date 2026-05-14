package afk;

import java.awt.*;

public class DpiHelper {

    private static double scaleX     = 1.0;
    private static double scaleY     = 1.0;
    private static boolean initialized = false;

    /** เรียกตอนเริ่มโปรแกรม พร้อมส่งค่า scale จาก AppConfig */
    public static void init(double scale) {
        if (initialized) return;
        initialized = true;
        scaleX = scale;
        scaleY = scale;
        System.out.println("DPI Scale (user-set): " + scaleX + "x" + scaleY);
    }

    /** Fallback ไม่มี scale จาก user */
    public static void init() {
        if (initialized) return;
        init(1.0);
    }

    public static double getScaleX() { return scaleX; }
    public static double getScaleY() { return scaleY; }

    /**
     * MouseInfo ให้ logical coords (หลัง DPI virtualization)
     * คูณ scale → physical pixels สำหรับ Robot
     */
    public static Point getPhysicalMousePos() {
        try {
            PointerInfo pi = MouseInfo.getPointerInfo();
            if (pi != null) {
                Point p = pi.getLocation();
                return new Point(
                    (int) Math.round(p.x * scaleX),
                    (int) Math.round(p.y * scaleY)
                );
            }
        } catch (Exception ignored) {}
        return new Point(0, 0);
    }
}
