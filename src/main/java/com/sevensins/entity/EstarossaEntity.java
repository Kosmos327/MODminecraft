package com.sevensins.entity;

import com.sevensins.boss.BossManager;
import com.sevensins.boss.BossPhase;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.network.ModNetwork;
import com.sevensins.network.packet.SyncBossStatePacket;
import com.sevensins.story.StoryFlag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

/**
 * Estarossa — the fourth major boss and primary antagonist of Chapter 5.
 *
 * <h2>Stats</h2>
 * <ul>
 *   <li>HP:     {@value #MAX_HP}</li>
 *   <li>Damage: {@value #BASE_DAMAGE}</li>
 * </ul>
 *
 * <h2>Phase system</h2>
 * <ul>
 *   <li>{@link BossPhase#PHASE_1}  – controlled heavy duelist (full HP → 65%).</li>
 *   <li>{@link BossPhase#PHASE_2}  – increased pressure, corruption aura starts (65% → 30%).</li>
 *   <li>{@link BossPhase#ENRAGED} – maximum aggression, rapid aura pulses (below 30%).</li>
 * </ul>
 *
 * <h2>Special mechanics</h2>
 * <ul>
 *   <li><b>Corruption aura pulse</b> – periodic AOE damage to nearby players;
 *       active from Phase 2, faster in Enraged.</li>
 *   <li><b>Retaliation burst</b> – after taking 3+ hits within a short window,
 *       Estarossa unleashes a dark pulse that damages nearby players.</li>
 * </ul>
 *
 * <h2>Networking</h2>
 * Every 10 ticks while alive the entity broadcasts a {@link SyncBossStatePacket}
 * to players within {@value #SYNC_RANGE} blocks.
 *
 * <p>Registered in {@link com.sevensins.registry.ModEntities}.</p>
 */
public class EstarossaEntity extends Monster {

    /** Registry name used in {@link com.sevensins.registry.ModEntities}. */
    public static final String REGISTRY_NAME = "estarossa";

    /** Base maximum health (HP). */
    public static final float MAX_HP = 3000.0f;

    /**
     * Base melee attack damage.
     * Intentionally higher than previous bosses; centralised here for easy rebalance.
     */
    public static final double BASE_DAMAGE = 28.0;

    /** HP fraction at which Phase 2 begins (65 %). */
    private static final float PHASE_2_THRESHOLD = 0.65f;

    /** HP fraction at which the Enraged phase begins (30 %). */
    private static final float ENRAGED_THRESHOLD = 0.30f;

    /** Radius (blocks) within which boss state is synced to players. */
    private static final double SYNC_RANGE = 100.0;

    /** Phase-2 movement speed multiplier (replaces base 0.3). */
    private static final double PHASE_2_SPEED = 0.38;

    /** Enraged movement speed. */
    private static final double ENRAGED_SPEED = 0.46;

    // --- Corruption aura ---

    /** Corruption aura pulse interval in Phase 2 (ticks). */
    private static final int AURA_INTERVAL_PHASE2 = 80;

    /** Corruption aura pulse interval in Enraged phase (ticks). */
    private static final int AURA_INTERVAL_ENRAGED = 40;

    /** Radius (blocks) of the corruption aura pulse. */
    private static final double AURA_RADIUS = 7.0;

    /** Damage dealt per corruption aura pulse in Phase 2. */
    private static final float AURA_DAMAGE_PHASE2 = 4.0f;

    /** Damage dealt per corruption aura pulse in Enraged phase. */
    private static final float AURA_DAMAGE_ENRAGED = 6.0f;

    // --- Retaliation burst ---

    /** Number of incoming hits within the window that triggers the retaliation burst. */
    private static final int RETALIATION_HIT_THRESHOLD = 3;

    /** Tick window for counting retaliation hits. */
    private static final int RETALIATION_WINDOW_TICKS = 40;

    /** Radius (blocks) of the retaliation dark pulse. */
    private static final double RETALIATION_RADIUS = 6.0;

    /** Damage dealt by the retaliation dark pulse. */
    private static final float RETALIATION_DAMAGE = 5.0f;

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    private BossPhase phase = BossPhase.PHASE_1;

    /** Tick count at which the retaliation window started. */
    private int retaliationWindowStart = -1;

    /** Number of hits received within the current retaliation window. */
    private int retaliationHitCount = 0;

    public EstarossaEntity(EntityType<? extends EstarossaEntity> type, Level level) {
        super(type, level);
    }

