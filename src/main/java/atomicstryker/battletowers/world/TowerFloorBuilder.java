package atomicstryker.battletowers.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

final class TowerFloorBuilder {
    private TowerFloorBuilder() {}

    static void build(ServerLevel level, BlockPos origin, TowerType type, boolean groundFloor, boolean underground) {
        for (int y = 0; y < BattleTowerGenerator.FLOOR_HEIGHT; y++) {
            for (int x = -TowerShape.RADIUS; x <= TowerShape.RADIUS; x++) {
                for (int z = -TowerShape.RADIUS; z <= TowerShape.RADIUS; z++) {
                    boolean wall = TowerShape.wall(x, z);
                    boolean inside = TowerShape.inside(x, z);
                    if (!wall && !inside) continue;

                    BlockPos pos = origin.offset(x, y, z);
                    if (y == 0 && inside) {
                        level.setBlock(pos, type.floor().defaultBlockState(), 2);
                    } else if (wall) {
                        if (!underground && TowerShape.window(x, z, y)) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        } else {
                            level.setBlock(pos, type.wall().defaultBlockState(), 2);
                        }
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        if (groundFloor && !underground) {
            for (int y = 1; y <= 3; y++) {
                level.setBlock(origin.offset(0, y, TowerShape.RADIUS), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(origin.offset(1, y, TowerShape.RADIUS), Blocks.AIR.defaultBlockState(), 2);
            }
        }
        TowerStairs.build(level, origin, type);
    }

    static void buildRoof(ServerLevel level, BlockPos origin, TowerType type) {
        for (int x = -TowerShape.RADIUS; x <= TowerShape.RADIUS; x++) {
            for (int z = -TowerShape.RADIUS; z <= TowerShape.RADIUS; z++) {
                if (TowerShape.inside(x, z) || TowerShape.wall(x, z)) {
                    level.setBlock(origin.offset(x, 0, z), type.floor().defaultBlockState(), 2);
                }
                if (TowerShape.wall(x, z)) {
                    level.setBlock(origin.offset(x, 1, z), type.wall().defaultBlockState(), 2);
                }
            }
        }
    }
}
