package com.sevensins.quest;

import java.util.Objects;

public class Quest {

    private final String id;
    private final String title;
    private final String description;
    private final QuestType type;
    private final int targetValue;

    public Quest(String id, String title, String description, QuestType type, int targetValue) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.targetValue = targetValue;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public QuestType getType() {
        return type;
    }

    public int getTargetValue() {
        return targetValue;
    }
}
