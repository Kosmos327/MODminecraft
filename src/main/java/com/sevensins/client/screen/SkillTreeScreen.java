package com.sevensins.client.screen;

import com.sevensins.ability.AbilityType;
import com.sevensins.character.CharacterType;
import com.sevensins.character.PlayerCharacterData;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.character.skilltree.SkillTreeDefinition;
import com.sevensins.character.skilltree.SkillTreeNode;
import com.sevensins.character.skilltree.SkillTreeRegistry;
import com.sevensins.network.ModNetwork;
import com.sevensins.network.packet.UnlockSkillPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Set;

/**
 * Client-side skill tree screen.
 *
 * <p>Displays the current character's skill tree nodes in a simple vertical list.
 * Each node shows a button that is:
 * <ul>
 *   <li><b>disabled</b> – if the node is locked (prerequisite not met or no points)</li>
 *   <li><b>clickable</b> – if the node is unlockable</li>
 *   <li>shown with a suffix "[UNLOCKED]</li> – if already unlocked</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class SkillTreeScreen extends Screen {

    private static final int BUTTON_WIDTH  = 220;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 4;

    // Colours for the node label text drawn on top of the button strip
    private static final int COLOR_UNLOCKED  = 0xFF55FF55; // green
    private static final int COLOR_AVAILABLE = 0xFFFFFF55; // yellow
    private static final int COLOR_LOCKED    = 0xFFAAAAAA; // grey

    public SkillTreeScreen() {
        super(Component.translatable("screen.seven_sins.skill_tree"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ModCapabilities.get(mc.player).ifPresent(cap -> {
            PlayerCharacterData data = cap.getData();
            CharacterType character = data.getSelectedCharacter();

            // Guard: if no character is selected, skip adding interactive widgets.
            // The render() method displays a user-facing fallback message in this case.
            if (character == CharacterType.NONE) return;

            SkillTreeDefinition tree = SkillTreeRegistry.getTree(character);
            if (tree == null) return;

            Set<AbilityType> unlocked   = data.getUnlockedAbilities();
            int              skillPoints = data.getSkillPoints();

            List<SkillTreeNode> nodes = tree.getNodes();
            int totalHeight = nodes.size() * (BUTTON_HEIGHT + BUTTON_SPACING) - BUTTON_SPACING;
            int startY = (this.height - totalHeight) / 2;
            int x = this.width / 2 - BUTTON_WIDTH / 2;

            for (int i = 0; i < nodes.size(); i++) {
                SkillTreeNode node = nodes.get(i);
                int y = startY + i * (BUTTON_HEIGHT + BUTTON_SPACING);

                boolean alreadyUnlocked = unlocked.contains(node.getAbility());
                boolean prereqMet = node.getPrerequisite() == null
                        || unlocked.contains(node.getPrerequisite());
                boolean canAfford   = skillPoints >= node.getCost();
                boolean canUnlock   = !alreadyUnlocked && prereqMet && canAfford;

                String label = buildLabel(node.getAbility(), alreadyUnlocked, canUnlock);

                final AbilityType abilityToSend = node.getAbility();
                Button btn = Button.builder(
                                Component.literal(label),
                                b -> {
                                    ModNetwork.CHANNEL.send(
                                            PacketDistributor.SERVER.noArg(),
                                            new UnlockSkillPacket(abilityToSend)
                                    );
                                    // Refresh the screen after a short delay to reflect server response
                                    Minecraft.getInstance().setScreen(new SkillTreeScreen());
                                })
                        .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                        .build();

                btn.active = canUnlock;
                this.addRenderableWidget(btn);
            }
        });
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ModCapabilities.get(mc.player).ifPresent(cap -> {
            PlayerCharacterData data = cap.getData();
            CharacterType character = data.getSelectedCharacter();

            if (character == CharacterType.NONE) {
                // Player has not chosen a character yet — show a friendly hint
                String noCharMsg = Component.translatable("screen.seven_sins.no_character").getString();
                guiGraphics.drawCenteredString(this.font, noCharMsg, this.width / 2, this.height / 2, 0xFF5555);
                return;
            }

            String sinLabel = "Character: " + capitalize(character.name());
            String pointsLabel = "Skill Points: " + data.getSkillPoints();

            guiGraphics.drawCenteredString(this.font, sinLabel,   this.width / 2, 24, 0xAAFFFF);
            guiGraphics.drawCenteredString(this.font, pointsLabel, this.width / 2, 34, 0xFFFFAA);
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String buildLabel(AbilityType ability, boolean unlocked, boolean canUnlock) {
        String name = capitalize(ability.name());
        if (unlocked) return name + "  [UNLOCKED]";
        if (canUnlock) return name + "  [Unlock]";
        return name + "  [Locked]";
    }

    private static String capitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        return name.substring(0, 1).toUpperCase() +
               name.substring(1).toLowerCase().replace('_', ' ');
    }
}
