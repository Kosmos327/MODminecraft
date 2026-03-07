package com.sevensins.network.packet;

import com.sevensins.ability.AbilityManager;
import com.sevensins.ability.AbilityType;
import com.sevensins.ability.CooldownManager;
import com.sevensins.ability.IAbility;
import com.sevensins.character.CharacterType;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.mana.ManaManager;
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
 *       {@code IPlayerCharacterData} capability.</li>
 *   <li>Retrieves the character's ability list and locates the requested
 *       {@link AbilityType}.</li>
 *   <li>Checks the {@link CooldownManager} – aborts if still on cooldown.</li>
 *   <li>Checks mana via {@link ManaManager} – aborts if mana is insufficient.</li>
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

    public static void encode(UseAbilityPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.abilityType);
    }

    // -------------------------------------------------------------------------
    // Server-side handler

    public static void handle(UseAbilityPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            // 1. Determine the player's character via IPlayerCharacterData capability
            ModCapabilities.get(player).ifPresent(cap -> {
                CharacterType character = cap.getData().getSelectedCharacter();
                if (character == CharacterType.NONE) return;

                // 2. Retrieve the character's ability list and find the requested one
                List<? extends IAbility> abilities = AbilityManager.getAbilitiesFor(character);
                Optional<? extends IAbility> abilityOpt = abilities.stream()
                        .filter(a -> a.getType() == packet.abilityType)
                        .findFirst();

                if (abilityOpt.isEmpty()) return;
                IAbility ability = abilityOpt.get();

                // 3. Check cooldown
                if (CooldownManager.isOnCooldown(player.getUUID(), packet.abilityType)) return;

                // 4 & 5. Check mana, consume it, set cooldown, activate ability
                if (!ManaManager.hasEnoughMana(player, ability.getManaCost())) return;

                ManaManager.consumeMana(player, ability.getManaCost());
                CooldownManager.setCooldown(
                        player.getUUID(), packet.abilityType, ability.getCooldownTicks());
                ability.activate(player);
            });
        });
        ctx.setPacketHandled(true);
    }

    // -------------------------------------------------------------------------

    public AbilityType getAbilityType() {
        return abilityType;
    }
}
