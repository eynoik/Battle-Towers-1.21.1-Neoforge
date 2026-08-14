package atomicstryker.battletowers.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class BattleTowersConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue WORLDGEN_ENABLED;
    public static final ModConfigSpec.IntValue MIN_DISTANCE_FROM_SPAWN;
    public static final ModConfigSpec.IntValue MIN_DISTANCE_BETWEEN_TOWERS;
    public static final ModConfigSpec.IntValue UNDERGROUND_CHANCE_PERCENT;
    public static final ModConfigSpec.IntValue DEFAULT_FLOOR_COUNT;
    public static final ModConfigSpec.IntValue MAX_SURFACE_DIFFERENCE;

    public static final ModConfigSpec.IntValue WORLDGEN_CHECKS_PER_TICK;

    public static final ModConfigSpec.DoubleValue GOLEM_WAKE_DISTANCE;
    public static final ModConfigSpec.DoubleValue GOLEM_RESET_DELAY_SECONDS;
    public static final ModConfigSpec.DoubleValue GOLEM_BASE_HEALTH;
    public static final ModConfigSpec.DoubleValue GOLEM_HEALTH_PER_TOWER_TYPE;
    public static final ModConfigSpec.DoubleValue GOLEM_BASE_ATTACK_DAMAGE;
    public static final ModConfigSpec.DoubleValue GOLEM_ATTACK_DAMAGE_PER_TOWER_TYPE;
    public static final ModConfigSpec.BooleanValue GOLEM_EXPLOSIONS_ENABLED;
    public static final ModConfigSpec.DoubleValue GOLEM_SLAM_EXPLOSION_POWER;
    public static final ModConfigSpec.BooleanValue GOLEM_FIREBALL_ENABLED;
    public static final ModConfigSpec.IntValue GOLEM_FIREBALL_CHARGE_TICKS;
    public static final ModConfigSpec.IntValue GOLEM_FIREBALL_COOLDOWN_TICKS;
    public static final ModConfigSpec.IntValue GOLEM_FIREBALL_EXPLOSION_POWER;

    public static final ModConfigSpec.BooleanValue COLLAPSE_ENABLED;
    public static final ModConfigSpec.DoubleValue COLLAPSE_INITIAL_DELAY_SECONDS;
    public static final ModConfigSpec.DoubleValue COLLAPSE_FLOOR_INTERVAL_SECONDS;
    public static final ModConfigSpec.IntValue COLLAPSE_FLOORS_TO_DESTROY;
    public static final ModConfigSpec.DoubleValue COLLAPSE_EXPLOSION_POWER;
    public static final ModConfigSpec.BooleanValue COLLAPSE_CLEANUP_FLYING_BLOCKS;
    public static final ModConfigSpec.BooleanValue COLLAPSE_DESTROY_MOB_SPAWNERS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment(
                        "Natural Battle Tower generation.",
                        "This is a SERVER config, so every world can have its own balance settings.")
                .push("worldgen");

        WORLDGEN_ENABLED = builder
                .comment("Generate Battle Towers naturally in newly generated Overworld chunks.")
                .define("enabled", true);
        MIN_DISTANCE_FROM_SPAWN = builder
                .comment("Minimum horizontal distance in blocks between world spawn and a naturally generated tower.")
                .defineInRange("minimumDistanceFromSpawn", 96, 0, 4096);
        MIN_DISTANCE_BETWEEN_TOWERS = builder
                .comment("Target minimum horizontal distance in blocks between naturally generated towers.")
                .defineInRange("minimumDistanceBetweenTowers", 196, 32, 4096);
        UNDERGROUND_CHANCE_PERCENT = builder
                .comment("Chance, in percent, that a natural tower is generated underground.")
                .defineInRange("undergroundChancePercent", 15, 0, 100);
        DEFAULT_FLOOR_COUNT = builder
                .comment("Default number of floors used by natural generation and /battletowers spawn.")
                .defineInRange("defaultFloorCount", 10, 2, 12);
        MAX_SURFACE_DIFFERENCE = builder
                .comment("Maximum allowed height difference across terrain samples before tower generation is rejected.")
                .defineInRange("maxSurfaceDifference", 22, 0, 128);
        builder.pop();

        builder.comment("Performance safeguards for deferred natural generation checks.")
                .push("performance");
        WORLDGEN_CHECKS_PER_TICK = builder
                .comment("Maximum queued new-chunk tower checks processed per server tick.")
                .defineInRange("worldgenChecksPerTick", 4, 1, 64);
        builder.pop();

        builder.comment("Battle Tower Golem combat and scaling.")
                .push("golem");
        GOLEM_WAKE_DISTANCE = builder
                .comment("Distance in blocks at which a dormant Golem wakes when it can see a player.")
                .defineInRange("wakeDistance", 6.0D, 1.0D, 32.0D);
        GOLEM_RESET_DELAY_SECONDS = builder
                .comment("Time without a valid player target before the Golem fully heals and becomes dormant again.")
                .defineInRange("resetDelaySeconds", 4.5D, 1.0D, 60.0D);
        GOLEM_BASE_HEALTH = builder
                .comment("Base Golem health before the tower-type bonus is applied.")
                .defineInRange("baseHealth", 150.0D, 20.0D, 2000.0D);
        GOLEM_HEALTH_PER_TOWER_TYPE = builder
                .comment("Additional max health for every legacy tower-type tier.")
                .defineInRange("healthPerTowerType", 50.0D, 0.0D, 500.0D);
        GOLEM_BASE_ATTACK_DAMAGE = builder
                .comment("Base melee attack damage before the tower-type bonus is applied.")
                .defineInRange("baseAttackDamage", 7.0D, 0.0D, 100.0D);
        GOLEM_ATTACK_DAMAGE_PER_TOWER_TYPE = builder
                .comment("Additional melee attack damage for every legacy tower-type tier.")
                .defineInRange("attackDamagePerTowerType", 1.0D, 0.0D, 20.0D);
        GOLEM_EXPLOSIONS_ENABLED = builder
                .comment("Allow the Golem's rage/slam attack to create explosions. Replaces the old inverted noGolemExplosions option.")
                .define("explosionsEnabled", true);
        GOLEM_SLAM_EXPLOSION_POWER = builder
                .comment("Explosion power used by the Golem slam when the target is below it.")
                .defineInRange("slamExplosionPower", 4.0D, 0.0D, 16.0D);
        GOLEM_FIREBALL_ENABLED = builder
                .comment("Allow the classic charge -> fireball attack. The projectile can be hit back by players.")
                .define("fireballEnabled", true);
        GOLEM_FIREBALL_CHARGE_TICKS = builder
                .comment("Ticks from beginning the ranged attack until the fireball is fired. Charge sound plays halfway through.")
                .defineInRange("fireballChargeTicks", 20, 2, 200);
        GOLEM_FIREBALL_COOLDOWN_TICKS = builder
                .comment("Cooldown ticks after firing before another charge can begin. 20 ticks = 1 second.")
                .defineInRange("fireballCooldownTicks", 40, 0, 1200);
        GOLEM_FIREBALL_EXPLOSION_POWER = builder
                .comment("Explosion power of the classic Golem fireball. Original Battle Towers used 1.")
                .defineInRange("fireballExplosionPower", 1, 0, 8);
        builder.pop();

        builder.comment("Tower collapse sequence after the guardian dies.")
                .push("collapse");
        COLLAPSE_ENABLED = builder
                .comment("Collapse the tower after its Golem dies.")
                .define("enabled", true);
        COLLAPSE_INITIAL_DELAY_SECONDS = builder
                .comment("Delay before the first floor explosion. Original Battle Towers used 15 seconds.")
                .defineInRange("initialDelaySeconds", 15.0D, 0.0D, 300.0D);
        COLLAPSE_FLOOR_INTERVAL_SECONDS = builder
                .comment("Delay between consecutive floor explosions. Original Battle Towers used 5 seconds.")
                .defineInRange("floorIntervalSeconds", 5.0D, 0.05D, 60.0D);
        COLLAPSE_FLOORS_TO_DESTROY = builder
                .comment("Maximum number of tower floor segments destroyed by the collapse sequence.")
                .defineInRange("floorsToDestroy", 6, 1, 12);
        COLLAPSE_EXPLOSION_POWER = builder
                .comment("Explosion power for each collapsing floor. Original Battle Towers used 10.")
                .defineInRange("explosionPower", 10.0D, 0.0D, 32.0D);
        COLLAPSE_CLEANUP_FLYING_BLOCKS = builder
                .comment("Remove blocks left floating above each exploded surface-tower floor, matching the classic cleanup pass.")
                .define("cleanupFlyingBlocks", true);
        COLLAPSE_DESTROY_MOB_SPAWNERS = builder
                .comment("After collapse finishes, remove remaining mob spawners in the tower footprint. This was an option in the original mod.")
                .define("destroyMobSpawners", false);
        builder.pop();

        SPEC = builder.build();
    }

    private BattleTowersConfig() {
    }

    public static boolean worldgenEnabled() {
        return get(WORLDGEN_ENABLED);
    }

    public static int minDistanceFromSpawn() {
        return get(MIN_DISTANCE_FROM_SPAWN);
    }

    public static int minDistanceBetweenTowers() {
        return get(MIN_DISTANCE_BETWEEN_TOWERS);
    }

    public static int undergroundChancePercent() {
        return get(UNDERGROUND_CHANCE_PERCENT);
    }

    public static int defaultFloorCount() {
        return get(DEFAULT_FLOOR_COUNT);
    }

    public static int maxSurfaceDifference() {
        return get(MAX_SURFACE_DIFFERENCE);
    }

    public static int worldgenChecksPerTick() {
        return get(WORLDGEN_CHECKS_PER_TICK);
    }

    public static double golemWakeDistance() {
        return get(GOLEM_WAKE_DISTANCE);
    }

    public static int golemResetDelayTicks() {
        return Math.max(1, (int) Math.round(get(GOLEM_RESET_DELAY_SECONDS) * 20.0D));
    }

    public static double golemBaseHealth() {
        return get(GOLEM_BASE_HEALTH);
    }

    public static double golemHealthPerTowerType() {
        return get(GOLEM_HEALTH_PER_TOWER_TYPE);
    }

    public static double golemBaseAttackDamage() {
        return get(GOLEM_BASE_ATTACK_DAMAGE);
    }

    public static double golemAttackDamagePerTowerType() {
        return get(GOLEM_ATTACK_DAMAGE_PER_TOWER_TYPE);
    }

    public static boolean golemExplosionsEnabled() {
        return get(GOLEM_EXPLOSIONS_ENABLED);
    }

    public static float golemSlamExplosionPower() {
        return get(GOLEM_SLAM_EXPLOSION_POWER).floatValue();
    }

    public static boolean golemFireballEnabled() {
        return get(GOLEM_FIREBALL_ENABLED);
    }

    public static int golemFireballChargeTicks() {
        return get(GOLEM_FIREBALL_CHARGE_TICKS);
    }

    public static int golemFireballCooldownTicks() {
        return get(GOLEM_FIREBALL_COOLDOWN_TICKS);
    }

    public static int golemFireballExplosionPower() {
        return get(GOLEM_FIREBALL_EXPLOSION_POWER);
    }

    public static boolean collapseEnabled() {
        return get(COLLAPSE_ENABLED);
    }

    public static int collapseInitialDelayTicks() {
        return Math.max(0, (int) Math.round(get(COLLAPSE_INITIAL_DELAY_SECONDS) * 20.0D));
    }

    public static int collapseFloorIntervalTicks() {
        return Math.max(1, (int) Math.round(get(COLLAPSE_FLOOR_INTERVAL_SECONDS) * 20.0D));
    }

    public static int collapseFloorsToDestroy() {
        return get(COLLAPSE_FLOORS_TO_DESTROY);
    }

    public static float collapseExplosionPower() {
        return get(COLLAPSE_EXPLOSION_POWER).floatValue();
    }

    public static boolean collapseCleanupFlyingBlocks() {
        return get(COLLAPSE_CLEANUP_FLYING_BLOCKS);
    }

    public static boolean collapseDestroyMobSpawners() {
        return get(COLLAPSE_DESTROY_MOB_SPAWNERS);
    }

    private static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }
}
