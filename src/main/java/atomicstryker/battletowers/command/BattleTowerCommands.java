package atomicstryker.battletowers.command;

import atomicstryker.battletowers.world.BattleTowerGenerator;
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
    private BattleTowerCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("battletowers")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("spawn")
                                .executes(context -> spawn(context.getSource(), "random", BattleTowerGenerator.DEFAULT_FLOORS, false))
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .executes(context -> spawn(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "type"),
                                                BattleTowerGenerator.DEFAULT_FLOORS,
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
                                                                BoolArgumentType.getBool(context, "underground"))))))
                        .then(Commands.literal("types")
                                .executes(context -> listTypes(context.getSource())))
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
}
