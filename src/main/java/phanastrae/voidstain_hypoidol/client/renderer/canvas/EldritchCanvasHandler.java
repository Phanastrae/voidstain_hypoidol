package phanastrae.voidstain_hypoidol.client.renderer.canvas;

import java.util.*;

public class EldritchCanvasHandler {
    private static final Map<String, EldritchCanvas> CANVAS_MAP = new HashMap<>();
    private static int ACTIVE_CANVASE_COUNT;
    private static int FRAMES_SINCE_LAST_CLEAR = 0;

    public static void close() {
        clearCanvases();
    }

    public static EldritchCanvas getCanvas(String canvasId) {
        return CANVAS_MAP.computeIfAbsent(canvasId, EldritchCanvas::new);
    }

    public static void clearCanvases() {
        CANVAS_MAP.forEach((_, canvas) -> canvas.close());
        CANVAS_MAP.clear();
        ACTIVE_CANVASE_COUNT = 0;
        FRAMES_SINCE_LAST_CLEAR = 0;
    }

    public static void tryClearOldCanvases() {
        FRAMES_SINCE_LAST_CLEAR++;
        if (FRAMES_SINCE_LAST_CLEAR > 200) {
            FRAMES_SINCE_LAST_CLEAR = 0;

            List<String> removeList = new ArrayList<>();
            for (EldritchCanvas canvas : CANVAS_MAP.values()) {
                canvas.clearChecksSinceLastUse++;
                if (canvas.clearChecksSinceLastUse >= 2) {
                    removeList.add(canvas.getCanvasId());
                }
            }

            for (String key : removeList) {
                CANVAS_MAP.remove(key);
            }
        }
    }

    public static void setActiveCanvaseCount(int activeCanvaseCount) {
        ACTIVE_CANVASE_COUNT = activeCanvaseCount;
    }

    public static String getCanvasStatistics() {
        return "(VsHi) Canvases: " + ACTIVE_CANVASE_COUNT + "/" + CANVAS_MAP.size();
    }
}