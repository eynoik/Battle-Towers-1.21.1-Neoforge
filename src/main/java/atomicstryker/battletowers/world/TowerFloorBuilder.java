package atomicstryker.battletowers.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

final class TowerFloorBuilder {
    private TowerFloorBuilder() {
    }

    static void build(ServerLevel level, BlockPos origin, TowerType type, boolean groundFloor, boolean underground, boolean topFloor) {
        for (int y = 0; y < BattleTowerGenerator.FLOOR_HEIGHT; y++) {
            // The legacy generator starts the first surface floor four blocks into
            // the segment and then extends its walls down to the terrain.
            if (groundFloor && !underground && y < 4) {
                continue;
            }

            for (int x = -7; x < 7; x++) {
                for (int z = -7; z < 7; z++) {
                    BlockPos pos = origin.offset(x, y, z);

                    if (z == -7) {
                        if (x > -5 && x < 4) {
                            wall(level, pos, type, groundFloor, y);
                        }
                        continue;
                    }

                    if (z == -6 || z == -5) {
                        if (x == -5 || x == 4) {
                            wall(level, pos, type, groundFloor, y);
                            continue;
                        }

                        if (z == -6) {
                            if (x == (y + 1) % 7 - 3) {
                                if (!(underground && groundFloor)) {
                                    placeStair(level, pos, type);
                                }
                                if (y == 5) {
                                    floor(level, pos.offset(-7, 0, 0), type);
                                }
                                if (y == 6 && topFloor) {
                                    wall(level, pos, type, groundFloor, y);
                                }
                                continue;
                            }
                            if (x < 4 && x > -5) {
                                air(level, pos);
                            }
                            continue;
                        }

                        if (x <= -5 || x >= 5) {
                            continue;
                        }

                        if ((y != 0 && y != 6) || (x != -4 && x != 3)) {
                            if (y == 5 && (x == 3 || x == -4)) {
                                floor(level, pos, type);
                            } else {
                                wall(level, pos, type, groundFloor, y);
                            }
                        } else {
                            air(level, pos);
                        }
                        continue;
                    }

                    if (z == -4 || z == -3 || z == 2 || z == 3) {
                        if (x == -6 || x == 5) {
                            wall(level, pos, type, groundFloor, y);
                            continue;
                        }
                        if (x <= -6 || x >= 5) {
                            continue;
                        }
                        if (y == 5) {
                            floor(level, pos, type);
                        } else {
                            airUnlessChest(level, pos);
                        }
                        continue;
                    }

                    if (z > -3 && z < 2) {
                        if (x == -7 || x == 6) {
                            boolean window = !underground && y <= 3 && (z == -1 || z == 0);
                            if (window) {
                                air(level, pos);
                            } else {
                                wall(level, pos, type, groundFloor, y);
                            }
                            continue;
                        }
                        if (x <= -7 || x >= 6) {
                            continue;
                        }
                        if (y == 5) {
                            floor(level, pos, type);
                        } else {
                            air(level, pos);
                        }
                        continue;
                    }

                    if (z == 4) {
                        if (x == -5 || x == 4) {
                            wall(level, pos, type, groundFloor, y);
                            continue;
                        }
                        if (x <= -5 || x >= 4) {
                            continue;
                        }
                        if (y == 5) {
                            floor(level, pos, type);
                        } else {
                            air(level, pos);
                        }
                        continue;
                    }

                    if (z == 5) {
                        if (x == -4 || x == -3 || x == 2 || x == 3) {
                            wall(level, pos, type, groundFloor, y);
                            continue;
                        }
                        if (x <= -3 || x >= 2) {
                            continue;
                        }
                        if (y == 5) {
                            floor(level, pos, type);
                        } else {
                            wall(level, pos, type, groundFloor, y);
                        }
                        continue;
                    }

                    if (z == 6 && x > -3 && x < 2) {
                        wall(level, pos, type, groundFloor, y);
                    }
                }
            }
        }
    }

    private static void placeStair(ServerLevel level, BlockPos pos, TowerType type) {
        BlockState state = type.stairs().defaultBlockState();
        if (type.stairs() instanceof StairBlock) {
            state = state.setValue(StairBlock.FACING, Direction.EAST);
        }
        level.setBlock(pos, state, 2);
    }

    private static void floor(ServerLevel level, BlockPos pos, TowerType type) {
        level.setBlock(pos, type.floor().defaultBlockState(), 2);
    }

    private static void wall(ServerLevel level, BlockPos pos, TowerType type, boolean groundFloor, int localY) {
        level.setBlock(pos, type.wall().defaultBlockState(), 2);
        if (groundFloor && localY == 4) {
            fillBaseToGround(level, pos.below(), type);
        }
    }

    private static void fillBaseToGround(ServerLevel level, BlockPos start, TowerType type) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        int minY = level.getMinBuildHeight() + 1;
        while (cursor.getY() > minY) {
            BlockState state = level.getBlockState(cursor);
            if (!state.isAir() && state.getFluidState().isEmpty()) {
                break;
            }
            level.setBlock(cursor, type.wall().defaultBlockState(), 2);
            cursor.move(Direction.DOWN);
        }
    }

    private static void air(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
    }

    private static void airUnlessChest(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).is(Blocks.CHEST)) {
            air(level, pos);
        }
    }
}
