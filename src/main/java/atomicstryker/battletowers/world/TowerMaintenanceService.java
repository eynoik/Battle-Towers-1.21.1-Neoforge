package atomicstryker.battletowers.world;

import atomicstryker.battletowers.entity.BattleTowerGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class TowerMaintenanceService {
    private TowerMaintenanceService() {
    }

    public static boolean deleteNearest(ServerLevel level, BlockPos position, double maxDistance) {
        TowerRegistrySavedData data = TowerRegistrySavedData.get(level);
        return data.nearest(level, position, maxDistance).map(record -> {
            clearTower(level, record);
            data.remove(level, record.origin());
            return true;
        }).orElse(false);
    }

    public static boolean regenerateNearest(ServerLevel level, BlockPos position, double maxDistance) {
        TowerRegistrySavedData data = TowerRegistrySavedData.get(level);
        return data.nearest(level, position, maxDistance).map(record -> {
            clearTower(level, record);
            TowerAssembler.build(level, record.origin(), record.towerType(), record.floors(), record.underground());
            data.addOrReplace(level, record.origin(), record.towerType(), record.floors(), record.underground());
            return true;
        }).orElse(false);
    }

    public static int deleteAll(ServerLevel level) {
        TowerRegistrySavedData data = TowerRegistrySavedData.get(level);
        List<TowerRegistrySavedData.TowerRecord> records = data.all(level);
        for (TowerRegistrySavedData.TowerRecord record : records) {
            clearTower(level, record);
        }
        data.clear(level);
        return records.size();
    }

    public static int regenerateAll(ServerLevel level) {
        TowerRegistrySavedData data = TowerRegistrySavedData.get(level);
        List<TowerRegistrySavedData.TowerRecord> records = data.all(level);
        for (TowerRegistrySavedData.TowerRecord record : records) {
            clearTower(level, record);
            TowerAssembler.build(level, record.origin(), record.towerType(), record.floors(), record.underground());
        }
        return records.size();
    }

    private static void clearTower(ServerLevel level, TowerRegistrySavedData.TowerRecord record) {
        int minY = record.underground() ? record.origin().getY() : record.origin().getY() + 4;
        int maxY = record.origin().getY() + record.floors() * BattleTowerGenerator.FLOOR_HEIGHT + 8;

        AABB entityArea = new AABB(
                record.origin().getX() - 9, minY - 4, record.origin().getZ() - 9,
                record.origin().getX() + 9, maxY + 4, record.origin().getZ() + 9);
        level.getEntitiesOfClass(BattleTowerGolem.class, entityArea,
                        golem -> golem.getTowerOrigin().equals(record.origin()))
                .forEach(golem -> golem.discard());

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -7; x <= 6; x++) {
            for (int z = -7; z <= 6; z++) {
                for (int y = minY; y <= maxY; y++) {
                    cursor.set(record.origin().getX() + x, y, record.origin().getZ() + z);
                    if (!level.getBlockState(cursor).isAir()) {
                        level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }
}
