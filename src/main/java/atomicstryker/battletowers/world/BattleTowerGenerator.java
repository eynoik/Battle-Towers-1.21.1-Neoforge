package atomicstryker.battletowers.world;

import atomicstryker.battletowers.config.BattleTowersConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public final class BattleTowerGenerator {
    public static final int FLOOR_HEIGHT = 7;
    public static final int DEFAULT_FLOORS = 10;

    private BattleTowerGenerator() {
    }

    public static BlockPos findSurface(ServerLevel level, int x, int z) {
        return new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z), z);
    }

    public static TowerType chooseType(ServerLevel level, BlockPos center, RandomSource random) {
        BlockState surface = level.getBlockState(findSurface(level, center.getX(), center.getZ()).below());
        if (surface.is(Blocks.SAND) || surface.is(Blocks.SANDSTONE)) return TowerType.SANDSTONE;
        if (surface.is(Blocks.SNOW) || surface.is(Blocks.SNOW_BLOCK) || surface.is(Blocks.ICE)) return TowerType.ICE;
        if (!surface.getFluidState().isEmpty()) return TowerType.MOSSY_COBBLESTONE;
        if (random.nextInt(10) == 0) return TowerType.NETHERRACK;
        return random.nextInt(5) == 0 ? TowerType.SMOOTH_STONE : TowerType.COBBLESTONE;
    }

    public static boolean generate(ServerLevel level, BlockPos surface, TowerType type) {
        return generate(level, surface, type, BattleTowersConfig.defaultFloorCount(), false);
    }

    public static boolean generate(ServerLevel level, BlockPos surface, TowerType type, int requestedFloors, boolean underground) {
        int floors = Math.max(2, Math.min(12, requestedFloors));
        int baseY = underground
                ? Math.max(level.getMinBuildHeight() + 8, surface.getY() - floors * FLOOR_HEIGHT)
                : surface.getY() - 6;
        BlockPos origin = new BlockPos(surface.getX(), baseY, surface.getZ());
        floors = Math.min(floors, Math.max(2, (level.getMaxBuildHeight() - baseY - 8) / FLOOR_HEIGHT));
        TowerAssembler.build(level, origin, type, floors, underground);
        TowerRegistrySavedData.get(level).addOrReplace(level, origin, type, floors, underground);
        return true;
    }
}
