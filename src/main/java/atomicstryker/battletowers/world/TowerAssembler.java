package atomicstryker.battletowers.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

final class TowerAssembler {
    private TowerAssembler() {}

    static void build(ServerLevel level, BlockPos origin, TowerType type, int floors, boolean underground) {
        for (int floor = 0; floor < floors; floor++) {
            BlockPos floorOrigin = origin.above(floor * BattleTowerGenerator.FLOOR_HEIGHT);
            TowerFloorBuilder.build(level, floorOrigin, type, floor == 0, underground);
        }
        TowerFloorBuilder.buildRoof(level, origin.above(floors * BattleTowerGenerator.FLOOR_HEIGHT), type);
    }
}
