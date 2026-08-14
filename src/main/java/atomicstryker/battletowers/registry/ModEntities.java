package atomicstryker.battletowers.registry;

import atomicstryker.battletowers.BattleTowers;
import atomicstryker.battletowers.entity.BattleTowerGolem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, BattleTowers.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<BattleTowerGolem>> BATTLE_TOWER_GOLEM =
            ENTITY_TYPES.register("battle_tower_golem", () -> EntityType.Builder
                    .<BattleTowerGolem>of(BattleTowerGolem::new, MobCategory.MONSTER)
                    .setShouldReceiveVelocityUpdates(true)
                    .setTrackingRange(10)
                    .setUpdateInterval(2)
                    .sized(1.4F, 4.0F)
                    .build("battle_tower_golem"));

    private ModEntities() {
    }
}
