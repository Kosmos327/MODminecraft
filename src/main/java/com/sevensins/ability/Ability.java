package com.sevensins.ability;

import com.sevensins.character.PlayerCharacterData;
import com.sevensins.character.PlayerDataRegistry;
import net.minecraft.world.entity.player.Player;

/**
 * Base class for all character abilities.
 *
 * <p>Subclasses implement {@link #execute(Player)} to define ability-specific
 * behaviour. The template method {@link #use(Player)} handles the common
 * pre-flight checks (mana) and mana deduction before delegating to
 * {@code execute}.
 */
public abstract class Ability {

    protected final AbilityType type;
    protected final int manaCost;
    protected final int cooldownTicks;

    protected Ability(AbilityType type, int manaCost, int cooldownTicks) {
        this.type = type;
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownTicks;
    }

    public AbilityType getType() {
        return type;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    /**
     * Returns {@code true} when the player currently has enough mana to
     * activate this ability.
     */
    public boolean canUse(Player player) {
        PlayerCharacterData data = PlayerDataRegistry.getOrCreate(player);
        return data.getMana() >= manaCost;
    }

    /**
     * Template method: validates mana, deducts it, then delegates to
     * {@link #execute(Player)}.  Does nothing when {@link #canUse} returns
     * {@code false}.
     */
    public final void use(Player player) {
        if (!canUse(player)) {
            return;
        }
        PlayerDataRegistry.getOrCreate(player).consumeMana(manaCost);
        execute(player);
    }

    /**
     * Ability-specific logic executed after the mana check and deduction.
     * Implementations should guard server-side-only operations with
     * {@code !player.level().isClientSide}.
     */
    protected abstract void execute(Player player);
}
