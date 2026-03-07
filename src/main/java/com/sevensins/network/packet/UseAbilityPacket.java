package com.sevensins.network.packet;

import com.sevensins.ability.AbilityType;
import com.sevensins.ability.CooldownManager;
import com.sevensins.ability.IAbility;
import com.sevensins.capability.ManaCapability;
import com.sevensins.character.CharacterType;
import com.sevensins.common.capability.ModCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Client → Server packet that requests activation of a specific ability.
 *
 * <p>On the server the handler:
 * <ol>
 *   <li>Resolves the player's assigned {@link CharacterType} via the
 *       {@code ISinData} capability.</li>
 *   <li>Retrieves the character's ability list and locates the requested
 *       {@link AbilityType}.</li>
 *   <li>Checks the {@link CooldownManager} – aborts if still on cooldown.</li>
 *   <li>Checks the {@link ManaCapability} – aborts if mana is insufficient.</li>
 *   <li>Consumes mana, records a new cooldown, then calls
 *       {@link IAbility#activate(ServerPlayer)}.</li>
 * </ol>
 * </p>
 */
public class UseAbilityPacket {

    private final AbilityType abilityType;

    public UseAbilityPacket(AbilityType abilityType) {
        this.abilityType = abilityType;
    }

    // -------------------------------------------------------------------------
    // Codec

    public static UseAbilityPacket decode(FriendlyByteBuf buf) {
        return new UseAbilityPacket(buf.readEnum(AbilityType.class));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(abilityType);
    }

    // -------------------------------------------------------------------------
    // Server-side handler

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            // 1. Determine the player's character via ISinData capability
            player.getCapability(ModCapabilities.SIN_DATA).ifPresent(sinData -> {
                CharacterType character = sinData.getCharacter();
                if (character == null) return;

                // 2. Retrieve the character's ability list and find the requested one
                List<IAbility> abilities = character.getAbilities();
                Optional<IAbility> abilityOpt = abilities.stream()
                        .filter(a -> a.getType() == abilityType)
                        .findFirst();

                if (abilityOpt.isEmpty()) return;
                IAbility ability = abilityOpt.get();

                // 3. Check cooldown
                if (CooldownManager.isOnCooldown(player.getUUID(), abilityType)) return;

                // 4 & 5. Check mana, consume it, set cooldown, activate ability
                player.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(mana -> {
                    if (!mana.consumeMana(ability.getManaCost())) return;

                    CooldownManager.setCooldown(
                            player.getUUID(), abilityType, ability.getCooldownTicks());
                    ability.activate(player);
                });
            });
        });
        ctx.setPacketHandled(true);
    }

    // -------------------------------------------------------------------------

    public AbilityType getAbilityType() {
        return abilityType;
    }
}
