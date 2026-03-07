package com.sevensins.ability;

import net.minecraft.server.level.ServerPlayer;

/**
 * Convenience base class that stores the three common fields
 * so concrete abilities only need to implement {@link #activate}.
 */
public abstract class AbstractAbility implements IAbility {

    private final AbilityType type;
    private final int manaCost;
    private final int cooldownTicks;

    protected AbstractAbility(AbilityType type, int manaCost, int cooldownTicks) {
        this.type = type;
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownTicks;
    }

    @Override
    public AbilityType getType() {
        return type;
    }

    @Override
    public int getManaCost() {
        return manaCost;
    }

    @Override
    public int getCooldownTicks() {
        return cooldownTicks;
    }

    @Override
    public abstract void activate(ServerPlayer player);
}
