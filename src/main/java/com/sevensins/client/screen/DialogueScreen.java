package com.sevensins.client.screen;

import com.sevensins.network.ModNetwork;
import com.sevensins.network.packet.AdvanceDialoguePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Simple client-side screen that displays one NPC dialogue line at a time.
 *
 * <p>The player pages through lines using the "Next" button.  When the last
 * line has been shown the button changes to "Finish" and clicking it:
 * <ol>
 *   <li>Sends {@link AdvanceDialoguePacket} to the server so the server can
 *       execute any pending dialogue action (quest assignment, story flags).</li>
 *   <li>Closes the screen.</li>
 * </ol>
 */
@OnlyIn(Dist.CLIENT)
public class DialogueScreen extends Screen {

    private static final int BOX_PADDING   = 12;
    private static final int BOX_HEIGHT    = 80;
    private static final int BUTTON_WIDTH  = 80;
    private static final int BUTTON_HEIGHT = 20;

    /** Ordered list of {@code [speakerName, text]} pairs. */
    private final List<String[]> lines;
    private int currentIndex = 0;

    private Button actionButton;

    public DialogueScreen(List<String[]> lines) {
        super(Component.literal("Dialogue"));
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("lines must not be empty");
        }
        this.lines = lines;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        int boxY = this.height - BOX_HEIGHT - 10;
        int btnX = this.width / 2 - BUTTON_WIDTH / 2;
        int btnY = boxY + BOX_HEIGHT - BUTTON_HEIGHT - BOX_PADDING;

        actionButton = Button.builder(
                        getButtonLabel(),
                        btn -> advance())
                .bounds(btnX, btnY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(actionButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int boxX = BOX_PADDING;
        int boxY = this.height - BOX_HEIGHT - 10;
        int boxW = this.width - BOX_PADDING * 2;

        // Translucent background box
        guiGraphics.fill(boxX, boxY, boxX + boxW, boxY + BOX_HEIGHT, 0xCC000000);

        String[] line = lines.get(currentIndex);
        String speaker = line[0];
        String text = line[1];

        // Speaker name
        guiGraphics.drawString(this.font, speaker + ":", boxX + BOX_PADDING, boxY + BOX_PADDING, 0xFFD700, true);

        // Dialogue text (word-wrapped)
        int textX = boxX + BOX_PADDING;
        int textY = boxY + BOX_PADDING + 14;
        int maxWidth = boxW - BOX_PADDING * 2;
        for (net.minecraft.util.FormattedCharSequence seq :
                this.font.split(Component.literal(text), maxWidth)) {
            guiGraphics.drawString(this.font, seq, textX, textY, 0xFFFFFF, false);
            textY += this.font.lineHeight + 2;
        }

        // Page indicator
        String pageInfo = (currentIndex + 1) + "/" + lines.size();
        guiGraphics.drawString(this.font, pageInfo,
                boxX + boxW - this.font.width(pageInfo) - BOX_PADDING,
                boxY + BOX_PADDING, 0xAAAAAA, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // -----------------------------------------------------------------------
    // Navigation
    // -----------------------------------------------------------------------

    private void advance() {
        if (currentIndex < lines.size() - 1) {
            currentIndex++;
            actionButton.setMessage(getButtonLabel());
        } else {
            // Last line acknowledged — notify server and close
            ModNetwork.CHANNEL.sendToServer(new AdvanceDialoguePacket());
            this.onClose();
        }
    }

    private Component getButtonLabel() {
        return currentIndex < lines.size() - 1
                ? Component.literal("Next")
                : Component.literal("Finish");
    }
}
