package atomicstryker.battletowers.entity;

import atomicstryker.battletowers.config.BattleTowersConfig;
import atomicstryker.battletowers.registry.ModSounds;
import atomicstryker.battletowers.world.TowerDestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class BattleTowerGolem extends IronGolem {
    private static final EntityDataAccessor<Boolean> AWAKE = SynchedEntityData.defineId(BattleTowerGolem.class, EntityDataSerializers.BOOLEAN);

    private int rageCounter = 175;
    private int noTargetCountdown = 90;
    private int attackCounter;
    private int towerType;
    private int drops = 1;
    private BlockPos towerOrigin = BlockPos.ZERO;
    private BlockPos towerBossPosition = BlockPos.ZERO;
    private boolean towerUnderground;

    public BattleTowerGolem(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
        setPlayerCreated(false);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return IronGolem.createAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 12.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AWAKE, false);
    }

    public boolean isDormant() {
        return !entityData.get(AWAKE);
    }

    public void setDormant() {
        if (!level().isClientSide && isAlive()) {
            entityData.set(AWAKE, false);
            setNoAi(true);
            setTarget(null);
            attackCounter = 0;
        }
    }

    public void setAwake() {
        if (!level().isClientSide) {
            if (isDormant()) {
                resetNoTargetCountdown();
                level().playSound(null, blockPosition(), ModSounds.GOLEM_AWAKEN.get(), SoundSource.HOSTILE, 4.0F, 1.0F);
            }
            entityData.set(AWAKE, true);
            setNoAi(false);
        }
    }

    public void setTowerType(int towerType) {
        this.towerType = Math.max(0, towerType);
        this.drops = 5 + this.towerType;

        if (getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            double damage = BattleTowersConfig.golemBaseAttackDamage()
                    + BattleTowersConfig.golemAttackDamagePerTowerType() * this.towerType;
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        }
        if (getAttribute(Attributes.MAX_HEALTH) != null) {
            double health = BattleTowersConfig.golemBaseHealth()
                    + BattleTowersConfig.golemHealthPerTowerType() * this.towerType;
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
            setHealth(getMaxHealth());
        }
    }

    public int getTowerType() {
        return towerType;
    }

    public void setTowerOrigin(BlockPos towerOrigin) {
        this.towerOrigin = towerOrigin.immutable();
    }

    public BlockPos getTowerOrigin() {
        return towerOrigin;
    }

    public void setTowerBossPosition(BlockPos towerBossPosition) {
        this.towerBossPosition = towerBossPosition.immutable();
    }

    public BlockPos getTowerBossPosition() {
        return towerBossPosition;
    }

    public void setTowerUnderground(boolean towerUnderground) {
        this.towerUnderground = towerUnderground;
    }

    public boolean isTowerUnderground() {
        return towerUnderground;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player && !level().isClientSide) {
            setAwake();
            setTarget(player);
        }
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide || !isAlive()) {
            return;
        }

        if (isDormant()) {
            setNoAi(true);
            Player nearby = level().getNearestPlayer(this, BattleTowersConfig.golemWakeDistance());
            if (nearby != null && !nearby.isSpectator() && hasLineOfSight(nearby)) {
                setAwake();
                setTarget(nearby);
                rageCounter = 175;
            }
            return;
        }

        Player target = getTarget() instanceof Player player ? player : null;
        if (target == null || !target.isAlive()) {
            if (attackCounter > 0) {
                attackCounter--;
            }
            if (--noTargetCountdown <= 0 && onGround()) {
                heal(getMaxHealth());
                rageCounter = 125;
                setDormant();
            }
            return;
        }

        resetNoTargetCountdown();
        tickRangedAttack(target);

        boolean nearby = distanceToSqr(target) < 36.0D;
        boolean targetBelow = getY() - target.getY() > 0.3D;

        if (!nearby || targetBelow) {
            rageCounter -= 2;
        } else {
            rageCounter = 175;
        }

        if (rageCounter <= 0 && onGround()) {
            performSlam(target);
            rageCounter = 125;
        }
    }

    private void tickRangedAttack(Player target) {
        if (!BattleTowersConfig.golemFireballEnabled()) {
            attackCounter = 0;
            return;
        }
        if (!hasLineOfSight(target)) {
            if (attackCounter > 0) {
                attackCounter--;
            }
            return;
        }

        int chargeTicks = BattleTowersConfig.golemFireballChargeTicks();
        int chargeSoundTick = Math.max(1, chargeTicks / 2);
        if (attackCounter == chargeSoundTick) {
            level().playSound(null, blockPosition(), ModSounds.GOLEM_CHARGE.get(), SoundSource.HOSTILE, 4.0F,
                    1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        }

        attackCounter++;
        if (attackCounter >= chargeTicks) {
            conjureFireball(target);
            attackCounter = -BattleTowersConfig.golemFireballCooldownTicks();
        }
    }

    private void conjureFireball(Player target) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double sourceY = getY() + getBbHeight() * 0.8D;
        double targetY = target.getBoundingBox().minY + target.getBbHeight() * 0.5D;
        Vec3 direction = new Vec3(target.getX() - getX(), targetY - sourceY, target.getZ() - getZ()).normalize();
        Vec3 look = getLookAngle();

        level().playSound(null, blockPosition(), SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 4.0F,
                1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);

        // Modern LargeFireball already implements projectile deflection, so players can
        // punch this back at the Golem just like the classic Battle Towers projectile.
        LargeFireball fireball = new LargeFireball(
                serverLevel,
                this,
                direction,
                BattleTowersConfig.golemFireballExplosionPower());
        fireball.setPos(getX() + look.x * 2.0D, sourceY + look.y * 0.5D, getZ() + look.z * 2.0D);
        serverLevel.addFreshEntity(fireball);
    }

    private void performSlam(Player target) {
        level().playSound(null, blockPosition(), ModSounds.GOLEM_SPECIAL.get(), SoundSource.HOSTILE, 4.0F, 1.0F);
        if (getHealth() <= getMaxHealth() * 0.5F) {
            heal(20.0F);
        }

        if (level() instanceof ServerLevel serverLevel
                && getY() - target.getY() > 0.3D
                && BattleTowersConfig.golemExplosionsEnabled()) {
            serverLevel.explode(this, getX(), getY() - 0.3D, getZ(), BattleTowersConfig.golemSlamExplosionPower(), Level.ExplosionInteraction.MOB);
        } else {
            target.hurt(damageSources().mobAttack(this), 3.5F);
        }
    }

    private void resetNoTargetCountdown() {
        noTargetCountdown = BattleTowersConfig.golemResetDelayTicks();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isDormant() ? null : ModSounds.GOLEM.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.GOLEM_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GOLEM_DEATH.get();
    }

    @Override
    public void die(DamageSource source) {
        boolean wasAlive = isAlive();
        super.die(source);
        if (wasAlive && level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < drops; i++) {
                spawnAtLocation(Items.DIAMOND);
                spawnAtLocation(Items.REDSTONE);
            }
            int clayDrops = random.nextInt(4) + 8;
            for (int i = 0; i < clayDrops; i++) {
                spawnAtLocation(Blocks.CLAY.asItem());
            }

            BlockPos collapseCenter = towerBossPosition.equals(BlockPos.ZERO) ? blockPosition() : towerBossPosition;
            TowerDestructionManager.start(serverLevel, collapseCenter, towerUnderground);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Awake", !isDormant());
        tag.putInt("RageCounter", rageCounter);
        tag.putInt("NoTargetCountdown", noTargetCountdown);
        tag.putInt("AttackCounter", attackCounter);
        tag.putInt("TowerType", towerType);
        tag.putInt("Drops", drops);
        tag.putInt("TowerX", towerOrigin.getX());
        tag.putInt("TowerY", towerOrigin.getY());
        tag.putInt("TowerZ", towerOrigin.getZ());
        tag.putInt("BossX", towerBossPosition.getX());
        tag.putInt("BossY", towerBossPosition.getY());
        tag.putInt("BossZ", towerBossPosition.getZ());
        tag.putBoolean("TowerUnderground", towerUnderground);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        rageCounter = tag.getInt("RageCounter");
        noTargetCountdown = tag.getInt("NoTargetCountdown");
        attackCounter = tag.getInt("AttackCounter");
        towerType = tag.getInt("TowerType");
        drops = Math.max(1, tag.getInt("Drops"));
        towerOrigin = new BlockPos(tag.getInt("TowerX"), tag.getInt("TowerY"), tag.getInt("TowerZ"));
        towerBossPosition = new BlockPos(tag.getInt("BossX"), tag.getInt("BossY"), tag.getInt("BossZ"));
        towerUnderground = tag.getBoolean("TowerUnderground");
        setTowerType(towerType);
        if (tag.getBoolean("Awake")) {
            setAwake();
        } else {
            setDormant();
        }
    }
}
