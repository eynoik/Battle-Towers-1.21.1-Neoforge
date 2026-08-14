package atomicstryker.battletowers.world;

import atomicstryker.battletowers.BattleTowers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Persistent cross-dimension index of generated Battle Towers, stored in Overworld SavedData. */
public final class TowerRegistrySavedData extends SavedData {
    private static final String DATA_NAME = BattleTowers.MOD_ID + "_towers";
    private final List<TowerRecord> towers = new ArrayList<>();

    public static TowerRegistrySavedData get(ServerLevel level) {
        ServerLevel storageLevel = level.getServer().overworld();
        return storageLevel.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(TowerRegistrySavedData::new, TowerRegistrySavedData::load),
                DATA_NAME);
    }

    public static TowerRegistrySavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        TowerRegistrySavedData data = new TowerRegistrySavedData();
        ListTag list = tag.getList("Towers", Tag.TAG_COMPOUND);
        for (Tag value : list) {
            CompoundTag tower = (CompoundTag) value;
            data.towers.add(new TowerRecord(
                    tower.getString("Dimension"),
                    new BlockPos(tower.getInt("X"), tower.getInt("Y"), tower.getInt("Z")),
                    tower.getString("Type"),
                    tower.getInt("Floors"),
                    tower.getBoolean("Underground")));
        }
        return data;
    }

    public void addOrReplace(ServerLevel level, BlockPos origin, TowerType type, int floors, boolean underground) {
        String dimension = dimensionId(level);
        towers.removeIf(record -> record.dimension().equals(dimension) && record.origin().equals(origin));
        towers.add(new TowerRecord(dimension, origin.immutable(), type.serializedName(), floors, underground));
        setDirty();
    }

    public boolean remove(ServerLevel level, BlockPos origin) {
        String dimension = dimensionId(level);
        boolean changed = towers.removeIf(record -> record.dimension().equals(dimension) && record.origin().equals(origin));
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public void clear(ServerLevel level) {
        String dimension = dimensionId(level);
        if (towers.removeIf(record -> record.dimension().equals(dimension))) {
            setDirty();
        }
    }

    public List<TowerRecord> all(ServerLevel level) {
        String dimension = dimensionId(level);
        return towers.stream().filter(record -> record.dimension().equals(dimension)).toList();
    }

    public Optional<TowerRecord> nearest(ServerLevel level, BlockPos pos, double maxDistance) {
        String dimension = dimensionId(level);
        double maxDistanceSq = maxDistance * maxDistance;
        return towers.stream()
                .filter(record -> record.dimension().equals(dimension))
                .filter(record -> record.origin().distSqr(pos) <= maxDistanceSq)
                .min(Comparator.comparingDouble(record -> record.origin().distSqr(pos)));
    }

    public Optional<TowerRecord> containing(ServerLevel level, BlockPos pos) {
        String dimension = dimensionId(level);
        return towers.stream()
                .filter(record -> record.dimension().equals(dimension))
                .filter(record -> contains(record, pos))
                .min(Comparator.comparingDouble(record -> record.origin().distSqr(pos)));
    }

    private static boolean contains(TowerRecord record, BlockPos pos) {
        int minX = record.origin().getX() - 7;
        int maxX = record.origin().getX() + 6;
        int minZ = record.origin().getZ() - 7;
        int maxZ = record.origin().getZ() + 6;
        int minY = record.origin().getY();
        int maxY = minY + record.floors() * BattleTowerGenerator.FLOOR_HEIGHT + 8;
        return pos.getX() >= minX && pos.getX() <= maxX
                && pos.getZ() >= minZ && pos.getZ() <= maxZ
                && pos.getY() >= minY && pos.getY() <= maxY;
    }

    private static String dimensionId(ServerLevel level) {
        return level.dimension().location().toString();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (TowerRecord record : towers) {
            CompoundTag tower = new CompoundTag();
            tower.putString("Dimension", record.dimension());
            tower.putInt("X", record.origin().getX());
            tower.putInt("Y", record.origin().getY());
            tower.putInt("Z", record.origin().getZ());
            tower.putString("Type", record.type());
            tower.putInt("Floors", record.floors());
            tower.putBoolean("Underground", record.underground());
            list.add(tower);
        }
        tag.put("Towers", list);
        return tag;
    }

    public record TowerRecord(String dimension, BlockPos origin, String type, int floors, boolean underground) {
        public TowerType towerType() {
            return TowerType.byName(type);
        }
    }
}
