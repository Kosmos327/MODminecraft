package com.sevensins.client.hud;

import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityManager;
import com.sevensins.ability.AbilityType;
import com.sevensins.character.CharacterType;
import com.sevensins.character.capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side HUD overlay that renders cooldown status for the active player's
 * abilities, displayed above the vanilla hotbar (bottom-center of the screen).
 *
 * <p>Cooldown data is provided by the server via
 * {@link com.sevensins.network.packet.SyncCooldownPacket} and cached in
 * {@link #CLIENT_COOLDOWNS}.  The overlay is safe when data is missing: it
 * simply shows nothing or "READY" for any ability without a recorded expiry.</p>
 *
 * <p>Register this class on the mod event bus:
 * <pre>{@code
 *   modEventBus.register(CooldownHudOverlay.class);
 * }</pre>
 * </p>
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CooldownHudOverlay {

    /** Maximum number of ability slots displayed at once. */
    private static final int MAX_ABILITY_SLOTS = 3;

    /** Vertical gap between each ability line (pixels). */
    private static final int LINE_HEIGHT = 12;

    /** Y offset from the bottom of the screen for the lowest cooldown slot. */
    private static final int BOTTOM_OFFSET = 50;

    /** Colour used for ability labels (white). */
    private static final int LABEL_COLOR = 0xFFFFFFFF;

    /** Colour used when an ability is ready (green). */
    private static final int READY_COLOR = 0xFF55FF55;

    /** Colour used when an ability is on cooldown (red). */
    private static final int COOLDOWN_COLOR = 0xFFFF5555;

    /**
     * Client-side cache of ability cooldown expiry times.
     * AbilityType → absolute expiry time in milliseconds (from
     * {@link System#currentTimeMillis()}).
     * Updated by {@link com.sevensins.network.packet.SyncCooldownPacket}.
     */
    private static final Map<AbilityType, Long> CLIENT_COOLDOWNS =
            new EnumMap<>(AbilityType.class);

    // -------------------------------------------------------------------------
    // Public API used by SyncCooldownPacket
    // -------------------------------------------------------------------------

    /**
     * Merges the received expiry map into the local cooldown cache.
     * Called on the client thread when a {@link com.sevensins.network.packet.SyncCooldownPacket}
     * is received.
     *
     * @param expiryTimes ability → absolute expiry time in ms
     */
    public static void updateCooldowns(Map<AbilityType, Long> expiryTimes) {
        CLIENT_COOLDOWNS.putAll(expiryTimes);
    }

    // -------------------------------------------------------------------------
    // Overlay implementation
    // -------------------------------------------------------------------------

    /**
     * The HUD overlay that renders ability cooldown indicators above the hotbar.
     */
    public static final IGuiOverlay COOLDOWN_HUD = new IGuiOverlay() {
        @Override
        public void render(ForgeGui gui, GuiGraphics guiGraphics,
                           float partialTick, int screenWidth, int screenHeight) {

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) return;

            // Resolve the player's selected character
            var charDataOpt = ModCapabilities.get(mc.player).resolve();
            if (charDataOpt.isEmpty()) return;

            CharacterType character = charDataOpt.get().getData().getSelectedCharacter();
            if (character == CharacterType.NONE) return;

            List<Ability> abilities = AbilityManager.getAbilitiesFor(character);
            if (abilities.isEmpty()) return;

            int slotCount = Math.min(MAX_ABILITY_SLOTS, abilities.size());
            long now = System.currentTimeMillis();

            for (int i = 0; i < slotCount; i++) {
                Ability ability = abilities.get(i);
                AbilityType type = ability.getType();

                long expiry = CLIENT_COOLDOWNS.getOrDefault(type, 0L);
                long remainingMs = Math.max(0L, expiry - now);

                String abilityLabel = "[" + formatAbilityName(type) + "] ";
                String statusText;
                int statusColor;

                if (remainingMs <= 0L) {
                    statusText = Component.translatable("hud.seven_sins.ready").getString();
                    statusColor = READY_COLOR;
                } else {
                    float seconds = remainingMs / 1000.0f;
                    statusText = String.format("%.1fs", seconds);
                    statusColor = COOLDOWN_COLOR;
                }

                // Stack slots upward: bottom slot = i=0, each subsequent line above it
                int y = screenHeight - BOTTOM_OFFSET - (i * LINE_HEIGHT);
                int labelWidth = mc.font.width(abilityLabel);
                int totalWidth = labelWidth + mc.font.width(statusText);
                int x = (screenWidth - totalWidth) / 2;

                guiGraphics.drawString(mc.font, abilityLabel, x, y, LABEL_COLOR, true);
                guiGraphics.drawString(mc.font, statusText, x + labelWidth, y, statusColor, true);
            }
        }
    };

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * Registers {@link #COOLDOWN_HUD} with the Forge overlay system.
     * Automatically picked up because the class carries {@link Mod.EventBusSubscriber}.
     *
     * @param event the overlay registration event fired on the mod event bus
     */
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("cooldown_hud", COOLDOWN_HUD);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Converts an {@link AbilityType} enum name to a human-readable title-case label.
     * For example {@code HELL_BLAZE} becomes {@code "Hell Blaze"}.
     */
    private static String formatAbilityName(AbilityType type) {
        String raw = type.name();
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '_') {
                sb.append(' ');
                capitalizeNext = true;
            } else if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }
}
