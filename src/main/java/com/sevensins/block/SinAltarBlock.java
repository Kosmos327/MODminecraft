package com.sevensins.block;

import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Sin Altar block – the focal point of the sin alignment system.
 *
 * <p>Right-clicking the altar while holding nothing shows the player's current
 * sin alignment status. Additional story-driven interactions (e.g. offering
 * items to level up, unlocking sin abilities) can be added to this class.
 */
public class SinAltarBlock extends Block {

    public SinAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        player.getCapability(ModCapabilities.SIN_DATA).ifPresent(sinData -> {
            if (sinData.isAligned()) {
                player.displayClientMessage(
                        Component.translatable(
                                "block.seven_sins.sin_altar.aligned",
                                Component.translatable(sinData.getActiveSin().getTranslationKey()),
                                sinData.getSinLevel()),
                        false);
            } else {
                player.displayClientMessage(
                        Component.translatable("block.seven_sins.sin_altar.unaligned"),
                        false);
            }
            player.playSound(ModSounds.ALTAR_ACTIVATE.get(), 1.0f, 1.0f);
        });

        return InteractionResult.CONSUME;
    }
}
