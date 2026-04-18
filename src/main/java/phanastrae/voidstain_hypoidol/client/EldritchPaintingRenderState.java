package phanastrae.voidstain_hypoidol.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;

public class EldritchPaintingRenderState extends EntityRenderState {
    public Direction direction = Direction.NORTH;
    public int[] lightCoordsPerBlock = new int[0];
}