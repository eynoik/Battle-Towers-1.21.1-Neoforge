package atomicstryker.battletowers.registry;

import atomicstryker.battletowers.BattleTowers;
import atomicstryker.battletowers.entity.BattleTowerGolem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModEntities {
    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(BattleTowers.MOD_ID);

    public static final Supplier<EntityType<BattleTowerGolem>> BATTLE_TOWER_GOLEM = ENTITY_TYPES.registerEntityType(
            "battle_tower_golem",
            BattleTowerGolem::new,
            MobCategory.MONSTER,
            builder -> builder.sized(1.4F, 4.0F).clientTrackingRange(10).updateInterval(2)
    );

    private ModEntities() {
    }
}
