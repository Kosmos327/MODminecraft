package com.sevensins.client.screen;

import com.sevensins.character.CharacterType;
import com.sevensins.network.ModNetwork;
import com.sevensins.network.packet.SelectCharacterPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Character selection screen shown to a player on first login when no
 * character has been selected yet ({@code selectedCharacter == NONE}).
 * <p>
 * The server never opens this screen directly. Instead it sends an
 * {@link com.sevensins.network.OpenCharacterSelectionPacket} which is
 * handled on the client thread and calls
 * {@code Minecraft.getInstance().setScreen(new CharacterSelectionScreen())}.
 */
@OnlyIn(Dist.CLIENT)
public class CharacterSelectionScreen extends Screen {

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 4;

    private static final CharacterType[] SELECTABLE = {
            CharacterType.MELIODAS,
            CharacterType.DIANE,
            CharacterType.BAN,
            CharacterType.KING,
            CharacterType.GOWTHER,
            CharacterType.MERLIN,
            CharacterType.ESCANOR
    };

    public CharacterSelectionScreen() {
        super(Component.translatable("screen.seven_sins.character_selection"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        int totalHeight = SELECTABLE.length * (BUTTON_HEIGHT + BUTTON_SPACING) - BUTTON_SPACING;
        int startY = (this.height - totalHeight) / 2;
        int x = this.width / 2 - BUTTON_WIDTH / 2;

        for (int i = 0; i < SELECTABLE.length; i++) {
            final CharacterType type = SELECTABLE[i];
            int y = startY + i * (BUTTON_HEIGHT + BUTTON_SPACING);
            this.addRenderableWidget(Button.builder(
                            Component.translatable("character.seven_sins." + type.name().toLowerCase()),
                            btn -> selectCharacter(type))
                    .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
        }
    }

    private void selectCharacter(CharacterType type) {
        ModNetwork.CHANNEL.sendToServer(new SelectCharacterPacket(type));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int totalHeight = SELECTABLE.length * (BUTTON_HEIGHT + BUTTON_SPACING) - BUTTON_SPACING;
        int titleY = (this.height - totalHeight) / 2 - 20;
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, titleY, 0xFFFFFF);
    }
}
