package atomicstryker.battletowers.world;

import atomicstryker.battletowers.BattleTowers;
import atomicstryker.battletowers.config.BattleTowersConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class BattleTowerWorldgen {
    private static final int[][] TERRAIN_SAMPLES = {
            {4, -5}, {4, 0}, {4, 5},
            {0, -5}, {0, 0}, {0, 5},
            {-4, -5}, {-4, 0}, {-4, 5}
    };

    private static final long SPAWN_CHANCE_SALT = 0x4C4F4F54544F5745L;
    private static final Queue<PendingChunk> PENDING = new ConcurrentLinkedQueue<>();

    private BattleTowerWorldgen() {
    }

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!BattleTowersConfig.worldgenEnabled()) {
            return;
        }
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!level.dimension().equals(Level.OVERWORLD)) {
            return;
        }

        ChunkPos chunk = event.getChunk().getPos();
        PENDING.offer(new PendingChunk(level.dimension(), chunk.x, chunk.z, 1));
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (!BattleTowersConfig.worldgenEnabled()) {
            PENDING.clear();
            return;
        }

        MinecraftServer server = event.getServer();
        int count = Math.min(PENDING.size(), BattleTowersConfig.worldgenChecksPerTick());

        for (int i = 0; i < count; i++) {
            PendingChunk pending = PENDING.poll();
            if (pending == null) {
                break;
            }
            if (pending.delayTicks > 0) {
                PENDING.offer(pending.withDelay(pending.delayTicks - 1));
                continue;
            }

            ServerLevel level = server.getLevel(pending.dimension);
            if (level != null) {
                attemptNaturalGeneration(level, new ChunkPos(pending.chunkX, pending.chunkZ));
            }
        }
    }

    private static void attemptNaturalGeneration(ServerLevel level, ChunkPos chunk) {
        long seed = level.getSeed();
        if (!isCandidateChunk(seed, chunk)) {
            return;
        }
        if (!passesSpawnChance(seed, chunk)) {
            return;
        }

        int x = (chunk.x << 4) + 8;
        int z = (chunk.z << 4) + 8;
        BlockPos surface = BattleTowerGenerator.findSurface(level, x, z);

        int minimumSpawnDistance = BattleTowersConfig.minDistanceFromSpawn();
        BlockPos spawn = level.getSharedSpawnPos();
        long spawnDx = (long) spawn.getX() - x;
        long spawnDz = (long) spawn.getZ() - z;
        if (spawnDx * spawnDx + spawnDz * spawnDz < (long) minimumSpawnDistance * minimumSpawnDistance) {
            return;
        }

        RandomSource random = RandomSource.create(mix64(seed ^ chunk.toLong()));
        TowerType type = chooseTypeAndValidate(level, surface, random);
        if (type == null) {
            return;
        }

        boolean underground = random.nextInt(100) < BattleTowersConfig.undergroundChancePercent();
        BattleTowerGenerator.generate(level, surface, type, BattleTowersConfig.defaultFloorCount(), underground);
        BattleTowers.LOGGER.info("Generated natural {} Battle Tower at [{}, {}], underground={}", type.serializedName(), x, z, underground);
    }

    private static boolean passesSpawnChance(long seed, ChunkPos chunk) {
        int chance = BattleTowersConfig.spawnChancePercent();
        if (chance <= 0) {
            return false;
        }
        if (chance >= 100) {
            return true;
        }

        RandomSource chanceRandom = RandomSource.create(mix64(seed ^ chunk.toLong() ^ SPAWN_CHANCE_SALT));
        return chanceRandom.nextInt(100) < chance;
    }

    private static TowerType chooseTypeAndValidate(ServerLevel level, BlockPos center, RandomSource random) {
        int water = 0;
        int snow = 0;
        int sand = 0;
        int foliage = 0;
        int other = 0;
        int maxSurfaceDifference = BattleTowersConfig.maxSurfaceDifference();

        for (int[] sample : TERRAIN_SAMPLES) {
            BlockPos surface = BattleTowerGenerator.findSurface(level, center.getX() + sample[0], center.getZ() + sample[1]);
            if (Math.abs(surface.getY() - center.getY()) > maxSurfaceDifference) {
                return null;
            }

            BlockState ground = level.getBlockState(surface.below());
            if (ground.is(Blocks.LAVA) || !ground.getFluidState().isEmpty() && !ground.getFluidState().isSource()) {
                return null;
            }

            for (int depth = 1; depth <= 5; depth++) {
                BlockState below = level.getBlockState(surface.below(depth));
                if (below.isAir()) {
                    return null;
                }
            }

            if (ground.is(Blocks.SAND) || ground.is(Blocks.SANDSTONE) || ground.is(Blocks.RED_SAND) || ground.is(Blocks.RED_SANDSTONE)) {
                sand++;
            } else if (ground.is(Blocks.SNOW) || ground.is(Blocks.SNOW_BLOCK) || ground.is(Blocks.ICE) || ground.is(Blocks.PACKED_ICE)) {
                snow++;
            } else if (!ground.getFluidState().isEmpty()) {
                water++;
            } else if (ground.is(BlockTags.LEAVES) || ground.is(BlockTags.LOGS)) {
                foliage++;
            } else {
                other++;
            }
        }

        int max = Math.max(Math.max(water, snow), Math.max(sand, Math.max(foliage, other)));

        if (sand == max) {
            return firstEnabled(
                    TowerType.SANDSTONE,
                    TowerType.COBBLESTONE,
                    TowerType.SMOOTH_STONE,
                    TowerType.MOSSY_COBBLESTONE,
                    TowerType.ICE,
                    TowerType.JUNGLE);
        }
        if (snow == max) {
            return firstEnabled(
                    TowerType.ICE,
                    TowerType.COBBLESTONE,
                    TowerType.SMOOTH_STONE,
                    TowerType.MOSSY_COBBLESTONE,
                    TowerType.SANDSTONE,
                    TowerType.JUNGLE);
        }
        if (water == max) {
            return firstEnabled(
                    TowerType.MOSSY_COBBLESTONE,
                    TowerType.COBBLESTONE,
                    TowerType.SMOOTH_STONE,
                    TowerType.SANDSTONE,
                    TowerType.ICE,
                    TowerType.JUNGLE);
        }
        if (foliage == max) {
            if (BattleTowersConfig.spawnJungle()) {
                return TowerType.JUNGLE;
            }
            return firstEnabled(
                    TowerType.MOSSY_COBBLESTONE,
                    TowerType.COBBLESTONE,
                    TowerType.SMOOTH_STONE,
                    TowerType.SANDSTONE,
                    TowerType.ICE);
        }

        if (random.nextInt(5) == 0 && BattleTowersConfig.spawnSmoothStone()) {
            return TowerType.SMOOTH_STONE;
        }
        return firstEnabled(
                TowerType.COBBLESTONE,
                TowerType.SMOOTH_STONE,
                TowerType.MOSSY_COBBLESTONE,
                TowerType.SANDSTONE,
                TowerType.ICE,
                TowerType.JUNGLE);
    }

    private static TowerType firstEnabled(TowerType... candidates) {
        for (TowerType candidate : candidates) {
            if (isNaturalOverworldTypeEnabled(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isNaturalOverworldTypeEnabled(TowerType type) {
        return switch (type) {
            case COBBLESTONE -> BattleTowersConfig.spawnCobblestone();
            case MOSSY_COBBLESTONE -> BattleTowersConfig.spawnMossyCobblestone();
            case SANDSTONE -> BattleTowersConfig.spawnSandstone();
            case ICE -> BattleTowersConfig.spawnIce();
            case SMOOTH_STONE -> BattleTowersConfig.spawnSmoothStone();
            case JUNGLE -> BattleTowersConfig.spawnJungle();
            case NETHERRACK -> false;
        };
    }

    private static boolean isCandidateChunk(long seed, ChunkPos chunk) {
        int minimumDistance = BattleTowersConfig.minDistanceBetweenTowers();
        int cellSizeChunks = Math.max(4, ((minimumDistance + 15) / 16) + 1);
        int cellX = Math.floorDiv(chunk.x, cellSizeChunks);
        int cellZ = Math.floorDiv(chunk.z, cellSizeChunks);
        ChunkPos candidate = candidateForCell(seed, cellX, cellZ, cellSizeChunks);
        if (!candidate.equals(chunk)) {
            return false;
        }

        long priority = cellPriority(seed, cellX, cellZ);
        long minDistanceSq = (long) minimumDistance * minimumDistance;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                ChunkPos neighbor = candidateForCell(seed, cellX + dx, cellZ + dz, cellSizeChunks);
                long blockDx = (long) (candidate.x - neighbor.x) * 16L;
                long blockDz = (long) (candidate.z - neighbor.z) * 16L;
                if (blockDx * blockDx + blockDz * blockDz < minDistanceSq
                        && Long.compareUnsigned(cellPriority(seed, cellX + dx, cellZ + dz), priority) < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private static ChunkPos candidateForCell(long seed, int cellX, int cellZ, int cellSizeChunks) {
        long hash = mix64(seed
                ^ (long) cellX * 341873128712L
                ^ (long) cellZ * 132897987541L
                ^ 0x5DEECE66DL);
        int usable = cellSizeChunks - 2;
        int offsetX = 1 + Math.floorMod((int) hash, usable);
        int offsetZ = 1 + Math.floorMod((int) (hash >>> 32), usable);
        return new ChunkPos(cellX * cellSizeChunks + offsetX, cellZ * cellSizeChunks + offsetZ);
    }

    private static long cellPriority(long seed, int cellX, int cellZ) {
        return mix64(seed
                + (long) cellX * 0x9E3779B97F4A7C15L
                + (long) cellZ * 0xC2B2AE3D27D4EB4FL);
    }

    private static long mix64(long value) {
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private record PendingChunk(ResourceKey<Level> dimension, int chunkX, int chunkZ, int delayTicks) {
        private PendingChunk withDelay(int delay) {
            return new PendingChunk(dimension, chunkX, chunkZ, delay);
        }
    }
}
