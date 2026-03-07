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

    public static CharacterType fromName(String value) {
        if (value == null) {
            return NONE;
        }
        for (CharacterType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return NONE;
    }
}
