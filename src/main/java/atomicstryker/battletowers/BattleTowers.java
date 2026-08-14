package atomicstryker.battletowers;

import atomicstryker.battletowers.command.BattleTowerCommands;
import atomicstryker.battletowers.config.BattleTowersConfig;
import atomicstryker.battletowers.event.TowerInteractionHandler;
import atomicstryker.battletowers.registry.ModEntities;
import atomicstryker.battletowers.registry.ModSounds;
import atomicstryker.battletowers.world.BattleTowerWorldgen;
import atomicstryker.battletowers.world.TowerDestructionManager;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(BattleTowers.MOD_ID)
public final class BattleTowers {
    public static final String MOD_ID = "battletowers";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BattleTowers(IEventBus modEventBus, ModContainer modContainer) {
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, BattleTowersConfig.SPEC, "battletowers-server.toml");

        NeoForge.EVENT_BUS.addListener(BattleTowerCommands::register);
        NeoForge.EVENT_BUS.addListener(BattleTowerWorldgen::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(BattleTowerWorldgen::onServerTick);
        NeoForge.EVENT_BUS.addListener(TowerDestructionManager::onServerTick);
        NeoForge.EVENT_BUS.addListener(TowerInteractionHandler::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(TowerInteractionHandler::onLeftClickBlock);
        LOGGER.info("Battle Towers NeoForge 1.21.1 port loaded");
    }
}
