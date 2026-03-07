package com.sevensins.capability;

/**
 * Interface for reading and modifying a player's mana pool.
 */
public interface IMana {

    /** Returns the current mana amount. */
    int getMana();

    /** Sets the mana to an absolute value. */
    void setMana(int mana);

    /** Returns the maximum mana this player can hold. */
    int getMaxMana();

    /**
     * Reduces mana by {@code amount}.
     *
     * @return {@code true} if there was enough mana and it was consumed,
     *         {@code false} if the player had insufficient mana.
     */
    default boolean consumeMana(int amount) {
        if (getMana() < amount) return false;
        setMana(getMana() - amount);
        return true;
    }
}
