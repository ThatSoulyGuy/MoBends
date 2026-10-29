package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

public class LayerCustomItemInHand<E extends LivingEntity, M extends EntityModel<E> & ArmedModel>
        extends ItemInHandLayer<E, M>
{
    private final RenderLayer<E, ?> delegate;

    public LayerCustomItemInHand(RenderLayerParent<E, M> renderer, RenderLayer<E, ?> delegate)
    {
        super(renderer, Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer());
        this.delegate = delegate;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       E entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        this.delegate.render(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount,
                partialTicks, ageInTicks, netHeadYaw, headPitch);
    }
}
