package com.sevensins.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sevensins.SevenSinsMod;
import com.sevensins.entity.RedDemonEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Humanoid-proportioned model for {@link RedDemonEntity}.
 *
 * <p>This is a placeholder model using standard humanoid geometry until a
 * custom Blockbench-exported model is available. The texture atlas is
 * {@code assets/seven_sins/textures/entity/red_demon.png} (64×64).</p>
 *
 * <p>Registered via
 * {@link com.sevensins.client.event.ModClientEventHandler#onRegisterLayerDefinitions}.</p>
 */
public class RedDemonModel<T extends RedDemonEntity> extends EntityModel<T> {

    /** Layer location used when registering and baking this model. */
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(SevenSinsMod.MODID, "red_demon"), "main");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public RedDemonModel(ModelPart root) {
        this.head     = root.getChild("head");
        this.body     = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm  = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg  = root.getChild("left_leg");
    }

    /**
     * Builds the {@link LayerDefinition} that describes the model geometry.
     * Called once during renderer registration.
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDef = new MeshDefinition();
        PartDefinition partDef = meshDef.getRoot();

        // Head — 8×8×8 cube centred at origin, UV (0, 0)
        partDef.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Body — 8×12×4, UV (16, 16)
        partDef.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Right arm — 4×12×4, UV (40, 16)
        partDef.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        // Left arm — mirrored, UV (40, 16)
        partDef.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).mirror()
                        .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        // Right leg — 4×12×4, UV (0, 16)
        partDef.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        // Left leg — mirrored, UV (0, 16)
        partDef.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16).mirror()
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshDef, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float toRad = (float) (Math.PI / 180.0);

        this.head.yRot = netHeadYaw * toRad;
        this.head.xRot = headPitch * toRad;

        float swing = limbSwing * 0.6662F;
        this.rightArm.xRot = (float) (-Math.PI / 5.0) * limbSwingAmount * (float) Math.sin(swing);
        this.leftArm.xRot  = (float) (-Math.PI / 5.0) * limbSwingAmount * (float) Math.sin(swing + Math.PI);
        this.rightLeg.xRot = (float) (Math.PI * 0.35) * limbSwingAmount * (float) Math.sin(swing + Math.PI);
        this.leftLeg.xRot  = (float) (Math.PI * 0.35) * limbSwingAmount * (float) Math.sin(swing);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
