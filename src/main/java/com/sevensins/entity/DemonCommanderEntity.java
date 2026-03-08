package com.sevensins.entity;

import com.sevensins.boss.BossManager;
import com.sevensins.boss.BossPhase;
import com.sevensins.network.ModNetwork;
import com.sevensins.network.packet.SyncBossStatePacket;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

/**
 * The Demon Commander — the third major boss encounter of the Seven Deadly Sins RPG.
 *
 * <h2>Stats</h2>
 * <ul>
 *   <li>HP: {@value #MAX_HP}</li>
 *   <li>Damage: {@value #BASE_DAMAGE}</li>
 * </ul>
 *
 * <h2>Phase system</h2>
 * <ul>
 *   <li>{@link BossPhase#PHASE_1} – full health; melee + occasional summon wave.</li>
 *   <li>{@link BossPhase#PHASE_2} – below {@value #PHASE_2_HP_RATIO}% HP; increased
 *       aggression, shorter summon cooldown, command roar.</li>
 *   <li>{@link BossPhase#ENRAGED} – below {@value #ENRAGED_HP_RATIO}% HP; maximum
 *       pressure, fastest summon cycle, command roar on cooldown.</li>
 * </ul>
 *
 * <h2>Minion summoning</h2>
 * The boss spawns up to {@value #MAX_ACTIVE_MINIONS} tracked minion entities.
 * Minions are registered with {@link BossManager} and cleaned up on boss death.
 *
 * <p>Registered in {@link com.sevensins.registry.ModEntities}.</p>
 */
public class DemonCommanderEntity extends Monster {

    /** Registry name used in {@link com.sevensins.registry.ModEntities}. */
    public static final String REGISTRY_NAME = "demon_commander";

    /** Base maximum health (HP). */
    public static final float MAX_HP = 1500.0f;

    /** Base melee attack damage. */
    public static final double BASE_DAMAGE = 22.0;

    /** HP percentage at which PHASE_2 begins (70 %). */
    private static final float PHASE_2_HP_RATIO = 0.70f;

    /** HP percentage at which ENRAGED phase begins (30 %). */
    private static final float ENRAGED_HP_RATIO = 0.30f;

    /** Radius (blocks) within which boss state is synced to players. */
    private static final double SYNC_RANGE = 120.0;

    /** Phase-2 movement speed multiplier over the base 0.28. */
    private static final double PHASE_2_SPEED = 0.34;

    /** Enraged movement speed. */
    private static final double ENRAGED_SPEED = 0.40;

    /** Maximum number of simultaneously tracked minions. */
    private static final int MAX_ACTIVE_MINIONS = 4;

    /** Summon cooldown ticks in PHASE_1 (every ~15 s). */
    private static final int SUMMON_COOLDOWN_PHASE1 = 300;

    /** Summon cooldown ticks in PHASE_2 (every ~10 s). */
    private static final int SUMMON_COOLDOWN_PHASE2 = 200;

    /** Summon cooldown ticks in ENRAGED phase (every ~6 s). */
    private static final int SUMMON_COOLDOWN_ENRAGED = 120;

    /** Command roar cooldown ticks (~20 s). */
    private static final int ROAR_COOLDOWN = 400;

    private BossPhase phase = BossPhase.PHASE_1;
    private int summonTimer = SUMMON_COOLDOWN_PHASE1;
    private int roarTimer = ROAR_COOLDOWN;

    public DemonCommanderEntity(EntityType<? extends DemonCommanderEntity> type, Level level) {
        super(type, level);
    }

