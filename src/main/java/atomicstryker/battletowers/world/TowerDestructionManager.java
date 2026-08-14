package atomicstryker.battletowers.world;

import atomicstryker.battletowers.config.BattleTowersConfig;
import atomicstryker.battletowers.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class TowerDestructionManager {
    private static final int FLOOR_DISTANCE = 7;
    private static final Queue<Task> PENDING = new ConcurrentLinkedQueue<>();
    private static final List<Task> ACTIVE = new ArrayList<>();

    private TowerDestructionManager() {
    }

    public static void start(ServerLevel level, BlockPos golemPosition, boolean underground) {
        if (!BattleTowersConfig.collapseEnabled()) {
            return;
        }

        PENDING.offer(new Task(
                level.dimension(),
                golemPosition.immutable(),
                underground,
                BattleTowersConfig.collapseInitialDelayTicks(),
                BattleTowersConfig.collapseFloorIntervalTicks(),
                BattleTowersConfig.collapseFloorsToDestroy(),
                BattleTowersConfig.collapseExplosionPower(),
                BattleTowersConfig.collapseCleanupFlyingBlocks(),
                BattleTowersConfig.collapseDestroyMobSpawners()));

        level.playSound(null, golemPosition, ModSounds.TOWER_BREAK_START.get(), SoundSource.HOSTILE, 4.0F, 1.0F);
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        Task queued;
        while ((queued = PENDING.poll()) != null) {
            ACTIVE.add(queued);
        }

        MinecraftServer server = event.getServer();
        Iterator<Task> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            ServerLevel level = server.getLevel(task.dimension);
            if (level == null || task.tick(level)) {
                iterator.remove();
            }
        }
    }

    private static final class Task {
        private final ResourceKey<Level> dimension;
        private final BlockPos bossCenter;
        private final boolean underground;
        private final int initialDelayTicks;
        private final int perFloorDelayTicks;
        private final int floorsToDestroy;
        private final float explosionPower;
        private final boolean cleanupFlyingBlocks;
        private final boolean destroyMobSpawners;
        private int age;
        private int destroyedFloors;

        private Task(
                ResourceKey<Level> dimension,
                BlockPos bossCenter,
                boolean underground,
                int initialDelayTicks,
                int perFloorDelayTicks,
                int floorsToDestroy,
                float explosionPower,
                boolean cleanupFlyingBlocks,
                boolean destroyMobSpawners) {
            this.dimension = dimension;
            this.bossCenter = bossCenter;
            this.underground = underground;
            this.initialDelayTicks = initialDelayTicks;
            this.perFloorDelayTicks = perFloorDelayTicks;
            this.floorsToDestroy = floorsToDestroy;
            this.explosionPower = explosionPower;
            this.cleanupFlyingBlocks = cleanupFlyingBlocks;
            this.destroyMobSpawners = destroyMobSpawners;
        }

        private boolean tick(ServerLevel level) {
            age++;

            if (age < initialDelayTicks) {
                createWarningEffects(level);
                return false;
            }

            int ticksAfterInitial = age - initialDelayTicks;
            if (ticksAfterInitial % perFloorDelayTicks != 0) {
                createWarningEffects(level);
                return false;
            }

            if (destroyedFloors >= floorsToDestroy) {
                finish(level);
                return true;
            }

            int y = floorY();
            level.playSound(null, bossCenter.getX(), y, bossCenter.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 4.0F, 0.75F);
            level.explode(null, bossCenter.getX() + 0.5D, y, bossCenter.getZ() + 0.5D,
                    explosionPower, Level.ExplosionInteraction.TNT);
            if (!underground && cleanupFlyingBlocks) {
                cleanFlyingBlocks(level, y);
            }

            destroyedFloors++;
            if (destroyedFloors >= floorsToDestroy) {
                finish(level);
                return true;
            }
            return false;
        }

        private int floorY() {
            int offset = destroyedFloors * FLOOR_DISTANCE;
            return underground ? bossCenter.getY() + offset : bossCenter.getY() - offset;
        }

        private void createWarningEffects(ServerLevel level) {
            if (age % 8 == 0) {
                int y = floorY();
                double x = bossCenter.getX() - 7 + level.random.nextInt(15) + level.random.nextDouble();
                double z = bossCenter.getZ() - 7 + level.random.nextInt(15) + level.random.nextDouble();
                double py = y + level.random.nextDouble() * 7.0D;
                level.sendParticles(level.random.nextBoolean() ? ParticleTypes.SMOKE : ParticleTypes.LARGE_SMOKE,
                        x, py, z, 2, 0.15D, 0.15D, 0.15D, 0.01D);
            }

            if (age % 80 == 0) {
                if (level.random.nextBoolean()) {
                    level.playSound(null, bossCenter, ModSounds.TOWER_CRUMBLE.get(), SoundSource.HOSTILE, 4.0F, 1.0F);
                } else {
                    level.playSound(null, bossCenter, SoundEvents.LAVA_AMBIENT, SoundSource.HOSTILE, 4.0F, 1.0F);
                }
            }
        }

        private void cleanFlyingBlocks(ServerLevel level, int explosionY) {
            for (int x = -8; x < 8; x++) {
                for (int z = -8; z < 8; z++) {
                    for (int y = 1; y < 9; y++) {
                        BlockPos pos = new BlockPos(bossCenter.getX() + x, explosionY + y, bossCenter.getZ() + z);
                        if (!level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        private void finish(ServerLevel level) {
            if (!destroyMobSpawners) {
                return;
            }

            int verticalRange = 12 * FLOOR_DISTANCE + 12;
            int minY = underground ? bossCenter.getY() - 4 : bossCenter.getY() - verticalRange;
            int maxY = underground ? bossCenter.getY() + verticalRange : bossCenter.getY() + 4;
            minY = Math.max(minY, level.getMinBuildHeight());
            maxY = Math.min(maxY, level.getMaxBuildHeight() - 1);

            for (int x = -8; x < 8; x++) {
                for (int z = -8; z < 8; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        BlockPos pos = new BlockPos(bossCenter.getX() + x, y, bossCenter.getZ() + z);
                        if (level.getBlockState(pos).is(Blocks.SPAWNER)) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }
}
