package com.sevensins.character;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple static registry mapping player UUIDs to their {@link PlayerCharacterData}.
 * Serves as a runtime cache until a full capability/attachment system is implemented.
 */
public class PlayerDataRegistry {

    private static final Map<UUID, PlayerCharacterData> DATA = new ConcurrentHashMap<>();

    private PlayerDataRegistry() {}

    public static PlayerCharacterData getOrCreate(Player player) {
        return DATA.computeIfAbsent(player.getUUID(), uuid -> new PlayerCharacterData());
    }

    public static void set(Player player, PlayerCharacterData data) {
        DATA.put(player.getUUID(), data);
    }

    public static void remove(Player player) {
        DATA.remove(player.getUUID());
    }
}
