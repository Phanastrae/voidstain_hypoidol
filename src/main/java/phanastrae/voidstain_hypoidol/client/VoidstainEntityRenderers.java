package phanastrae.voidstain_hypoidol.client;

import net.minecraft.client.renderer.entity.EntityRenderers;
import phanastrae.voidstain_hypoidol.common.entity.VoidstainEntityTypes;

public class VoidstainEntityRenderers {

    public static void init() {
        EntityRenderers.register(VoidstainEntityTypes.ELDRITCH_PAINTING, EldritchPaintingRenderer::new);
    }
}