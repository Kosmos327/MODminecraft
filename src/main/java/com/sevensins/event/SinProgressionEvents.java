package com.sevensins.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.character.CharacterProgressionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge event subscriber that awards sin XP when players kill mobs.
 *
 * <p>Listens to {@link LivingDeathEvent} on the server side and delegates XP
 * calculation to {@link CharacterProgressionManager}.</p>
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID)
public class SinProgressionEvents {

    /** XP awarded for killing a regular mob. */
    private static final int XP_NORMAL_MOB = 10;

    /** XP awarded for killing a boss-tier mob. */
    private static final int XP_BOSS = 200;

    /**
     * Max-health threshold above which a mob is treated as a boss.
     * Entities with {@code maxHealth > BOSS_HEALTH_THRESHOLD} yield boss XP.
     */
    private static final float BOSS_HEALTH_THRESHOLD = 100f;

    private SinProgressionEvents() {}

    /**
     * Called when any {@link net.minecraft.world.entity.LivingEntity} dies.
     *
     * <ul>
     *   <li>Skips the event when the dying entity is itself a player.</li>
     *   <li>Skips the event when the killer is not a server-side player.</li>
     *   <li>Awards XP via {@link CharacterProgressionManager#addXP} — which
     *       internally checks that the player has a chosen character.</li>
     * </ul>
     *
     * @param event the living death event fired by Forge
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // Mob must not be a player
        if (event.getEntity() instanceof Player) return;

        // Killer must be a server-side player
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;

        float maxHealth = event.getEntity().getMaxHealth();
        int xp = maxHealth > BOSS_HEALTH_THRESHOLD ? XP_BOSS : XP_NORMAL_MOB;

        CharacterProgressionManager.addXP(killer, xp);
    }
}