    // -----------------------------------------------------------------------
    // AI goals
    // -----------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0f));
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
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!level().isClientSide()) {
            BossManager.getInstance().registerBoss(
                    getUUID(), "Demon Commander", getHealth(), getMaxHealth());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() || !isAlive()) return;

        checkPhaseTransitions();

        if (tickCount % 10 == 0) {
            syncBossState();
        }

        // Summon timer countdown
        if (summonTimer > 0) {
            summonTimer--;
        } else {
            trySummonMinions();
            summonTimer = getSummonCooldown();
        }

        // Roar timer countdown (PHASE_2 and beyond)
        if (phase != BossPhase.PHASE_1) {
            if (roarTimer > 0) {
                roarTimer--;
            } else {
                performCommandRoar();
                roarTimer = ROAR_COOLDOWN;
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide()) {
            if (level() instanceof ServerLevel serverLevel) {
                BossManager.getInstance().cleanupMinions(getUUID(), serverLevel);
            }
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
        if (speedAttr != null) speedAttr.setBaseValue(PHASE_2_SPEED);

        BossManager.getInstance().updateBoss(getUUID(), getHealth(), phase);
        broadcastToNearbyPlayers(
                Component.literal("The Demon Commander issues a rallying war cry!")
                        .withStyle(ChatFormatting.DARK_RED));
        syncBossState();

        // Immediate summon wave on phase transition
        summonTimer = 0;
    }

    private void enterEnraged() {
        phase = BossPhase.ENRAGED;

        var speedAttr = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) speedAttr.setBaseValue(ENRAGED_SPEED);

        BossManager.getInstance().updateBoss(getUUID(), getHealth(), phase);
        broadcastToNearbyPlayers(
                Component.literal("The Demon Commander enters a terrifying rage!")
                        .withStyle(ChatFormatting.DARK_PURPLE));
        syncBossState();

        // Immediate summon wave on phase transition
        summonTimer = 0;
        roarTimer = 0;
    }

    // -----------------------------------------------------------------------
    // Summon mechanic
    // -----------------------------------------------------------------------

    /**
     * Attempts to spawn minions up to {@value #MAX_ACTIVE_MINIONS}.
     * Safely skips spawning if the cap is already reached or if spawning fails.
     */
    private void trySummonMinions() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        int currentMinions = BossManager.getInstance().getActiveMinionCount(getUUID());
        if (currentMinions >= MAX_ACTIVE_MINIONS) return;

        int toSpawn = MAX_ACTIVE_MINIONS - currentMinions;
        // Cap individual wave size to avoid sudden overwhelming spawns
        int waveSize = Math.min(toSpawn, phase == BossPhase.ENRAGED ? 3 : 2);

        boolean firstSummon = currentMinions == 0 && tickCount > 40;

        for (int i = 0; i < waveSize; i++) {
            try {
                spawnMinion(serverLevel);
            } catch (Exception ignored) {
                // Safety: never crash if minion spawn fails
            }
        }

        if (firstSummon) {
            broadcastToNearbyPlayers(
                    Component.literal("The Demon Commander calls for reinforcements!")
                            .withStyle(ChatFormatting.DARK_RED));
        }
    }

    /**
     * Spawns a single lesser demon minion near the commander and registers it
     * with {@link BossManager}.
     */
    private void spawnMinion(ServerLevel serverLevel) {
        // Use a Zombie as a placeholder lesser demon minion
        net.minecraft.world.entity.monster.Zombie minion =
                new net.minecraft.world.entity.monster.Zombie(
                        net.minecraft.world.entity.EntityType.ZOMBIE, serverLevel);

        // Position randomly around the commander within 4 blocks
        double offsetX = (random.nextDouble() - 0.5) * 8.0;
        double offsetZ = (random.nextDouble() - 0.5) * 8.0;
        minion.setPos(getX() + offsetX, getY(), getZ() + offsetZ);

        // Give the minion a custom name so players can identify it
        minion.setCustomName(Component.literal("Lesser Demon").withStyle(ChatFormatting.DARK_RED));
        minion.setCustomNameVisible(true);

        // Boost minion health to match the encounter tier
        var healthAttr = minion.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) healthAttr.setBaseValue(40.0);
        minion.setHealth(40.0f);

        var damageAttr = minion.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) damageAttr.setBaseValue(8.0);

        boolean added = serverLevel.addFreshEntity(minion);
        if (added) {
            BossManager.getInstance().registerMinion(getUUID(), minion.getUUID());
        }
    }

    // -----------------------------------------------------------------------
    // Command roar
    // -----------------------------------------------------------------------

    /**
     * Performs the command roar: deals a small pulse of damage to nearby players
     * and buffs nearby minions with speed.
     */
    private void performCommandRoar() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        broadcastToNearbyPlayers(
                Component.literal("The Demon Commander roars a commanding order!")
                        .withStyle(ChatFormatting.DARK_PURPLE));

        // Dark pulse: deal damage to all players within 8 blocks
        double pulseRadius = 8.0;
        AABB pulseBounds = new AABB(
                getX() - pulseRadius, getY() - 2, getZ() - pulseRadius,
                getX() + pulseRadius, getY() + 4, getZ() + pulseRadius);

        for (Player player : serverLevel.getEntitiesOfClass(Player.class, pulseBounds)) {
            player.hurt(level().damageSources().magic(), 6.0f);
        }

        // Buff nearby minions with a brief speed boost via potion effect
        for (net.minecraft.world.entity.monster.Zombie minion :
                serverLevel.getEntitiesOfClass(
                        net.minecraft.world.entity.monster.Zombie.class,
                        getBoundingBox().inflate(20.0))) {
            // Only buff our tracked minions
            if (BossManager.getInstance().getTrackedMinions(getUUID())
                    .contains(minion.getUUID())) {
                minion.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 200, 1));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private int getSummonCooldown() {
        return switch (phase) {
            case PHASE_1 -> SUMMON_COOLDOWN_PHASE1;
            case PHASE_2 -> SUMMON_COOLDOWN_PHASE2;
            case ENRAGED -> SUMMON_COOLDOWN_ENRAGED;
            // PHASE_3 and FINAL_PHASE are not used by DemonCommander; fall back to ENRAGED rate
            default -> SUMMON_COOLDOWN_ENRAGED;
        };
    }

    // -----------------------------------------------------------------------
    // Network sync helpers
    // -----------------------------------------------------------------------

    private void syncBossState() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        BossManager.getInstance().updateBoss(getUUID(), getHealth(), phase);
        SyncBossStatePacket packet = new SyncBossStatePacket(
                "Demon Commander", getHealth(), getMaxHealth(), phase);
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
