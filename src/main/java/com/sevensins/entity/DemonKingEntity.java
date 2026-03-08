package com.sevensins.entity;

import com.sevensins.boss.BossManager;
import com.sevensins.boss.BossPhase;
import com.sevensins.network.ModNetwork;
import com.sevensins.network.packet.SyncBossStatePacket;
import com.sevensins.story.StoryFlag;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.world.BossArenaManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

/**
 * The Demon King — the supreme final boss of the Seven Deadly Sins RPG.
 *
 * <h2>Stats</h2>
 * <ul>
 *   <li>HP: {@value #MAX_HP}</li>
 *   <li>Base damage: {@value #BASE_DAMAGE}</li>
 * </ul>
 *
 * <h2>Phase system</h2>
 * <ul>
 *   <li>{@link BossPhase#PHASE_1}  — full health → 75 %.</li>
 *   <li>{@link BossPhase#PHASE_2}  — 75 % → 45 %; corruption pulse, increased speed.</li>
 *   <li>{@link BossPhase#PHASE_3}  — 45 % → 15 %; signature dark shockwave more frequent.</li>
 *   <li>{@link BossPhase#FINAL_PHASE} — below 15 %; maximum aggression.</li>
 * </ul>
 *
 * <h2>Signature mechanic</h2>
 * A dark shockwave that radiates from the Demon King every
 * {@value #SHOCKWAVE_COOLDOWN_TICKS} ticks (reduced in later phases), dealing
 * heavy damage to all players within {@value #SHOCKWAVE_RADIUS} blocks and
 * applying a darkness-like corruption penalty.
 *
 * <h2>Networking</h2>
 * Every 10 ticks while alive the entity broadcasts a
 * {@link SyncBossStatePacket} to nearby players so the
 * {@link com.sevensins.client.hud.BossHealthOverlay} stays accurate.
 *
 * <p>Registered in {@link com.sevensins.registry.ModEntities}.</p>
 */
public class DemonKingEntity extends Monster {

    /** Registry name used in {@link com.sevensins.registry.ModEntities}. */
    public static final String REGISTRY_NAME = "demon_king";

    /** Base maximum health (HP). Clearly above Estarossa (3 000). */
    public static final float MAX_HP = 10_000.0f;

    /**
     * Base melee attack damage.  Final-boss-tier, clearly above Estarossa (28).
     * Centralised here for easy rebalancing.
     */
    public static final double BASE_DAMAGE = 40.0;

    /** Radius (blocks) within which boss state is synced to players. */
    private static final double SYNC_RANGE = 120.0;

    // Phase HP thresholds
    private static final float PHASE_2_THRESHOLD = 0.75f;
    private static final float PHASE_3_THRESHOLD = 0.45f;
    private static final float FINAL_PHASE_THRESHOLD = 0.15f;

    // Movement speeds per phase
    private static final double PHASE_1_SPEED = 0.28;
    private static final double PHASE_2_SPEED = 0.33;
    private static final double PHASE_3_SPEED = 0.38;
    private static final double FINAL_PHASE_SPEED = 0.45;

    // Signature dark shockwave
    /** Radius of the dark shockwave in blocks. */
    private static final double SHOCKWAVE_RADIUS = 12.0;
    /** Base shockwave damage. */
    private static final float SHOCKWAVE_BASE_DAMAGE = 18.0f;
    /** Default ticks between shockwave pulses (Phase 1/2). */
    private static final int SHOCKWAVE_COOLDOWN_TICKS = 120;
    /** Faster shockwave in Phase 3. */
    private static final int SHOCKWAVE_COOLDOWN_PHASE_3 = 80;
    /** Fastest shockwave in Final Phase. */
    private static final int SHOCKWAVE_COOLDOWN_FINAL = 50;

