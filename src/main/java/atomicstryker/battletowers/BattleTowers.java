package atomicstryker.battletowers;

import atomicstryker.battletowers.command.BattleTowerCommands;
import atomicstryker.battletowers.registry.ModEntities;
import atomicstryker.battletowers.world.BattleTowerWorldgen;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(BattleTowers.MOD_ID)
public final class BattleTowers {
    public static final String MOD_ID = "battletowers";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BattleTowers(IEventBus modEventBus, ModContainer modContainer) {
        ModEntities.ENTITY_TYPES.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(BattleTowerCommands::register);
        NeoForge.EVENT_BUS.addListener(BattleTowerWorldgen::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(BattleTowerWorldgen::onServerTick);
        LOGGER.info("Battle Towers NeoForge 1.21.1 port loaded");
    }
}
