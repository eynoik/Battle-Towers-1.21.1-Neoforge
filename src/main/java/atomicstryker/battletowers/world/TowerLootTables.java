package atomicstryker.battletowers.world;

import atomicstryker.battletowers.BattleTowers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

final class TowerLootTables {
    private static final ResourceKey<LootTable>[] FLOORS = createFloorKeys();

    private TowerLootTables() {
    }

    static ResourceKey<LootTable> forFloor(int floorIndex, int floorCount, boolean underground) {
        int logicalFloor;
        if (underground) {
            logicalFloor = floorIndex == 0 ? 9 : Math.max(0, Math.min(8, floorCount - 1 - floorIndex));
        } else {
            logicalFloor = floorIndex >= floorCount - 1 ? 9 : Math.max(0, Math.min(8, floorIndex));
        }
        return FLOORS[logicalFloor];
    }

    @SuppressWarnings("unchecked")
    private static ResourceKey<LootTable>[] createFloorKeys() {
        ResourceKey<LootTable>[] keys = new ResourceKey[10];
        for (int i = 0; i < keys.length; i++) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(BattleTowers.MOD_ID, "chests/floor_" + (i + 1));
            keys[i] = ResourceKey.create(Registries.LOOT_TABLE, id);
        }
        return keys;
    }
}
