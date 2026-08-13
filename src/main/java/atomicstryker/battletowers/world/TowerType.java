package atomicstryker.battletowers.world;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum TowerType {
    COBBLESTONE("cobblestone", 1, Blocks.COBBLESTONE, Blocks.SMOOTH_STONE, Blocks.STONE_BRICK_STAIRS, Blocks.TORCH),
    MOSSY_COBBLESTONE("mossy", 2, Blocks.MOSSY_COBBLESTONE, Blocks.SMOOTH_STONE, Blocks.STONE_BRICK_STAIRS, Blocks.TORCH),
    SANDSTONE("sandstone", 3, Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.SANDSTONE_STAIRS, Blocks.TORCH),
    ICE("ice", 4, Blocks.PACKED_ICE, Blocks.CLAY, Blocks.OAK_STAIRS, Blocks.AIR),
    SMOOTH_STONE("smoothstone", 5, Blocks.STONE, Blocks.SMOOTH_STONE, Blocks.STONE_BRICK_STAIRS, Blocks.TORCH),
    NETHERRACK("netherrack", 6, Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.NETHER_BRICK_STAIRS, Blocks.GLOWSTONE),
    JUNGLE("jungle", 7, Blocks.MOSSY_COBBLESTONE, Blocks.DIRT, Blocks.JUNGLE_STAIRS, Blocks.COBWEB);

    private final String serializedName;
    private final int legacyId;
    private final Block wall;
    private final Block floor;
    private final Block stairs;
    private final Block light;

    TowerType(String serializedName, int legacyId, Block wall, Block floor, Block stairs, Block light) {
        this.serializedName = serializedName;
        this.legacyId = legacyId;
        this.wall = wall;
        this.floor = floor;
        this.stairs = stairs;
        this.light = light;
    }

    public String serializedName() {
        return serializedName;
    }

    public int legacyId() {
        return legacyId;
    }

    public Block wall() {
        return wall;
    }

    public Block floor() {
        return floor;
    }

    public Block stairs() {
        return stairs;
    }

    public Block light() {
        return light;
    }

    public static TowerType byName(String name) {
        for (TowerType type : values()) {
            if (type.serializedName.equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return COBBLESTONE;
    }
}
