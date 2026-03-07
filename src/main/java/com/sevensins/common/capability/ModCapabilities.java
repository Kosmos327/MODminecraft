package com.sevensins.common.capability;

import com.sevensins.common.data.PlayerCharacterData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/**
 * Holds the Forge {@link Capability} token for {@link PlayerCharacterData}.
 *
 * <p>Register via {@link net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent}
 * in your mod event-bus listener before any player joins.</p>
 */
public final class ModCapabilities {

    /** Capability instance – populated by Forge after registration. */
    public static final Capability<PlayerCharacterData> PLAYER_CHARACTER =
            CapabilityManager.get(new CapabilityToken<>() {});

    private ModCapabilities() {}
}
