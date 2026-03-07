package com.sevensins.dialogue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DialogueManager {

    private final Map<String, DialogueNode> nodes = new HashMap<>();

    public DialogueManager() {
        registerDefaultDialogues();
    }

    private void registerDefaultDialogues() {
        // Elizabeth intro
        register(new DialogueNode(
                "elizabeth_intro",
                "Elizabeth",
                "Oh! Are you perhaps a Holy Knight? Please, I need your help to find the Seven Deadly Sins!",
                List.of()
        ));

        // Meliodas intro
        register(new DialogueNode(
                "meliodas_intro",
                "Meliodas",
                "Welcome to the Boar Hat! Want something to drink? Or maybe you're looking for the Seven Deadly Sins?",
                List.of()
        ));

        // Diane intro
        register(new DialogueNode(
                "diane_intro",
                "Diane",
                "Hm? A tiny human? Don't get in my way unless you want to get stepped on!",
                List.of()
        ));
    }

    private void register(DialogueNode node) {
        nodes.put(node.getId(), node);
    }

    public Optional<DialogueNode> getNode(String id) {
        return Optional.ofNullable(nodes.get(id));
    }
}