    // Corruption pulse (Phase 2+)
    /** Radius of the corruption aura pulse. */
    private static final double CORRUPTION_PULSE_RADIUS = 8.0;
    /** Damage dealt per corruption pulse. */
    private static final float CORRUPTION_PULSE_DAMAGE = 6.0f;
    /** Ticks between corruption pulses. */
    private static final int CORRUPTION_PULSE_COOLDOWN = 60;

    private BossPhase phase = BossPhase.PHASE_1;
    private int shockwaveTimer = 0;
    private int corruptionPulseTimer = 0;

    public DemonKingEntity(EntityType<? extends DemonKingEntity> type, Level level) {
        super(type, level);
    }

    // -----------------------------------------------------------------------
    // AI goals
    // -----------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -----------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------

    /** Called during entity registration to supply the base attribute map. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, PHASE_1_SPEED)
                .add(Attributes.FOLLOW_RANGE, 60.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!level().isClientSide()) {
            BossManager.getInstance().registerBoss(getUUID(), "Demon King", getHealth(), getMaxHealth());
            BossArenaManager.getInstance().openDemonKingArena(getUUID(), getX(), getY(), getZ());
            broadcastToNearbyPlayers(
                    Component.literal("The Demon King has awakened!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && isAlive()) {
            checkPhaseTransitions();
            tickSignatureMechanics();
            if (tickCount % 10 == 0) {
                syncBossState();
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide()) {
            BossManager.getInstance().unregisterBoss(getUUID());
            BossArenaManager.getInstance().closeArena(getUUID());
            clearClientOverlay();
        }
        super.remove(reason);
    }

    // -----------------------------------------------------------------------
    // Phase transitions
    // -----------------------------------------------------------------------

    private void checkPhaseTransitions() {
        float ratio = getHealth() / getMaxHealth();

        if (phase == BossPhase.PHASE_1 && ratio <= PHASE_2_THRESHOLD) {
            enterPhase2();
        } else if (phase == BossPhase.PHASE_2 && ratio <= PHASE_3_THRESHOLD) {
            enterPhase3();
        } else if (phase == BossPhase.PHASE_3 && ratio <= FINAL_PHASE_THRESHOLD) {
            enterFinalPhase();
        }
    }

    private void enterPhase2() {
        phase = BossPhase.PHASE_2;
        setSpeed(PHASE_2_SPEED);
        BossManager.getInstance().updateBoss(getUUID(), getHealth(), phase);
        notifyPhaseChange(StoryFlag.DEMON_KING_PHASE_2_SEEN,
                "The Demon King's power swells — Phase 2!", ChatFormatting.RED);
        syncBossState();
    }

    private void enterPhase3() {
        phase = BossPhase.PHASE_3;
        setSpeed(PHASE_3_SPEED);
        BossManager.getInstance().updateBoss(getUUID(), getHealth(), phase);
        notifyPhaseChange(StoryFlag.DEMON_KING_PHASE_3_SEEN,
                "The Demon King unleashes true power — Phase 3!", ChatFormatting.DARK_RED);
        syncBossState();
    }

    private void enterFinalPhase() {
        phase = BossPhase.FINAL_PHASE;
        setSpeed(FINAL_PHASE_SPEED);
        BossManager.getInstance().updateBoss(getUUID(), getHealth(), phase);
        notifyPhaseChange(StoryFlag.DEMON_KING_FINAL_PHASE_SEEN,
                "⚠ The Demon King enters his FINAL PHASE! ⚠", ChatFormatting.DARK_PURPLE);
        syncBossState();
    }

    /** Applies a new base movement speed to the entity's speed attribute. */
    private void setSpeed(double speed) {
        var speedAttr = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(speed);
        }
    }

    /**
     * Broadcasts a phase-transition message and sets a story flag on all nearby
     * players with an active character.
     */
    private void notifyPhaseChange(StoryFlag flag, String message, ChatFormatting color) {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        for (ServerPlayer player : serverLevel.players()) {
            if (distanceTo(player) <= SYNC_RANGE) {
                player.sendSystemMessage(Component.literal(message).withStyle(color));
                setStoryFlagSafe(player, flag);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Signature mechanics
    // -----------------------------------------------------------------------

    private void tickSignatureMechanics() {
        shockwaveTimer++;
        int shockwaveCooldown = getShockwaveCooldown();
        if (shockwaveTimer >= shockwaveCooldown) {
            shockwaveTimer = 0;
            performDarkShockwave();
        }

        if (phase != BossPhase.PHASE_1) {
            corruptionPulseTimer++;
            if (corruptionPulseTimer >= CORRUPTION_PULSE_COOLDOWN) {
                corruptionPulseTimer = 0;
                performCorruptionPulse();
            }
        }
    }

    private int getShockwaveCooldown() {
        return switch (phase) {
            case PHASE_3 -> SHOCKWAVE_COOLDOWN_PHASE_3;
            case FINAL_PHASE -> SHOCKWAVE_COOLDOWN_FINAL;
            default -> SHOCKWAVE_COOLDOWN_TICKS;
        };
    }

    /**
     * Signature mechanic: dark shockwave.
     * Deals heavy damage to all players within {@value #SHOCKWAVE_RADIUS} blocks.
     * Damage is increased in later phases.
     */
    private void performDarkShockwave() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        float damage = getShockwaveDamage();
        for (ServerPlayer player : serverLevel.players()) {
            if (distanceTo(player) <= SHOCKWAVE_RADIUS) {
                player.hurt(damageSources().mobAttack(this), damage);
                player.sendSystemMessage(
                        Component.literal("The Demon King's dark shockwave engulfs you!")
                                .withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
    }

    private float getShockwaveDamage() {
        return switch (phase) {
            case PHASE_2 -> SHOCKWAVE_BASE_DAMAGE * 1.25f;
            case PHASE_3 -> SHOCKWAVE_BASE_DAMAGE * 1.5f;
            case FINAL_PHASE -> SHOCKWAVE_BASE_DAMAGE * 2.0f;
            default -> SHOCKWAVE_BASE_DAMAGE;
        };
    }

    /**
     * Corruption pulse: area dark damage to nearby players (Phase 2+).
     */
    private void performCorruptionPulse() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        for (ServerPlayer player : serverLevel.players()) {
            if (distanceTo(player) <= CORRUPTION_PULSE_RADIUS) {
                player.hurt(damageSources().mobAttack(this), CORRUPTION_PULSE_DAMAGE);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Network sync helpers
    // -----------------------------------------------------------------------

    private void syncBossState() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        BossManager.getInstance().updateBoss(getUUID(), getHealth(), phase);
        SyncBossStatePacket packet = new SyncBossStatePacket(
                "Demon King", getHealth(), getMaxHealth(), phase);
        for (ServerPlayer player : serverLevel.players()) {
            if (distanceTo(player) <= SYNC_RANGE) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
                // Mark encounter start flag the first time the player is nearby
                setStoryFlagSafe(player, StoryFlag.DEMON_KING_ENCOUNTERED);
            }
        }
    }

    private void clearClientOverlay() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        SyncBossStatePacket clearPacket = SyncBossStatePacket.clear();
        for (ServerPlayer player : serverLevel.players()) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), clearPacket);
        }
    }

    private void broadcastToNearbyPlayers(Component message) {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        for (ServerPlayer player : serverLevel.players()) {
            if (distanceTo(player) <= SYNC_RANGE) {
                player.sendSystemMessage(message);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Story flag helper
    // -----------------------------------------------------------------------

    private static void setStoryFlagSafe(ServerPlayer player, StoryFlag flag) {
        try {
            ModCapabilities.get(player).ifPresent(cap ->
                    cap.getData().getQuestData().addStoryFlag(flag.getId()));
        } catch (Exception ignored) {
            // failsafe: never crash on flag set
        }
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    /** Returns the current {@link BossPhase} of this entity. */
    public BossPhase getPhase() {
        return phase;
    }
}
