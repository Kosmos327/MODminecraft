package com.sevensins.entity;

import com.sevensins.boss.BossManager;
import com.sevensins.boss.BossPhase;
import com.sevensins.config.BalanceHelper;
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
 * Every 10 ticks while alive, the entity checks whether the boss state has
 * changed meaningfully (health delta &ge; 2 % of max HP, or a phase change)
 * and broadcasts a {@link SyncBossStatePacket} to nearby players only when
 * needed.  Phase transitions always trigger an immediate sync regardless of
 * the interval.
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
    protected static final double PHASE_2_SPEED = 0.4;

    /**
     * Minimum HP change (as a fraction of max HP) required to trigger a sync.
     * A value of 0.02 means syncs occur when HP changes by 2 % or more.
     */
    private static final float SYNC_HP_DELTA_THRESHOLD = 0.02f;

    /** Current phase — protected so mythic subclasses can extend phase behaviour. */
    protected BossPhase phase = BossPhase.PHASE_1;

    /** HP at the time of the last broadcast – used to suppress redundant syncs. */
    private float lastSyncedHp = MAX_HP;
    /** Phase at the time of the last broadcast; {@code null} until the first sync. */
    private BossPhase lastSyncedPhase = null;

    public RedDemonEntity(EntityType<? extends RedDemonEntity> type, Level level) {
        super(type, level);
    }

    // -----------------------------------------------------------------------
    // Boss display name — override in subclasses
    // -----------------------------------------------------------------------

    /**
     * Returns the display name sent in {@link SyncBossStatePacket} and shown
     * in {@link com.sevensins.client.hud.BossHealthOverlay}.
     *
     * <p>Subclasses (e.g. {@link MythicRedDemonEntity}) override this to show
     * a different name.</p>
     */
    protected String getBossDisplayName() {
        return "Red Demon";
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
            BossManager.getInstance().registerBoss(getUUID(), getBossDisplayName(), getHealth(), getMaxHealth());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && isAlive()) {
            checkPhaseTransition();
            if (tickCount % 10 == 0 && shouldSync()) {
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
            // Phase transition always forces an immediate sync
            syncBossState();
        }
    }

    // -----------------------------------------------------------------------
    // Network sync helpers
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} when the boss state has changed enough to justify
     * sending a sync packet to nearby players.
     *
     * <p>A sync is needed when:
     * <ul>
     *   <li>No sync has been sent yet (first tick).</li>
     *   <li>The active phase differs from the last-synced phase.</li>
     *   <li>HP has changed by more than {@value #SYNC_HP_DELTA_THRESHOLD} of
     *       max HP since the last sync.</li>
     * </ul>
     * </p>
     */
    private boolean shouldSync() {
        if (lastSyncedPhase == null) return true;
        if (phase != lastSyncedPhase) return true;
        float delta = Math.abs(getHealth() - lastSyncedHp);
        return delta >= getMaxHealth() * SYNC_HP_DELTA_THRESHOLD;
    }

    private void syncBossState() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        BossManager.getInstance().updateBoss(getUUID(), getHealth(), phase);
        SyncBossStatePacket packet = new SyncBossStatePacket(
                getBossDisplayName(), getHealth(), getMaxHealth(), phase);
        for (ServerPlayer player : serverLevel.players()) {
            if (distanceTo(player) <= SYNC_RANGE) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
        // Record the state we just synced so redundant sends can be suppressed.
        lastSyncedHp    = getHealth();
        lastSyncedPhase = phase;
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
