package com.sevensins.entity;

import com.sevensins.boss.BossManager;
import com.sevensins.boss.BossPhase;
import com.sevensins.network.ModNetwork;
import com.sevensins.network.packet.SyncBossStatePacket;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

/**
 * The Gray Demon — the second major boss encounter of the Seven Deadly Sins RPG.
 *
 * <h2>Stats</h2>
 * <ul>
 *   <li>HP: {@value #MAX_HP}</li>
 *   <li>Damage: {@value #BASE_DAMAGE}</li>
 * </ul>
 *
 * <h2>Phase system</h2>
 * <ul>
 *   <li>{@link BossPhase#PHASE_1} – normal combat (full health).</li>
 *   <li>{@link BossPhase#PHASE_2} – starts at 60 % HP; increased speed and
 *       periodic corruption pulse.</li>
 *   <li>{@link BossPhase#ENRAGED} – starts at 25 % HP; further speed boost,
 *       higher melee damage, and more frequent corruption pulses.</li>
 * </ul>
 *
 * <h2>Special mechanic — Corruption Pulse</h2>
 * Every {@value #PULSE_INTERVAL_PHASE2} ticks in PHASE_2 (and every
 * {@value #PULSE_INTERVAL_ENRAGED} ticks in ENRAGED) the Gray Demon releases a
 * dark-energy corruption pulse that damages all players within
 * {@value #PULSE_RADIUS} blocks.
 *
 * <h2>Networking</h2>
 * Every 10 ticks while alive, the entity broadcasts a
 * {@link SyncBossStatePacket} to players within {@value #SYNC_RANGE} blocks.
 *
 * <p>Registered in {@link com.sevensins.registry.ModEntities}.</p>
 */
public class GrayDemonEntity extends Monster {

    /** Registry name used in {@link com.sevensins.registry.ModEntities}. */
    public static final String REGISTRY_NAME = "gray_demon";

    /** Base maximum health (HP). */
    public static final float MAX_HP = 800.0f;

    /** Base melee attack damage. */
    public static final double BASE_DAMAGE = 18.0;

    /** Radius (blocks) within which boss state is synced to players. */
    private static final double SYNC_RANGE = 100.0;

    /** HP ratio at which Phase 2 begins. */
    private static final float PHASE_2_HP_RATIO = 0.60f;

    /** HP ratio at which Enraged phase begins. */
    private static final float ENRAGED_HP_RATIO = 0.25f;

    /** Phase-2 movement speed. */
    private static final double PHASE_2_SPEED = 0.38;

    /** Enraged movement speed. */
    private static final double ENRAGED_SPEED = 0.46;

    /** Attack damage multiplier applied in the Enraged phase. */
    private static final double ENRAGED_DAMAGE = 26.0;

    /** Radius (blocks) of the corruption pulse special attack. */
    private static final double PULSE_RADIUS = 8.0;

    /** Damage dealt to each player hit by the corruption pulse. */
    private static final float PULSE_DAMAGE = 6.0f;

    /** Ticks between corruption pulses in PHASE_2. */
    private static final int PULSE_INTERVAL_PHASE2 = 80;

    /** Ticks between corruption pulses in ENRAGED. */
    private static final int PULSE_INTERVAL_ENRAGED = 40;

    private BossPhase phase = BossPhase.PHASE_1;

    public GrayDemonEntity(EntityType<? extends GrayDemonEntity> type, Level level) {
        super(type, level);
    }

    // -----------------------------------------------------------------------
    // AI goals
    // -----------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomWalkingGoal(this, 0.8));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
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
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!level().isClientSide()) {
            BossManager.getInstance().registerBoss(getUUID(), "Gray Demon", getHealth(), getMaxHealth());
            broadcastToNearbyPlayers(
                    Component.literal("A Gray Demon has appeared!").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && isAlive()) {
            checkPhaseTransitions();
            tickCorruptionPulse();
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
    // Phase transitions
    // -----------------------------------------------------------------------

    private void checkPhaseTransitions() {
        float hp = getHealth();
        float maxHp = getMaxHealth();

        if (phase == BossPhase.PHASE_1 && hp <= maxHp * PHASE_2_HP_RATIO) {
            enterPhase2();
        } else if (phase == BossPhase.PHASE_2 && hp <= maxHp * ENRAGED_HP_RATIO) {
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
        broadcastToNearbyPlayers(
                Component.literal("The Gray Demon's power intensifies!")
                        .withStyle(ChatFormatting.DARK_PURPLE));
        syncBossState();
    }

    private void enterEnraged() {
        phase = BossPhase.ENRAGED;

        var speedAttr = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(ENRAGED_SPEED);
        }

        var damageAttr = getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.setBaseValue(ENRAGED_DAMAGE);
        }

        BossManager.getInstance().updateBoss(getUUID(), getHealth(), phase);
        broadcastToNearbyPlayers(
                Component.literal("The Gray Demon has become ENRAGED!")
                        .withStyle(ChatFormatting.DARK_PURPLE).withStyle(ChatFormatting.BOLD));
        syncBossState();
    }

    // -----------------------------------------------------------------------
    // Corruption pulse special attack
    // -----------------------------------------------------------------------

    private void tickCorruptionPulse() {
        int interval = switch (phase) {
            case PHASE_2  -> PULSE_INTERVAL_PHASE2;
            case ENRAGED  -> PULSE_INTERVAL_ENRAGED;
            default       -> 0; // disabled in PHASE_1
        };

        if (interval <= 0 || tickCount % interval != 0) return;

        fireCorruptionPulse();
    }

    private void fireCorruptionPulse() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        AABB pulseBox = getBoundingBox().inflate(PULSE_RADIUS);
        List<Player> targets = serverLevel.getEntitiesOfClass(Player.class, pulseBox,
                p -> p.isAlive() && !p.isCreative() && !p.isSpectator());

        if (targets.isEmpty()) return;

        DamageSource pulseSource = damageSources().magic();
        for (Player target : targets) {
            target.hurt(pulseSource, PULSE_DAMAGE);
        }

        broadcastToNearbyPlayers(
                Component.literal("The Gray Demon unleashes a corruption pulse!")
                        .withStyle(ChatFormatting.DARK_PURPLE));
    }

    // -----------------------------------------------------------------------
    // Network sync helpers
    // -----------------------------------------------------------------------

    private void syncBossState() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        BossManager.getInstance().updateBoss(getUUID(), getHealth(), phase);
        SyncBossStatePacket packet = new SyncBossStatePacket(
                "Gray Demon", getHealth(), getMaxHealth(), phase);
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

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    /** Returns the current {@link BossPhase} of this entity. */
    public BossPhase getPhase() {
        return phase;
    }
}