    // -----------------------------------------------------------------------
    // AI goals
    // -----------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomWalkingGoal(this, 0.8));
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
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75);
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!level().isClientSide()) {
            BossManager.getInstance().registerBoss(getUUID(), "Estarossa", getHealth(), getMaxHealth());
            setStoryFlagForNearbyPlayers(StoryFlag.ESTAROSSA_ENCOUNTERED);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && isAlive()) {
            checkPhaseTransitions();
            tickCorruptionAura();
            if (tickCount % 10 == 0) {
                syncBossState();
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide()) {
            BossManager.getInstance().unregisterBoss(getUUID());
            clearClientOverlay();
        }
        super.remove(reason);
    }

    // -----------------------------------------------------------------------
    // Incoming damage — retaliation mechanic
    // -----------------------------------------------------------------------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean damaged = super.hurt(source, amount);
        if (damaged && !level().isClientSide() && phase != BossPhase.PHASE_1) {
            trackRetaliation();
        }
        return damaged;
    }

    /**
     * Tracks incoming hits and triggers a dark pulse when the retaliation
     * threshold is reached within the configured window.
     */
    private void trackRetaliation() {
        // Reset window if it has expired
        if (retaliationWindowStart < 0 || tickCount - retaliationWindowStart > RETALIATION_WINDOW_TICKS) {
            retaliationWindowStart = tickCount;
            retaliationHitCount = 0;
        }
        retaliationHitCount++;
        if (retaliationHitCount >= RETALIATION_HIT_THRESHOLD) {
            triggerRetaliationBurst();
            retaliationWindowStart = -1;
            retaliationHitCount = 0;
        }
    }

    /**
     * Unleashes a dark retaliatory pulse that damages nearby players.
     */
    private void triggerRetaliationBurst() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        DamageSource source = level().damageSources().mobAttack(this);
        for (ServerPlayer player : serverLevel.players()) {
            if (distanceTo(player) <= RETALIATION_RADIUS) {
                player.hurt(source, RETALIATION_DAMAGE);
                player.sendSystemMessage(
                        Component.literal("Estarossa retaliates with a dark pulse!")
                                .withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Phase transitions
    // -----------------------------------------------------------------------

    private void checkPhaseTransitions() {
        float hpFraction = getHealth() / getMaxHealth();

        if (phase == BossPhase.PHASE_1 && hpFraction <= PHASE_2_THRESHOLD) {
            enterPhase2();
        } else if (phase == BossPhase.PHASE_2 && hpFraction <= ENRAGED_THRESHOLD) {
            enterEnraged();
        }
    }

    private void enterPhase2() {
        phase = BossPhase.PHASE_2;

        var speedAttr = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(PHASE_2_SPEED);
        }

        BossManager.getInstance().updateBoss(getUUID(), getHealth(), phase);
        setStoryFlagForNearbyPlayers(StoryFlag.ESTAROSSA_PHASE_2_SEEN);
        broadcastToNearbyPlayers(
                Component.literal("Estarossa's dark aura intensifies!")
                        .withStyle(ChatFormatting.DARK_PURPLE));
        syncBossState();
    }

    private void enterEnraged() {
        phase = BossPhase.ENRAGED;

        var speedAttr = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(ENRAGED_SPEED);
        }

        BossManager.getInstance().updateBoss(getUUID(), getHealth(), phase);
        setStoryFlagForNearbyPlayers(StoryFlag.ESTAROSSA_ENRAGED_SEEN);
        broadcastToNearbyPlayers(
                Component.literal("Estarossa: \"You are nothing before my power!\"")
                        .withStyle(ChatFormatting.DARK_RED));
        syncBossState();
    }

    // -----------------------------------------------------------------------
    // Corruption aura pulse
    // -----------------------------------------------------------------------

    private void tickCorruptionAura() {
        if (phase == BossPhase.PHASE_1) return;

        int interval = phase == BossPhase.ENRAGED ? AURA_INTERVAL_ENRAGED : AURA_INTERVAL_PHASE2;
        float damage = phase == BossPhase.ENRAGED ? AURA_DAMAGE_ENRAGED : AURA_DAMAGE_PHASE2;

        if (tickCount % interval == 0) {
            pulseCorruptionAura(damage);
        }
    }

    private void pulseCorruptionAura(float damage) {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        DamageSource source = level().damageSources().mobAttack(this);
        for (ServerPlayer player : serverLevel.players()) {
            if (distanceTo(player) <= AURA_RADIUS) {
                player.hurt(source, damage);
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
                "Estarossa", getHealth(), getMaxHealth(), phase);
        for (ServerPlayer player : serverLevel.players()) {
            if (distanceTo(player) <= SYNC_RANGE) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
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

    /**
     * Sets a story flag for all nearby players who have a character selected.
     * Silently no-ops if the capability is missing.
     */
    private void setStoryFlagForNearbyPlayers(StoryFlag flag) {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        for (ServerPlayer player : serverLevel.players()) {
            if (distanceTo(player) > SYNC_RANGE) continue;
            ModCapabilities.get(player).ifPresent(cap ->
                    cap.getData().getQuestData().addStoryFlag(flag.getId()));
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
