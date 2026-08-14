package atomicstryker.battletowers.world;

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
    private static final int INITIAL_DELAY_TICKS = 20 * 15;
    private static final int PER_FLOOR_DELAY_TICKS = 20 * 5;
    private static final int FLOORS_TO_DESTROY = 6;
    private static final int FLOOR_DISTANCE = 7;
    private static final float EXPLOSION_POWER = 10.0F;

    private static final Queue<Task> PENDING = new ConcurrentLinkedQueue<>();
    private static final List<Task> ACTIVE = new ArrayList<>();

    private TowerDestructionManager() {
    }

    public static void start(ServerLevel level, BlockPos golemPosition, boolean underground) {
        PENDING.offer(new Task(level.dimension(), golemPosition.immutable(), underground));
        level.playSound(null, golemPosition, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.5F, 0.65F);
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
        private int age;
        private int destroyedFloors;

        private Task(ResourceKey<Level> dimension, BlockPos bossCenter, boolean underground) {
            this.dimension = dimension;
            this.bossCenter = bossCenter;
            this.underground = underground;
        }

        private boolean tick(ServerLevel level) {
            age++;

            if (age < INITIAL_DELAY_TICKS) {
                spawnWarningParticles(level);
                return false;
            }

            int ticksAfterInitial = age - INITIAL_DELAY_TICKS;
            if (ticksAfterInitial % PER_FLOOR_DELAY_TICKS != 0) {
                spawnWarningParticles(level);
                return false;
            }

            if (destroyedFloors >= FLOORS_TO_DESTROY) {
                return true;
            }

            int y = floorY();
            level.playSound(null, bossCenter.getX(), y, bossCenter.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 4.0F, 0.75F);
            level.explode(null, bossCenter.getX() + 0.5D, y, bossCenter.getZ() + 0.5D,
                    EXPLOSION_POWER, Level.ExplosionInteraction.TNT);
            if (!underground) {
                cleanFlyingBlocks(level, y);
            }
            destroyedFloors++;
            return destroyedFloors >= FLOORS_TO_DESTROY;
        }

        private int floorY() {
            int offset = destroyedFloors * FLOOR_DISTANCE;
            return underground ? bossCenter.getY() + offset : bossCenter.getY() - offset;
        }

        private void spawnWarningParticles(ServerLevel level) {
            if (age % 8 != 0) {
                return;
            }
            int y = floorY();
            double x = bossCenter.getX() - 7 + level.random.nextInt(15) + level.random.nextDouble();
            double z = bossCenter.getZ() - 7 + level.random.nextInt(15) + level.random.nextDouble();
            double py = y + level.random.nextDouble() * 7.0D;
            level.sendParticles(level.random.nextBoolean() ? ParticleTypes.SMOKE : ParticleTypes.LARGE_SMOKE,
                    x, py, z, 2, 0.15D, 0.15D, 0.15D, 0.01D);
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
    }
}
