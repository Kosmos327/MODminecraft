package com.sevensins.client.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.client.renderer.MeliodasNpcRenderer;
import com.sevensins.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles MOD-bus client events, such as entity renderer registration.
 *
 * <p>Kept separate from {@link ClientEventHandler} because that class
 * subscribes to the FORGE bus, while renderer registration requires the MOD bus.</p>
 */
@Mod.EventBusSubscriber(
        modid = SevenSinsMod.MODID,
        bus   = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public class ModClientEventHandler {

    private ModClientEventHandler() {}

    /**
     * Registers entity renderers.
     *
     * <p>{@link com.sevensins.entity.MeliodasNpcEntity} uses
     * {@link MeliodasNpcRenderer} (humanoid model, zombie skin placeholder).</p>
     */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MELIODAS_NPC.get(), MeliodasNpcRenderer::new);
    }
}
