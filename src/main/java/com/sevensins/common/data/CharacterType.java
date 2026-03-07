package com.sevensins.common.data;

/**
 * Represents each of the Seven Deadly Sins characters a player can choose.
 */
public enum CharacterType {

    MELIODAS("Meliodas"),
    DIANE("Diane"),
    BAN("Ban"),
    KING("King"),
    GOWTHER("Gowther"),
    MERLIN("Merlin"),
    ESCANOR("Escanor");

    private final String displayName;

    CharacterType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
