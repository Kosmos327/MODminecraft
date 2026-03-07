package com.sevensins.network.packet;

import com.sevensins.ability.AbilityType;
import com.sevensins.character.CharacterType;
import com.sevensins.character.PlayerCharacterData;
import com.sevensins.character.capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Server → Client packet that synchronises the full {@link PlayerCharacterData}
 * capability to the client so the HUD, cooldown display, and skill-tree screen
 * always reflect authoritative server state.
 *
 * <p>Sent after:
 * <ul>
 *   <li>Player login</li>
 *   <li>Player respawn</li>
 *   <li>Dimension change</li>
 *   <li>Character selection</li>
 *   <li>Skill unlock</li>
 * </ul>
 * </p>
 */
public class SyncCharacterDataPacket {

    private final boolean isFullSync;
    private final CharacterType selectedCharacter;
    private final int level;
    private final int experience;
    private final int mana;
    private final int maxMana;
    private final int skillPoints;
    private final Set<AbilityType> unlockedAbilities;

    /** Full-data constructor used for login/respawn/dimension-change syncs. */
    public SyncCharacterDataPacket(PlayerCharacterData data) {
        this.isFullSync        = true;
        this.selectedCharacter = data.getSelectedCharacter();
        this.level             = data.getLevel();
        this.experience        = data.getExperience();
        this.mana              = data.getMana();
        this.maxMana           = data.getMaxMana();
        this.skillPoints       = data.getSkillPoints();
        Set<AbilityType> src   = data.getUnlockedAbilities();
        this.unlockedAbilities = src.isEmpty()
                ? EnumSet.noneOf(AbilityType.class)
                : EnumSet.copyOf(src);
    }

    /**
     * Partial constructor kept for compatibility with {@link UnlockSkillPacket}.
     * Only {@code skillPoints} and {@code unlockedAbilities} are applied on the client.
     */
    public SyncCharacterDataPacket(int skillPoints, Set<AbilityType> unlockedAbilities) {
        this.isFullSync        = false;
        this.selectedCharacter = CharacterType.NONE;
        this.level             = 0;
        this.experience        = 0;
        this.mana              = 0;
        this.maxMana           = 0;
        this.skillPoints       = skillPoints;
        this.unlockedAbilities = unlockedAbilities.isEmpty()
                ? EnumSet.noneOf(AbilityType.class)
                : EnumSet.copyOf(unlockedAbilities);
    }

    // -------------------------------------------------------------------------
    // Codec
    // -------------------------------------------------------------------------

    public static void encode(SyncCharacterDataPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.isFullSync);
        if (packet.isFullSync) {
            buf.writeEnum(packet.selectedCharacter);
            buf.writeInt(packet.level);
            buf.writeInt(packet.experience);
            buf.writeInt(packet.mana);
            buf.writeInt(packet.maxMana);
        }
        buf.writeInt(packet.skillPoints);
        buf.writeInt(packet.unlockedAbilities.size());
        for (AbilityType ability : packet.unlockedAbilities) {
            buf.writeEnum(ability);
        }
    }

    public static SyncCharacterDataPacket decode(FriendlyByteBuf buf) {
        boolean isFullSync = buf.readBoolean();
        if (isFullSync) {
            CharacterType character = buf.readEnum(CharacterType.class);
            int level      = buf.readInt();
            int experience = buf.readInt();
            int mana       = buf.readInt();
            int maxMana    = buf.readInt();
            int skillPoints = buf.readInt();
            int size = buf.readInt();
            Set<AbilityType> abilities = EnumSet.noneOf(AbilityType.class);
            for (int i = 0; i < size; i++) {
                abilities.add(buf.readEnum(AbilityType.class));
            }
            return new SyncCharacterDataPacket(character, level, experience, mana, maxMana,
                    skillPoints, abilities);
        } else {
            int skillPoints = buf.readInt();
            int size = buf.readInt();
            Set<AbilityType> abilities = EnumSet.noneOf(AbilityType.class);
            for (int i = 0; i < size; i++) {
                abilities.add(buf.readEnum(AbilityType.class));
            }
            return new SyncCharacterDataPacket(skillPoints, abilities);
        }
    }

    /** Internal full-parameter constructor used by {@link #decode}. */
    private SyncCharacterDataPacket(CharacterType selectedCharacter, int level, int experience,
                                    int mana, int maxMana, int skillPoints,
                                    Set<AbilityType> unlockedAbilities) {
        this.isFullSync        = true;
        this.selectedCharacter = selectedCharacter;
        this.level             = level;
        this.experience        = experience;
        this.mana              = mana;
        this.maxMana           = maxMana;
        this.skillPoints       = skillPoints;
        this.unlockedAbilities = unlockedAbilities.isEmpty()
                ? EnumSet.noneOf(AbilityType.class)
                : EnumSet.copyOf(unlockedAbilities);
    }

    // -------------------------------------------------------------------------
    // Handler (runs on the client)
    // -------------------------------------------------------------------------

    public static void handle(SyncCharacterDataPacket packet,
                               Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> handleOnClient(packet)));
        ctx.setPacketHandled(true);
    }

    private static void handleOnClient(SyncCharacterDataPacket packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ModCapabilities.get(player).ifPresent(cap -> {
            PlayerCharacterData data = cap.getData();
            if (packet.isFullSync) {
                if (packet.selectedCharacter != CharacterType.NONE) {
                    data.setSelectedCharacter(packet.selectedCharacter);
                }
                data.setLevel(packet.level);
                data.setExperience(packet.experience);
                data.setMaxMana(packet.maxMana);
                data.setMana(packet.mana);
            }
            data.setSkillPoints(packet.skillPoints);
            data.setUnlockedAbilities(packet.unlockedAbilities);
        });
    }
}
