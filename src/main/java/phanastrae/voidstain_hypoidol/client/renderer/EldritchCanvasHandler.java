package phanastrae.voidstain_hypoidol.client.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EldritchCanvasHandler {
    private static final Map<String, EldritchCanvas> CANVAS_MAP = new HashMap<>();
    private static int ACTIVE_CANVASES;
    private static long LAST_CLEAR = System.nanoTime();

    public static void close() {
        clearCanvases();
    }

    public static EldritchCanvas getCanvas(String canvasId) {
        return CANVAS_MAP.computeIfAbsent(canvasId, EldritchCanvas::new);
    }

    public static void clearCanvases() {
        CANVAS_MAP.forEach((_, canvas) -> canvas.close());
        CANVAS_MAP.clear();
        ACTIVE_CANVASES = 0;
        LAST_CLEAR = System.nanoTime();
    }

    public static void updateCanvases() {
        tryClearOldCanvases();
        ACTIVE_CANVASES = 0;
        if (!CANVAS_MAP.isEmpty()) {
            ACTIVE_CANVASES += EldritchCanvasRenderer.renderCanvases(CANVAS_MAP.values());
        }
    }

    public static void tryClearOldCanvases() {
        long time = System.nanoTime();
        if (Math.abs(time - LAST_CLEAR) > 5 * 1E9) {
            LAST_CLEAR = time;

            List<String> removeList = new ArrayList<>();
            for (EldritchCanvas canvas : CANVAS_MAP.values()) {
                if (!canvas.needsFilling() && Math.abs(canvas.timeSinceLastNeeded()) > 15 * 1E9) {
                    removeList.add(canvas.getId());
                }
            }

            for (String key : removeList) {
                CANVAS_MAP.remove(key);
            }
        }
    }

    public static String getCanvasStatistics() {
        return "(VsHi) Canvases: " + ACTIVE_CANVASES + "/" + CANVAS_MAP.size();
    }
}