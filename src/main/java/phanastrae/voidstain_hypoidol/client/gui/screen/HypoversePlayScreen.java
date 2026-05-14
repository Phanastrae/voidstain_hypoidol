package phanastrae.voidstain_hypoidol.client.gui.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.client.hypoverse.hypoentity.player.LocalPlayerHypoEntity;
import phanastrae.voidstain_hypoidol.common.network.c2s.DebugKillHypoPlayerPayload;

public class HypoversePlayScreen extends Screen {

    public HypoversePlayScreen() {
        super(GameNarrator.NO_TITLE);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            this.minecraft.setScreen(new PauseScreen(true));
            return true;
        }

        Options options = this.minecraft.options;
        if (options.keyChat.matches(event)) {
            // use execute to avoid the chat screen immediately receiving the charTyped event for this same key
            this.minecraft.execute(() -> this.minecraft.openChatScreen(ChatComponent.ChatMethod.MESSAGE));
            return true;
        }
        if (options.keyCommand.matches(event)) {
            // use execute to avoid the chat screen immediately receiving the charTyped event for this same key
            this.minecraft.execute(() -> this.minecraft.openChatScreen(ChatComponent.ChatMethod.COMMAND));
            return true;
        }

        LocalPlayerHypoEntity player = VoidstainHypoidolClient.HYPOVERSE.hypoPlayer;
        if (player != null) {
            if (options.keyUp.matches(event)) {
                player.upHeld = true;
                return true;
            }
            if (options.keyDown.matches(event)) {
                player.downHeld = true;
                return true;
            }
            if (options.keyRight.matches(event)) {
                player.rightHeld = true;
                return true;
            }
            if (options.keyLeft.matches(event)) {
                player.leftHeld = true;
                return true;
            }
        }

        LocalPlayer realPlayer = this.minecraft.player;
        if (realPlayer == null || !realPlayer.isAlive() || (options.keyShift.matches(event) && realPlayer.getAbilities().instabuild)) {
            ClientPlayNetworking.send(DebugKillHypoPlayerPayload.INSTANCE);
            return true;
        }

        return false;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        Options options = this.minecraft.options;

        LocalPlayerHypoEntity player = VoidstainHypoidolClient.HYPOVERSE.hypoPlayer;
        if (player != null) {
            if (options.keyUp.matches(event)) {
                player.upHeld = false;
                return true;
            }
            if (options.keyDown.matches(event)) {
                player.downHeld = false;
                return true;
            }
            if (options.keyRight.matches(event)) {
                player.rightHeld = false;
                return true;
            }
            if (options.keyLeft.matches(event)) {
                player.leftHeld = false;
                return true;
            }
        }

        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        LocalPlayer player = this.minecraft.player;
        if (player != null && player.getAbilities().instabuild) {
            float alpha = 1f;
            Component component = Component.translatable("menu.voidstain_hypoidol.leave_hypoverse", Component.translatable(this.minecraft.options.keyShift.getName()));
            int width = font.width(component);
            int color = ARGB.color(alpha, -1);
            int textX = (graphics.guiWidth() - width) / 2;
            int textY = graphics.guiHeight() - font.lineHeight - 5;
            graphics.textWithBackdrop(this.font, component, textX, textY, width, color);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isAllowedInPortal() {
        return true;
    }

    @Override
    public void removed() {
        LocalPlayerHypoEntity player = VoidstainHypoidolClient.HYPOVERSE.hypoPlayer;
        if (player != null) {
            player.upHeld = false;
            player.downHeld = false;
            player.leftHeld = false;
            player.rightHeld = false;
        }
    }
}
