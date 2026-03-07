package com.sevensins.client;

import com.sevensins.SevenSinsMod;
import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityManager;
import com.sevensins.ability.AbilityType;
import com.sevensins.ability.UltimateAbilityManager;
import com.sevensins.character.CharacterType;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.client.screen.SkillTreeScreen;
import com.sevensins.network.ModNetwork;
import com.sevensins.network.packet.UseAbilityPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Registers the three ability keybindings and dispatches
 * {@link UseAbilityPacket} to the server when a key is pressed.
 *
 * <p>Key assignments:
 * <ul>
 *   <li><b>R</b> – ability one (first ability of the player's character)</li>
 *   <li><b>F</b> – ability two (second ability of the player's character)</li>
 *   <li><b>V</b> – ability three (third ability of the player's character)</li>
 * </ul>
 * </p>
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Keybinds {

    private static final String KEY_CATEGORY = "key.category.seven_sins";

    /** R – activates the character's first ability. */
    public static final KeyMapping ABILITY_ONE = new KeyMapping(
            "key.seven_sins.ability_one",
            GLFW.GLFW_KEY_R,
            KEY_CATEGORY
    );

    /** F – activates the character's second ability. */
    public static final KeyMapping ABILITY_TWO = new KeyMapping(
            "key.seven_sins.ability_two",
            GLFW.GLFW_KEY_F,
            KEY_CATEGORY
    );

    /** V – reserved for ability three (TODO). */
    public static final KeyMapping ABILITY_THREE = new KeyMapping(
            "key.seven_sins.ability_three",
            GLFW.GLFW_KEY_V,
            KEY_CATEGORY
    );

    /** K – opens the Skill Tree screen. */
    public static final KeyMapping OPEN_SKILL_TREE = new KeyMapping(
            "key.seven_sins.open_skill_tree",
            GLFW.GLFW_KEY_K,
            KEY_CATEGORY
    );

    /** G – activates the character's ultimate ability. */
    public static final KeyMapping ULTIMATE = new KeyMapping(
            "key.seven_sins.ultimate",
            GLFW.GLFW_KEY_G,
            KEY_CATEGORY
    );

    // -------------------------------------------------------------------------
    // MOD bus – register keybindings

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ABILITY_ONE);
        event.register(ABILITY_TWO);
        event.register(ABILITY_THREE);
        event.register(OPEN_SKILL_TREE);
        event.register(ULTIMATE);
    }

    // -------------------------------------------------------------------------
    // FORGE bus – poll for key presses each client tick

    /**
     * Inner class subscribed to the FORGE event bus so that key presses can be
     * handled via {@link TickEvent.ClientTickEvent} without conflicting with the
     * outer class's MOD bus subscription.
     */
    @Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class KeyInputHandler {

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            // Ability one (R) – sends the first ability of the player's character.
            // The player's character is resolved from their IPlayerCharacterData capability.
            while (ABILITY_ONE.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) break;

                ModCapabilities.get(mc.player).ifPresent(cap -> {
                    CharacterType character = cap.getData().getSelectedCharacter();
                    if (character == CharacterType.NONE) return;

                    List<Ability> abilities = AbilityManager.getAbilitiesFor(character);
                    if (abilities.isEmpty()) return;

                    ModNetwork.CHANNEL.send(
                            PacketDistributor.SERVER.noArg(),
                            new UseAbilityPacket(abilities.get(0).getType())
                    );
                });
            }

            // Ability two (F) – sends the second ability of the player's character to the server.
            while (ABILITY_TWO.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) break;

                ModCapabilities.get(mc.player).ifPresent(cap -> {
                    CharacterType character = cap.getData().getSelectedCharacter();
                    if (character == CharacterType.NONE) return;

                    List<Ability> abilities = AbilityManager.getAbilitiesFor(character);
                    if (abilities.size() < 2) return;

                    ModNetwork.CHANNEL.send(
                            PacketDistributor.SERVER.noArg(),
                            new UseAbilityPacket(abilities.get(1).getType())
                    );
                });
            }

            // Ability three (V) – sends the third ability of the player's character to the server.
            while (ABILITY_THREE.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) break;

                ModCapabilities.get(mc.player).ifPresent(cap -> {
                    CharacterType character = cap.getData().getSelectedCharacter();
                    if (character == CharacterType.NONE) return;

                    List<Ability> abilities = AbilityManager.getAbilitiesFor(character);
                    if (abilities.size() < 3) return;

                    ModNetwork.CHANNEL.send(
                            PacketDistributor.SERVER.noArg(),
                            new UseAbilityPacket(abilities.get(2).getType())
                    );
                });
            }

            // Skill Tree (K) – opens the SkillTreeScreen on the client.
            while (OPEN_SKILL_TREE.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.screen == null) {
                    mc.setScreen(new SkillTreeScreen());
                }
            }

            // Ultimate (G) – sends the ultimate ability for the player's character to the server.
            while (ULTIMATE.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) break;

                ModCapabilities.get(mc.player).ifPresent(cap -> {
                    CharacterType character = cap.getData().getSelectedCharacter();
                    if (character == CharacterType.NONE) return;

                    AbilityType ultimateType = UltimateAbilityManager.getUltimateAbilityFor(character);
                    if (ultimateType == AbilityType.NONE) return;

                    ModNetwork.CHANNEL.send(
                            PacketDistributor.SERVER.noArg(),
                            new UseAbilityPacket(ultimateType)
                    );
                });
            }
        }
    }
}
