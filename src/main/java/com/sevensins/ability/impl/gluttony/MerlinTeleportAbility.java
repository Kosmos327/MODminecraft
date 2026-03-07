package com.sevensins.ability.impl.gluttony;

import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Merlin (Gluttony) ability — Teleport.
 *
 * <p>Merlin instantly repositions herself a short distance in her look
 * direction, representing her mastery of space and instant-movement magic.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#TELEPORT}</li>
 *   <li>Mana cost: 20</li>
 *   <li>Cooldown: 60 ticks (3 s)</li>
 * </ul>
 */
public class MerlinTeleportAbility extends Ability {

    /** Distance of the teleport in blocks. */
    private static final double TELEPORT_DISTANCE = 6.0;

    public MerlinTeleportAbility() {
        super(AbilityType.TELEPORT, 20, 60);
    }

    @Override
    protected void execute(Player player) {
        if (player.level().isClientSide) {
            return;
        }

        Vec3 look = player.getLookAngle();
        Vec3 origin = player.position();
        // Horizontal teleport only — Y is preserved so the player stays at ground level
        Vec3 destination = origin.add(look.multiply(TELEPORT_DISTANCE, 0.0, TELEPORT_DISTANCE));

        player.teleportTo(destination.x, origin.y, destination.z);

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                0.7f,
                1.6f
        );
    }
}
