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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

/**
 * The Red Demon — the first major boss encounter of the Seven Deadly Sins RPG.
 *
 * <h2>Stats</h2>
 * <ul>
 *   <li>HP: {@value #MAX_HP}</li>
 *   <li>Damage: {@value #BASE_DAMAGE}</li>
 * </ul>
 *
 * <h2>Phase system</h2>
 * The entity starts in {@link BossPhase#PHASE_1}.  When health drops to or
 * below 50 % of maximum, it transitions to {@link BossPhase#PHASE_2}, gaining
 * increased movement speed and warning nearby players.
 *
 * <h2>Networking</h2>
 * Every 10 ticks while alive, the entity broadcasts a
 * {@link SyncBossStatePacket} to players within {@value #SYNC_RANGE} blocks so
 * the {@link com.sevensins.client.hud.BossHealthOverlay} stays accurate.
 *
 * <p>Registered in {@link com.sevensins.registry.ModEntities}.</p>
 */
public class RedDemonEntity extends Monster {

    /** Registry name used in {@link com.sevensins.registry.ModEntities}. */
    public static final String REGISTRY_NAME = "red_demon";

    /** Base maximum health (HP). */
    public static final float MAX_HP = 500.0f;

    /** Base melee attack damage. */
    public static final double BASE_DAMAGE = 12.0;

    /** Radius (blocks) within which boss state is synced to players. */
    private static final double SYNC_RANGE = 100.0;

    /** Phase-2 movement speed (replaces base 0.3 at 50 % HP). */
    private static final double PHASE_2_SPEED = 0.4;

    private BossPhase phase = BossPhase.PHASE_1;

    public RedDemonEntity(EntityType<? extends RedDemonEntity> type, Level level) {
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
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!level().isClientSide()) {
            BossManager.getInstance().registerBoss(getUUID(), "Red Demon", getHealth(), getMaxHealth());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && isAlive()) {
            checkPhaseTransition();
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
    // Phase transition
    // -----------------------------------------------------------------------

    private void checkPhaseTransition() {
        if (phase == BossPhase.PHASE_1 && getHealth() <= getMaxHealth() * 0.5f) {
            phase = BossPhase.PHASE_2;

            var speedAttr = getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.setBaseValue(PHASE_2_SPEED);
            }

            BossManager.getInstance().updateBoss(getUUID(), getHealth(), phase);
            broadcastToNearbyPlayers(
                    Component.literal("The Red Demon grows stronger!").withStyle(ChatFormatting.RED));
            syncBossState();
        }
    }

    // -----------------------------------------------------------------------
    // Network sync helpers
    // -----------------------------------------------------------------------

    private void syncBossState() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        BossManager.getInstance().updateBoss(getUUID(), getHealth(), phase);
        SyncBossStatePacket packet = new SyncBossStatePacket(
                "Red Demon", getHealth(), getMaxHealth(), phase);
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
