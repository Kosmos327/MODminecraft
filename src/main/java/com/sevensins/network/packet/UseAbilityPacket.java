package com.sevensins.network.packet;

import com.sevensins.ability.AbilityManager;
import com.sevensins.ability.AbilityType;
import com.sevensins.ability.CooldownManager;
import com.sevensins.ability.IAbility;
import com.sevensins.ability.UltimateAbilityManager;
import com.sevensins.character.CharacterType;
import com.sevensins.character.capability.ISinData;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.config.BalanceHelper;
import com.sevensins.mana.ManaManager;
import com.sevensins.network.ModNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
 *   <li>Sends a {@link SyncCooldownPacket} back to the client so the
 *       cooldown HUD updates immediately.</li>
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

                // 2b. Verify the ability has been unlocked in the skill tree
                if (!cap.getData().hasUnlockedAbility(packet.abilityType)) return;

                // 3. Check cooldown
                if (CooldownManager.isOnCooldown(player.getUUID(), packet.abilityType)) return;

                // 4 & 5. Check mana, consume it, set cooldown, activate ability
                int effectiveMana = BalanceHelper.getEffectiveManaCost(
                        player, packet.abilityType, ability.getManaCost());
                int effectiveCooldown = BalanceHelper.getEffectiveCooldownTicks(
                        player, packet.abilityType, ability.getCooldownTicks());

                if (!ManaManager.hasEnoughMana(player, effectiveMana)) return;

                ManaManager.consumeMana(player, effectiveMana);
                CooldownManager.setCooldown(player.getUUID(), packet.abilityType, effectiveCooldown);
                ability.activate(player);

                // 6. Sync the new cooldown state back to the client HUD
                long expiryMs = System.currentTimeMillis()
                        + (long) effectiveCooldown * 50L;
                Map<AbilityType, Long> cooldownMap = new EnumMap<>(AbilityType.class);
                cooldownMap.put(packet.abilityType, expiryMs);
                ModNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncCooldownPacket(cooldownMap));
            });
        });
        ctx.setPacketHandled(true);
    }

    // -------------------------------------------------------------------------

    public AbilityType getAbilityType() {
        return abilityType;
    }
}
