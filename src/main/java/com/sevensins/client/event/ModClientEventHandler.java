package com.sevensins.client.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.client.renderer.DemonEntityRenderer;
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
     *
     * <p>All boss demon entities use {@link DemonEntityRenderer} as a shared
     * placeholder renderer (zombie model/texture) until custom Blockbench assets
     * are added. To replace a specific boss renderer later, create a dedicated
     * renderer class and register it here instead.</p>
     */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MELIODAS_NPC.get(), MeliodasNpcRenderer::new);
        event.registerEntityRenderer(ModEntities.RED_DEMON.get(), DemonEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.GRAY_DEMON.get(), DemonEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.DEMON_COMMANDER.get(), DemonEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.MYTHIC_RED_DEMON.get(), DemonEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.ESTAROSSA.get(), DemonEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.DEMON_KING.get(), DemonEntityRenderer::new);
    }
}
