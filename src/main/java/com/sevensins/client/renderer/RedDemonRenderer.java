package com.sevensins.client.renderer;

import com.sevensins.client.model.RedDemonModel;
import com.sevensins.entity.RedDemonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for {@link RedDemonEntity} using the custom {@link RedDemonModel}.
 */
public class RedDemonRenderer extends MobRenderer<RedDemonEntity, RedDemonModel<RedDemonEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("seven_sins", "textures/entity/red_demon.png");

    public RedDemonRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new RedDemonModel<>(ctx.bakeLayer(RedDemonModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(RedDemonEntity entity) {
        return TEXTURE;
    }
}
