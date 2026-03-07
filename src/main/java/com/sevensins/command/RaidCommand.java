package com.sevensins.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.sevensins.world.NightRaidManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Registers the {@code /sevensins raid} sub-command tree.
 *
 * <h2>Available sub-commands</h2>
 * <pre>
 * /sevensins raid start         — starts a Night Demon Raid for the executing player
 * /sevensins raid start night   — starts a Night Demon Raid only if it is currently night
 * /sevensins raid status        — shows whether the player has an active raid
 * </pre>
 *
 * <p>All commands require operator permission level 2.</p>
 *
 * <p>Registered by
 * {@link com.sevensins.event.CommandRegistrationEvents#onRegisterCommands}.</p>
 */
public final class RaidCommand {

    private RaidCommand() {}

    /**
     * Registers the {@code raid} sub-tree under the given root literal builder.
     * The root must already have the required permission check applied.
     */
    public static void registerUnder(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("raid")
                .then(Commands.literal("start")
                        // /sevensins raid start — debug start, no nighttime requirement
                        .executes(ctx -> startRaid(ctx.getSource(), false))
                        // /sevensins raid start night — require nighttime
                        .then(Commands.literal("night")
                                .executes(ctx -> startRaid(ctx.getSource(), true))))
                .then(Commands.literal("status")
                        .executes(ctx -> raidStatus(ctx.getSource()))));
    }

    // -------------------------------------------------------------------------
    // Executors
    // -------------------------------------------------------------------------

    private static int startRaid(CommandSourceStack source, boolean requireNight) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        boolean started = NightRaidManager.getInstance().startRaid(player, requireNight);
        if (!started) {
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("Night Demon Raid started!"), false);
        return 1;
    }

    private static int raidStatus(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        boolean active = NightRaidManager.getInstance()
                .getActiveRaids()
                .containsKey(player.getUUID());

        source.sendSuccess(
                () -> Component.literal(active ? "Raid is active." : "No active raid."),
                false);
        return 1;
    }
}
