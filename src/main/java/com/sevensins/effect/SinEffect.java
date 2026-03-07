package com.sevensins.effect;

import com.sevensins.common.data.SinType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * Base {@link MobEffect} subclass shared by all seven sin effects.
 * Each instance is parameterised with a {@link SinType} that drives its behaviour.
 *
 * <p>The default tick implementation applies attribute-based buffs/debuffs that
 * mirror the thematic flavour of each sin. More complex logic (particles, sounds,
 * special interactions) can be added here or in dedicated subclasses as the mod evolves.
 */
public class SinEffect extends MobEffect {

    private static final UUID WRATH_DAMAGE_UUID    = UUID.fromString("4a2e3f1c-0001-0000-0000-000000000001");
    private static final UUID GREED_SPEED_UUID     = UUID.fromString("4a2e3f1c-0002-0000-0000-000000000002");
    private static final UUID SLOTH_SPEED_UUID     = UUID.fromString("4a2e3f1c-0003-0000-0000-000000000003");
    private static final UUID PRIDE_HEALTH_UUID    = UUID.fromString("4a2e3f1c-0004-0000-0000-000000000004");
    private static final UUID LUST_SPEED_UUID      = UUID.fromString("4a2e3f1c-0005-0000-0000-000000000005");
    private static final UUID ENVY_DAMAGE_UUID     = UUID.fromString("4a2e3f1c-0006-0000-0000-000000000006");
    private static final UUID GLUTTONY_HEALTH_UUID = UUID.fromString("4a2e3f1c-0007-0000-0000-000000000007");

    private final SinType sinType;

    public SinEffect(SinType sinType, MobEffectCategory category, int color) {
        super(category, color);
        this.sinType = sinType;
        applyAttributeModifiers();
    }

    /** Returns the {@link SinType} this effect represents. */
    public SinType getSinType() {
        return sinType;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Attribute modifiers are already applied; tick logic for special behaviours
        // is left here for future per-sin implementations (e.g. Wrath rage state,
        // Gluttony consuming nearby food items, etc.).
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Apply every second (20 ticks)
        return duration % 20 == 0;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Registers attribute modifiers that characterise each sin.
     * These are applied when the effect is added and removed when it expires.
     */
    private void applyAttributeModifiers() {
        switch (sinType) {
            case WRATH ->
                // Wrath: increased melee attack damage
                addAttributeModifier(
                        Attributes.ATTACK_DAMAGE,
                        WRATH_DAMAGE_UUID.toString(),
                        0.2,
                        AttributeModifier.Operation.MULTIPLY_TOTAL);

            case GREED ->
                // Greed: increased movement speed (chase loot faster)
                addAttributeModifier(
                        Attributes.MOVEMENT_SPEED,
                        GREED_SPEED_UUID.toString(),
                        0.1,
                        AttributeModifier.Operation.MULTIPLY_TOTAL);

            case SLOTH ->
                // Sloth: reduced movement speed
                addAttributeModifier(
                        Attributes.MOVEMENT_SPEED,
                        SLOTH_SPEED_UUID.toString(),
                        -0.15,
                        AttributeModifier.Operation.MULTIPLY_TOTAL);

            case PRIDE ->
                // Pride: increased max health
                addAttributeModifier(
                        Attributes.MAX_HEALTH,
                        PRIDE_HEALTH_UUID.toString(),
                        4.0,
                        AttributeModifier.Operation.ADDITION);

            case LUST ->
                // Lust: agility boost
                addAttributeModifier(
                        Attributes.MOVEMENT_SPEED,
                        LUST_SPEED_UUID.toString(),
                        0.15,
                        AttributeModifier.Operation.MULTIPLY_TOTAL);

            case ENVY ->
                // Envy: slightly increased attack damage
                addAttributeModifier(
                        Attributes.ATTACK_DAMAGE,
                        ENVY_DAMAGE_UUID.toString(),
                        0.1,
                        AttributeModifier.Operation.MULTIPLY_TOTAL);

            case GLUTTONY ->
                // Gluttony: increased max health but sluggish
                addAttributeModifier(
                        Attributes.MAX_HEALTH,
                        GLUTTONY_HEALTH_UUID.toString(),
                        6.0,
                        AttributeModifier.Operation.ADDITION);
        }
    }
}
