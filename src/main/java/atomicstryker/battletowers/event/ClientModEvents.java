package atomicstryker.battletowers.event;

import atomicstryker.battletowers.BattleTowers;
import atomicstryker.battletowers.client.BattleTowerGolemModel;
import atomicstryker.battletowers.client.BattleTowerGolemRenderer;
import atomicstryker.battletowers.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = BattleTowers.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BattleTowerGolemModel.LAYER, BattleTowerGolemModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BATTLE_TOWER_GOLEM.get(), BattleTowerGolemRenderer::new);
    }
}
