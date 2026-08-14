package atomicstryker.battletowers.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

final class TowerFloorPopulator {
    private TowerFloorPopulator() {
    }

    static void populate(ServerLevel level, BlockPos floorOrigin, TowerType type, int floorIndex, int floorCount, boolean underground) {
        placeLoot(level, floorOrigin, type, floorIndex, floorCount, underground);

        boolean bossFloor = underground ? floorIndex == 0 : floorIndex == floorCount - 1;
        if (!bossFloor) {
            placeSpawner(level, floorOrigin.offset(2, 6, 2), selectMob(level.random));
            placeSpawner(level, floorOrigin.offset(-3, 6, 2), selectMob(level.random));
        }
    }

    private static void placeLoot(ServerLevel level, BlockPos origin, TowerType type, int floorIndex, int floorCount, boolean underground) {
        level.setBlock(origin.offset(0, 6, 3), type.floor().defaultBlockState(), 2);
        level.setBlock(origin.offset(-1, 6, 3), type.floor().defaultBlockState(), 2);
        placeLootChest(level, origin.offset(0, 7, 3), floorIndex, floorCount, underground);
        placeLootChest(level, origin.offset(-1, 7, 3), floorIndex, floorCount, underground);
    }

    private static void placeLootChest(ServerLevel level, BlockPos pos, int floorIndex, int floorCount, boolean underground) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(TowerLootTables.forFloor(floorIndex, floorCount, underground));
        }
    }

    private static void placeSpawner(ServerLevel level, BlockPos pos, EntityType<?> mobType) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(mobType, level, level.random, pos);
        }
    }

    private static EntityType<?> selectMob(RandomSource random) {
        return switch (random.nextInt(10)) {
            case 0, 1, 2 -> EntityType.SKELETON;
            case 3, 4, 5, 6 -> EntityType.ZOMBIE;
            case 7, 8 -> EntityType.SPIDER;
            default -> EntityType.CAVE_SPIDER;
        };
    }
}
