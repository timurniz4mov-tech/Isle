package com.islesurvival.world;

import com.islesurvival.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

import java.util.Random;

/** Builds an original playable island around the command user's position. */
public final class IsleIslandBuilder {
    private static final int SEA_LEVEL = 62;
    private static final int ISLAND_RADIUS = 96;
    private static final int OCEAN_RADIUS = 124;

    private IsleIslandBuilder() { }

    public static BlockPos build(ServerLevel level, BlockPos origin, Player player) {
        int cx = origin.getX();
        int cz = origin.getZ();
        generateTerrain(level, cx, cz);
        generateTrees(level, cx, cz);

        int shipX = cx - 51;
        int shipZ = cz - 20;
        buildShipwreck(level, shipX, surfaceY(shipX - cx, shipZ - cz), shipZ);

        int labX = cx + 32;
        int labZ = cz - 24;
        int labY = surfaceY(labX - cx, labZ - cz);
        buildResearchStation(level, labX, labY, labZ);

        int towerX = cx - 8;
        int towerZ = cz - 57;
        buildRadioTower(level, towerX, surfaceY(towerX - cx, towerZ - cz), towerZ);

        int lightX = cx + 63;
        int lightZ = cz - 57;
        buildLighthouse(level, lightX, surfaceY(lightX - cx, lightZ - cz), lightZ);

        int dockX = cx + 64;
        int dockZ = cz + 45;
        buildEvacuationDock(level, dockX, surfaceY(dockX - cx, dockZ - cz), dockZ);

        BlockPos marker = new BlockPos(cx, 38, cz);
        set(level, marker, Blocks.LODESTONE);
        return new BlockPos(cx + 4, surfaceY(4, 4) + 2, cz + 4);
    }

    private static void generateTerrain(ServerLevel level, int cx, int cz) {
        for (int dx = -OCEAN_RADIUS; dx <= OCEAN_RADIUS; dx++) {
            for (int dz = -OCEAN_RADIUS; dz <= OCEAN_RADIUS; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > OCEAN_RADIUS) continue;
                int x = cx + dx;
                int z = cz + dz;
                if (distance <= ISLAND_RADIUS) {
                    int top = surfaceY(dx, dz);
                    for (int y = 46; y < top - 3; y++) set(level, new BlockPos(x, y, z), Blocks.STONE);
                    for (int y = top - 3; y < top; y++) set(level, new BlockPos(x, y, z), Blocks.DIRT);
                    Block surface = distance > 82 || top <= SEA_LEVEL + 1 ? Blocks.SAND : Blocks.GRASS_BLOCK;
                    set(level, new BlockPos(x, top, z), surface);
                    if (top < SEA_LEVEL) {
                        for (int y = top + 1; y <= SEA_LEVEL; y++) set(level, new BlockPos(x, y, z), Blocks.WATER);
                    }
                    for (int y = Math.max(top, SEA_LEVEL) + 1; y <= Math.max(top, SEA_LEVEL) + 7; y++) {
                        set(level, new BlockPos(x, y, z), Blocks.AIR);
                    }
                } else {
                    set(level, new BlockPos(x, 53, z), Blocks.GRAVEL);
                    for (int y = 54; y <= SEA_LEVEL; y++) set(level, new BlockPos(x, y, z), Blocks.WATER);
                    for (int y = SEA_LEVEL + 1; y <= SEA_LEVEL + 8; y++) set(level, new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
    }

    private static int surfaceY(int dx, int dz) {
        double distance = Math.sqrt(dx * dx + dz * dz);
        double hill = Math.max(0.0D, 1.0D - distance / (ISLAND_RADIUS + 1.0D)) * 11.0D;
        int waves = (int) Math.round(1.6D * Math.sin(dx * 0.13D) + 1.4D * Math.cos(dz * 0.11D));
        int height = SEA_LEVEL + (int) Math.round(hill) + waves;
        if (distance > 82.0D) height = Math.min(height, SEA_LEVEL + 1);
        return Math.max(SEA_LEVEL - 1, height);
    }

    private static void generateTrees(ServerLevel level, int cx, int cz) {
        Random random = new Random(91271L);
        for (int i = 0; i < 78; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int radius = 34 + random.nextInt(44);
            int dx = (int) Math.round(Math.cos(angle) * radius);
            int dz = (int) Math.round(Math.sin(angle) * radius);
            if (Math.abs(dx - 32) < 15 && Math.abs(dz + 24) < 14) continue;
            if (dx < -38 && dz < -8 && dz > -34) continue;
            int x = cx + dx;
            int z = cz + dz;
            int y = surfaceY(dx, dz);
            buildTree(level, x, y, z, 4 + random.nextInt(3));
        }
    }

    private static void buildTree(ServerLevel level, int x, int ground, int z, int height) {
        for (int y = 1; y <= height; y++) set(level, new BlockPos(x, ground + y, z), Blocks.OAK_LOG);
        for (int dy = height - 2; dy <= height + 1; dy++) {
            int radius = dy == height + 1 ? 1 : 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius && dy > height - 1) continue;
                    BlockPos leaf = new BlockPos(x + dx, ground + dy, z + dz);
                    if (level.isEmptyBlock(leaf)) setState(level, leaf, Blocks.OAK_LEAVES.defaultBlockState()
                            .setValue(BlockStateProperties.DISTANCE, 1));
                }
            }
        }
    }

