package com.sevensins.character;

public enum CharacterType {
    MELIODAS,
    DIANE,
    BAN,
    KING,
    GOWTHER,
    MERLIN,
    ESCANOR,
    NONE;

    public String getSerializedName() {
        return this.name().toLowerCase();
    }

    public static CharacterType fromName(String name) {
        for (CharacterType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return NONE;
    }
}
