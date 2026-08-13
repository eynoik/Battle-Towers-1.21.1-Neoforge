package atomicstryker.battletowers.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

final class TowerStairs {
    private TowerStairs() {}

    static void build(ServerLevel level, BlockPos origin, TowerType type) {
        if (!(type.stairs() instanceof StairBlock)) return;
        for (int step = 0; step < 6; step++) {
            BlockPos pos = origin.offset(-3 + step, step + 1, -4);
            BlockState stair = type.stairs().defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
            level.setBlock(pos, stair, 2);
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
        }
    }
}
