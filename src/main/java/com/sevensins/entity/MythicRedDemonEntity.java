package com.sevensins.entity;

import com.sevensins.boss.BossPhase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * The Mythic Red Demon — a harder, endgame-tier variant of the
 * {@link RedDemonEntity} encountered during Night Demon Raids.
 *
 * <h2>Mythic tier adjustments</h2>
 * <ul>
 *   <li>HP: {@value #MYTHIC_MAX_HP} ({@value #HP_MULTIPLIER}× base)</li>
 *   <li>Damage: {@value #MYTHIC_BASE_DAMAGE} ({@value #DAMAGE_MULTIPLIER}× base)</li>
 *   <li>Speed: slightly increased in Phase 2</li>
 * </ul>
 *
 * <p>All phase logic, networking, and boss-tracking logic are inherited from
 * {@link RedDemonEntity}.  This class only overrides stats, registry name,
 * and display name.</p>
 *
 * <p>Registered in {@link com.sevensins.registry.ModEntities}.</p>
 */
public class MythicRedDemonEntity extends RedDemonEntity {

    /** Registry name used in {@link com.sevensins.registry.ModEntities}. */
    public static final String REGISTRY_NAME = "mythic_red_demon";

    // -------------------------------------------------------------------------
    // Mythic tier multipliers (centralised)
    // -------------------------------------------------------------------------

    /** HP multiplier relative to the base Red Demon. */
    public static final float HP_MULTIPLIER = 3.0f;

    /** Damage multiplier relative to the base Red Demon. */
    public static final float DAMAGE_MULTIPLIER = 2.0f;

    /** Mythic reward multiplier (XP / rewards). */
    public static final float REWARD_MULTIPLIER = 3.0f;

    // -------------------------------------------------------------------------
    // Derived stat constants
    // -------------------------------------------------------------------------

    /** Mythic maximum health. */
    public static final float MYTHIC_MAX_HP     = MAX_HP * HP_MULTIPLIER;

    /** Mythic base melee damage. */
    public static final double MYTHIC_BASE_DAMAGE = BASE_DAMAGE * DAMAGE_MULTIPLIER;

    /** Mythic Phase-2 movement speed (slightly faster than normal Phase-2). */
    private static final double MYTHIC_PHASE_2_SPEED = PHASE_2_SPEED + 0.05;

    /** Epsilon for floating-point speed comparisons. */
    private static final double SPEED_EPSILON = 0.001;

    /** Range (blocks) within which the mythic boss appearance broadcast is sent. */
    private static final double APPEARANCE_BROADCAST_RANGE = 100.0;

    /** Tracks whether the mythic Phase-2 speed boost has been applied to avoid repeated updates. */
    private boolean mythicSpeedApplied = false;

    public MythicRedDemonEntity(EntityType<? extends MythicRedDemonEntity> type, Level level) {
        super(type, level);
    }

    // -----------------------------------------------------------------------
    // Display name override
    // -----------------------------------------------------------------------

    @Override
    protected String getBossDisplayName() {
        return "Mythic Red Demon";
    }

    // -----------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------

    /** Called during entity registration to supply the mythic attribute map. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MYTHIC_MAX_HP)
                .add(Attributes.ATTACK_DAMAGE, MYTHIC_BASE_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 50.0);
    }

    // -----------------------------------------------------------------------
    // Phase 2 — slightly faster than the base demon
    // -----------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        // Phase 2 speed is handled by the parent's checkPhaseTransition.
        // We patch the speed value after parent has applied Phase 2 to use
        // the higher mythic speed. Only apply once to avoid repeated attribute lookups.
        if (!level().isClientSide() && !mythicSpeedApplied
                && phase == BossPhase.PHASE_2 && isAlive()) {
            var speedAttr = getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null
                    && speedAttr.getBaseValue() < MYTHIC_PHASE_2_SPEED - SPEED_EPSILON) {
                speedAttr.setBaseValue(MYTHIC_PHASE_2_SPEED);
                mythicSpeedApplied = true;
            }
        }
    }

    // -----------------------------------------------------------------------
    // Phase 2 broadcast — override message
    // -----------------------------------------------------------------------

    /**
     * Sends a mythic-specific warning when Phase 2 begins.
     * The parent's {@code checkPhaseTransition()} still broadcasts the generic
     * message; we additionally broadcast the mythic warning here via the death
     * message path so players know this is a stronger encounter.
     */
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                if (distanceTo(player) <= APPEARANCE_BROADCAST_RANGE) {
                    player.sendSystemMessage(
                            Component.literal("A Mythic demon has appeared!")
                                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
                }
            }
        }
    }
}
