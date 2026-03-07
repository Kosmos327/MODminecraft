package com.sevensins.client.renderer;

import com.sevensins.entity.MeliodasNpcEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for {@link MeliodasNpcEntity}.
 *
 * <p>Uses the vanilla humanoid model with the zombie skin as a placeholder.
 * A custom texture can be added later at the path used by
 * {@link #TEXTURE}.</p>
 */
public class MeliodasNpcRenderer
        extends HumanoidMobRenderer<MeliodasNpcEntity, HumanoidModel<MeliodasNpcEntity>> {

    /** Placeholder texture — uses the vanilla zombie skin until a custom one is added. */
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("textures/entity/zombie/zombie.png");

    public MeliodasNpcRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(MeliodasNpcEntity entity) {
        return TEXTURE;
    }
}
