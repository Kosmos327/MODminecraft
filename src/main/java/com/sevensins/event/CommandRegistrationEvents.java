package com.sevensins.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.command.DungeonCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers all server-side commands for the mod.
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandRegistrationEvents {

    private CommandRegistrationEvents() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        DungeonCommand.register(event.getDispatcher());
    }
}
