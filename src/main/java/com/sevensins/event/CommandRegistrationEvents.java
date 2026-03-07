package com.sevensins.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.command.DungeonCommand;
import com.sevensins.command.RaidCommand;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers all server-side commands for the mod under a single
 * {@code /sevensins} root to avoid duplicate root registrations.
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandRegistrationEvents {

    private CommandRegistrationEvents() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var root = Commands.literal("sevensins")
                .requires(src -> src.hasPermission(2));
        DungeonCommand.registerUnder(root);
        RaidCommand.registerUnder(root);
        event.getDispatcher().register(root);
    }
}
