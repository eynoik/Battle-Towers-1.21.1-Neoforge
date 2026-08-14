package atomicstryker.battletowers.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

final class TowerAssembler {
    private TowerAssembler() {
    }

    static void build(ServerLevel level, BlockPos origin, TowerType type, int floors, boolean underground) {
        BlockPos bossFloorOrigin = null;

        for (int floor = 0; floor < floors; floor++) {
            BlockPos floorOrigin = origin.above(floor * BattleTowerGenerator.FLOOR_HEIGHT);
            boolean bossFloor = underground ? floor == 0 : floor == floors - 1;

            TowerFloorBuilder.build(level, floorOrigin, type, floor == 0, underground, bossFloor);
            TowerFloorPopulator.populate(level, floorOrigin, type, floor, floors, underground);

            if (bossFloor) {
                bossFloorOrigin = floorOrigin;
            }
        }

        if (bossFloorOrigin != null) {
            TowerBossSpawner.spawn(level, origin, bossFloorOrigin.offset(0, 6, 0), type);
        }
    }
}
