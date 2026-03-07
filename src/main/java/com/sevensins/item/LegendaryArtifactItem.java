package com.sevensins.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base class for all Legendary Artifact items.
 *
 * <p>Legendary Artifacts are powerful late-game relics distinct from Sacred
 * Treasures.  They provide bonuses when held in the player's main hand or
 * offhand slot.  Subclasses supply bonus values via the protected constructor.
 *
 * <p>Unlike Sacred Treasures, Legendary Artifacts are not linked to a specific
 * character type — any player can benefit from them.</p>
 *
 * <p>Version-1 bonus types:</p>
 * <ul>
 *   <li>Max mana percentage bonus (e.g. 20 = +20%).</li>
 *   <li>Resistance percentage bonus (e.g. 10 = +10%).</li>
 *   <li>Power level flat bonus.</li>
 * </ul>
 */
public abstract class LegendaryArtifactItem extends Item {

    /** Display name for this artifact (shown in tooltip). */
    private final String artifactName;

    /** Max-mana increase expressed as a whole percentage (e.g. 20 = +20%). */
    private final int manaBonusPercent;

    /** Resistance bonus expressed as a whole percentage (e.g. 10 = +10%). */
    private final int resistanceBonusPercent;

    /** Flat power-level bonus. */
    private final int powerLevelBonus;

    protected LegendaryArtifactItem(String artifactName,
                                     int manaBonusPercent,
                                     int resistanceBonusPercent,
                                     int powerLevelBonus,
                                     Properties properties) {
        super(properties);
        this.artifactName            = artifactName;
        this.manaBonusPercent        = manaBonusPercent;
        this.resistanceBonusPercent  = resistanceBonusPercent;
        this.powerLevelBonus         = powerLevelBonus;
    }

    // -------------------------------------------------------------------------
    // Bonus accessors
    // -------------------------------------------------------------------------

    /** Max-mana bonus as a whole percentage (e.g. 20 = +20%). */
    public int getManaBonusPercent() {
        return manaBonusPercent;
    }

    /** Resistance bonus as a whole percentage (e.g. 10 = +10%). */
    public int getResistanceBonusPercent() {
        return resistanceBonusPercent;
    }

    /** Flat power-level bonus. */
    public int getPowerLevelBonus() {
        return powerLevelBonus;
    }

    // -------------------------------------------------------------------------
    // Tooltip
    // -------------------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(
                Component.literal(artifactName)
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        tooltipComponents.add(
                Component.literal("Legendary Artifact")
                        .withStyle(ChatFormatting.GOLD));

        if (manaBonusPercent > 0) {
            tooltipComponents.add(
                    Component.literal("+" + manaBonusPercent + "% Max Mana")
                            .withStyle(ChatFormatting.AQUA));
        }
        if (resistanceBonusPercent > 0) {
            tooltipComponents.add(
                    Component.literal("+" + resistanceBonusPercent + "% Resistance")
                            .withStyle(ChatFormatting.GREEN));
        }
        if (powerLevelBonus > 0) {
            tooltipComponents.add(
                    Component.literal("+" + powerLevelBonus + " Power Level")
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        tooltipComponents.add(
                Component.literal("Active when held in main hand or offhand.")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
