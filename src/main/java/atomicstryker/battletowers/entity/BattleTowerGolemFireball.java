package atomicstryker.battletowers.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Battle Towers fireball behavior layered on top of Minecraft's modern
 * LargeFireball implementation. The vanilla projectile already provides the
 * robust 1.21.1 collision and deflection rules, while this class gives the
 * Battle Towers shot its own feedback without introducing a second networked
 * projectile type.
 */
public final class BattleTowerGolemFireball extends LargeFireball {
    public BattleTowerGolemFireball(Level level, LivingEntity owner, Vec3 direction, int explosionPower) {
        super(level, owner, direction, explosionPower);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean deflected = super.hurt(source, amount);
        if (deflected && source.getEntity() instanceof Player && !level().isClientSide) {
            level().playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.5F, 1.2F);
        }
        return deflected;
    }
}
