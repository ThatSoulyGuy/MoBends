package goblinbob.mobends.standard.client.model.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Custom elytra model for Mo' Bends animations.
 * Updated for Minecraft 1.20.1 - uses modern model architecture.
 *
 * NOTE: This is a simplified implementation. Full integration with the
 * MoBends animation system would require additional work.
 */
@OnlyIn(Dist.CLIENT)
public class ModelBendsElytra<T extends LivingEntity> extends ElytraModel<T>
{
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public ModelBendsElytra(ModelPart root)
    {
        super(root);
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
    }

    public static LayerDefinition createLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(22, 0)
                        .addBox(-10.0F, 0.0F, 0.0F, 10, 20, 2, new CubeDeformation(1.0F)),
                PartPose.offset(5.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(22, 0)
                        .mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 10, 20, 2, new CubeDeformation(1.0F)),
                PartPose.offset(-5.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        float f = 0.2617994F;
        float f1 = -0.2617994F;
        float f2 = 0.0F;
        float f3 = 0.0F;

        if (entity.isFallFlying())
        {
            float f4 = 1.0F;
            Vec3 deltaMovement = entity.getDeltaMovement();

            if (deltaMovement.y < 0.0D)
            {
                Vec3 vec3d = deltaMovement.normalize();
                f4 = 1.0F - (float)Math.pow(-vec3d.y, 1.5D);
            }

            f = f4 * 0.34906584F + (1.0F - f4) * f;
            f1 = f4 * -((float)Math.PI / 2F) + (1.0F - f4) * f1;
        }

        this.leftWing.x = 5.0F;
        this.leftWing.y = f2;

        // In 1.20.1, the elytra rotation fields are handled differently
        // For now, use simpler animation
        if (entity instanceof AbstractClientPlayer)
        {
            // Apply smooth rotation (simplified from original)
            this.leftWing.xRot = f;
            this.leftWing.yRot = f3;
            this.leftWing.zRot = f1;
        }
        else
        {
            this.leftWing.xRot = f;
            this.leftWing.zRot = f1;
            this.leftWing.yRot = f3;
        }

        this.rightWing.x = -this.leftWing.x;
        this.rightWing.yRot = -this.leftWing.yRot;
        this.rightWing.y = this.leftWing.y;
        this.rightWing.xRot = this.leftWing.xRot;
        this.rightWing.zRot = -this.leftWing.zRot;
    }
}
