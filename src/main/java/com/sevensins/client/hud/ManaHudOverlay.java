package com.sevensins.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side HUD overlay that renders the player's current and maximum mana
 * in the bottom-left corner of the screen.
 *
 * <p>Register this class on the mod event bus:
 * <pre>{@code
 *   modEventBus.register(ManaHudOverlay.class);
 * }</pre>
 * </p>
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ManaHudOverlay {

    /** Margin from the left edge of the screen (pixels). */
    private static final int MARGIN_LEFT = 5;

    /** Margin from the bottom edge of the screen (pixels). */
    private static final int MARGIN_BOTTOM = 20;

    /** Text colour: blue-ish to represent mana (0xAARRGGBB). */
    private static final int MANA_TEXT_COLOR = 0xFF5555FF;

    /**
     * The actual overlay implementation.
     *
     * <p>Reads mana values from the player's capability (if available) and
     * renders them as plain text.  Falls back to 0 / 0 when the capability
     * is not yet attached (e.g., during world loading).</p>
     */
    public static final IGuiOverlay MANA_HUD = new IGuiOverlay() {
        @Override
        public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick,
                           int screenWidth, int screenHeight) {

            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if (player == null || mc.options.hideGui) {
                return;
            }

            int currentMana = 0;
            int maxMana = 0;

            // Retrieve mana values from the capability attached to the player.
            // The capability key is expected to be registered under
            // com.sevensins.capability.ManaCapability.MANA_CAPABILITY.
            var capabilityOpt = player.getCapability(
                    com.sevensins.capability.ManaCapability.MANA_CAPABILITY
            ).resolve();

            if (capabilityOpt.isPresent()) {
                var mana = capabilityOpt.get();
                currentMana = mana.getMana();
                maxMana = mana.getMaxMana();
            }

            String manaText = "Mana: " + currentMana + " / " + maxMana;

            int x = MARGIN_LEFT;
            int y = screenHeight - MARGIN_BOTTOM;

            guiGraphics.drawString(mc.font, manaText, x, y, MANA_TEXT_COLOR, true);
        }
    };

    /**
     * Registers {@link #MANA_HUD} with the Forge overlay system.
     *
     * <p>This listener is automatically picked up by Forge because the class
     * is annotated with {@link Mod.EventBusSubscriber}.</p>
     *
     * @param event the overlay registration event fired on the mod event bus
     */
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        // Rendered above the vanilla hotbar overlay so it is always visible.
        event.registerAboveAll("mana_hud", MANA_HUD);
    }
}
