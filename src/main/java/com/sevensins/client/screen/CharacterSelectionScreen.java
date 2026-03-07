package com.sevensins.client.screen;

import com.sevensins.common.data.CharacterType;
import com.sevensins.network.ModNetwork;
import com.sevensins.network.packet.SelectCharacterPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Client-side GUI screen that lets a player choose one of the Seven Deadly Sins characters.
 *
 * TODO: Replace plain buttons with character portraits / illustrated cards and
 *       add a decorative background once artwork assets are available.
 */
public class CharacterSelectionScreen extends Screen {

    /** Horizontal spacing between character buttons. */
    private static final int BUTTON_WIDTH  = 120;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP    = 4;

    private static final CharacterType[] CHARACTERS = CharacterType.values();

    public CharacterSelectionScreen() {
        super(Component.translatable("screen.seven_sins.choose_your_sin"));
    }

    @Override
    protected void init() {
        super.init();

        int totalHeight = CHARACTERS.length * (BUTTON_HEIGHT + BUTTON_GAP) - BUTTON_GAP;
        int startX = (this.width  - BUTTON_WIDTH)  / 2;
        int startY = (this.height - totalHeight)    / 2;

        for (int i = 0; i < CHARACTERS.length; i++) {
            final CharacterType character = CHARACTERS[i];
            int y = startY + i * (BUTTON_HEIGHT + BUTTON_GAP);

            this.addRenderableWidget(
                Button.builder(
                    Component.literal(character.getDisplayName()),
                    btn -> onCharacterSelected(character)
                )
                .bounds(startX, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build()
            );
        }
    }

    /**
     * Called when the player clicks a character button.
     * Sends a {@link SelectCharacterPacket} to the server instead of modifying
     * client-side data directly.
     */
    private void onCharacterSelected(CharacterType character) {
        ModNetwork.CHANNEL.sendToServer(new SelectCharacterPacket(character));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(
            this.font,
            this.title,
            this.width / 2,
            20,
            0xFFFFFF
        );
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /** The screen should not pause the game while open. */
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
