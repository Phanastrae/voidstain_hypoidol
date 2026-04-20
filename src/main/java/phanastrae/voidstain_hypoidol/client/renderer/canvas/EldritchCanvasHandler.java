package phanastrae.voidstain_hypoidol.client.renderer.canvas;

import java.util.*;

public class EldritchCanvasHandler {
    private static final Map<UUID, CanvasTexture> CANVAS_MAP = new HashMap<>();
    private static int ACTIVE_CANVASE_COUNT;
    private static int FRAMES_SINCE_LAST_CLEAR = 0;

    public static void close() {
        clearCanvases();
    }

    public static CanvasTexture getCanvas(UUID uuid) {
        return CANVAS_MAP.computeIfAbsent(uuid, CanvasTexture::new);
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

            List<UUID> removeList = new ArrayList<>();
            for (CanvasTexture canvas : CANVAS_MAP.values()) {
                canvas.clearChecksSinceLastUse++;
                if (canvas.clearChecksSinceLastUse >= 2) {
                    removeList.add(canvas.getCanvasId());
                }
            }

            for (UUID uuid : removeList) {
                CANVAS_MAP.remove(uuid);
            }
        }
    }

    public static void removeCanvas(UUID uuid) {
        if (CANVAS_MAP.containsKey(uuid)) {
            CANVAS_MAP.remove(uuid).close();
        }
    }

    public static void setActiveCanvaseCount(int activeCanvaseCount) {
        ACTIVE_CANVASE_COUNT = activeCanvaseCount;
    }

    public static String getCanvasStatistics() {
        return "(VsHi) Canvas Textures: " + ACTIVE_CANVASE_COUNT + "/" + CANVAS_MAP.size();
    }
}