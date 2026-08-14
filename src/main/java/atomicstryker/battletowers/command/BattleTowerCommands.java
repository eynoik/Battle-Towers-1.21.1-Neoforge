package atomicstryker.battletowers.command;

import atomicstryker.battletowers.config.BattleTowersConfig;
import atomicstryker.battletowers.world.BattleTowerGenerator;
import atomicstryker.battletowers.world.TowerMaintenanceService;
import atomicstryker.battletowers.world.TowerRegistrySavedData;
import atomicstryker.battletowers.world.TowerType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class BattleTowerCommands {
    private static final double ADMIN_SEARCH_RANGE = 192.0D;

    private BattleTowerCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("battletowers")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("spawn")
                                .executes(context -> spawn(context.getSource(), "random", BattleTowersConfig.defaultFloorCount(), false))
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .executes(context -> spawn(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "type"),
                                                BattleTowersConfig.defaultFloorCount(),
                                                false))
                                        .then(Commands.argument("floors", IntegerArgumentType.integer(2, 12))
                                                .executes(context -> spawn(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "type"),
                                                        IntegerArgumentType.getInteger(context, "floors"),
                                                        false))
                                                .then(Commands.argument("underground", BoolArgumentType.bool())
                                                        .executes(context -> spawn(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "type"),
                                                                IntegerArgumentType.getInteger(context, "floors"),
                                                                BoolArgumentType.getBool(context, "underground")))))))
                        .then(Commands.literal("types")
                                .executes(context -> listTypes(context.getSource())))
                        .then(Commands.literal("list")
                                .executes(context -> listTowers(context.getSource())))
                        .then(Commands.literal("delete")
                                .executes(context -> deleteNearest(context.getSource())))
                        .then(Commands.literal("regenerate")
                                .executes(context -> regenerateNearest(context.getSource())))
                        .then(Commands.literal("deleteall")
                                .executes(context -> deleteAll(context.getSource())))
                        .then(Commands.literal("regenerateall")
                                .executes(context -> regenerateAll(context.getSource())))
        );
    }

    private static int spawn(CommandSourceStack source, String requestedType, int floors, boolean underground) {
        ServerLevel level = source.getLevel();
        BlockPos sourcePos = BlockPos.containing(source.getPosition());
        BlockPos surface = BattleTowerGenerator.findSurface(level, sourcePos.getX(), sourcePos.getZ());
        TowerType type = requestedType.equalsIgnoreCase("random")
                ? BattleTowerGenerator.chooseType(level, surface, level.random)
                : TowerType.byName(requestedType);

        BattleTowerGenerator.generate(level, surface, type, floors, underground);
        source.sendSuccess(() -> Component.literal(
                "Generated " + type.serializedName() + " Battle Tower with " + floors + " floors at "
                        + surface.getX() + " " + surface.getY() + " " + surface.getZ()
                        + (underground ? " (underground)" : "")), true);
        return 1;
    }

    private static int listTypes(CommandSourceStack source) {
        String types = Arrays.stream(TowerType.values())
                .map(TowerType::serializedName)
                .collect(Collectors.joining(", "));
        source.sendSuccess(() -> Component.literal("Battle Tower types: random, " + types), false);
        return 1;
    }

    private static int listTowers(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        int count = TowerRegistrySavedData.get(level).all(level).size();
        source.sendSuccess(() -> Component.literal("Tracked Battle Towers in this dimension: " + count), false);
        return count;
    }

    private static int deleteNearest(CommandSourceStack source) {
        BlockPos pos = BlockPos.containing(source.getPosition());
        if (!TowerMaintenanceService.deleteNearest(source.getLevel(), pos, ADMIN_SEARCH_RANGE)) {
            source.sendFailure(Component.literal("No tracked Battle Tower found within " + (int) ADMIN_SEARCH_RANGE + " blocks."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Deleted nearest tracked Battle Tower."), true);
        return 1;
    }

    private static int regenerateNearest(CommandSourceStack source) {
        BlockPos pos = BlockPos.containing(source.getPosition());
        if (!TowerMaintenanceService.regenerateNearest(source.getLevel(), pos, ADMIN_SEARCH_RANGE)) {
            source.sendFailure(Component.literal("No tracked Battle Tower found within " + (int) ADMIN_SEARCH_RANGE + " blocks."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Regenerated nearest tracked Battle Tower."), true);
        return 1;
    }

    private static int deleteAll(CommandSourceStack source) {
        int count = TowerMaintenanceService.deleteAll(source.getLevel());
        source.sendSuccess(() -> Component.literal("Deleted " + count + " tracked Battle Tower(s) in this dimension."), true);
        return count;
    }

    private static int regenerateAll(CommandSourceStack source) {
        int count = TowerMaintenanceService.regenerateAll(source.getLevel());
        source.sendSuccess(() -> Component.literal("Regenerated " + count + " tracked Battle Tower(s) in this dimension."), true);
        return count;
    }
}
