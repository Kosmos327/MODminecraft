package com.sevensins.common.item;

import com.sevensins.common.capability.ModCapabilities;
import com.sevensins.common.data.SinType;
import com.sevensins.common.registry.ModSounds;
import com.sevensins.network.ModNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A Sin Emblem item representing one of the Seven Deadly Sins.
 *
 * <p>When right-clicked by an un-aligned player the emblem is consumed and the
 * player becomes aligned with the corresponding sin. A player who is already
 * aligned receives an informational message instead.
 */
public class SinEmblemItem extends Item {

    private final SinType sinType;

    public SinEmblemItem(SinType sinType, Properties properties) {
        super(properties);
        this.sinType = sinType;
    }

    /** Returns the {@link SinType} this emblem represents. */
    public SinType getSinType() {
        return sinType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        player.getCapability(ModCapabilities.SIN_DATA).ifPresent(sinData -> {
            if (sinData.isAligned()) {
                player.displayClientMessage(
                        Component.translatable("message.seven_sins.already_aligned"), true);
                return;
            }

            sinData.setActiveSin(sinType);
            sinData.setSinLevel(1);

            player.displayClientMessage(
                    Component.translatable("message.seven_sins.sin_chosen",
                            Component.translatable(sinType.getTranslationKey())), false);

            player.playSound(ModSounds.SIN_ALIGN.get(), 1.0f, 1.0f);

            if (player instanceof ServerPlayer serverPlayer) {
                ModNetwork.syncToPlayer(sinData, serverPlayer);
            }

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        });

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(
                Component.translatable(sinType.getDescriptionKey())
                        .withStyle(style -> style.withColor(sinType.getColor()).withItalic(true)));
    }
}
