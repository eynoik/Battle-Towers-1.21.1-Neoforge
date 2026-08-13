package atomicstryker.battletowers.event;

import atomicstryker.battletowers.BattleTowers;
import atomicstryker.battletowers.entity.BattleTowerGolem;
import atomicstryker.battletowers.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = BattleTowers.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BATTLE_TOWER_GOLEM.get(), context ->
                (EntityRenderer<BattleTowerGolem>) (EntityRenderer) new IronGolemRenderer(context));
    }
}
