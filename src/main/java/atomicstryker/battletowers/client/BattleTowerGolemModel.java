package atomicstryker.battletowers.client;

import atomicstryker.battletowers.BattleTowers;
import atomicstryker.battletowers.entity.BattleTowerGolem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Legacy-compatible biped model for the Battle Towers Golem.
 *
 * The original ModelBiped used a logical 64x32 UV layout. The bundled 128x64
 * textures are simply 2x-resolution versions of that old layout. Baking the
 * modern PLAYER layer (64x64 logical UVs) therefore slices the texture badly.
 */
public final class BattleTowerGolemModel extends HumanoidModel<BattleTowerGolem> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(BattleTowers.MOD_ID, "battle_tower_golem"),
            "main");

    public BattleTowerGolemModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32);
    }
}
