package com.sevensins.client.screen;

import net.minecraft.client.gui.GuiGraphics;
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

    public CharacterSelectionScreen() {
        super(Component.translatable("screen.seven_sins.character_selection"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                this.height / 2 - 10,
                0xFFFFFF
        );
    }
}
