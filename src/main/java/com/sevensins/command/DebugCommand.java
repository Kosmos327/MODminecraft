package com.sevensins.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.sevensins.character.CharacterType;
import com.sevensins.character.CharacterProgressionManager;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.debug.ProgressionDebugHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Developer / QA debug sub-commands grouped under {@code /sevensins debug}.
 *
 * <h2>Available sub-commands</h2>
 * <pre>
 * /sevensins debug state           — prints full progression snapshot
 * /sevensins debug story           — prints story chapter and active flags
 * /sevensins debug quest           — prints active quest and progress
 * /sevensins debug boss            — prints active boss state
 * /sevensins debug give_xp &lt;n&gt;    — awards n sin XP to the executing player
 * </pre>
 *
 * <p>All commands require operator permission level 2.</p>
 *
 * <p>Register by calling {@link #registerUnder(LiteralArgumentBuilder)} from
 * {@link com.sevensins.event.CommandRegistrationEvents}.</p>
 */
public final class DebugCommand {

    private DebugCommand() {}

    /**
     * Attaches all {@code debug} sub-commands to the given {@code /sevensins} root node.
     *
     * @param root the root {@code /sevensins} literal builder
     */
    public static void registerUnder(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("debug")
                .then(Commands.literal("state")
                        .executes(ctx -> execState(ctx.getSource())))
                .then(Commands.literal("story")
                        .executes(ctx -> execStory(ctx.getSource())))
                .then(Commands.literal("quest")
                        .executes(ctx -> execQuest(ctx.getSource())))
                .then(Commands.literal("boss")
                        .executes(ctx -> execBoss(ctx.getSource())))
                .then(Commands.literal("give_xp")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> execGiveXp(
                                        ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "amount"))))));
    }

    // -------------------------------------------------------------------------
    // Executors
    // -------------------------------------------------------------------------

    private static int execState(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }
        ProgressionDebugHelper.buildFullState(player)
                .forEach(line -> source.sendSuccess(() -> line, false));
        return 1;
    }

    private static int execStory(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }
        ProgressionDebugHelper.buildCharacterSection(player)
                .forEach(line -> source.sendSuccess(() -> line, false));
        ProgressionDebugHelper.buildStorySection(player)
                .forEach(line -> source.sendSuccess(() -> line, false));
        return 1;
    }

    private static int execQuest(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }
        ProgressionDebugHelper.buildQuestSection(player)
                .forEach(line -> source.sendSuccess(() -> line, false));
        return 1;
    }

    private static int execBoss(CommandSourceStack source) {
        ProgressionDebugHelper.buildBossSection()
                .forEach(line -> source.sendSuccess(() -> line, false));
        return 1;
    }

    private static int execGiveXp(CommandSourceStack source, int amount) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }
        boolean hasCharacter = ModCapabilities.get(player)
                .map(cap -> cap.getData().getSelectedCharacter() != CharacterType.NONE)
                .orElse(false);
        if (!hasCharacter) {
            source.sendFailure(Component.literal("Player has not chosen a character yet."));
            return 0;
        }
        CharacterProgressionManager.addXP(player, amount);
        source.sendSuccess(() -> Component.literal("Awarded " + amount + " sin XP to " + player.getName().getString()), false);
        return 1;
    }
}
