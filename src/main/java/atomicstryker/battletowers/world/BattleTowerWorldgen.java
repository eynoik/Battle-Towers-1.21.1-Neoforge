package atomicstryker.battletowers.world;

import atomicstryker.battletowers.BattleTowers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
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
    private static final int CELL_SIZE_CHUNKS = 14;
    private static final int MIN_DISTANCE_BETWEEN_TOWERS = 196;
    private static final int MIN_DISTANCE_FROM_SPAWN = 96;
    private static final int MAX_SURFACE_DIFFERENCE = 22;
    private static final int UNDERGROUND_CHANCE_PERCENT = 15;
    private static final int CHECKS_PER_TICK = 4;

    private static final int[][] TERRAIN_SAMPLES = {
            {4, -5}, {4, 0}, {4, 5},
            {0, -5}, {0, 0}, {0, 5},
            {-4, -5}, {-4, 0}, {-4, 5}
    };

    private static final Queue<PendingChunk> PENDING = new ConcurrentLinkedQueue<>();

    private BattleTowerWorldgen() {
    }

    public static void onChunkLoad(ChunkEvent.Load event) {
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
        MinecraftServer server = event.getServer();
        int count = Math.min(PENDING.size(), CHECKS_PER_TICK);

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

        int x = (chunk.x << 4) + 8;
        int z = (chunk.z << 4) + 8;
        BlockPos surface = BattleTowerGenerator.findSurface(level, x, z);

        BlockPos spawn = level.getSharedSpawnPos();
        long spawnDx = (long) spawn.getX() - x;
        long spawnDz = (long) spawn.getZ() - z;
        if (spawnDx * spawnDx + spawnDz * spawnDz < (long) MIN_DISTANCE_FROM_SPAWN * MIN_DISTANCE_FROM_SPAWN) {
            return;
        }

        RandomSource random = RandomSource.create(mix64(seed ^ chunk.toLong()));
        TowerType type = chooseTypeAndValidate(level, surface, random);
        if (type == null) {
            return;
        }

        boolean underground = random.nextInt(100) < UNDERGROUND_CHANCE_PERCENT;
        BattleTowerGenerator.generate(level, surface, type, BattleTowerGenerator.DEFAULT_FLOORS, underground);
        BattleTowers.LOGGER.info("Generated natural {} Battle Tower at [{}, {}], underground={}", type.serializedName(), x, z, underground);
    }

    private static TowerType chooseTypeAndValidate(ServerLevel level, BlockPos center, RandomSource random) {
        int water = 0;
        int snow = 0;
        int sand = 0;
        int foliage = 0;
        int other = 0;

        for (int[] sample : TERRAIN_SAMPLES) {
            BlockPos surface = BattleTowerGenerator.findSurface(level, center.getX() + sample[0], center.getZ() + sample[1]);
            if (Math.abs(surface.getY() - center.getY()) > MAX_SURFACE_DIFFERENCE) {
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
        if (sand == max) return TowerType.SANDSTONE;
        if (snow == max) return TowerType.ICE;
        if (water == max || foliage == max) return TowerType.MOSSY_COBBLESTONE;
        if (random.nextInt(10) == 0) return TowerType.NETHERRACK;
        return random.nextInt(5) == 0 ? TowerType.SMOOTH_STONE : TowerType.COBBLESTONE;
    }

    private static boolean isCandidateChunk(long seed, ChunkPos chunk) {
        int cellX = Math.floorDiv(chunk.x, CELL_SIZE_CHUNKS);
        int cellZ = Math.floorDiv(chunk.z, CELL_SIZE_CHUNKS);
        ChunkPos candidate = candidateForCell(seed, cellX, cellZ);
        if (!candidate.equals(chunk)) {
            return false;
        }

        long priority = cellPriority(seed, cellX, cellZ);
        long minDistanceSq = (long) MIN_DISTANCE_BETWEEN_TOWERS * MIN_DISTANCE_BETWEEN_TOWERS;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                ChunkPos neighbor = candidateForCell(seed, cellX + dx, cellZ + dz);
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

    private static ChunkPos candidateForCell(long seed, int cellX, int cellZ) {
        long hash = mix64(seed
                ^ (long) cellX * 341873128712L
                ^ (long) cellZ * 132897987541L
                ^ 0x5DEECE66DL);
        int usable = CELL_SIZE_CHUNKS - 2;
        int offsetX = 1 + Math.floorMod((int) hash, usable);
        int offsetZ = 1 + Math.floorMod((int) (hash >>> 32), usable);
        return new ChunkPos(cellX * CELL_SIZE_CHUNKS + offsetX, cellZ * CELL_SIZE_CHUNKS + offsetZ);
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
