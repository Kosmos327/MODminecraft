package com.sevensins.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.sevensins.quest.QuestManager;
import com.sevensins.quest.QuestRegistry;
import com.sevensins.world.DungeonManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Registers the {@code /sevensins dungeon} sub-command tree.
 *
 * <h2>Available sub-commands</h2>
 * <pre>
 * /sevensins dungeon spawn demon_cave   — spawns a Demon Cave near the executing player
 * /sevensins dungeon quest assign       — assigns the clear_demon_cave quest to the player
 * </pre>
 *
 * <p>All commands require operator permission level 2.</p>
 */
public final class DungeonCommand {

    private DungeonCommand() {}

    /**
     * Registers the {@code dungeon} sub-tree under the given root literal builder.
     * The root must already have the required permission check applied.
     */
    public static void registerUnder(LiteralArgumentBuilder<CommandSourceStack> root) {
        // /sevensins dungeon spawn demon_cave
        root.then(Commands.literal("dungeon")
                .then(Commands.literal("spawn")
                        .then(Commands.literal("demon_cave")
                                .executes(ctx -> spawnDemonCave(ctx.getSource()))))
                .then(Commands.literal("quest")
                        .then(Commands.literal("assign")
                                .executes(ctx -> assignDungeonQuest(ctx.getSource())))));
    }

    /**
     * Registers all {@code /sevensins dungeon} sub-commands with the given
     * {@link CommandDispatcher}.
     *
     * <p><em>Note:</em> {@link com.sevensins.event.CommandRegistrationEvents} now builds a
     * single shared root and calls {@link #registerUnder} directly. This method is kept
     * for backward compatibility and standalone testing only.</p>
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root =
                Commands.literal("sevensins")
                        .requires(src -> src.hasPermission(2));
        registerUnder(root);
        dispatcher.register(root);
    }

    // -------------------------------------------------------------------------
    // Executors
    // -------------------------------------------------------------------------

    private static int spawnDemonCave(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        UUID dungeonId = DungeonManager.getInstance().spawnDemonCave(player);
        if (dungeonId == null) {
            source.sendFailure(Component.literal("Failed to spawn Demon Cave."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(
                "Demon Cave spawned (id: " + dungeonId + ")"), false);
        return 1;
    }

    private static int assignDungeonQuest(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        QuestManager.assignQuest(player, QuestRegistry.CLEAR_DEMON_CAVE_ID);
        source.sendSuccess(() -> Component.literal(
                "Assigned quest: " + QuestRegistry.CLEAR_DEMON_CAVE_ID), false);
        return 1;
    }
}
