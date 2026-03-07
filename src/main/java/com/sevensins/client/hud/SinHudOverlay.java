package com.sevensins.client.hud;

import com.sevensins.character.CharacterStats;
import com.sevensins.character.CharacterType;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.common.data.SinType;
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

/**
 * Client-side HUD overlay that renders the player's core RPG identity information
 * in the top-left corner of the screen.
 *
 * <p>Displayed information (when a sin has been chosen):
 * <ul>
 *   <li>Active sin name (in the sin's characteristic colour)</li>
 *   <li>Sin level</li>
 *   <li>Current XP / required XP to next level</li>
 *   <li>Available skill points</li>
 *   <li>Power level (computed by {@link CharacterStats#getPowerLevel(net.minecraft.world.entity.player.Player)})</li>
 * </ul>
 *
 * <p>Nothing is rendered when:
 * <ul>
 *   <li>The player entity is {@code null}</li>
 *   <li>The HUD is hidden ({@code F1} mode)</li>
 *   <li>The player has not chosen a sin ({@link com.sevensins.character.capability.ISinData#isAligned()} is {@code false})</li>
 * </ul>
 *
 * <p>Register this class on the mod event bus:
 * <pre>{@code
 *   modEventBus.register(SinHudOverlay.class);
 * }</pre>
 * </p>
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SinHudOverlay {

    /** Left-edge margin in pixels. */
    private static final int MARGIN_X = 5;

    /** Top-edge margin in pixels. */
    private static final int MARGIN_Y = 5;

    /** Vertical distance between lines in pixels. */
    private static final int LINE_HEIGHT = 10;

    /** Text colour for level, XP, and other neutral labels (white). */
    private static final int TEXT_COLOR_WHITE = 0xFFFFFFFF;

    /** Text colour for XP values (yellow). */
    private static final int TEXT_COLOR_XP = 0xFFFFFF55;

    /** Text colour for skill points (green). */
    private static final int TEXT_COLOR_SP = 0xFF55FF55;

    /** Text colour for power level (light blue / cyan). */
    private static final int TEXT_COLOR_POWER = 0xFF55FFFF;

    /**
     * XP scaling factor that matches {@link com.sevensins.character.CharacterProgressionManager}.
     * XP required for the next level = {@code sinLevel × XP_PER_LEVEL}.
     */
    private static final int XP_PER_LEVEL = 100;

    // -------------------------------------------------------------------------
    // Overlay implementation
    // -------------------------------------------------------------------------

    /**
     * The HUD overlay that renders sin / level / XP / power-level information.
     */
    public static final IGuiOverlay SIN_HUD = new IGuiOverlay() {
        @Override
        public void render(ForgeGui gui, GuiGraphics guiGraphics,
                           float partialTick, int screenWidth, int screenHeight) {

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) return;

            // ---- Retrieve ISinData ----
            var sinDataOpt = mc.player.getCapability(ModCapabilities.SIN_DATA).resolve();
            if (sinDataOpt.isEmpty()) return;

            var sinData = sinDataOpt.get();
            if (!sinData.isAligned()) return;

            SinType activeSin = sinData.getActiveSin();
            if (activeSin == null) return;

            // ---- Retrieve IPlayerCharacterData ----
            int skillPoints = 0;
            var charDataOpt = ModCapabilities.get(mc.player).resolve();
            if (charDataOpt.isPresent()) {
                CharacterType character = charDataOpt.get().getData().getSelectedCharacter();
                // Only render the HUD for players who have actually selected a character
                if (character == CharacterType.NONE) return;
                skillPoints = charDataOpt.get().getData().getSkillPoints();
            }

            // ---- Compute display values ----
            int sinLevel = sinData.getSinLevel();
            int currentXp = sinData.getSinExperience();
            int requiredXp = sinLevel * XP_PER_LEVEL;
            int powerLevel = CharacterStats.getPowerLevel(mc.player);

            String sinName = Component.translatable(activeSin.getTranslationKey()).getString();

            // ---- Render lines ----
            int x = MARGIN_X;
            int y = MARGIN_Y;

            // Sin name – rendered in the sin's characteristic colour
            String sinLine = Component.translatable("hud.seven_sins.sin", sinName).getString();
            guiGraphics.drawString(mc.font, sinLine, x, y, activeSin.getColor() | 0xFF000000, true);
            y += LINE_HEIGHT;

            // Level
            String levelLine = Component.translatable("hud.seven_sins.level", sinLevel).getString();
            guiGraphics.drawString(mc.font, levelLine, x, y, TEXT_COLOR_WHITE, true);
            y += LINE_HEIGHT;

            // XP
            String xpLine = Component.translatable("hud.seven_sins.xp", currentXp, requiredXp).getString();
            guiGraphics.drawString(mc.font, xpLine, x, y, TEXT_COLOR_XP, true);
            y += LINE_HEIGHT;

            // Skill points
            String spLine = Component.translatable("hud.seven_sins.skill_points", skillPoints).getString();
            guiGraphics.drawString(mc.font, spLine, x, y, TEXT_COLOR_SP, true);
            y += LINE_HEIGHT;

            // Power level
            String plLine = Component.translatable("hud.seven_sins.power_level", powerLevel).getString();
            guiGraphics.drawString(mc.font, plLine, x, y, TEXT_COLOR_POWER, true);
        }
    };

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * Registers {@link #SIN_HUD} with the Forge overlay system.
     * Automatically picked up because the class carries {@link Mod.EventBusSubscriber}.
     *
     * @param event the overlay registration event fired on the mod event bus
     */
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("sin_hud", SIN_HUD);
    }
}