    private static void buildShipwreck(ServerLevel level, int x, int y, int z) {
        // Broken wooden hull and a tilted-looking deckhouse.
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) == 6 && Math.abs(dz) > 1) continue;
                set(level, new BlockPos(x + dx, y + 1, z + dz), Blocks.SPRUCE_PLANKS);
                if (Math.abs(dx) >= 4 || Math.abs(dz) == 3) {
                    set(level, new BlockPos(x + dx, y, z + dz), Blocks.SPRUCE_PLANKS);
                }
            }
        }
        for (int dx = -5; dx <= 5; dx++) {
            if (dx == -2 || dx == 3) continue;
            set(level, new BlockPos(x + dx, y + 2, z - 3), Blocks.SPRUCE_PLANKS);
            set(level, new BlockPos(x + dx, y + 2, z + 3), Blocks.SPRUCE_PLANKS);
        }
        for (int dy = 2; dy <= 8; dy++) set(level, new BlockPos(x - 1, y + dy, z), Blocks.SPRUCE_LOG);
        set(level, new BlockPos(x - 1, y + 7, z - 1), Blocks.WHITE_WOOL);
        set(level, new BlockPos(x - 1, y + 6, z + 1), Blocks.WHITE_WOOL);
        set(level, new BlockPos(x + 2, y + 2, z), Blocks.BARREL);
        BlockPos chestPos = new BlockPos(x + 4, y + 2, z + 1);
        set(level, chestPos, Blocks.CHEST);
        fillChest(level, chestPos, true);
        set(level, new BlockPos(x - 4, y + 2, z - 1), Blocks.CAMPFIRE);
        set(level, new BlockPos(x + 5, y + 2, z - 2), Blocks.COBWEB);
    }

    private static void buildResearchStation(ServerLevel level, int x, int y, int z) {
        int width = 18;
        int depth = 14;
        for (int dx = -width / 2; dx <= width / 2; dx++) {
            for (int dz = -depth / 2; dz <= depth / 2; dz++) {
                set(level, new BlockPos(x + dx, y + 1, z + dz), Blocks.POLISHED_ANDESITE);
                boolean wall = Math.abs(dx) == width / 2 || Math.abs(dz) == depth / 2;
                if (wall) {
                    for (int dy = 2; dy <= 6; dy++) {
                        Block block = (dy == 3 || dy == 4) && (Math.abs(dx) == width / 2 || Math.abs(dz) == depth / 2)
                                ? Blocks.LIGHT_BLUE_STAINED_GLASS : Blocks.LIGHT_GRAY_CONCRETE;
                        if (dx == 0 && dz == depth / 2 && (dy == 2 || dy == 3)) continue;
                        set(level, new BlockPos(x + dx, y + dy, z + dz), block);
                    }
                }
            }
        }
        for (int dx = -width / 2 - 1; dx <= width / 2 + 1; dx++) {
            for (int dz = -depth / 2 - 1; dz <= depth / 2 + 1; dz++) {
                set(level, new BlockPos(x + dx, y + 7, z + dz), Blocks.SMOOTH_STONE_SLAB);
            }
        }
        // Interior desks, a generator room and a marked underground access hatch.
        set(level, new BlockPos(x - 4, y + 2, z - 2), Blocks.CRAFTING_TABLE);
        set(level, new BlockPos(x - 2, y + 2, z - 2), Blocks.FURNACE);
        set(level, new BlockPos(x + 4, y + 2, z - 2), Blocks.REDSTONE_LAMP);
        set(level, new BlockPos(x + 5, y + 2, z + 2), Blocks.IRON_BLOCK);
        set(level, new BlockPos(x, y + 2, z + 3), Blocks.IRON_TRAPDOOR);
        for (int dy = 1; dy <= 9; dy++) set(level, new BlockPos(x + 8, y + dy, z - 5), Blocks.IRON_BARS);
        BlockPos chestPos = new BlockPos(x - 5, y + 2, z + 4);
        set(level, chestPos, Blocks.CHEST);
        fillChest(level, chestPos, false);

        // Small underground service room below the station.
        int roomY = y - 7;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                set(level, new BlockPos(x + dx, roomY, z + dz), Blocks.DEEPSLATE_BRICKS);
                if (Math.abs(dx) == 4 || Math.abs(dz) == 4) {
                    for (int dy = 1; dy <= 3; dy++) set(level, new BlockPos(x + dx, roomY + dy, z + dz), Blocks.DEEPSLATE_BRICKS);
                } else {
                    for (int dy = 1; dy <= 3; dy++) set(level, new BlockPos(x + dx, roomY + dy, z + dz), Blocks.AIR);
                }
            }
        }
        set(level, new BlockPos(x, roomY + 1, z), Blocks.SEA_LANTERN);
        BlockPos vaultChest = new BlockPos(x + 2, roomY + 1, z + 2);
        set(level, vaultChest, Blocks.CHEST);
        fillChest(level, vaultChest, false);
    }

    private static void buildRadioTower(ServerLevel level, int x, int y, int z) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) == 3 || Math.abs(dz) == 3) set(level, new BlockPos(x + dx, y + 1, z + dz), Blocks.COBBLESTONE);
            }
        }
        for (int dy = 2; dy <= 17; dy++) {
            set(level, new BlockPos(x, y + dy, z), Blocks.IRON_BARS);
            if (dy == 8 || dy == 13) {
                for (int dx = -3; dx <= 3; dx++) set(level, new BlockPos(x + dx, y + dy, z), Blocks.IRON_BARS);
                for (int dz = -3; dz <= 3; dz++) set(level, new BlockPos(x, y + dy, z + dz), Blocks.IRON_BARS);
            }
        }
        set(level, new BlockPos(x, y + 18, z), Blocks.LIGHTNING_ROD);
        set(level, new BlockPos(x + 2, y + 2, z), Blocks.BARREL);
    }

    private static void buildLighthouse(ServerLevel level, int x, int y, int z) {
        for (int dy = 1; dy <= 10; dy++) {
            int radius = dy <= 2 ? 4 : 3;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                        set(level, new BlockPos(x + dx, y + dy, z + dz), Blocks.WHITE_CONCRETE);
                    }
                }
            }
        }
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            set(level, new BlockPos(x + dx, y + 11, z + dz), Blocks.RED_CONCRETE);
        }
        set(level, new BlockPos(x, y + 9, z), Blocks.SEA_LANTERN);
        set(level, new BlockPos(x, y + 2, z), Blocks.LADDER);
        set(level, new BlockPos(x + 1, y + 2, z), Blocks.CHEST);
    }

    private static void buildEvacuationDock(ServerLevel level, int x, int y, int z) {
        for (int dz = 0; dz <= 17; dz++) {
            for (int dx = -4; dx <= 4; dx++) {
                set(level, new BlockPos(x + dx, y + 1, z + dz), Blocks.DARK_OAK_PLANKS);
            }
        }
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                if (dx * dx + dz * dz <= 49) set(level, new BlockPos(x + dx, y + 2, z + 4 + dz), Blocks.LIGHT_GRAY_CONCRETE);
            }
        }
        for (int dx = -6; dx <= 6; dx++) {
            set(level, new BlockPos(x + dx, y + 3, z + 4), Blocks.YELLOW_CONCRETE);
        }
        BlockPos chestPos = new BlockPos(x + 2, y + 2, z + 13);
        set(level, chestPos, Blocks.CHEST);
        fillChest(level, chestPos, false);
        set(level, new BlockPos(x - 2, y + 2, z + 13), Blocks.BARREL);
    }

    private static void fillChest(ServerLevel level, BlockPos pos, boolean shipCache) {
        if (!(level.getBlockEntity(pos) instanceof ChestBlockEntity chest)) return;
        if (shipCache) {
            chest.setItem(0, new ItemStack(ModItems.HUNTING_RIFLE.get()));
            chest.setItem(1, new ItemStack(ModItems.RIFLE_AMMO.get(), 18));
            chest.setItem(2, new ItemStack(ModItems.FIELD_RATION.get(), 3));
            chest.setItem(3, new ItemStack(ModItems.MEDKIT.get(), 1));
        } else {
            chest.setItem(0, new ItemStack(ModItems.KEYCARD.get()));
            chest.setItem(1, new ItemStack(ModItems.RIFLE_AMMO.get(), 24));
            chest.setItem(2, new ItemStack(ModItems.SHOTGUN_SHELLS.get(), 10));
            chest.setItem(3, new ItemStack(ModItems.RADIO_PART.get(), 2));
            chest.setItem(4, new ItemStack(ModItems.MEDKIT.get(), 2));
        }
        chest.setChanged();
    }

    private static void set(ServerLevel level, BlockPos pos, Block block) {
        setState(level, pos, block.defaultBlockState());
    }

    private static void setState(ServerLevel level, BlockPos pos, BlockState state) {
        if (pos.getY() >= level.getMinBuildHeight() && pos.getY() < level.getMaxBuildHeight()) {
            level.setBlock(pos, state, 2);
        }
    }
}
