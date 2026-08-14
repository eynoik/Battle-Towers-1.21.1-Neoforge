package atomicstryker.battletowers.event;

import atomicstryker.battletowers.entity.BattleTowerGolem;
import atomicstryker.battletowers.world.TowerRegistrySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class TowerInteractionHandler {
    private TowerInteractionHandler() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        awakenGuardianForChest(event.getEntity(), event.getPos());
    }

    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        awakenGuardianForChest(event.getEntity(), event.getPos());
    }

    private static void awakenGuardianForChest(Player player, BlockPos chestPos) {
        if (!(player.level() instanceof ServerLevel level)
                || !(level.getBlockState(chestPos).getBlock() instanceof ChestBlock)) {
            return;
        }

        TowerRegistrySavedData.get(level).containing(level, chestPos).ifPresent(record -> {
            AABB search = new AABB(record.origin()).inflate(16.0D, record.floors() * 7.0D + 16.0D, 16.0D);
            level.getEntitiesOfClass(BattleTowerGolem.class, search, golem -> golem.isAlive()
                            && golem.getTowerOrigin().equals(record.origin()))
                    .stream()
                    .findFirst()
                    .ifPresent(golem -> {
                        golem.setAwake();
                        golem.setTarget(player);
                    });
        });
    }
}
