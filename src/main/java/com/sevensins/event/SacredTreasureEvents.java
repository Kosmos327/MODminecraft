package com.sevensins.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.character.CharacterType;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.item.ChastiefolItem;
import com.sevensins.item.LostvayneItem;
import com.sevensins.item.RhittaItem;
import com.sevensins.quest.QuestManager;
import com.sevensins.quest.QuestRegistry;
import com.sevensins.story.StoryFlag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles item-pickup events for Sacred Treasure items.
 *
 * <p>When a player picks up a Sacred Treasure ({@link LostvayneItem},
 * {@link RhittaItem}, or {@link ChastiefolItem}), the corresponding
 * story flag is set and any active obtain-quest for that treasure is
 * completed.</p>
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SacredTreasureEvents {

    private SacredTreasureEvents() {}

    /**
     * Called when a player picks up an item entity.
     * Detects Sacred Treasure pickups and advances story/quest state.
     */
    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ModCapabilities.get(player).ifPresent(cap -> {
            if (cap.getData().getSelectedCharacter() == CharacterType.NONE) return;

            ItemStack stack = event.getItem().getItem();

            if (stack.getItem() instanceof LostvayneItem) {
                cap.getData().getQuestData()
                        .addStoryFlag(StoryFlag.OBTAINED_LOSTVAYNE.getId());
                // No dedicated quest ID for Lostvayne in QuestRegistry; flag only.
            } else if (stack.getItem() instanceof RhittaItem) {
                cap.getData().getQuestData()
                        .addStoryFlag(StoryFlag.OBTAINED_RHITTA.getId());
                QuestManager.completeEventQuest(player, QuestRegistry.OBTAIN_RHITTA_ID);
            } else if (stack.getItem() instanceof ChastiefolItem) {
                cap.getData().getQuestData()
                        .addStoryFlag(StoryFlag.OBTAINED_CHASTIEFOL.getId());
                QuestManager.completeEventQuest(player, QuestRegistry.OBTAIN_CHASTIEFOL_ID);
            }
        });
    }
}
