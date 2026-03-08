package com.sevensins.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Monster;

/**
 * Generic placeholder renderer for all demon boss entities.
 *
 * <p>Uses the vanilla zombie humanoid model and zombie skin texture as a
 * placeholder until custom Blockbench-exported models and textures are added.
 * To replace a specific entity's appearance later, create a dedicated renderer
 * class (following the pattern of {@link MeliodasNpcRenderer}) and re-register
 * it in {@link com.sevensins.client.event.ModClientEventHandler}.</p>
 *
 * @param <T> the entity type — must extend {@link Monster}
 */
public class DemonEntityRenderer<T extends Monster>
        extends HumanoidMobRenderer<T, HumanoidModel<T>> {

    /** Placeholder texture — vanilla zombie skin used until custom assets are added. */
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("textures/entity/zombie/zombie.png");

    public DemonEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }
}
