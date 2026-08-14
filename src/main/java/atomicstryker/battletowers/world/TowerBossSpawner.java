package atomicstryker.battletowers.world;

import atomicstryker.battletowers.entity.BattleTowerGolem;
import atomicstryker.battletowers.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

final class TowerBossSpawner {
    private TowerBossSpawner() {
    }

    static void spawn(ServerLevel level, BlockPos towerOrigin, BlockPos bossPos, TowerType type, boolean underground) {
        BattleTowerGolem golem = ModEntities.BATTLE_TOWER_GOLEM.get().create(level);
        if (golem == null) {
            return;
        }

        golem.setTowerType(type.legacyId());
        golem.setTowerOrigin(towerOrigin);
        golem.setTowerBossPosition(bossPos);
        golem.setTowerUnderground(underground);
        golem.moveTo(bossPos.getX() + 0.5D, bossPos.getY(), bossPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        golem.setDormant();
        level.addFreshEntity(golem);
    }
}
