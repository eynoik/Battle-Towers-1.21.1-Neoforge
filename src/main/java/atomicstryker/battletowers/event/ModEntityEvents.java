package atomicstryker.battletowers.event;

import atomicstryker.battletowers.BattleTowers;
import atomicstryker.battletowers.entity.BattleTowerGolem;
import atomicstryker.battletowers.registry.ModEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = BattleTowers.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEntityEvents {
    private ModEntityEvents() {}

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.BATTLE_TOWER_GOLEM.get(), BattleTowerGolem.createAttributes().build());
    }
}
