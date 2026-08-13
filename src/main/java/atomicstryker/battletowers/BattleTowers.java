package atomicstryker.battletowers;

import atomicstryker.battletowers.registry.ModEntities;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(BattleTowers.MOD_ID)
public final class BattleTowers {
    public static final String MOD_ID = "battletowers";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BattleTowers(IEventBus modEventBus, ModContainer modContainer) {
        ModEntities.ENTITY_TYPES.register(modEventBus);
        LOGGER.info("Battle Towers NeoForge 1.21.1 port loaded");
    }
}
