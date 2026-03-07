package com.sevensins.entity;

import com.sevensins.dialogue.DialogueManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;

/**
 * Meliodas mentor NPC entity.
 *
 * <p>This is a minimal interactive NPC for Chapter 2:
 * <ul>
 *   <li>No hostile AI — it will not attack the player.</li>
 *   <li>Right-clicking triggers the appropriate Meliodas dialogue
 *       via {@link DialogueManager}.</li>
 * </ul>
 *
 * <p>Registered in {@link com.sevensins.registry.ModEntities}.</p>
 */
public class MeliodasNpcEntity extends PathfinderMob {

    /** Registry name used in {@link com.sevensins.registry.ModEntities} and as the NPC ID. */
    public static final String REGISTRY_NAME = "meliodas_npc";

    public MeliodasNpcEntity(EntityType<? extends MeliodasNpcEntity> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);
    }

    // -----------------------------------------------------------------------
    // AI goals
    // -----------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    // -----------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------

    /** Called during entity registration to supply the base attribute map. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    // -----------------------------------------------------------------------
    // Interaction (right-click)
    // -----------------------------------------------------------------------

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND
                && !this.level().isClientSide()
                && player instanceof ServerPlayer serverPlayer) {
            DialogueManager.getInstance().onPlayerInteract(serverPlayer, REGISTRY_NAME);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    // -----------------------------------------------------------------------
    // Misc overrides
    // -----------------------------------------------------------------------

    /** Prevent the NPC from despawning naturally. */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
}
