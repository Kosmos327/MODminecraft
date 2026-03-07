package com.sevensins.client.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.common.data.SinType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only event handler for rendering HUD overlays and other visual effects.
 *
 * <p>This class is only loaded on the client distribution; server code must never
 * reference it directly.
 */
@Mod.EventBusSubscriber(
        modid = SevenSinsMod.MODID,
        bus   = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public class ClientEventHandler {

    /**
     * Renders a small sin indicator in the lower-left corner of the screen,
     * showing the player's active sin and current level.
     *
     * <p>Rendered after the vanilla player-health overlay so it never occludes
     * critical vanilla HUD elements.
     */
    @SubscribeEvent
    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        mc.player.getCapability(ModCapabilities.SIN_DATA).ifPresent(sinData -> {
            if (!sinData.isAligned()) return;
            renderSinHud(event.getGuiGraphics(), mc, sinData.getActiveSin(),
                    sinData.getSinLevel(), sinData.getSinExperience());
        });
    }

    // -------------------------------------------------------------------------
    // Private rendering helpers
    // -------------------------------------------------------------------------

    private static void renderSinHud(GuiGraphics graphics, Minecraft mc,
                                     SinType sin, int level, int experience) {
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        String sinName = Component.translatable(sin.getTranslationKey()).getString();
        String label   = sinName + "  Lv." + level + "  [" + experience + " XP]";

        // Draw a translucent background strip
        int textWidth = mc.font.width(label);
        graphics.fill(2, screenHeight - 28, textWidth + 6, screenHeight - 17, 0x88000000);

        // Draw the sin name in the sin's characteristic colour
        graphics.drawString(mc.font, label, 4, screenHeight - 26, sin.getColor(), false);
    }
}
