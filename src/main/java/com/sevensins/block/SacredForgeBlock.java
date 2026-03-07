package com.sevensins.block;

import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.item.SacredTreasureData;
import com.sevensins.item.SacredTreasureUpgradeHelper;
import com.sevensins.item.SacredTreasureUpgradeHelper.UpgradeResult;
import com.sevensins.story.StoryFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Sacred Forge — the upgrade station for Sacred Treasures.
 *
 * <p>Right-clicking the forge while holding a Sacred Treasure in the main hand
 * attempts to upgrade it using materials from the player's inventory
 * (Sin Fragments + Magic Scrolls).  Feedback is sent via the chat/action-bar
 * message system; a sound effect confirms success.</p>
 *
 * <p>Registered in {@link com.sevensins.registry.ModBlocks#SACRED_FORGE}.</p>
 */
public class SacredForgeBlock extends Block {

    public SacredForgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        // Return early on the client side – the server handles all logic.
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // Only react to main-hand interactions to avoid double-firing.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        UpgradeResult result = SacredTreasureUpgradeHelper.tryUpgrade(player);

        switch (result) {
            case SUCCESS -> {
                int newLevel = SacredTreasureData.getUpgradeLevel(player.getMainHandItem());
                player.displayClientMessage(
                        Component.translatable(
                                "block.seven_sins.sacred_forge.upgraded", newLevel),
                        false);
                level.playSound(null, pos,
                        SoundEvents.ANVIL_USE,
                        SoundSource.BLOCKS,
                        1.0f, 1.0f + (newLevel * 0.05f));
                // Set the story flag for the first upgrade (once only)
                ModCapabilities.get(player).ifPresent(cap -> {
                    var questData = cap.getData().getQuestData();
                    if (!questData.hasStoryFlag(StoryFlag.SACRED_TREASURE_UPGRADED.getId())) {
                        questData.addStoryFlag(StoryFlag.SACRED_TREASURE_UPGRADED.getId());
                    }
                });
            }
            case INVALID_ITEM -> player.displayClientMessage(
                    Component.translatable("block.seven_sins.sacred_forge.invalid_item"),
                    true);
            case ALREADY_MAX_LEVEL -> player.displayClientMessage(
                    Component.translatable("block.seven_sins.sacred_forge.max_level"),
                    true);
            case NOT_ENOUGH_FRAGMENTS -> player.displayClientMessage(
                    Component.translatable("block.seven_sins.sacred_forge.need_fragments"),
                    true);
            case NOT_ENOUGH_SCROLLS -> player.displayClientMessage(
                    Component.translatable("block.seven_sins.sacred_forge.need_scrolls"),
                    true);
        }

        return InteractionResult.CONSUME;
    }
}
