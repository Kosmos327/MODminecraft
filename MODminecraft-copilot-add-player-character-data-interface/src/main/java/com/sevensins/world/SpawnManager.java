package com.sevensins.world;

import com.sevensins.character.CharacterType;
import com.sevensins.character.capability.ModCapabilities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public class SpawnManager {

    private static final double[] MELIODAS_SPAWN = {0, 100, 0};
    private static final double[] DIANE_SPAWN    = {300, 110, 300};
    private static final double[] BAN_SPAWN      = {600, 90, 0};
    private static final double[] KING_SPAWN     = {-300, 110, 300};
    private static final double[] GOWTHER_SPAWN  = {-600, 90, 0};
    private static final double[] MERLIN_SPAWN   = {0, 120, 600};
    private static final double[] ESCANOR_SPAWN  = {0, 100, -600};

    public static void teleportToStartingArea(ServerPlayer player, CharacterType type) {
        if (player.level().isClientSide()) {
            return;
        }

        ModCapabilities.get(player).ifPresent(data -> {
            String selected = data.getData().getSelectedCharacter();
            if (!selected.isEmpty()) {
                return;
            }

            double[] coords = getSpawnCoords(type);
            if (coords == null) {
                return;
            }

            ServerLevel level = player.serverLevel();
            player.teleportTo(level, coords[0], coords[1], coords[2], Set.of(), 0.0f, 0.0f);
        });
    }

    private static double[] getSpawnCoords(CharacterType type) {
        return switch (type) {
            case MELIODAS -> MELIODAS_SPAWN;
            case DIANE    -> DIANE_SPAWN;
            case BAN      -> BAN_SPAWN;
            case KING     -> KING_SPAWN;
            case GOWTHER  -> GOWTHER_SPAWN;
            case MERLIN   -> MERLIN_SPAWN;
            case ESCANOR  -> ESCANOR_SPAWN;
            case NONE     -> null;
        };
    }
}
