package com.sevensins.network.packet;

import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityManager;
import com.sevensins.ability.AbilityType;
import com.sevensins.ability.CooldownManager;
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
 */
public class UseAbilityPacket {

    private final AbilityType abilityType;

    public UseAbilityPacket(AbilityType abilityType) {
        this.abilityType = abilityType;
    }

    public static UseAbilityPacket decode(FriendlyByteBuf buf) {
        return new UseAbilityPacket(buf.readEnum(AbilityType.class));
    }

    public static void encode(UseAbilityPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.abilityType);
    }

    public static void handle(UseAbilityPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            ModCapabilities.get(player).ifPresent(cap -> {
                CharacterType character = cap.getData().getSelectedCharacter();
                if (character == CharacterType.NONE) return;

                List<Ability> abilities = AbilityManager.getAbilitiesFor(character);
                Optional<Ability> abilityOpt = abilities.stream()
                        .filter(a -> a.getType() == packet.abilityType)
                        .findFirst();

                if (abilityOpt.isEmpty()) return;

                Ability ability = abilityOpt.get();

                if (CooldownManager.isOnCooldown(player.getUUID(), packet.abilityType)) return;
                if (!ManaManager.hasEnoughMana(player, ability.getManaCost())) return;

                ManaManager.consumeMana(player, ability.getManaCost());
                CooldownManager.setCooldown(
                        player.getUUID(),
                        packet.abilityType,
                        ability.getCooldownTicks()
                );

                ability.activate(player);
            });
        });
        ctx.setPacketHandled(true);
    }

    public AbilityType getAbilityType() {
        return abilityType;
    }
}