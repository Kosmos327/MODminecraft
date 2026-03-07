package com.sevensins.item;

import com.sevensins.character.CharacterType;
import com.sevensins.character.capability.ModCapabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base class for all Sacred Treasure items.
 *
 * <p>Each Sacred Treasure is linked to a specific {@link CharacterType}. If the
 * wielding player's selected character matches the treasure's linked type, the
 * full sacred bonus is granted; otherwise no bonus is applied.
 *
 * <p>Subclasses supply bonus values via the protected constructor. Helper
 * methods centralise all bonus queries so callers never need to duplicate
 * the compatibility check.
 */
public abstract class SacredTreasureItem extends Item {

    private final CharacterType linkedCharacter;

    /** Flat ability-damage bonus added to matching-character abilities. */
    private final int abilityDamageBonus;

    /** Melee damage increase expressed as a whole percentage (e.g. 15 = +15%). */
    private final int meleeDamageBonusPercent;

    /** Max-mana increase expressed as a whole percentage (e.g. 10 = +10%). */
    private final int manaBonusPercent;

    protected SacredTreasureItem(CharacterType linkedCharacter,
                                  int abilityDamageBonus,
                                  int meleeDamageBonusPercent,
                                  int manaBonusPercent,
                                  Properties properties) {
        super(properties);
        this.linkedCharacter = linkedCharacter;
        this.abilityDamageBonus = abilityDamageBonus;
        this.meleeDamageBonusPercent = meleeDamageBonusPercent;
        this.manaBonusPercent = manaBonusPercent;
    }

    // -------------------------------------------------------------------------
    // Identity
    // -------------------------------------------------------------------------

    /** The {@link CharacterType} this treasure is designed for. */
    public CharacterType getLinkedCharacter() {
        return linkedCharacter;
    }

    // -------------------------------------------------------------------------
    // Compatibility
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} when the given player's selected character matches
     * this treasure's linked character type.
     *
     * <p>Safe to call from any logical side; returns {@code false} if the
     * capability is unavailable.</p>
     *
     * @param player the player to check
     * @return {@code true} if the player is compatible with this treasure
     */
    public boolean isCompatible(Player player) {
        return ModCapabilities.get(player).resolve()
                .map(cap -> cap.getData().getSelectedCharacter() == linkedCharacter)
                .orElse(false);
    }

    // -------------------------------------------------------------------------
    // Bonus accessors — return 0 when player is not compatible
    // -------------------------------------------------------------------------

    /**
     * Returns the flat ability-damage bonus for this treasure when the player
     * is compatible, or {@code 0} otherwise.
     *
     * @param player the wielding player
     * @return flat ability-damage bonus
     */
    public int getAbilityBonus(Player player) {
        return isCompatible(player) ? abilityDamageBonus : 0;
    }

    /**
     * Returns the melee-damage percentage bonus (e.g. 15 = +15%) when the
     * player is compatible, or {@code 0} otherwise.
     *
     * @param player the wielding player
     * @return melee-damage bonus percentage
     */
    public int getDamageBonus(Player player) {
        return isCompatible(player) ? meleeDamageBonusPercent : 0;
    }

    /**
     * Returns the max-mana percentage bonus (e.g. 10 = +10%) when the player
     * is compatible, or {@code 0} otherwise.
     *
     * @param player the wielding player
     * @return max-mana bonus percentage
     */
    public int getManaBonus(Player player) {
        return isCompatible(player) ? manaBonusPercent : 0;
    }

    // -------------------------------------------------------------------------
    // Raw bonus accessors (independent of player — used by CharacterStats)
    // -------------------------------------------------------------------------

    /** Raw ability-damage bonus value regardless of compatibility. */
    public int rawAbilityDamageBonus()    { return abilityDamageBonus; }

    /** Raw melee-damage percentage regardless of compatibility. */
    public int rawMeleeDamageBonusPercent() { return meleeDamageBonusPercent; }

    /** Raw max-mana percentage regardless of compatibility. */
    public int rawManaBonusPercent()       { return manaBonusPercent; }

    // -------------------------------------------------------------------------
    // Tooltip
    // -------------------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(
                Component.translatable(
                        "item.seven_sins.sacred_treasure.character",
                        Component.translatable("character.seven_sins." + linkedCharacter.getSerializedName()))
                        .withStyle(ChatFormatting.GOLD));

        if (abilityDamageBonus > 0) {
            tooltipComponents.add(
                    Component.translatable("item.seven_sins.sacred_treasure.ability_bonus",
                            abilityDamageBonus)
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (meleeDamageBonusPercent > 0) {
            tooltipComponents.add(
                    Component.translatable("item.seven_sins.sacred_treasure.melee_bonus",
                            meleeDamageBonusPercent)
                            .withStyle(ChatFormatting.RED));
        }
        if (manaBonusPercent > 0) {
            tooltipComponents.add(
                    Component.translatable("item.seven_sins.sacred_treasure.mana_bonus",
                            manaBonusPercent)
                            .withStyle(ChatFormatting.AQUA));
        }
    }
}
