package com.sevensins.ability;

import net.minecraft.server.level.ServerPlayer;

/**
 * Contract for every activatable ability in the mod.
 */
public interface IAbility {

    /** The enum constant that identifies this ability. */
    AbilityType getType();

    /** Mana cost consumed when the ability is activated. */
    int getManaCost();

    /** Duration in ticks before the ability can be used again. */
    int getCooldownTicks();

    /** Called on the server when the ability is activated by a player. */
    void activate(ServerPlayer player);
}
