package com.sevensins.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.command.DebugCommand;
import com.sevensins.command.DungeonCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers all server-side commands for the mod under a single
 * {@code /sevensins} root.
 *
 * <h2>Command tree</h2>
 * <pre>
 * /sevensins dungeon spawn demon_cave
 * /sevensins dungeon quest assign
 * /sevensins debug state
 * /sevensins debug story
 * /sevensins debug quest
 * /sevensins debug boss
 * /sevensins debug give_xp &lt;n&gt;
 * </pre>
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandRegistrationEvents {

    private CommandRegistrationEvents() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root =
                Commands.literal("sevensins")
                        .requires(src -> src.hasPermission(2));

        DungeonCommand.registerUnder(root);
        DebugCommand.registerUnder(root);

        event.getDispatcher().register(root);
    }
}
