package com.sevensins.debug;

import com.sevensins.character.CharacterType;
import com.sevensins.character.PlayerCharacterData;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.ability.AbilityType;
import com.sevensins.ability.CooldownManager;
import com.sevensins.boss.BossManager;
import com.sevensins.quest.PlayerQuestData;
import com.sevensins.story.StoryChapter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Read-only helper that formats a player's major progression state for
 * developer / QA inspection.
 *
 * <p>All methods are safe to call from server-side code.  Nothing is mutated.</p>
 */
public final class ProgressionDebugHelper {

    private ProgressionDebugHelper() {}

    // -------------------------------------------------------------------------
    // Top-level aggregates
    // -------------------------------------------------------------------------

    /**
     * Returns a multi-line snapshot of all major state sections for {@code player}.
     *
     * <p>The returned list is ready to be sent as individual chat lines via
     * {@link net.minecraft.server.level.ServerPlayer#sendSystemMessage}.</p>
     */
    public static List<Component> buildFullState(ServerPlayer player) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("=== Seven Sins Debug State: " + player.getName().getString() + " ==="));
        lines.addAll(buildCharacterSection(player));
        lines.addAll(buildSinSection(player));
        lines.addAll(buildManaSection(player));
        lines.addAll(buildAbilitiesSection(player));
        lines.addAll(buildQuestSection(player));
        lines.addAll(buildStorySection(player));
        lines.addAll(buildBossSection());
        return lines;
    }

    /** Returns lines describing character type, level, XP and skill points. */
    public static List<Component> buildCharacterSection(ServerPlayer player) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("-- Character --"));
        ModCapabilities.get(player).resolve().ifPresentOrElse(cap -> {
            PlayerCharacterData data = cap.getData();
            CharacterType character = data.getSelectedCharacter();
            lines.add(Component.literal("  Character : " + character.name()));
            lines.add(Component.literal("  Level     : " + data.getLevel()));
            lines.add(Component.literal("  XP        : " + data.getExperience() + " / " + data.getXpToNextLevel()));
            lines.add(Component.literal("  SkillPts  : " + data.getSkillPoints()));
            lines.add(Component.literal("  StoryStage: " + data.getPersonalStoryStage()
                    + " (" + StoryChapter.fromStage(data.getPersonalStoryStage()).name() + ")"));
        }, () -> lines.add(Component.literal("  [capability unavailable]")));
        return lines;
    }

    /** Returns lines describing the player's sin alignment and sin-level XP. */
    public static List<Component> buildSinSection(ServerPlayer player) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("-- Sin --"));
        player.getCapability(ModCapabilities.SIN_DATA).resolve().ifPresentOrElse(sinData -> {
            String sin = sinData.isAligned()
                    ? sinData.getActiveSin().name()
                    : "NONE";
            lines.add(Component.literal("  Active Sin : " + sin));
            lines.add(Component.literal("  Sin Level  : " + sinData.getSinLevel()));
            lines.add(Component.literal("  Sin XP     : " + sinData.getSinExperience()));
        }, () -> lines.add(Component.literal("  [sin capability unavailable]")));
        return lines;
    }

    /** Returns lines describing current/max mana. */
    public static List<Component> buildManaSection(ServerPlayer player) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("-- Mana --"));
        ModCapabilities.get(player).resolve().ifPresentOrElse(cap -> {
            PlayerCharacterData data = cap.getData();
            lines.add(Component.literal("  Mana: " + data.getMana() + " / " + data.getMaxMana()));
        }, () -> lines.add(Component.literal("  [capability unavailable]")));
        return lines;
    }

    /** Returns lines listing every unlocked ability and its cooldown status. */
    public static List<Component> buildAbilitiesSection(ServerPlayer player) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("-- Abilities --"));
        ModCapabilities.get(player).resolve().ifPresentOrElse(cap -> {
            Set<AbilityType> unlocked = cap.getData().getUnlockedAbilities();
            if (unlocked.isEmpty()) {
                lines.add(Component.literal("  (none unlocked)"));
            } else {
                for (AbilityType ability : unlocked) {
                    long remainingMs = CooldownManager.getRemainingMs(player.getUUID(), ability);
                    String cooldown = remainingMs > 0
                            ? " [cooldown: " + (remainingMs / 1000) + "s]"
                            : " [ready]";
                    lines.add(Component.literal("  " + ability.name() + cooldown));
                }
            }
        }, () -> lines.add(Component.literal("  [capability unavailable]")));
        return lines;
    }

    /** Returns lines showing the active quest and its progress. */
    public static List<Component> buildQuestSection(ServerPlayer player) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("-- Quest --"));
        ModCapabilities.get(player).resolve().ifPresentOrElse(cap -> {
            PlayerQuestData qd = cap.getData().getQuestData();
            String active = qd.getActiveQuestId();
            if (active.isEmpty()) {
                lines.add(Component.literal("  Active: (none)"));
            } else {
                int progress = qd.getProgress(active);
                lines.add(Component.literal("  Active: " + active + "  progress=" + progress));
            }
            Set<String> completed = qd.getCompletedQuestIds();
            if (completed.isEmpty()) {
                lines.add(Component.literal("  Completed: (none)"));
            } else {
                lines.add(Component.literal("  Completed: " + String.join(", ", completed)));
            }
        }, () -> lines.add(Component.literal("  [capability unavailable]")));
        return lines;
    }

    /** Returns lines listing every active story flag. */
    public static List<Component> buildStorySection(ServerPlayer player) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("-- Story --"));
        ModCapabilities.get(player).resolve().ifPresentOrElse(cap -> {
            Set<String> flags = cap.getData().getQuestData().getStoryFlags();
            if (flags.isEmpty()) {
                lines.add(Component.literal("  Flags: (none)"));
            } else {
                lines.add(Component.literal("  Flags: " + String.join(", ", sorted(flags))));
            }
        }, () -> lines.add(Component.literal("  [capability unavailable]")));
        return lines;
    }

    /** Returns lines describing the current active boss state, if any. */
    public static List<Component> buildBossSection() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("-- Boss --"));
        BossManager.BossInfo bossState = BossManager.getClientBossState();
        if (bossState == null) {
            lines.add(Component.literal("  No active boss"));
        } else {
            lines.add(Component.literal("  Name   : " + bossState.name()));
            lines.add(Component.literal("  HP     : " + bossState.currentHp() + " / " + bossState.maxHp()));
            lines.add(Component.literal("  Phase  : " + bossState.phase().name()));
        }
        return lines;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static List<String> sorted(Collection<String> items) {
        List<String> sorted = new ArrayList<>(items);
        sorted.sort(null);
        return sorted;
    }
}
