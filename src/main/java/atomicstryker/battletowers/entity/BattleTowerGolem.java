package atomicstryker.battletowers.entity;

import atomicstryker.battletowers.world.TowerDestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class BattleTowerGolem extends IronGolem {
    private static final EntityDataAccessor<Boolean> AWAKE = SynchedEntityData.defineId(BattleTowerGolem.class, EntityDataSerializers.BOOLEAN);

    private int rageCounter = 175;
    private int noTargetCountdown = 90;
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
        }
    }

    public void setAwake() {
        if (!level().isClientSide) {
            if (isDormant()) {
                noTargetCountdown = 90;
                level().playSound(null, blockPosition(), SoundEvents.IRON_GOLEM_REPAIR, SoundSource.HOSTILE, 2.0F, 0.8F);
            }
            entityData.set(AWAKE, true);
            setNoAi(false);
        }
    }

    public void setTowerType(int towerType) {
        this.towerType = Math.max(0, towerType);
        this.drops = 5 + this.towerType;
        if (getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7.0D + this.towerType);
        }
        if (getAttribute(Attributes.MAX_HEALTH) != null) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(150.0D + 50.0D * this.towerType);
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
            Player nearby = level().getNearestPlayer(this, 6.0D);
            if (nearby != null && !nearby.isSpectator() && hasLineOfSight(nearby)) {
                setAwake();
                setTarget(nearby);
                rageCounter = 175;
            }
            return;
        }

        Player target = getTarget() instanceof Player player ? player : null;
        if (target == null || !target.isAlive()) {
            if (--noTargetCountdown <= 0 && onGround()) {
                heal(getMaxHealth());
                rageCounter = 125;
                setDormant();
            }
            return;
        }

        noTargetCountdown = 90;
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

    private void performSlam(Player target) {
        level().playSound(null, blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 2.0F, 0.65F);
        if (getHealth() <= getMaxHealth() * 0.5F) {
            heal(20.0F);
        }

        if (level() instanceof ServerLevel serverLevel && getY() - target.getY() > 0.3D) {
            serverLevel.explode(this, getX(), getY() - 0.3D, getZ(), 4.0F, Level.ExplosionInteraction.MOB);
        } else {
            target.hurt(damageSources().mobAttack(this), 3.5F);
        }
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
        towerType = tag.getInt("TowerType");
        drops = Math.max(1, tag.getInt("Drops"));
        towerOrigin = new BlockPos(tag.getInt("TowerX"), tag.getInt("TowerY"), tag.getInt("TowerZ"));
        towerBossPosition = new BlockPos(tag.getInt("BossX"), tag.getInt("BossY"), tag.getInt("BossZ"));
        towerUnderground = tag.getBoolean("TowerUnderground");
        if (tag.getBoolean("Awake")) {
            setAwake();
        } else {
            setDormant();
        }
    }
}
