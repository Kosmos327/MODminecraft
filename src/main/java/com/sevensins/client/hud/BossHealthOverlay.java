package com.sevensins.client.hud;

import com.sevensins.boss.BossManager;
import com.sevensins.boss.BossPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side HUD overlay that renders the active boss name, health bar, and
 * current phase at the top-centre of the screen.
 *
 * <p>The overlay is hidden automatically when no boss is active — i.e. when
 * {@link BossManager#getClientBossState()} returns {@code null}.</p>
 *
 * <p>Boss state is pushed from the server via
 * {@link com.sevensins.network.packet.SyncBossStatePacket}.</p>
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BossHealthOverlay {

    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 10;
    private static final int BAR_Y_OFFSET = 20;

    /** Dark background behind the bar (ARGB). */
    private static final int COLOR_BG = 0xFF333333;

    /** Red health fill (ARGB). */
    private static final int COLOR_HP = 0xFFCC0000;

    /** White text (ARGB). */
    private static final int COLOR_TEXT = 0xFFFFFFFF;

    /** Orange phase-2 text (ARGB). */
    private static final int COLOR_PHASE2 = 0xFFFF6600;

    /** Dark purple enraged text (ARGB). */
    private static final int COLOR_ENRAGED = 0xFFAA00FF;

    /**
     * The boss HUD overlay implementation.
     *
     * <p>Reads from {@link BossManager#getClientBossState()} and renders:
     * <ol>
     *   <li>Boss name above the bar.</li>
     *   <li>Red health bar (proportional to current / max HP).</li>
     *   <li>Numeric HP below the bar.</li>
     *   <li>Phase-2 label (orange) when the boss is in {@link BossPhase#PHASE_2}.</li>
     * </ol>
     * </p>
     */
    public static final IGuiOverlay BOSS_HUD = new IGuiOverlay() {
        @Override
        public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick,
                           int screenWidth, int screenHeight) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) return;

            BossManager.BossInfo bossInfo = BossManager.getClientBossState();
            if (bossInfo == null) return;

            int centerX = screenWidth / 2;
            int barX = centerX - BAR_WIDTH / 2;
            int barY = BAR_Y_OFFSET;

            // Background
            guiGraphics.fill(barX - 1, barY - 1,
                    barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1,
                    COLOR_BG);

            // Health fill
            float ratio = bossInfo.maxHp() > 0 ? bossInfo.currentHp() / bossInfo.maxHp() : 0f;
            int filledWidth = (int) (BAR_WIDTH * Math.max(0f, Math.min(1f, ratio)));
            if (filledWidth > 0) {
                guiGraphics.fill(barX, barY, barX + filledWidth, barY + BAR_HEIGHT, COLOR_HP);
            }

            // Boss name (centred, above bar)
            String name = bossInfo.name();
            int nameWidth = mc.font.width(name);
            guiGraphics.drawString(mc.font, name,
                    centerX - nameWidth / 2, barY - mc.font.lineHeight - 2,
                    COLOR_TEXT, true);

            // HP numbers (centred, below bar)
            String hpText = (int) bossInfo.currentHp() + " / " + (int) bossInfo.maxHp();
            int hpWidth = mc.font.width(hpText);
            guiGraphics.drawString(mc.font, hpText,
                    centerX - hpWidth / 2, barY + BAR_HEIGHT + 3,
                    COLOR_TEXT, false);

            // Phase label (only shown in PHASE_2 and beyond)
            String phaseText = getPhaseDisplayText(bossInfo.phase());
            if (phaseText != null) {
                int phaseColor = bossInfo.phase() == BossPhase.ENRAGED ? COLOR_ENRAGED : COLOR_PHASE2;
                int phaseWidth = mc.font.width(phaseText);
                guiGraphics.drawString(mc.font, phaseText,
                        centerX - phaseWidth / 2, barY + BAR_HEIGHT + 3 + mc.font.lineHeight + 1,
                        phaseColor, true);
            }
        }
    };

    /**
     * Returns the display label for a given {@link BossPhase}, or {@code null}
     * if no extra label should be shown (e.g. for {@link BossPhase#PHASE_1}).
     */
    @javax.annotation.Nullable
    private static String getPhaseDisplayText(BossPhase phase) {
        return switch (phase) {
            case PHASE_1 -> null;
            case PHASE_2 -> "Phase 2";
            case ENRAGED -> "ENRAGED";
        };
    }

    /**
     * Registers {@link #BOSS_HUD} with the Forge overlay system.
     * Automatically invoked because the class carries {@link Mod.EventBusSubscriber}.
     *
     * @param event the overlay registration event fired on the mod event bus
     */
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("boss_hud", BOSS_HUD);
    }
}
