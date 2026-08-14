package atomicstryker.battletowers.client;

import atomicstryker.battletowers.BattleTowers;
import atomicstryker.battletowers.entity.BattleTowerGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class BattleTowerGolemRenderer extends MobRenderer<BattleTowerGolem, BattleTowerGolemModel> {
    private static final ResourceLocation AWAKE = ResourceLocation.fromNamespaceAndPath(BattleTowers.MOD_ID, "textures/model/golem.png");
    private static final ResourceLocation DORMANT = ResourceLocation.fromNamespaceAndPath(BattleTowers.MOD_ID, "textures/model/golemdormant.png");

    public BattleTowerGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new BattleTowerGolemModel(context.bakeLayer(BattleTowerGolemModel.LAYER)), 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(BattleTowerGolem golem) {
        return golem.isDormant() ? DORMANT : AWAKE;
    }

    @Override
    protected void scale(BattleTowerGolem golem, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(2.0F, 2.0F, 2.0F);
    }
}
