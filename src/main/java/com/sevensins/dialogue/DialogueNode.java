package com.sevensins.dialogue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DialogueNode {

    private final String id;
    private String speakerName;
    private String text;
    private List<String> nextNodeIds;

    public DialogueNode(String id, String speakerName, String text, List<String> nextNodeIds) {
        this.id = id;
        this.speakerName = speakerName;
        this.text = text;
        this.nextNodeIds = new ArrayList<>(nextNodeIds);
    }

    public String getId() {
        return id;
    }

    public String getSpeakerName() {
        return speakerName;
    }

    public void setSpeakerName(String speakerName) {
        this.speakerName = speakerName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getNextNodeIds() {
        return Collections.unmodifiableList(nextNodeIds);
    }

    public void setNextNodeIds(List<String> nextNodeIds) {
        this.nextNodeIds = new ArrayList<>(nextNodeIds);
    }
}
